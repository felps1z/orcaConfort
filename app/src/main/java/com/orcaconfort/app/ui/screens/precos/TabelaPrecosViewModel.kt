package com.orcaconfort.app.ui.screens.precos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orcaconfort.app.core.util.Formatadores
import com.orcaconfort.app.domain.model.CategoriaMaterial
import com.orcaconfort.app.domain.model.MaterialCatalogo
import com.orcaconfort.app.domain.repository.CatalogoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Item individual exibido dentro de uma categoria da Tabela de Preços. */
data class ItemPrecoUi(
    val id: Long,
    val nome: String,
    val atualizadoEm: String,
    val valor: String,
    val unidade: String,
) {
    val precoFormatado: String
        get() = "R$ $valor / $unidade"
}

/** Agrupamento visual de materiais por categoria na Tabela de Preços. */
data class CategoriaPrecoUi(
    val categoria: CategoriaMaterial,
    val nome: String,
    val unidadePadrao: String,
    val itens: List<ItemPrecoUi>,
)

/**
 * ViewModel da Tabela de Preços.
 *
 * Observa o catálogo persistido no Room ([CatalogoRepository]) e expõe ações de edição de preço
 * e cadastro de novas variações por categoria (RN03).
 */
@HiltViewModel
class TabelaPrecosViewModel @Inject constructor(
    private val catalogoRepository: CatalogoRepository,
) : ViewModel() {

    val categorias: StateFlow<List<CategoriaPrecoUi>> =
        catalogoRepository.observarMateriaisAtivos()
            .map { materiais -> agruparPorCategoria(materiais) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    /** Atualiza apenas o valor unitário de um material existente no catálogo. */
    fun atualizarPreco(materialId: Long, novoValorTexto: String) {
        val novoPreco = Formatadores.parseNumeroPtBr(novoValorTexto)
        if (novoPreco < 0.0) return

        viewModelScope.launch {
            catalogoRepository.atualizarPreco(
                materialId = materialId,
                novoPreco = Formatadores.arredondar2Casas(novoPreco),
            )
        }
    }

    /** Cadastra uma nova variação de material dentro de uma categoria (ex.: novo tecido). */
    fun adicionarMaterial(
        categoria: CategoriaMaterial,
        nome: String,
        unidade: String,
        valorTexto: String,
    ) {
        val nomeLimpo = nome.trim()
        val unidadeLimpa = unidade.trim().ifEmpty { unidadePadraoPara(categoria) }
        val preco = Formatadores.parseNumeroPtBr(valorTexto)
        if (nomeLimpo.isEmpty() || preco < 0.0) return

        viewModelScope.launch {
            catalogoRepository.salvarMaterial(
                MaterialCatalogo(
                    categoria = categoria,
                    nome = nomeLimpo,
                    unidade = unidadeLimpa,
                    precoUnitario = Formatadores.arredondar2Casas(preco),
                ),
            )
        }
    }

    private fun agruparPorCategoria(materiais: List<MaterialCatalogo>): List<CategoriaPrecoUi> {
        val agrupados = materiais.groupBy { it.categoria }
        val categoriasVisiveis = CategoriaMaterial.entries.filter { categoria ->
            categoria != CategoriaMaterial.OUTROS || !agrupados[categoria].isNullOrEmpty()
        }

        return categoriasVisiveis.map { categoria ->
            val itens = agrupados[categoria].orEmpty().map { material ->
                ItemPrecoUi(
                    id = material.id,
                    nome = material.nome,
                    atualizadoEm = Formatadores.formatarData(material.atualizadoEm),
                    valor = Formatadores.formatarValorSemSimbolo(material.precoUnitario),
                    unidade = material.unidade,
                )
            }
            CategoriaPrecoUi(
                categoria = categoria,
                nome = categoria.tituloExibicao,
                unidadePadrao = unidadePadraoPara(categoria),
                itens = itens,
            )
        }
    }

    private fun unidadePadraoPara(categoria: CategoriaMaterial): String = when (categoria) {
        CategoriaMaterial.TECIDO -> "m"
        CategoriaMaterial.ESPUMA -> "m"
        CategoriaMaterial.FIBRA -> "kg"
        CategoriaMaterial.PERCINTA -> "m"
        CategoriaMaterial.MADEIRA -> "un."
        CategoriaMaterial.FOLHA -> "folha"
        CategoriaMaterial.PE -> "un."
        CategoriaMaterial.MOLA -> "un."
        CategoriaMaterial.OUTROS -> "un."
    }
}

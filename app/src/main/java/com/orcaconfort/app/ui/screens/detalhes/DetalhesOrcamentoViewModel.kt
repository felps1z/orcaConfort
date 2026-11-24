package com.orcaconfort.app.ui.screens.detalhes

import androidx.annotation.DrawableRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orcaconfort.app.R
import com.orcaconfort.app.core.util.Formatadores
import com.orcaconfort.app.domain.model.CategoriaMaterial
import com.orcaconfort.app.domain.model.ItemMaterialOrcamento
import com.orcaconfort.app.domain.model.ModeloSofa
import com.orcaconfort.app.domain.model.Orcamento
import com.orcaconfort.app.domain.model.StatusOrcamento
import com.orcaconfort.app.domain.model.TipoServico
import com.orcaconfort.app.domain.repository.OrcamentoRepository
import com.orcaconfort.app.domain.usecase.GerarPdfOrcamentoUseCase
import com.orcaconfort.app.domain.usecase.PdfOrcamentoGerado
import com.orcaconfort.app.ui.navigation.Rotas
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Mapeamento do modelo de domínio para a imagem ilustrativa do estofado. */
enum class ModeloSofaIlustrativo(
    val tituloModelo: String,
    @param:DrawableRes val imagemRes: Int,
) {
    FIXO(
        tituloModelo = "Sofá fixo",
        imagemRes = R.drawable.img_sofa_fixo,
    ),
    RETRATIL(
        tituloModelo = "Sofá retrátil",
        imagemRes = R.drawable.img_sofa_retratil,
    ),
    MODULAR(
        tituloModelo = "Sofá modular",
        imagemRes = R.drawable.img_sofa_modular,
    );

    companion object {
        fun fromDominio(modelo: ModeloSofa): ModeloSofaIlustrativo = when (modelo) {
            ModeloSofa.FIXO -> FIXO
            ModeloSofa.RETRATIL -> RETRATIL
            ModeloSofa.MODULAR -> MODULAR
        }
    }
}

data class MaterialDetalheUi(
    val nome: String,
    val quantidade: String,
    val observacao: String? = null,
)

data class LinhaFinanceiraUi(
    val label: String,
    val valor: String,
)

data class DetalhesOrcamentoUiState(
    val id: Long,
    val codigoFormatado: String,
    val clienteNome: String,
    val status: StatusOrcamento,
    val tipoServico: String,
    val dataOrcamento: String,
    val dataEntregaPrevista: String,
    val modelo: ModeloSofaIlustrativo,
    val dimensoes: String,
    val tecidoECor: String,
    val almofadas: String,
    val observacoesAcordadas: String,
    val materiais: List<MaterialDetalheUi>,
    val notaMateriais: String,
    val itensCustosMateriais: List<LinhaFinanceiraUi>,
    val subtotalMateriais: LinhaFinanceiraUi,
    val itensCustosServicos: List<LinhaFinanceiraUi>,
    val totalCalculado: LinhaFinanceiraUi,
    val ajusteComercial: LinhaFinanceiraUi?,
    val notaFinanceira: String?,
    val valorTotalFinal: String,
    val legendaValorFinal: String,
)

sealed interface DetalhesScreenState {
    data object Carregando : DetalhesScreenState
    data object NaoEncontrado : DetalhesScreenState
    data class Sucesso(val detalhes: DetalhesOrcamentoUiState) : DetalhesScreenState
}

/**
 * ViewModel da tela de Detalhes do Orçamento.
 *
 * Observa o orçamento completo (cabeçalho, peças e snapshot de itens) no banco local Room
 * e permite atualizar o [StatusOrcamento] diretamente pelo badge de status.
 */
@HiltViewModel
class DetalhesOrcamentoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val orcamentoRepository: OrcamentoRepository,
    private val gerarPdfOrcamentoUseCase: GerarPdfOrcamentoUseCase,
) : ViewModel() {

    private val orcamentoId: Long = savedStateHandle[Rotas.ARG_ORCAMENTO_ID] ?: -1L

    private var orcamentoAtual: Orcamento? = null

    private val _gerandoPdf = MutableStateFlow(false)
    val gerandoPdf: StateFlow<Boolean> = _gerandoPdf.asStateFlow()

    val screenState: StateFlow<DetalhesScreenState> =
        orcamentoRepository.observarOrcamentoPorId(orcamentoId)
            .map { orcamento ->
                orcamentoAtual = orcamento
                if (orcamento == null) {
                    DetalhesScreenState.NaoEncontrado
                } else {
                    DetalhesScreenState.Sucesso(mapearParaUi(orcamento))
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DetalhesScreenState.Carregando,
            )

    fun atualizarStatus(novoStatus: StatusOrcamento) {
        if (orcamentoId <= 0L) return
        viewModelScope.launch {
            orcamentoRepository.atualizarStatus(orcamentoId, novoStatus)
        }
    }

    fun gerarPdf(
        onSucesso: (PdfOrcamentoGerado) -> Unit,
        onErro: (String) -> Unit,
    ) {
        if (_gerandoPdf.value) return
        viewModelScope.launch {
            _gerandoPdf.value = true
            val orcamento = orcamentoAtual ?: orcamentoRepository.buscarOrcamentoPorId(orcamentoId)
            if (orcamento == null) {
                _gerandoPdf.value = false
                onErro("Orçamento não encontrado para gerar o PDF.")
                return@launch
            }

            val resultado = gerarPdfOrcamentoUseCase(orcamento)
            _gerandoPdf.value = false
            resultado.fold(
                onSuccess = onSucesso,
                onFailure = { erro ->
                    onErro(erro.message ?: "Não foi possível gerar o PDF do orçamento.")
                },
            )
        }
    }

    private fun mapearParaUi(orcamento: Orcamento): DetalhesOrcamentoUiState {
        val isReforma = orcamento.tipoServico == TipoServico.REFORMA

        val dimensoesTexto = when {
            orcamento.sofa.pecas.isEmpty() -> "Não informadas"
            orcamento.sofa.pecas.size == 1 -> {
                val p = orcamento.sofa.pecas.first()
                "${Formatadores.formatarQuantidade(p.larguraCm)} × ${Formatadores.formatarQuantidade(p.profundidadeCm)} × ${Formatadores.formatarQuantidade(p.alturaCm)} cm"
            }
            else -> orcamento.sofa.pecas.joinToString("\n") { p ->
                "Peça ${p.ordem}: ${Formatadores.formatarQuantidade(p.larguraCm)} × ${Formatadores.formatarQuantidade(p.profundidadeCm)} × ${Formatadores.formatarQuantidade(p.alturaCm)} cm"
            }
        }

        val tecidoECor = listOfNotNull(
            orcamento.sofa.nomeTecidoSelecionado.takeIf { it.isNotBlank() },
            orcamento.sofa.corTecido.lowercase().takeIf { it.isNotBlank() },
        ).joinToString(" ").ifBlank { "Não informado" }

        val almofadasTexto = if (orcamento.sofa.possuiAlmofadas) {
            val qtd = orcamento.sofa.qtdAlmofadas
            val unidade = if (qtd == 1) "unidade" else "unidades"
            "Sim • $qtd $unidade"
        } else {
            "Não"
        }

        val materiaisUi = orcamento.itens.map { item ->
            mapearMaterialDetalhe(item, isReforma)
        }

        val custoTecido = orcamento.itens
            .filter { it.categoria == CategoriaMaterial.TECIDO }
            .sumOf { it.custoTotal }
        val custoEspumaEnchimento = orcamento.itens
            .filter { it.categoria == CategoriaMaterial.ESPUMA || it.categoria == CategoriaMaterial.FIBRA }
            .sumOf { it.custoTotal }
        val custoEstruturaAdicionais = orcamento.itens
            .filter {
                it.categoria != CategoriaMaterial.TECIDO &&
                    it.categoria != CategoriaMaterial.ESPUMA &&
                    it.categoria != CategoriaMaterial.FIBRA
            }
            .sumOf { it.custoTotal }

        val fin = orcamento.financeiro
        val ajuste = fin.ajusteComercial
        val linhaAjuste = ajuste?.let { valorAjuste ->
            val valorTexto = if (valorAjuste > 0.0) {
                "+ ${Formatadores.formatarMoeda(valorAjuste)}"
            } else {
                Formatadores.formatarMoeda(valorAjuste)
            }
            LinhaFinanceiraUi("Ajuste comercial", valorTexto)
        }

        val legendaFinal = if (linhaAjuste != null) {
            "Valor final com ajuste comercial aplicado"
        } else {
            "Valor final conforme cálculo dos materiais e serviços"
        }

        return DetalhesOrcamentoUiState(
            id = orcamento.id,
            codigoFormatado = Formatadores.formatarCodigoOrcamento(orcamento.id),
            clienteNome = orcamento.cliente.nome,
            status = orcamento.status,
            tipoServico = orcamento.tipoServico.tituloExibicao,
            dataOrcamento = Formatadores.formatarData(orcamento.criadoEm),
            dataEntregaPrevista = Formatadores.formatarData(orcamento.cliente.dataEntregaTimestamp),
            modelo = ModeloSofaIlustrativo.fromDominio(orcamento.sofa.modelo),
            dimensoes = dimensoesTexto,
            tecidoECor = tecidoECor,
            almofadas = almofadasTexto,
            observacoesAcordadas = orcamento.cliente.observacoes.ifBlank { "Sem observações registradas." },
            materiais = materiaisUi,
            notaMateriais = "Quantidades conforme o orçamento salvo (preços congelados na data de emissão). Itens zerados não representam material novo.",
            itensCustosMateriais = listOf(
                LinhaFinanceiraUi("Total em tecido", Formatadores.formatarMoeda(custoTecido)),
                LinhaFinanceiraUi("Espuma / enchimento", Formatadores.formatarMoeda(custoEspumaEnchimento)),
                LinhaFinanceiraUi("Estrutura / materiais adicionais", Formatadores.formatarMoeda(custoEstruturaAdicionais)),
            ),
            subtotalMateriais = LinhaFinanceiraUi(
                "Subtotal dos materiais",
                Formatadores.formatarMoeda(fin.subtotalMateriais),
            ),
            itensCustosServicos = listOf(
                LinhaFinanceiraUi(
                    "Reserva para extras • ${Formatadores.formatarQuantidade(fin.margemExtrasPercentual)}%",
                    Formatadores.formatarMoeda(fin.valorMargemExtras),
                ),
                LinhaFinanceiraUi(
                    "Mão de obra • ${Formatadores.formatarQuantidade(fin.diasTrabalhados)} × ${Formatadores.formatarMoeda(fin.valorDiaria)}",
                    Formatadores.formatarMoeda(fin.totalMaoDeObra),
                ),
                LinhaFinanceiraUi(
                    "Frete",
                    Formatadores.formatarMoeda(fin.custoFrete),
                ),
            ),
            totalCalculado = LinhaFinanceiraUi(
                "Total calculado",
                Formatadores.formatarMoeda(fin.totalCalculado),
            ),
            ajusteComercial = linhaAjuste,
            notaFinanceira = null,
            valorTotalFinal = Formatadores.formatarMoeda(fin.valorFinalSalvo),
            legendaValorFinal = legendaFinal,
        )
    }

    private fun mapearMaterialDetalhe(
        item: ItemMaterialOrcamento,
        isReforma: Boolean,
    ): MaterialDetalheUi {
        val qtdTexto = "${Formatadores.formatarQuantidade(item.quantidade)} ${item.unidade}"
        val observacao = when {
            item.quantidade <= 0.0 && isReforma -> "Item original mantido / não incluído"
            item.quantidade <= 0.0 -> "Não utilizado neste orçamento"
            item.editadoManualmente -> "Quantidade ajustada manualmente"
            else -> null
        }
        return MaterialDetalheUi(
            nome = item.descricao,
            quantidade = qtdTexto,
            observacao = observacao,
        )
    }
}

package com.orcaconfort.app.domain.usecase

import com.orcaconfort.app.core.util.Formatadores
import com.orcaconfort.app.domain.model.CategoriaMaterial
import com.orcaconfort.app.domain.model.ChecklistReforma
import com.orcaconfort.app.domain.model.EspecificacoesSofa
import com.orcaconfort.app.domain.model.ItemMaterialOrcamento
import com.orcaconfort.app.domain.model.MaterialCatalogo
import com.orcaconfort.app.domain.model.ModeloSofa
import com.orcaconfort.app.domain.model.TipoServico
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.max

/**
 * Motor de cálculo automático de materiais (Seção 2 da Especificação v1.0).
 *
 * Responsabilidades:
 * 1. Aplicar as fórmulas proporcionais à largura total (`L` em metros, somando todas as peças).
 * 2. Respeitar a RN01 (em `Sofá Novo` estrutura e espuma entram automaticamente;
 *    em `Reforma` os itens estruturais dependem do [ChecklistReforma]).
 * 3. Congelar o preço unitário vigente do catálogo em cada item (Padrão Snapshot - RN05).
 */
class CalcularMateriaisOrcamentoUseCase @Inject constructor() {

    operator fun invoke(
        tipoServico: TipoServico,
        sofa: EspecificacoesSofa,
        checklistReforma: ChecklistReforma,
        catalogo: List<MaterialCatalogo>,
    ): List<ItemMaterialOrcamento> {
        val larguraTotalM = sofa.larguraTotalMetros.coerceAtLeast(0.0)
        val isSofaNovo = tipoServico == TipoServico.SOFA_NOVO

        // 2.1 Tecido (sempre entra no orçamento)
        val qtdTecido = calcularTecidoMetros(
            larguraTotalM = larguraTotalM,
            modelo = sofa.modelo,
            possuiAlmofadas = sofa.possuiAlmofadas,
        )
        val precoTecido = if (sofa.precoTecidoSelecionado > 0.0) {
            sofa.precoTecidoSelecionado
        } else {
            buscarMaterialNoCatalogo(catalogo, CategoriaMaterial.TECIDO, sofa.nomeTecidoSelecionado)
                ?.precoUnitario ?: 0.0
        }
        val nomeTecido = sofa.nomeTecidoSelecionado.ifBlank { "Tecido" }
        val sufixoCor = if (sofa.corTecido.isNotBlank()) " / Cor: ${sofa.corTecido}" else ""

        // 2.2 Fibra Siliconada
        val qtdFibra = calcularFibraKg(
            larguraTotalM = larguraTotalM,
            modelo = sofa.modelo,
            possuiAlmofadas = sofa.possuiAlmofadas,
            qtdAlmofadas = sofa.qtdAlmofadas,
        )

        // 2.3 Espumas (automáticas em Sofá Novo; condicionais ao checkbox na Reforma)
        val incluirEspuma = isSofaNovo || checklistReforma.substituirEspuma
        val qtdEspuma10cm = if (incluirEspuma && larguraTotalM > 0.0) {
            Formatadores.arredondar2Casas(
                (larguraTotalM / RegrasCalculoConfig.LARGURA_BASE_ESPUMA_METROS) *
                    RegrasCalculoConfig.ESPUMA_10CM_BASE_METROS,
            )
        } else {
            0.0
        }
        val qtdEspuma3cm = if (incluirEspuma && larguraTotalM > 0.0) {
            Formatadores.arredondar2Casas(
                (larguraTotalM / RegrasCalculoConfig.LARGURA_BASE_ESPUMA_METROS) *
                    RegrasCalculoConfig.ESPUMA_3CM_BASE_METROS,
            )
        } else {
            0.0
        }

        // 2.4 Percinta Elástica
        val incluirPercinta = isSofaNovo || checklistReforma.substituirPercinta
        val qtdPercinta = if (incluirPercinta && larguraTotalM > 0.0) {
            ceil(larguraTotalM * RegrasCalculoConfig.FATOR_PERCINTA_POR_METRO)
        } else {
            0.0
        }

        // 2.5 Madeira Estrutural (Tábuas de 3,00m x 0,15m em unidades)
        val incluirMadeira = isSofaNovo || checklistReforma.substituirMadeira
        val qtdMadeira = if (incluirMadeira && larguraTotalM > 0.0) {
            ceil(larguraTotalM / RegrasCalculoConfig.LARGURA_POR_TABUA_METROS)
        } else {
            0.0
        }

        // 2.6 Chapas (Duratex / Compensado em folhas)
        val incluirChapas = isSofaNovo || checklistReforma.substituirChapas
        val qtdChapas = if (incluirChapas && larguraTotalM > 0.0) {
            max(1.0, ceil(larguraTotalM / RegrasCalculoConfig.LARGURA_BASE_CHAPA_METROS))
        } else {
            0.0
        }

        // 2.6 Molas (unidades - sugestão editável conforme avaliação)
        val qtdMolas = if (!isSofaNovo && checklistReforma.substituirMolas && larguraTotalM > 0.0) {
            RegrasCalculoConfig.MOLAS_SUGESTAO_REFORMA
        } else {
            0.0
        }

        // 2.6 Pés (obrigatório em Sofá Novo; calculado por peça para respeitar múltiplos módulos)
        val qtdPes = if (isSofaNovo) calcularPesUnidades(sofa) else 0.0

        return listOf(
            criarItem(
                chave = RegrasCalculoConfig.CHAVE_TECIDO,
                categoria = CategoriaMaterial.TECIDO,
                descricao = "Tecido: $nomeTecido$sufixoCor",
                unidade = "m",
                precoUnitario = precoTecido,
                quantidade = qtdTecido,
            ),
            criarItemDoCatalogo(
                catalogo = catalogo,
                chave = RegrasCalculoConfig.CHAVE_FIBRA,
                categoria = CategoriaMaterial.FIBRA,
                termoBusca = "Fibra",
                descricaoPadrao = "Fibra siliconada",
                unidadePadrao = "kg",
                quantidade = qtdFibra,
            ),
            criarItemDoCatalogo(
                catalogo = catalogo,
                chave = RegrasCalculoConfig.CHAVE_ESPUMA_1CM,
                categoria = CategoriaMaterial.ESPUMA,
                termoBusca = "1cm",
                descricaoPadrao = "Espuma 1cm",
                unidadePadrao = "m",
                quantidade = 0.0,
            ),
            criarItemDoCatalogo(
                catalogo = catalogo,
                chave = RegrasCalculoConfig.CHAVE_ESPUMA_2CM,
                categoria = CategoriaMaterial.ESPUMA,
                termoBusca = "2cm",
                descricaoPadrao = "Espuma 2cm",
                unidadePadrao = "m",
                quantidade = 0.0,
            ),
            criarItemDoCatalogo(
                catalogo = catalogo,
                chave = RegrasCalculoConfig.CHAVE_ESPUMA_3CM,
                categoria = CategoriaMaterial.ESPUMA,
                termoBusca = "3cm",
                descricaoPadrao = "Espuma 3cm",
                unidadePadrao = "m",
                quantidade = qtdEspuma3cm,
            ),
            criarItemDoCatalogo(
                catalogo = catalogo,
                chave = RegrasCalculoConfig.CHAVE_ESPUMA_5CM,
                categoria = CategoriaMaterial.ESPUMA,
                termoBusca = "5cm",
                descricaoPadrao = "Espuma 5cm",
                unidadePadrao = "m",
                quantidade = 0.0,
            ),
            criarItemDoCatalogo(
                catalogo = catalogo,
                chave = RegrasCalculoConfig.CHAVE_ESPUMA_10CM,
                categoria = CategoriaMaterial.ESPUMA,
                termoBusca = "10cm",
                descricaoPadrao = "Espuma 10cm",
                unidadePadrao = "m",
                quantidade = qtdEspuma10cm,
            ),
            criarItemDoCatalogo(
                catalogo = catalogo,
                chave = RegrasCalculoConfig.CHAVE_PERCINTA,
                categoria = CategoriaMaterial.PERCINTA,
                termoBusca = "Percinta",
                descricaoPadrao = "Percinta elástica",
                unidadePadrao = "m",
                quantidade = qtdPercinta,
            ),
            criarItemDoCatalogo(
                catalogo = catalogo,
                chave = RegrasCalculoConfig.CHAVE_MADEIRA,
                categoria = CategoriaMaterial.MADEIRA,
                termoBusca = "Tábua",
                descricaoPadrao = "Tábuas de madeira",
                unidadePadrao = "un.",
                quantidade = qtdMadeira,
            ),
            criarItemDoCatalogo(
                catalogo = catalogo,
                chave = RegrasCalculoConfig.CHAVE_CHAPAS,
                categoria = CategoriaMaterial.FOLHA,
                termoBusca = "Chapa",
                descricaoPadrao = "Duratex / Compensado",
                unidadePadrao = "folha",
                quantidade = qtdChapas,
            ),
            criarItemDoCatalogo(
                catalogo = catalogo,
                chave = RegrasCalculoConfig.CHAVE_MOLAS,
                categoria = CategoriaMaterial.MOLA,
                termoBusca = "Mola",
                descricaoPadrao = "Molas",
                unidadePadrao = "un.",
                quantidade = qtdMolas,
            ),
            criarItemDoCatalogo(
                catalogo = catalogo,
                chave = RegrasCalculoConfig.CHAVE_PES,
                categoria = CategoriaMaterial.PE,
                termoBusca = "Pé",
                descricaoPadrao = "Pés",
                unidadePadrao = "un.",
                quantidade = qtdPes,
            ),
        )
    }

    /** Seção 2.1: Tecido = teto(L × fator do modelo). */
    private fun calcularTecidoMetros(
        larguraTotalM: Double,
        modelo: ModeloSofa,
        possuiAlmofadas: Boolean,
    ): Double {
        if (larguraTotalM <= 0.0) return 0.0
        val fator = when (modelo) {
            ModeloSofa.FIXO -> if (possuiAlmofadas) {
                RegrasCalculoConfig.FATOR_TECIDO_FIXO_COM_ALMOFADA
            } else {
                RegrasCalculoConfig.FATOR_TECIDO_FIXO_SEM_ALMOFADA
            }
            ModeloSofa.RETRATIL -> RegrasCalculoConfig.FATOR_TECIDO_RETRATIL
            ModeloSofa.MODULAR -> if (possuiAlmofadas) {
                RegrasCalculoConfig.FATOR_TECIDO_MODULAR_COM_ALMOFADA
            } else {
                RegrasCalculoConfig.FATOR_TECIDO_MODULAR_SEM_ALMOFADA
            }
        }
        return ceil(larguraTotalM * fator)
    }

    /**
     * Seção 2.2: Fibra Siliconada.
     * - Quando há almofadas informadas: `qtdAlmofadas × 2,0 kg`.
     * - Para sofás retráteis (encosto com fibra embutida) sem almofadas soltas: `teto(L × 2,67)`.
     * - Demais casos sem almofadas: `0 kg`.
     */
    private fun calcularFibraKg(
        larguraTotalM: Double,
        modelo: ModeloSofa,
        possuiAlmofadas: Boolean,
        qtdAlmofadas: Int,
    ): Double {
        if (possuiAlmofadas && qtdAlmofadas > 0) {
            return Formatadores.arredondar2Casas(qtdAlmofadas * RegrasCalculoConfig.FIBRA_KG_POR_ALMOFADA)
        }
        if (modelo == ModeloSofa.RETRATIL && larguraTotalM > 0.0) {
            return ceil(larguraTotalM * RegrasCalculoConfig.FIBRA_KG_POR_METRO_LARGURA)
        }
        return 0.0
    }

    /**
     * Seção 2.6: Pés calculados por peça.
     * Cada peça de sofá fixo até 1,80 m sugere 4 pés; peças maiores ou retráteis/modulares sugerem 6 pés.
     */
    private fun calcularPesUnidades(sofa: EspecificacoesSofa): Double {
        val pecas = sofa.pecas.ifEmpty { return 0.0 }
        val total = pecas.sumOf { peca ->
            val larguraPecaM = peca.larguraCm / 100.0
            if (larguraPecaM <= 0.0) {
                0
            } else if (sofa.modelo == ModeloSofa.FIXO &&
                larguraPecaM <= RegrasCalculoConfig.LIMITE_LARGURA_4_PES_METROS
            ) {
                RegrasCalculoConfig.PES_PADRAO_PEQUENO
            } else {
                RegrasCalculoConfig.PES_PADRAO_GRANDE_OU_MOVEL
            }
        }
        return total.toDouble()
    }

    private fun criarItemDoCatalogo(
        catalogo: List<MaterialCatalogo>,
        chave: String,
        categoria: CategoriaMaterial,
        termoBusca: String,
        descricaoPadrao: String,
        unidadePadrao: String,
        quantidade: Double,
    ): ItemMaterialOrcamento {
        val encontrado = buscarMaterialNoCatalogo(catalogo, categoria, termoBusca)
        return criarItem(
            chave = chave,
            categoria = categoria,
            descricao = descricaoPadrao,
            unidade = encontrado?.unidade ?: unidadePadrao,
            precoUnitario = encontrado?.precoUnitario ?: 0.0,
            quantidade = quantidade,
        )
    }

    private fun criarItem(
        chave: String,
        categoria: CategoriaMaterial,
        descricao: String,
        unidade: String,
        precoUnitario: Double,
        quantidade: Double,
    ): ItemMaterialOrcamento {
        val qtdLimpa = Formatadores.arredondar2Casas(quantidade.coerceAtLeast(0.0))
        val custoTotal = Formatadores.arredondar2Casas(qtdLimpa * precoUnitario)
        return ItemMaterialOrcamento(
            categoria = categoria,
            chaveMaterial = chave,
            descricao = descricao,
            unidade = unidade,
            precoUnitarioSnapshot = precoUnitario,
            quantidade = qtdLimpa,
            custoTotal = custoTotal,
            editadoManualmente = false,
        )
    }

    private fun buscarMaterialNoCatalogo(
        catalogo: List<MaterialCatalogo>,
        categoria: CategoriaMaterial,
        termo: String,
    ): MaterialCatalogo? {
        val daCategoria = catalogo.filter { it.categoria == categoria && it.ativo }
        if (daCategoria.isEmpty()) return null
        if (termo.isNotBlank()) {
            daCategoria.firstOrNull { it.nome.contains(termo, ignoreCase = true) }?.let { return it }
        }
        return daCategoria.firstOrNull()
    }
}

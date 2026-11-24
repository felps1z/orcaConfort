package com.orcaconfort.app.domain.model

/**
 * Bloco da Etapa 1: Dados cadastrais do cliente e entrega.
 * Para incluir novos campos no futuro (ex.: endereço, CPF), basta estender esta classe.
 */
data class DadosCliente(
    val nome: String,
    val telefone: String = "",
    val dataEntregaTimestamp: Long? = null,
    val observacoes: String = "",
)

/**
 * Dimensões individuais de uma peça do sofá (em centímetros).
 */
data class DimensaoPeca(
    val ordem: Int,
    val larguraCm: Double,
    val profundidadeCm: Double,
    val alturaCm: Double,
)

/**
 * Bloco das Etapas 2 e 3: Modelo, acabamento, tecido escolhido e dimensões por peça.
 */
data class EspecificacoesSofa(
    val modelo: ModeloSofa,
    val numeroPecas: Int = 1,
    val pecas: List<DimensaoPeca> = emptyList(),
    val possuiAlmofadas: Boolean = false,
    val qtdAlmofadas: Int = 0,
    val nomeTecidoSelecionado: String = "",
    val precoTecidoSelecionado: Double = 0.0,
    val corTecido: String = "",
) {
    /** Largura total somando todas as peças, convertida para metros (`L`). */
    val larguraTotalMetros: Double
        get() = pecas.sumOf { it.larguraCm } / 100.0

    /** Profundidade principal em cm (da primeira peça, ou 0 se vazio). */
    val profundidadePrincipalCm: Double
        get() = pecas.firstOrNull()?.profundidadeCm ?: 0.0

    /** Altura principal em cm (da primeira peça, ou 0 se vazio). */
    val alturaPrincipalCm: Double
        get() = pecas.firstOrNull()?.alturaCm ?: 0.0
}

/**
 * Bloco da Etapa 3: Checklist condicional exibido somente quando `tipoServico == TipoServico.REFORMA`.
 */
data class ChecklistReforma(
    val substituirMadeira: Boolean = false,
    val substituirEspuma: Boolean = false,
    val substituirMolas: Boolean = false,
    val substituirPercinta: Boolean = false,
    val substituirChapas: Boolean = false,
)

/**
 * Item de material calculado ou editado manualmente na Etapa 4 (Padrão Snapshot - RN05).
 *
 * Guarda o preço unitário congelado ([precoUnitarioSnapshot]) da data em que o orçamento
 * foi montado, garantindo imutabilidade histórica mesmo se a Tabela de Preços mudar depois.
 */
data class ItemMaterialOrcamento(
    val id: Long = 0L,
    val categoria: CategoriaMaterial,
    val chaveMaterial: String,
    val descricao: String,
    val unidade: String,
    val precoUnitarioSnapshot: Double,
    val quantidade: Double,
    val custoTotal: Double,
    val editadoManualmente: Boolean = false,
)

/**
 * Bloco financeiro consolidado das Etapas 4 e 5 (Seção 3 da especificação).
 */
data class ResumoFinanceiro(
    val subtotalMateriais: Double = 0.0,
    val margemExtrasPercentual: Double = 10.0,
    val valorMargemExtras: Double = 0.0,
    val valorDiaria: Double = 200.0,
    val diasTrabalhados: Double = 1.0,
    val totalMaoDeObra: Double = 0.0,
    val custoFrete: Double = 0.0,
    val totalCalculado: Double = 0.0,
    val valorSobrescritoManual: Double? = null,
    val valorFinalSalvo: Double = 0.0,
) {
    /**
     * Ajuste comercial dinâmico (`valorSobrescritoManual - totalCalculado`).
     * Retorna `null` quando não há sobrescrita manual ou a diferença é zero.
     */
    val ajusteComercial: Double?
        get() {
            val manual = valorSobrescritoManual ?: return null
            val diferenca = manual - totalCalculado
            return if (kotlin.math.abs(diferenca) < 0.005) null else diferenca
        }
}

/**
 * Entidade agregada principal de domínio representando um orçamento completo.
 */
data class Orcamento(
    val id: Long = 0L,
    val criadoEm: Long = System.currentTimeMillis(),
    val status: StatusOrcamento = StatusOrcamento.PENDENTE,
    val tipoServico: TipoServico,
    val cliente: DadosCliente,
    val sofa: EspecificacoesSofa,
    val checklistReforma: ChecklistReforma,
    val itens: List<ItemMaterialOrcamento> = emptyList(),
    val financeiro: ResumoFinanceiro = ResumoFinanceiro(),
)

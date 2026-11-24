package com.orcaconfort.app.domain.model

/**
 * Categorias do catálogo de materiais (Tabela de Preços e itens de orçamento).
 *
 * Para adicionar uma nova categoria no futuro, basta incluir um novo valor aqui
 * (os TypeConverters do Room salvam o nome do enum como String, sem quebrar o banco).
 */
enum class CategoriaMaterial(val tituloExibicao: String) {
    TECIDO("Tecidos"),
    ESPUMA("Espumas"),
    FIBRA("Fibra"),
    PERCINTA("Percinta"),
    MADEIRA("Madeira"),
    FOLHA("Folhas"),
    PE("Pés"),
    MOLA("Molas"),
    OUTROS("Outros");

    companion object {
        fun fromNomeSeguro(nome: String): CategoriaMaterial =
            entries.firstOrNull { it.name == nome } ?: OUTROS
    }
}

/**
 * Tipo de serviço do orçamento (RN01 - Reforma vs. Fabricação Nova).
 *
 * - [REFORMA]: itens estruturais dependem do checklist marcado pelo profissional.
 * - [SOFA_NOVO]: estrutura completa (madeira, percinta, chapas, espuma, pés) entra automaticamente.
 */
enum class TipoServico(val tituloExibicao: String) {
    REFORMA("Reforma"),
    SOFA_NOVO("Sofá Novo");

    companion object {
        fun fromNomeSeguro(nome: String): TipoServico =
            entries.firstOrNull { it.name == nome } ?: REFORMA

        fun fromTitulo(titulo: String): TipoServico =
            entries.firstOrNull { it.tituloExibicao.equals(titulo, ignoreCase = true) } ?: REFORMA
    }
}

/**
 * Modelos exclusivos suportados pelo aplicativo (RN02).
 */
enum class ModeloSofa(
    val tituloCurto: String,
    val tituloCompleto: String,
    val descricaoCurta: String,
) {
    FIXO(
        tituloCurto = "Fixo",
        tituloCompleto = "Sofá fixo",
        descricaoCurta = "Assento fixo",
    ),
    RETRATIL(
        tituloCurto = "Retrátil",
        tituloCompleto = "Sofá retrátil",
        descricaoCurta = "Assento móvel",
    ),
    MODULAR(
        tituloCurto = "Modular",
        tituloCompleto = "Sofá modular",
        descricaoCurta = "Por módulos",
    );

    companion object {
        fun fromNomeSeguro(nome: String): ModeloSofa =
            entries.firstOrNull { it.name == nome } ?: RETRATIL

        fun fromTituloCurto(titulo: String): ModeloSofa =
            entries.firstOrNull { it.tituloCurto.equals(titulo, ignoreCase = true) } ?: RETRATIL
    }
}

/**
 * Ciclo de vida do orçamento exibido nos cards da Home e na tela de Detalhes.
 */
enum class StatusOrcamento(val tituloExibicao: String) {
    PENDENTE("Pendente"),
    APROVADO("Aprovado"),
    PRODUCAO("Produção"),
    CONCLUIDO("Concluído");

    companion object {
        fun fromNomeSeguro(nome: String): StatusOrcamento =
            entries.firstOrNull { it.name == nome } ?: PENDENTE
    }
}

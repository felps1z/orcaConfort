package com.orcaconfort.app.domain.usecase

/**
 * Centralizador de constantes e multiplicadores do Motor de Cálculo (Seção 2 da Especificação v1.0).
 *
 * COMO AJUSTAR REGRAS NO FUTURO:
 * Caso o cliente valide multiplicadores diferentes durante os testes em campo
 * (ex.: fator de espuma em sofás > 2,50 m, margem padrão ou fator do modelo Modular),
 * basta alterar as constantes abaixo — os UseCases e testes unitários usarão os novos valores.
 */
object RegrasCalculoConfig {

    // --- 2.1 Tecido (metros lineares por metro de largura L) ---
    const val FATOR_TECIDO_FIXO_SEM_ALMOFADA = 3.2
    const val FATOR_TECIDO_FIXO_COM_ALMOFADA = 4.2
    const val FATOR_TECIDO_RETRATIL = 6.5

    // Nota: o documento v1.0 não definiu fator explícito para o modelo Modular.
    // Valores iniciais configuráveis para fácil validação com o tapeceiro.
    const val FATOR_TECIDO_MODULAR_SEM_ALMOFADA = 4.2
    const val FATOR_TECIDO_MODULAR_COM_ALMOFADA = 5.0

    // --- 2.2 Fibra Siliconada (kg) ---
    const val FIBRA_KG_POR_ALMOFADA = 2.0
    const val FIBRA_KG_POR_METRO_LARGURA = 2.67

    // --- 2.3 Espumas (metros de lâmina proporcional à largura base de 1,50 m) ---
    const val LARGURA_BASE_ESPUMA_METROS = 1.50
    const val ESPUMA_10CM_BASE_METROS = 1.00 // Assento e braços (espuma grossa)
    const val ESPUMA_3CM_BASE_METROS = 0.50  // Encosto e acabamento (espuma fina)

    // --- 2.4 Percinta Elástica (metros lineares por metro de largura L) ---
    const val FATOR_PERCINTA_POR_METRO = 12.2

    // --- 2.5 Madeira Estrutural (1 tábua de 3,00m x 0,15m a cada 0,30m de largura) ---
    const val LARGURA_POR_TABUA_METROS = 0.30

    // --- 2.6 Outros Materiais (sugestões iniciais editáveis) ---
    const val LIMITE_LARGURA_4_PES_METROS = 1.80
    const val PES_PADRAO_PEQUENO = 4
    const val PES_PADRAO_GRANDE_OU_MOVEL = 6
    const val LARGURA_BASE_CHAPA_METROS = 1.50
    const val MOLAS_SUGESTAO_REFORMA = 4.0

    // --- Chaves estáveis para identificar cada item de material no orçamento ---
    const val CHAVE_TECIDO = "tecido"
    const val CHAVE_FIBRA = "fibra"
    const val CHAVE_ESPUMA_1CM = "espuma_1cm"
    const val CHAVE_ESPUMA_2CM = "espuma_2cm"
    const val CHAVE_ESPUMA_3CM = "espuma_3cm"
    const val CHAVE_ESPUMA_5CM = "espuma_5cm"
    const val CHAVE_ESPUMA_10CM = "espuma_10cm"
    const val CHAVE_PERCINTA = "percinta"
    const val CHAVE_MADEIRA = "madeira"
    const val CHAVE_CHAPAS = "chapas"
    const val CHAVE_MOLAS = "molas"
    const val CHAVE_PES = "pes"
}

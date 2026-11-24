package com.orcaconfort.app.domain.model

/**
 * Perfil institucional da tapeçaria e parâmetros financeiros padrão (Tabela 4 do banco).
 *
 * Utilizado para preencher valores iniciais de novos orçamentos (diária e margem padrão)
 * e, futuramente, o cabeçalho do PDF gerado.
 */
data class PerfilTapeceiro(
    val id: Int = 1,
    val nomeNegocio: String = "Minha Tapeçaria",
    val telefone: String = "",
    val endereco: String = "",
    val instagram: String = "",
    val valorDiariaPadrao: Double = 200.0,
    val margemExtrasPadrao: Double = 10.0,
)

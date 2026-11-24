package com.orcaconfort.app.domain.model

/**
 * Representa um material cadastrado na Tabela de Preços (RN03).
 *
 * Alterações neste catálogo impactam apenas novos orçamentos; orçamentos já salvos
 * mantêm o snapshot de preço gravado no momento do salvamento (RN05).
 */
data class MaterialCatalogo(
    val id: Long = 0L,
    val categoria: CategoriaMaterial,
    val nome: String,
    val unidade: String,
    val precoUnitario: Double,
    val atualizadoEm: Long = System.currentTimeMillis(),
    val especificacao: String? = null,
    val ativo: Boolean = true,
)

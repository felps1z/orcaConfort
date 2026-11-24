package com.orcaconfort.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.orcaconfort.app.domain.model.CategoriaMaterial

/**
 * Tabela 1 (`materials_catalog`): Catálogo local de preços unitários dos insumos.
 *
 * Alimenta a tela "Tabela de Preços" e os seletores do fluxo de orçamento.
 * Usa `isActive` (soft-delete) para evitar perda de referências históricas.
 */
@Entity(tableName = "materials_catalog")
data class MaterialCatalogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val category: CategoriaMaterial,
    val name: String,
    val unit: String,
    val unitPrice: Double,

    @ColumnInfo(defaultValue = "0")
    val lastUpdated: Long = System.currentTimeMillis(),

    /** Campo flexível para detalhes técnicos (ex.: largura do rolo, medida da lâmina). */
    val specification: String? = null,

    @ColumnInfo(defaultValue = "1")
    val isActive: Boolean = true,
)

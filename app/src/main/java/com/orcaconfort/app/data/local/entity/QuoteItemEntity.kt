package com.orcaconfort.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.orcaconfort.app.domain.model.CategoriaMaterial

/**
 * Tabela 3 (`quote_items`): Itens de material congelados de um orçamento (Padrão Snapshot - RN05).
 *
 * Relação 1:N com `quotes` com `CASCADE`: excluir o orçamento apaga seus itens automaticamente.
 * Não referencia o catálogo por chave estrangeira para garantir imutabilidade histórica.
 */
@Entity(
    tableName = "quote_items",
    foreignKeys = [
        ForeignKey(
            entity = QuoteEntity::class,
            parentColumns = ["quoteId"],
            childColumns = ["quoteOwnerId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("quoteOwnerId")],
)
data class QuoteItemEntity(
    @PrimaryKey(autoGenerate = true)
    val itemId: Long = 0L,

    val quoteOwnerId: Long,
    val category: CategoriaMaterial,

    @ColumnInfo(defaultValue = "")
    val materialKey: String = "",

    val description: String,
    val unit: String,

    /** Preço unitário vigente no momento da criação/edição do orçamento. */
    val unitPriceSnapshot: Double,
    val quantity: Double,
    val totalCost: Double,

    @ColumnInfo(defaultValue = "0")
    val isManuallyEdited: Boolean = false,
)

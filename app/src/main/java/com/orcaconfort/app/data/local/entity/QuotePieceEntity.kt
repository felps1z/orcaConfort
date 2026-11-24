package com.orcaconfort.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tabela filha (`quote_pieces`): Dimensões individuais (L × P × A) de cada peça do sofá.
 *
 * Extensão limpa sobre o modelo base para suportar múltiplos módulos/peças informados
 * no campo "Número de peças" da Etapa 2 e detalhados na Etapa 3.
 */
@Entity(
    tableName = "quote_pieces",
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
data class QuotePieceEntity(
    @PrimaryKey(autoGenerate = true)
    val pieceId: Long = 0L,

    val quoteOwnerId: Long,
    val pieceOrder: Int,
    val widthCm: Double,
    val depthCm: Double,
    val heightCm: Double,
)

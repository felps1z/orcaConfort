package com.orcaconfort.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * POJO relacional do Room que carrega o orçamento junto com seus itens de material
 * e as dimensões individuais de cada peça em uma única transação.
 */
data class QuoteWithDetails(
    @Embedded
    val quote: QuoteEntity,

    @Relation(
        parentColumn = "quoteId",
        entityColumn = "quoteOwnerId",
    )
    val items: List<QuoteItemEntity>,

    @Relation(
        parentColumn = "quoteId",
        entityColumn = "quoteOwnerId",
    )
    val pieces: List<QuotePieceEntity>,
)

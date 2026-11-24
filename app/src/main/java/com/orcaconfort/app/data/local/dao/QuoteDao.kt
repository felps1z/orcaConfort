package com.orcaconfort.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.orcaconfort.app.data.local.entity.QuoteEntity
import com.orcaconfort.app.data.local.entity.QuoteItemEntity
import com.orcaconfort.app.data.local.entity.QuotePieceEntity
import com.orcaconfort.app.data.local.entity.QuoteWithDetails
import com.orcaconfort.app.domain.model.StatusOrcamento
import kotlinx.coroutines.flow.Flow

/**
 * DAO das Tabelas 2 e 3 (`quotes`, `quote_items` e `quote_pieces`).
 *
 * Usa `@Transaction` nas consultas relacionais e no salvamento completo para garantir
 * consistência atômica entre cabeçalho, peças e snapshot de materiais.
 */
@Dao
interface QuoteDao {

    /**
     * Lista todos os orçamentos para a Home ordenados pela data de entrega mais próxima.
     * Orçamentos sem data de entrega preenchida aparecem por último.
     */
    @Query(
        "SELECT * FROM quotes " +
            "ORDER BY CASE WHEN client_deliveryDate IS NULL THEN 1 ELSE 0 END, " +
            "client_deliveryDate ASC, quoteId DESC",
    )
    fun getAllQuotesSummaryFlow(): Flow<List<QuoteEntity>>

    @Transaction
    @Query("SELECT * FROM quotes WHERE quoteId = :id")
    fun observeQuoteWithDetailsById(id: Long): Flow<QuoteWithDetails?>

    @Transaction
    @Query("SELECT * FROM quotes WHERE quoteId = :id")
    suspend fun getQuoteWithDetailsById(id: Long): QuoteWithDetails?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertQuote(quote: QuoteEntity): Long

    @Update
    suspend fun updateQuote(quote: QuoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuoteItems(items: List<QuoteItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotePieces(pieces: List<QuotePieceEntity>)

    @Query("DELETE FROM quote_items WHERE quoteOwnerId = :quoteId")
    suspend fun clearQuoteItems(quoteId: Long)

    @Query("DELETE FROM quote_pieces WHERE quoteOwnerId = :quoteId")
    suspend fun clearQuotePieces(quoteId: Long)

    @Query("UPDATE quotes SET status = :status WHERE quoteId = :quoteId")
    suspend fun updateStatus(quoteId: Long, status: StatusOrcamento)

    @Query("DELETE FROM quotes WHERE quoteId = :quoteId")
    suspend fun deleteQuoteById(quoteId: Long)

    /**
     * Transação atômica para criar ou atualizar um orçamento com todos os seus itens e peças.
     * Evita `REPLACE` na tabela pai em atualizações para não disparar CASCADE indesejado.
     */
    @Transaction
    suspend fun saveCompleteQuote(
        quote: QuoteEntity,
        items: List<QuoteItemEntity>,
        pieces: List<QuotePieceEntity>,
    ): Long {
        val savedQuoteId = if (quote.quoteId == 0L) {
            insertQuote(quote)
        } else {
            updateQuote(quote)
            quote.quoteId
        }

        clearQuoteItems(savedQuoteId)
        if (items.isNotEmpty()) {
            insertQuoteItems(items.map { it.copy(itemId = 0L, quoteOwnerId = savedQuoteId) })
        }

        clearQuotePieces(savedQuoteId)
        if (pieces.isNotEmpty()) {
            insertQuotePieces(pieces.map { it.copy(pieceId = 0L, quoteOwnerId = savedQuoteId) })
        }

        return savedQuoteId
    }
}

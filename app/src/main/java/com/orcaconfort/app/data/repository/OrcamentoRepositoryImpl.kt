package com.orcaconfort.app.data.repository

import com.orcaconfort.app.data.local.dao.QuoteDao
import com.orcaconfort.app.data.mapper.toDomain
import com.orcaconfort.app.data.mapper.toDomainSummary
import com.orcaconfort.app.data.mapper.toEntity
import com.orcaconfort.app.data.mapper.toQuoteEntity
import com.orcaconfort.app.domain.model.Orcamento
import com.orcaconfort.app.domain.model.StatusOrcamento
import com.orcaconfort.app.domain.repository.OrcamentoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Implementação Room do [OrcamentoRepository]. */
@Singleton
class OrcamentoRepositoryImpl @Inject constructor(
    private val quoteDao: QuoteDao,
) : OrcamentoRepository {

    override fun observarTodosOrcamentos(): Flow<List<Orcamento>> =
        quoteDao.getAllQuotesSummaryFlow().map { lista -> lista.map { it.toDomainSummary() } }

    override fun observarOrcamentoPorId(id: Long): Flow<Orcamento?> =
        quoteDao.observeQuoteWithDetailsById(id).map { it?.toDomain() }

    override suspend fun buscarOrcamentoPorId(id: Long): Orcamento? =
        quoteDao.getQuoteWithDetailsById(id)?.toDomain()

    override suspend fun salvarOrcamentoCompleto(orcamento: Orcamento): Long {
        val quoteEntity = orcamento.toQuoteEntity()
        val itemsEntities = orcamento.itens.map { it.toEntity(orcamento.id) }
        val piecesEntities = orcamento.sofa.pecas.map { it.toEntity(orcamento.id) }
        return quoteDao.saveCompleteQuote(
            quote = quoteEntity,
            items = itemsEntities,
            pieces = piecesEntities,
        )
    }

    override suspend fun atualizarStatus(id: Long, novoStatus: StatusOrcamento) {
        quoteDao.updateStatus(quoteId = id, status = novoStatus)
    }

    override suspend fun excluirOrcamento(id: Long) {
        quoteDao.deleteQuoteById(id)
    }
}

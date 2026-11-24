package com.orcaconfort.app.domain.repository

import com.orcaconfort.app.domain.model.Orcamento
import com.orcaconfort.app.domain.model.StatusOrcamento
import kotlinx.coroutines.flow.Flow

/** Contrato de domínio para persistência e consulta de orçamentos. */
interface OrcamentoRepository {
    fun observarTodosOrcamentos(): Flow<List<Orcamento>>
    fun observarOrcamentoPorId(id: Long): Flow<Orcamento?>
    suspend fun buscarOrcamentoPorId(id: Long): Orcamento?
    suspend fun salvarOrcamentoCompleto(orcamento: Orcamento): Long
    suspend fun atualizarStatus(id: Long, novoStatus: StatusOrcamento)
    suspend fun excluirOrcamento(id: Long)
}

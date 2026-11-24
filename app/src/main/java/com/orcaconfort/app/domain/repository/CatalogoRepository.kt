package com.orcaconfort.app.domain.repository

import com.orcaconfort.app.domain.model.MaterialCatalogo
import kotlinx.coroutines.flow.Flow

/** Contrato de domínio para leitura e manutenção da Tabela de Preços. */
interface CatalogoRepository {
    fun observarMateriaisAtivos(): Flow<List<MaterialCatalogo>>
    suspend fun listarMateriaisAtivos(): List<MaterialCatalogo>
    suspend fun salvarMaterial(material: MaterialCatalogo)
    suspend fun atualizarPreco(
        materialId: Long,
        novoPreco: Double,
        atualizadoEm: Long = System.currentTimeMillis(),
    )
}

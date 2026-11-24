package com.orcaconfort.app.data.repository

import com.orcaconfort.app.data.local.dao.CatalogDao
import com.orcaconfort.app.data.mapper.toDomain
import com.orcaconfort.app.data.mapper.toEntity
import com.orcaconfort.app.domain.model.MaterialCatalogo
import com.orcaconfort.app.domain.repository.CatalogoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Implementação Room do [CatalogoRepository]. */
@Singleton
class CatalogoRepositoryImpl @Inject constructor(
    private val catalogDao: CatalogDao,
) : CatalogoRepository {

    override fun observarMateriaisAtivos(): Flow<List<MaterialCatalogo>> =
        catalogDao.getAllMaterialsFlow().map { lista -> lista.map { it.toDomain() } }

    override suspend fun listarMateriaisAtivos(): List<MaterialCatalogo> =
        catalogDao.getAllMaterials().map { it.toDomain() }

    override suspend fun salvarMaterial(material: MaterialCatalogo) {
        catalogDao.saveMaterial(material.toEntity())
    }

    override suspend fun atualizarPreco(
        materialId: Long,
        novoPreco: Double,
        atualizadoEm: Long,
    ) {
        catalogDao.updatePrice(
            materialId = materialId,
            novoPreco = novoPreco,
            atualizadoEm = atualizadoEm,
        )
    }
}

package com.orcaconfort.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.orcaconfort.app.data.local.entity.MaterialCatalogEntity
import kotlinx.coroutines.flow.Flow

/** DAO da Tabela 1 (`materials_catalog`): gerencia a Tabela de Preços local. */
@Dao
interface CatalogDao {

    @Query("SELECT * FROM materials_catalog WHERE isActive = 1 ORDER BY category, id")
    fun getAllMaterialsFlow(): Flow<List<MaterialCatalogEntity>>

    @Query("SELECT * FROM materials_catalog WHERE isActive = 1 ORDER BY category, id")
    suspend fun getAllMaterials(): List<MaterialCatalogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMaterial(material: MaterialCatalogEntity)

    @Query(
        "UPDATE materials_catalog " +
            "SET unitPrice = :novoPreco, lastUpdated = :atualizadoEm " +
            "WHERE id = :materialId",
    )
    suspend fun updatePrice(
        materialId: Long,
        novoPreco: Double,
        atualizadoEm: Long,
    )
}

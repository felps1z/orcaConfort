package com.orcaconfort.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.orcaconfort.app.data.local.entity.WorkshopProfileEntity
import kotlinx.coroutines.flow.Flow

/** DAO da Tabela 4 (`workshop_profile`): perfil institucional e padrões da tapeçaria. */
@Dao
interface WorkshopProfileDao {

    @Query("SELECT * FROM workshop_profile WHERE id = 1 LIMIT 1")
    fun observeProfile(): Flow<WorkshopProfileEntity?>

    @Query("SELECT * FROM workshop_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): WorkshopProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: WorkshopProfileEntity)
}

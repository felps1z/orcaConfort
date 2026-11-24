package com.orcaconfort.app.data.repository

import com.orcaconfort.app.data.local.dao.WorkshopProfileDao
import com.orcaconfort.app.data.mapper.toDomain
import com.orcaconfort.app.data.mapper.toEntity
import com.orcaconfort.app.domain.model.PerfilTapeceiro
import com.orcaconfort.app.domain.repository.PerfilTapeceiroRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Implementação Room do [PerfilTapeceiroRepository]. */
@Singleton
class PerfilTapeceiroRepositoryImpl @Inject constructor(
    private val workshopProfileDao: WorkshopProfileDao,
) : PerfilTapeceiroRepository {

    override fun observarPerfil(): Flow<PerfilTapeceiro> =
        workshopProfileDao.observeProfile().map { it?.toDomain() ?: PerfilTapeceiro() }

    override suspend fun obterPerfil(): PerfilTapeceiro =
        workshopProfileDao.getProfile()?.toDomain() ?: PerfilTapeceiro()

    override suspend fun salvarPerfil(perfil: PerfilTapeceiro) {
        workshopProfileDao.saveProfile(perfil.toEntity())
    }
}

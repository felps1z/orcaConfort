package com.orcaconfort.app.domain.repository

import com.orcaconfort.app.domain.model.PerfilTapeceiro
import kotlinx.coroutines.flow.Flow

/** Contrato de domínio para as configurações e perfil da tapeçaria. */
interface PerfilTapeceiroRepository {
    fun observarPerfil(): Flow<PerfilTapeceiro>
    suspend fun obterPerfil(): PerfilTapeceiro
    suspend fun salvarPerfil(perfil: PerfilTapeceiro)
}

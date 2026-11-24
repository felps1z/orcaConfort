package com.orcaconfort.app.di

import com.orcaconfort.app.data.pdf.AndroidPdfOrcamentoGenerator
import com.orcaconfort.app.data.repository.CatalogoRepositoryImpl
import com.orcaconfort.app.data.repository.OrcamentoRepositoryImpl
import com.orcaconfort.app.data.repository.PerfilTapeceiroRepositoryImpl
import com.orcaconfort.app.domain.repository.CatalogoRepository
import com.orcaconfort.app.domain.repository.GeradorPdfOrcamento
import com.orcaconfort.app.domain.repository.OrcamentoRepository
import com.orcaconfort.app.domain.repository.PerfilTapeceiroRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt que liga as interfaces de repositório (`domain/repository`) às suas
 * implementações (`data/`), preservando a inversão de dependência.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCatalogoRepository(impl: CatalogoRepositoryImpl): CatalogoRepository

    @Binds
    @Singleton
    abstract fun bindOrcamentoRepository(impl: OrcamentoRepositoryImpl): OrcamentoRepository

    @Binds
    @Singleton
    abstract fun bindPerfilTapeceiroRepository(
        impl: PerfilTapeceiroRepositoryImpl,
    ): PerfilTapeceiroRepository

    @Binds
    @Singleton
    abstract fun bindGeradorPdfOrcamento(
        impl: AndroidPdfOrcamentoGenerator,
    ): GeradorPdfOrcamento
}

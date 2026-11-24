package com.orcaconfort.app.di

import android.content.Context
import androidx.room.Room
import com.orcaconfort.app.data.local.DatabaseSeedCallback
import com.orcaconfort.app.data.local.OrcaConfortDatabase
import com.orcaconfort.app.data.local.dao.CatalogDao
import com.orcaconfort.app.data.local.dao.QuoteDao
import com.orcaconfort.app.data.local.dao.WorkshopProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt responsável por fornecer a instância singleton do Room Database e seus DAOs.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideOrcaConfortDatabase(
        @ApplicationContext context: Context,
    ): OrcaConfortDatabase =
        Room.databaseBuilder(
            context,
            OrcaConfortDatabase::class.java,
            OrcaConfortDatabase.DATABASE_NAME,
        )
            .addCallback(DatabaseSeedCallback())
            // Conforme guia da Seção 6.1: facilita ajustes de esquema durante o desenvolvimento.
            // Ao lançar em produção no aparelho do cliente, substituir por AutoMigration/Migration.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideCatalogDao(database: OrcaConfortDatabase): CatalogDao = database.catalogDao()

    @Provides
    fun provideQuoteDao(database: OrcaConfortDatabase): QuoteDao = database.quoteDao()

    @Provides
    fun provideWorkshopProfileDao(database: OrcaConfortDatabase): WorkshopProfileDao =
        database.workshopProfileDao()
}

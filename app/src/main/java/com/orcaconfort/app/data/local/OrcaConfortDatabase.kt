package com.orcaconfort.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.orcaconfort.app.data.local.converter.OrcaConfortTypeConverters
import com.orcaconfort.app.data.local.dao.CatalogDao
import com.orcaconfort.app.data.local.dao.QuoteDao
import com.orcaconfort.app.data.local.dao.WorkshopProfileDao
import com.orcaconfort.app.data.local.entity.MaterialCatalogEntity
import com.orcaconfort.app.data.local.entity.QuoteEntity
import com.orcaconfort.app.data.local.entity.QuoteItemEntity
import com.orcaconfort.app.data.local.entity.QuotePieceEntity
import com.orcaconfort.app.data.local.entity.WorkshopProfileEntity

/**
 * Banco de dados local Room do OrçaConfort.
 *
 * GUIA RÁPIDO DE EVOLUÇÃO DO ESQUEMA:
 * - Durante o desenvolvimento: basta incrementar `version` (o builder usa migração destrutiva).
 * - Em produção (celular do cliente com dados reais): adicione novas colunas com
 *   `@ColumnInfo(defaultValue = ...)` ou anuláveis e configure `autoMigrations` aqui.
 */
@Database(
    entities = [
        MaterialCatalogEntity::class,
        QuoteEntity::class,
        QuoteItemEntity::class,
        QuotePieceEntity::class,
        WorkshopProfileEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(OrcaConfortTypeConverters::class)
abstract class OrcaConfortDatabase : RoomDatabase() {

    abstract fun catalogDao(): CatalogDao
    abstract fun quoteDao(): QuoteDao
    abstract fun workshopProfileDao(): WorkshopProfileDao

    companion object {
        const val DATABASE_NAME = "orcaconfort.db"
    }
}

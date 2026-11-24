package com.orcaconfort.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tabela 4 (`workshop_profile`): Perfil institucional da tapeçaria e parâmetros padrão.
 *
 * Mantém sempre um único registro (`id = 1`) para preencher valores iniciais do orçamento
 * e o cabeçalho institucional do PDF.
 */
@Entity(tableName = "workshop_profile")
data class WorkshopProfileEntity(
    @PrimaryKey
    val id: Int = 1,

    @ColumnInfo(defaultValue = "Minha Tapeçaria")
    val businessName: String = "Minha Tapeçaria",

    @ColumnInfo(defaultValue = "")
    val phone: String = "",

    @ColumnInfo(defaultValue = "")
    val address: String = "",

    @ColumnInfo(defaultValue = "")
    val instagram: String = "",

    @ColumnInfo(defaultValue = "200.0")
    val defaultDailyRate: Double = 200.0,

    @ColumnInfo(defaultValue = "10.0")
    val defaultExtraMarginPercent: Double = 10.0,
)

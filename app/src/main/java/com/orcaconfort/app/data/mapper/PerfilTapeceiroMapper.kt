package com.orcaconfort.app.data.mapper

import com.orcaconfort.app.data.local.entity.WorkshopProfileEntity
import com.orcaconfort.app.domain.model.PerfilTapeceiro

/** Conversões entre [WorkshopProfileEntity] e [PerfilTapeceiro]. */
fun WorkshopProfileEntity.toDomain(): PerfilTapeceiro = PerfilTapeceiro(
    id = id,
    nomeNegocio = businessName,
    telefone = phone,
    endereco = address,
    instagram = instagram,
    valorDiariaPadrao = defaultDailyRate,
    margemExtrasPadrao = defaultExtraMarginPercent,
)

fun PerfilTapeceiro.toEntity(): WorkshopProfileEntity = WorkshopProfileEntity(
    id = id,
    businessName = nomeNegocio,
    phone = telefone,
    address = endereco,
    instagram = instagram,
    defaultDailyRate = valorDiariaPadrao,
    defaultExtraMarginPercent = margemExtrasPadrao,
)

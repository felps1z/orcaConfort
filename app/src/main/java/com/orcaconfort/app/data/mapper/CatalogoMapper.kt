package com.orcaconfort.app.data.mapper

import com.orcaconfort.app.data.local.entity.MaterialCatalogEntity
import com.orcaconfort.app.domain.model.MaterialCatalogo

/** Conversões entre a entidade Room [MaterialCatalogEntity] e o modelo de domínio [MaterialCatalogo]. */
fun MaterialCatalogEntity.toDomain(): MaterialCatalogo = MaterialCatalogo(
    id = id,
    categoria = category,
    nome = name,
    unidade = unit,
    precoUnitario = unitPrice,
    atualizadoEm = lastUpdated,
    especificacao = specification,
    ativo = isActive,
)

fun MaterialCatalogo.toEntity(): MaterialCatalogEntity = MaterialCatalogEntity(
    id = id,
    category = categoria,
    name = nome,
    unit = unidade,
    unitPrice = precoUnitario,
    lastUpdated = atualizadoEm,
    specification = especificacao,
    isActive = ativo,
)

package com.orcaconfort.app.data.mapper

import com.orcaconfort.app.data.local.entity.ClientData
import com.orcaconfort.app.data.local.entity.FinancialData
import com.orcaconfort.app.data.local.entity.QuoteEntity
import com.orcaconfort.app.data.local.entity.QuoteItemEntity
import com.orcaconfort.app.data.local.entity.QuotePieceEntity
import com.orcaconfort.app.data.local.entity.QuoteWithDetails
import com.orcaconfort.app.data.local.entity.ReformChecklist
import com.orcaconfort.app.data.local.entity.SofaSpecifications
import com.orcaconfort.app.domain.model.ChecklistReforma
import com.orcaconfort.app.domain.model.DadosCliente
import com.orcaconfort.app.domain.model.DimensaoPeca
import com.orcaconfort.app.domain.model.EspecificacoesSofa
import com.orcaconfort.app.domain.model.ItemMaterialOrcamento
import com.orcaconfort.app.domain.model.Orcamento
import com.orcaconfort.app.domain.model.ResumoFinanceiro

/** Conversões entre entidades Room de orçamento e o agregado de domínio [Orcamento]. */

fun QuoteEntity.toDomainSummary(): Orcamento = Orcamento(
    id = quoteId,
    criadoEm = createdAt,
    status = status,
    tipoServico = serviceType,
    cliente = client.toDomain(),
    sofa = sofa.toDomain(
        pecas = listOf(
            DimensaoPeca(
                ordem = 1,
                larguraCm = sofa.widthCm,
                profundidadeCm = sofa.depthCm,
                alturaCm = sofa.heightCm,
            ),
        ),
    ),
    checklistReforma = reformChecklist.toDomain(),
    itens = emptyList(),
    financeiro = financials.toDomain(),
)

fun QuoteWithDetails.toDomain(): Orcamento {
    val pecasOrdenadas = pieces
        .sortedBy { it.pieceOrder }
        .map { it.toDomain() }
        .ifEmpty {
            listOf(
                DimensaoPeca(
                    ordem = 1,
                    larguraCm = quote.sofa.widthCm,
                    profundidadeCm = quote.sofa.depthCm,
                    alturaCm = quote.sofa.heightCm,
                ),
            )
        }

    return Orcamento(
        id = quote.quoteId,
        criadoEm = quote.createdAt,
        status = quote.status,
        tipoServico = quote.serviceType,
        cliente = quote.client.toDomain(),
        sofa = quote.sofa.toDomain(pecasOrdenadas),
        checklistReforma = quote.reformChecklist.toDomain(),
        itens = items.map { it.toDomain() },
        financeiro = quote.financials.toDomain(),
    )
}

fun Orcamento.toQuoteEntity(): QuoteEntity = QuoteEntity(
    quoteId = id,
    createdAt = criadoEm,
    status = status,
    serviceType = tipoServico,
    client = ClientData(
        name = cliente.nome,
        phone = cliente.telefone,
        deliveryDate = cliente.dataEntregaTimestamp,
        observations = cliente.observacoes,
    ),
    sofa = SofaSpecifications(
        model = sofa.modelo,
        pieceCount = sofa.pecas.size.coerceAtLeast(1),
        hasPillows = sofa.possuiAlmofadas,
        pillowCount = sofa.qtdAlmofadas,
        selectedFabricName = sofa.nomeTecidoSelecionado,
        selectedFabricPrice = sofa.precoTecidoSelecionado,
        fabricColor = sofa.corTecido,
        widthCm = sofa.pecas.sumOf { it.larguraCm },
        depthCm = sofa.profundidadePrincipalCm,
        heightCm = sofa.alturaPrincipalCm,
    ),
    reformChecklist = ReformChecklist(
        replaceWood = checklistReforma.substituirMadeira,
        replaceFoam = checklistReforma.substituirEspuma,
        replaceSprings = checklistReforma.substituirMolas,
        replaceWebbing = checklistReforma.substituirPercinta,
        replaceSheets = checklistReforma.substituirChapas,
    ),
    financials = FinancialData(
        materialsSubtotal = financeiro.subtotalMateriais,
        extraMarginPercent = financeiro.margemExtrasPercentual,
        extraMarginValue = financeiro.valorMargemExtras,
        dailyRate = financeiro.valorDiaria,
        workDays = financeiro.diasTrabalhados,
        laborTotal = financeiro.totalMaoDeObra,
        freightCost = financeiro.custoFrete,
        calculatedTotal = financeiro.totalCalculado,
        manualOverrideTotal = financeiro.valorSobrescritoManual,
        finalSavedValue = financeiro.valorFinalSalvo,
    ),
)

fun ItemMaterialOrcamento.toEntity(quoteOwnerId: Long): QuoteItemEntity = QuoteItemEntity(
    itemId = id,
    quoteOwnerId = quoteOwnerId,
    category = categoria,
    materialKey = chaveMaterial,
    description = descricao,
    unit = unidade,
    unitPriceSnapshot = precoUnitarioSnapshot,
    quantity = quantidade,
    totalCost = custoTotal,
    isManuallyEdited = editadoManualmente,
)

fun DimensaoPeca.toEntity(quoteOwnerId: Long): QuotePieceEntity = QuotePieceEntity(
    quoteOwnerId = quoteOwnerId,
    pieceOrder = ordem,
    widthCm = larguraCm,
    depthCm = profundidadeCm,
    heightCm = alturaCm,
)

private fun ClientData.toDomain(): DadosCliente = DadosCliente(
    nome = name,
    telefone = phone,
    dataEntregaTimestamp = deliveryDate,
    observacoes = observations,
)

private fun SofaSpecifications.toDomain(pecas: List<DimensaoPeca>): EspecificacoesSofa =
    EspecificacoesSofa(
        modelo = model,
        numeroPecas = pieceCount.coerceAtLeast(pecas.size),
        pecas = pecas,
        possuiAlmofadas = hasPillows,
        qtdAlmofadas = pillowCount,
        nomeTecidoSelecionado = selectedFabricName,
        precoTecidoSelecionado = selectedFabricPrice,
        corTecido = fabricColor,
    )

private fun ReformChecklist.toDomain(): ChecklistReforma = ChecklistReforma(
    substituirMadeira = replaceWood,
    substituirEspuma = replaceFoam,
    substituirMolas = replaceSprings,
    substituirPercinta = replaceWebbing,
    substituirChapas = replaceSheets,
)

private fun FinancialData.toDomain(): ResumoFinanceiro = ResumoFinanceiro(
    subtotalMateriais = materialsSubtotal,
    margemExtrasPercentual = extraMarginPercent,
    valorMargemExtras = extraMarginValue,
    valorDiaria = dailyRate,
    diasTrabalhados = workDays,
    totalMaoDeObra = laborTotal,
    custoFrete = freightCost,
    totalCalculado = calculatedTotal,
    valorSobrescritoManual = manualOverrideTotal,
    valorFinalSalvo = finalSavedValue,
)

private fun QuoteItemEntity.toDomain(): ItemMaterialOrcamento = ItemMaterialOrcamento(
    id = itemId,
    categoria = category,
    chaveMaterial = materialKey,
    descricao = description,
    unidade = unit,
    precoUnitarioSnapshot = unitPriceSnapshot,
    quantidade = quantity,
    custoTotal = totalCost,
    editadoManualmente = isManuallyEdited,
)

private fun QuotePieceEntity.toDomain(): DimensaoPeca = DimensaoPeca(
    ordem = pieceOrder,
    larguraCm = widthCm,
    profundidadeCm = depthCm,
    alturaCm = heightCm,
)

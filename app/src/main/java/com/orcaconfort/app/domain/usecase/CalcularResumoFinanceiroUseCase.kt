package com.orcaconfort.app.domain.usecase

import com.orcaconfort.app.core.util.Formatadores
import com.orcaconfort.app.domain.model.ItemMaterialOrcamento
import com.orcaconfort.app.domain.model.ResumoFinanceiro
import javax.inject.Inject

/**
 * Calcula a composição financeira completa do orçamento (Seção 3 da Especificação v1.0).
 *
 * Fórmulas aplicadas:
 * - Subtotal Materiais (`C_mat`) = soma dos custos de todos os itens
 * - Margem Extras (`C_extra`) = `C_mat × (margemExtrasPercentual / 100)`
 * - Mão de Obra (`C_mo`) = `diasTrabalhados × valorDiaria`
 * - Total Calculado (`V_total`) = `C_mat + C_extra + C_mo + custoFrete`
 * - Valor Final Salvo = `valorSobrescritoManual` (se informado) ou `V_total`
 */
class CalcularResumoFinanceiroUseCase @Inject constructor() {

    operator fun invoke(
        itens: List<ItemMaterialOrcamento>,
        margemExtrasPercentual: Double,
        diasTrabalhados: Double,
        valorDiaria: Double,
        custoFrete: Double,
        valorSobrescritoManual: Double? = null,
    ): ResumoFinanceiro {
        val margemSegura = margemExtrasPercentual.coerceAtLeast(0.0)
        val diasSeguros = diasTrabalhados.coerceAtLeast(0.0)
        val diariaSegura = valorDiaria.coerceAtLeast(0.0)
        val freteSeguro = custoFrete.coerceAtLeast(0.0)

        val subtotalMateriais = Formatadores.arredondar2Casas(
            itens.sumOf { it.custoTotal },
        )
        val valorMargemExtras = Formatadores.arredondar2Casas(
            subtotalMateriais * (margemSegura / 100.0),
        )
        val totalMaoDeObra = Formatadores.arredondar2Casas(
            diasSeguros * diariaSegura,
        )
        val totalCalculado = Formatadores.arredondar2Casas(
            subtotalMateriais + valorMargemExtras + totalMaoDeObra + freteSeguro,
        )
        val sobrescritoLimpo = valorSobrescritoManual
            ?.takeIf { it > 0.0 }
            ?.let { Formatadores.arredondar2Casas(it) }

        val valorFinalSalvo = sobrescritoLimpo ?: totalCalculado

        return ResumoFinanceiro(
            subtotalMateriais = subtotalMateriais,
            margemExtrasPercentual = margemSegura,
            valorMargemExtras = valorMargemExtras,
            valorDiaria = diariaSegura,
            diasTrabalhados = diasSeguros,
            totalMaoDeObra = totalMaoDeObra,
            custoFrete = freteSeguro,
            totalCalculado = totalCalculado,
            valorSobrescritoManual = sobrescritoLimpo,
            valorFinalSalvo = valorFinalSalvo,
        )
    }
}

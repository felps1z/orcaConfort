package com.orcaconfort.app.domain.usecase

import com.orcaconfort.app.domain.model.CategoriaMaterial
import com.orcaconfort.app.domain.model.ItemMaterialOrcamento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalcularResumoFinanceiroUseCaseTest {

    private val useCase = CalcularResumoFinanceiroUseCase()

    private val itensExemplo = listOf(
        ItemMaterialOrcamento(
            categoria = CategoriaMaterial.TECIDO,
            chaveMaterial = RegrasCalculoConfig.CHAVE_TECIDO,
            descricao = "Tecido: Linho",
            unidade = "m",
            precoUnitarioSnapshot = 45.0,
            quantidade = 12.0,
            custoTotal = 540.0,
        ),
        ItemMaterialOrcamento(
            categoria = CategoriaMaterial.FIBRA,
            chaveMaterial = RegrasCalculoConfig.CHAVE_FIBRA,
            descricao = "Fibra siliconada",
            unidade = "kg",
            precoUnitarioSnapshot = 28.0,
            quantidade = 4.0,
            custoTotal = 112.0,
        ),
    )

    @Test
    fun `composicao financeira sem sobrescrita usa valor total calculado`() {
        val resumo = useCase(
            itens = itensExemplo,
            margemExtrasPercentual = 10.0,
            diasTrabalhados = 3.0,
            valorDiaria = 200.0,
            custoFrete = 120.0,
            valorSobrescritoManual = null,
        )

        // Subtotal = 652.0; Margem 10% = 65.2; Mão de obra = 600.0; Frete = 120.0 -> Total = 1437.20
        assertEquals(652.0, resumo.subtotalMateriais, 0.001)
        assertEquals(65.2, resumo.valorMargemExtras, 0.001)
        assertEquals(600.0, resumo.totalMaoDeObra, 0.001)
        assertEquals(1437.20, resumo.totalCalculado, 0.001)
        assertEquals(1437.20, resumo.valorFinalSalvo, 0.001)
        assertNull(resumo.ajusteComercial)
    }

    @Test
    fun `sobrescrita manual menor calcula ajuste comercial negativo`() {
        val resumo = useCase(
            itens = itensExemplo,
            margemExtrasPercentual = 10.0,
            diasTrabalhados = 3.0,
            valorDiaria = 200.0,
            custoFrete = 120.0,
            valorSobrescritoManual = 1400.0,
        )

        assertEquals(1437.20, resumo.totalCalculado, 0.001)
        assertEquals(1400.0, resumo.valorFinalSalvo, 0.001)
        assertEquals(-37.20, resumo.ajusteComercial!!, 0.001)
    }

    @Test
    fun `sobrescrita manual maior calcula ajuste comercial positivo`() {
        val resumo = useCase(
            itens = itensExemplo,
            margemExtrasPercentual = 10.0,
            diasTrabalhados = 2.0,
            valorDiaria = 200.0,
            custoFrete = 80.0,
            valorSobrescritoManual = 1250.0,
        )

        // Subtotal 652 + Margem 65.2 + MO 400 + Frete 80 = 1197.20 -> Ajuste = +52.80
        assertEquals(1197.20, resumo.totalCalculado, 0.001)
        assertEquals(1250.0, resumo.valorFinalSalvo, 0.001)
        assertEquals(52.80, resumo.ajusteComercial!!, 0.001)
    }
}

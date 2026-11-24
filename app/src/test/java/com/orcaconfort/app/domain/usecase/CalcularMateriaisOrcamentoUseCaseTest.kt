package com.orcaconfort.app.domain.usecase

import com.orcaconfort.app.domain.model.CategoriaMaterial
import com.orcaconfort.app.domain.model.ChecklistReforma
import com.orcaconfort.app.domain.model.DimensaoPeca
import com.orcaconfort.app.domain.model.EspecificacoesSofa
import com.orcaconfort.app.domain.model.MaterialCatalogo
import com.orcaconfort.app.domain.model.ModeloSofa
import com.orcaconfort.app.domain.model.TipoServico
import org.junit.Assert.assertEquals
import org.junit.Test

class CalcularMateriaisOrcamentoUseCaseTest {

    private val useCase = CalcularMateriaisOrcamentoUseCase()

    private val catalogoPadrao = listOf(
        MaterialCatalogo(1L, CategoriaMaterial.TECIDO, "Linho", "m", 45.0),
        MaterialCatalogo(2L, CategoriaMaterial.ESPUMA, "Espuma 1cm", "m", 6.0),
        MaterialCatalogo(3L, CategoriaMaterial.ESPUMA, "Espuma 2cm", "m", 5.0),
        MaterialCatalogo(4L, CategoriaMaterial.ESPUMA, "Espuma 3cm", "m", 5.0),
        MaterialCatalogo(5L, CategoriaMaterial.ESPUMA, "Espuma 5cm", "m", 12.0),
        MaterialCatalogo(6L, CategoriaMaterial.ESPUMA, "Espuma 10cm", "m", 22.0),
        MaterialCatalogo(7L, CategoriaMaterial.FIBRA, "Fibra siliconada", "kg", 28.0),
        MaterialCatalogo(8L, CategoriaMaterial.PERCINTA, "Percinta elástica", "m", 6.0),
        MaterialCatalogo(9L, CategoriaMaterial.MADEIRA, "Tábua de madeira", "un.", 18.0),
        MaterialCatalogo(10L, CategoriaMaterial.FOLHA, "Chapa compensado", "folha", 65.0),
        MaterialCatalogo(11L, CategoriaMaterial.PE, "Pé de madeira", "un.", 15.0),
        MaterialCatalogo(12L, CategoriaMaterial.MOLA, "Mola espiral", "un.", 12.0),
    )

    @Test
    fun `reforma fixo sem checklist zera estrutura e calcula apenas tecido`() {
        val sofa = EspecificacoesSofa(
            modelo = ModeloSofa.FIXO,
            numeroPecas = 1,
            pecas = listOf(DimensaoPeca(1, larguraCm = 200.0, profundidadeCm = 90.0, alturaCm = 85.0)),
            possuiAlmofadas = false,
            qtdAlmofadas = 0,
            nomeTecidoSelecionado = "Linho",
            precoTecidoSelecionado = 45.0,
            corTecido = "Cinza",
        )

        val itens = useCase(
            tipoServico = TipoServico.REFORMA,
            sofa = sofa,
            checklistReforma = ChecklistReforma(),
            catalogo = catalogoPadrao,
        ).associateBy { it.chaveMaterial }

        // ceil(2.0 * 3.2) = 7 m -> 7 * 45 = 315.0
        assertEquals(7.0, itens.getValue(RegrasCalculoConfig.CHAVE_TECIDO).quantidade, 0.001)
        assertEquals(315.0, itens.getValue(RegrasCalculoConfig.CHAVE_TECIDO).custoTotal, 0.001)

        // Demais itens zerados na reforma sem checkboxes marcados
        assertEquals(0.0, itens.getValue(RegrasCalculoConfig.CHAVE_FIBRA).quantidade, 0.001)
        assertEquals(0.0, itens.getValue(RegrasCalculoConfig.CHAVE_ESPUMA_10CM).quantidade, 0.001)
        assertEquals(0.0, itens.getValue(RegrasCalculoConfig.CHAVE_ESPUMA_3CM).quantidade, 0.001)
        assertEquals(0.0, itens.getValue(RegrasCalculoConfig.CHAVE_PERCINTA).quantidade, 0.001)
        assertEquals(0.0, itens.getValue(RegrasCalculoConfig.CHAVE_MADEIRA).quantidade, 0.001)
        assertEquals(0.0, itens.getValue(RegrasCalculoConfig.CHAVE_CHAPAS).quantidade, 0.001)
        assertEquals(0.0, itens.getValue(RegrasCalculoConfig.CHAVE_PES).quantidade, 0.001)
    }

    @Test
    fun `sofa novo fixo com almofadas inclui toda estrutura e 4 pes ate 180cm`() {
        val sofa = EspecificacoesSofa(
            modelo = ModeloSofa.FIXO,
            numeroPecas = 1,
            pecas = listOf(DimensaoPeca(1, larguraCm = 180.0, profundidadeCm = 90.0, alturaCm = 85.0)),
            possuiAlmofadas = true,
            qtdAlmofadas = 4,
            nomeTecidoSelecionado = "Linho",
            precoTecidoSelecionado = 45.0,
            corTecido = "Bege",
        )

        val itens = useCase(
            tipoServico = TipoServico.SOFA_NOVO,
            sofa = sofa,
            checklistReforma = ChecklistReforma(), // ignorado em Sofá Novo (RN01)
            catalogo = catalogoPadrao,
        ).associateBy { it.chaveMaterial }

        assertEquals(8.0, itens.getValue(RegrasCalculoConfig.CHAVE_TECIDO).quantidade, 0.001) // ceil(1.8 * 4.2) = 8
        assertEquals(8.0, itens.getValue(RegrasCalculoConfig.CHAVE_FIBRA).quantidade, 0.001) // 4 * 2.0
        assertEquals(1.2, itens.getValue(RegrasCalculoConfig.CHAVE_ESPUMA_10CM).quantidade, 0.001) // (1.8/1.5)*1.0
        assertEquals(0.6, itens.getValue(RegrasCalculoConfig.CHAVE_ESPUMA_3CM).quantidade, 0.001) // (1.8/1.5)*0.5
        assertEquals(22.0, itens.getValue(RegrasCalculoConfig.CHAVE_PERCINTA).quantidade, 0.001) // ceil(1.8 * 12.2)
        assertEquals(6.0, itens.getValue(RegrasCalculoConfig.CHAVE_MADEIRA).quantidade, 0.001) // ceil(1.8 / 0.30)
        assertEquals(2.0, itens.getValue(RegrasCalculoConfig.CHAVE_CHAPAS).quantidade, 0.001) // ceil(1.8 / 1.50)
        assertEquals(4.0, itens.getValue(RegrasCalculoConfig.CHAVE_PES).quantidade, 0.001) // Fixo <= 1.80m -> 4 pés
    }

    @Test
    fun `reforma retratil 220cm com espuma percinta e madeira calcula fibra proporcional`() {
        val sofa = EspecificacoesSofa(
            modelo = ModeloSofa.RETRATIL,
            numeroPecas = 1,
            pecas = listOf(DimensaoPeca(1, larguraCm = 220.0, profundidadeCm = 95.0, alturaCm = 90.0)),
            possuiAlmofadas = false,
            qtdAlmofadas = 0,
            nomeTecidoSelecionado = "Linho",
            precoTecidoSelecionado = 45.0,
            corTecido = "Cinza",
        )

        val itens = useCase(
            tipoServico = TipoServico.REFORMA,
            sofa = sofa,
            checklistReforma = ChecklistReforma(
                substituirMadeira = true,
                substituirEspuma = true,
                substituirPercinta = true,
                substituirMolas = false,
                substituirChapas = false,
            ),
            catalogo = catalogoPadrao,
        ).associateBy { it.chaveMaterial }

        assertEquals(15.0, itens.getValue(RegrasCalculoConfig.CHAVE_TECIDO).quantidade, 0.001) // ceil(2.2 * 6.5)
        assertEquals(6.0, itens.getValue(RegrasCalculoConfig.CHAVE_FIBRA).quantidade, 0.001) // ceil(2.2 * 2.67)
        assertEquals(1.47, itens.getValue(RegrasCalculoConfig.CHAVE_ESPUMA_10CM).quantidade, 0.001)
        assertEquals(0.73, itens.getValue(RegrasCalculoConfig.CHAVE_ESPUMA_3CM).quantidade, 0.001)
        assertEquals(27.0, itens.getValue(RegrasCalculoConfig.CHAVE_PERCINTA).quantidade, 0.001) // ceil(2.2 * 12.2)
        assertEquals(8.0, itens.getValue(RegrasCalculoConfig.CHAVE_MADEIRA).quantidade, 0.001) // ceil(2.2 / 0.30)
        assertEquals(0.0, itens.getValue(RegrasCalculoConfig.CHAVE_CHAPAS).quantidade, 0.001)
        assertEquals(0.0, itens.getValue(RegrasCalculoConfig.CHAVE_MOLAS).quantidade, 0.001)
    }

    @Test
    fun `sofa novo com multiplas pecas soma larguras e calcula pes por peca`() {
        val sofa = EspecificacoesSofa(
            modelo = ModeloSofa.FIXO,
            numeroPecas = 2,
            pecas = listOf(
                DimensaoPeca(1, larguraCm = 200.0, profundidadeCm = 90.0, alturaCm = 85.0), // > 1.80m -> 6 pés
                DimensaoPeca(2, larguraCm = 150.0, profundidadeCm = 90.0, alturaCm = 85.0), // <= 1.80m -> 4 pés
            ),
            possuiAlmofadas = false,
            qtdAlmofadas = 0,
            nomeTecidoSelecionado = "Linho",
            precoTecidoSelecionado = 45.0,
            corTecido = "Azul",
        )

        val itens = useCase(
            tipoServico = TipoServico.SOFA_NOVO,
            sofa = sofa,
            checklistReforma = ChecklistReforma(),
            catalogo = catalogoPadrao,
        ).associateBy { it.chaveMaterial }

        // Largura total = 3.50m -> tecido ceil(3.5 * 3.2) = 12 m
        assertEquals(12.0, itens.getValue(RegrasCalculoConfig.CHAVE_TECIDO).quantidade, 0.001)
        // Pés: 6 + 4 = 10
        assertEquals(10.0, itens.getValue(RegrasCalculoConfig.CHAVE_PES).quantidade, 0.001)
    }
}

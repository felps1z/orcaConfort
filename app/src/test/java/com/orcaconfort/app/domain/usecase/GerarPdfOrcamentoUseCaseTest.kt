package com.orcaconfort.app.domain.usecase

import com.orcaconfort.app.domain.model.ChecklistReforma
import com.orcaconfort.app.domain.model.DadosCliente
import com.orcaconfort.app.domain.model.EspecificacoesSofa
import com.orcaconfort.app.domain.model.ModeloSofa
import com.orcaconfort.app.domain.model.Orcamento
import com.orcaconfort.app.domain.model.PerfilTapeceiro
import com.orcaconfort.app.domain.model.TipoServico
import com.orcaconfort.app.domain.repository.GeradorPdfOrcamento
import com.orcaconfort.app.domain.repository.PerfilTapeceiroRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GerarPdfOrcamentoUseCaseTest {

    private class FakePerfilRepository(
        private val perfil: PerfilTapeceiro,
    ) : PerfilTapeceiroRepository {
        override fun observarPerfil(): Flow<PerfilTapeceiro> = flowOf(perfil)
        override suspend fun obterPerfil(): PerfilTapeceiro = perfil
        override suspend fun salvarPerfil(perfil: PerfilTapeceiro) = Unit
    }

    private class FakeGeradorPdf(
        private val resultado: Result<String>,
    ) : GeradorPdfOrcamento {
        var ultimoOrcamento: Orcamento? = null
        var ultimoPerfil: PerfilTapeceiro? = null

        override suspend fun gerarPdf(orcamento: Orcamento, perfil: PerfilTapeceiro): Result<String> {
            ultimoOrcamento = orcamento
            ultimoPerfil = perfil
            return resultado
        }
    }

    private fun criarOrcamentoTeste(id: Long = 24L): Orcamento = Orcamento(
        id = id,
        tipoServico = TipoServico.REFORMA,
        cliente = DadosCliente(
            nome = "Ana Souza",
            telefone = "(11) 98765-4321",
        ),
        sofa = EspecificacoesSofa(modelo = ModeloSofa.RETRATIL),
        checklistReforma = ChecklistReforma(),
    )

    @Test
    fun `combina orcamento com perfil da tapecaria e retorna metadados do pdf gerado`() = runBlocking {
        val perfil = PerfilTapeceiro(
            nomeNegocio = "Estofados São José",
            telefone = "(11) 99999-0000",
            endereco = "Rua das Flores, 123",
            instagram = "@estofadossaojose",
        )
        val orcamento = criarOrcamentoTeste(id = 24L)
        val fakeGerador = FakeGeradorPdf(Result.success("/cache/orcamentos/orcamento_024.pdf"))
        val useCase = GerarPdfOrcamentoUseCase(
            perfilTapeceiroRepository = FakePerfilRepository(perfil),
            geradorPdfOrcamento = fakeGerador,
        )

        val resultado = useCase(orcamento)

        assertTrue(resultado.isSuccess)
        val gerado = resultado.getOrThrow()
        assertEquals("/cache/orcamentos/orcamento_024.pdf", gerado.caminhoArquivo)
        assertEquals("Ana Souza", gerado.nomeCliente)
        assertEquals("(11) 98765-4321", gerado.telefoneCliente)
        assertEquals("Estofados São José", gerado.nomeNegocio)
        assertEquals(24L, gerado.idOrcamento)
        assertEquals(perfil, fakeGerador.ultimoPerfil)
        assertEquals(orcamento, fakeGerador.ultimoOrcamento)
    }

    @Test
    fun `propaga falha quando gerador de pdf retorna erro`() = runBlocking {
        val erroEsperado = IllegalStateException("Falha ao gravar arquivo PDF")
        val useCase = GerarPdfOrcamentoUseCase(
            perfilTapeceiroRepository = FakePerfilRepository(PerfilTapeceiro()),
            geradorPdfOrcamento = FakeGeradorPdf(Result.failure(erroEsperado)),
        )

        val resultado = useCase(criarOrcamentoTeste(id = 1L))

        assertTrue(resultado.isFailure)
        assertEquals("Falha ao gravar arquivo PDF", resultado.exceptionOrNull()?.message)
    }
}

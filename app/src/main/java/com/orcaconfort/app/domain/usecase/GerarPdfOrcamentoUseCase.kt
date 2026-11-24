package com.orcaconfort.app.domain.usecase

import com.orcaconfort.app.domain.model.Orcamento
import com.orcaconfort.app.domain.repository.GeradorPdfOrcamento
import com.orcaconfort.app.domain.repository.PerfilTapeceiroRepository
import javax.inject.Inject

/** Resultado da geração do PDF com dados prontos para compartilhamento. */
data class PdfOrcamentoGerado(
    val caminhoArquivo: String,
    val nomeCliente: String,
    val telefoneCliente: String,
    val nomeNegocio: String,
    val idOrcamento: Long,
)

/**
 * Caso de uso que combina o orçamento e o perfil institucional da tapeçaria
 * para gerar o arquivo PDF pronto para envio ao cliente.
 */
class GerarPdfOrcamentoUseCase @Inject constructor(
    private val perfilTapeceiroRepository: PerfilTapeceiroRepository,
    private val geradorPdfOrcamento: GeradorPdfOrcamento,
) {

    suspend operator fun invoke(orcamento: Orcamento): Result<PdfOrcamentoGerado> {
        val perfil = perfilTapeceiroRepository.obterPerfil()
        return geradorPdfOrcamento.gerarPdf(orcamento, perfil).map { caminho ->
            PdfOrcamentoGerado(
                caminhoArquivo = caminho,
                nomeCliente = orcamento.cliente.nome,
                telefoneCliente = orcamento.cliente.telefone,
                nomeNegocio = perfil.nomeNegocio,
                idOrcamento = orcamento.id,
            )
        }
    }
}

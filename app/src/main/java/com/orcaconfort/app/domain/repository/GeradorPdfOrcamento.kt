package com.orcaconfort.app.domain.repository

import com.orcaconfort.app.domain.model.Orcamento
import com.orcaconfort.app.domain.model.PerfilTapeceiro

/**
 * Contrato de domínio para geração do PDF de orçamento do cliente.
 *
 * Mantido sem dependências do framework Android para preservar o isolamento da camada `domain`.
 * Retorna o caminho absoluto do arquivo PDF gerado em disco.
 */
interface GeradorPdfOrcamento {
    suspend fun gerarPdf(
        orcamento: Orcamento,
        perfil: PerfilTapeceiro,
    ): Result<String>
}

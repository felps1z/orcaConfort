package com.orcaconfort.app.domain.usecase

import com.orcaconfort.app.domain.model.Orcamento
import com.orcaconfort.app.domain.repository.OrcamentoRepository
import javax.inject.Inject

/**
 * Valida regras mínimas de integridade e persiste o orçamento completo no repositório.
 */
class SalvarOrcamentoUseCase @Inject constructor(
    private val orcamentoRepository: OrcamentoRepository,
) {

    /**
     * Salva um orçamento novo ou atualiza um existente (mantendo o snapshot dos materiais).
     * Retorna [Result.success] com o ID salvo ou [Result.failure] com mensagem amigável de validação.
     */
    suspend operator fun invoke(orcamento: Orcamento): Result<Long> {
        if (orcamento.cliente.nome.isBlank()) {
            return Result.failure(IllegalArgumentException("Informe o nome do cliente antes de salvar."))
        }
        val possuiPecaValida = orcamento.sofa.pecas.any {
            it.larguraCm > 0.0 && it.profundidadeCm > 0.0 && it.alturaCm > 0.0
        }
        if (!possuiPecaValida) {
            return Result.failure(
                IllegalArgumentException("Informe medidas válidas (maiores que zero) na etapa de Dimensões."),
            )
        }
        return runCatching {
            orcamentoRepository.salvarOrcamentoCompleto(orcamento)
        }
    }
}

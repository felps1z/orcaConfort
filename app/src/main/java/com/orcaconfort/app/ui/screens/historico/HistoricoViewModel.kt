package com.orcaconfort.app.ui.screens.historico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orcaconfort.app.core.util.Formatadores
import com.orcaconfort.app.domain.model.Orcamento
import com.orcaconfort.app.domain.repository.OrcamentoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Resumo leve exibido em cada card do Histórico (Home). */
data class OrcamentoResumoUi(
    val id: Long,
    val clienteNome: String,
    val status: String,
    val dataEntrega: String,
    val subtituloModeloServico: String,
    val valorFormatado: String,
)

/**
 * ViewModel da tela inicial (Histórico de Orçamentos).
 *
 * Observa reativamente os orçamentos salvos no banco local Room, já ordenados por
 * proximidade da data de entrega (conforme especificação da Tela 1).
 */
@HiltViewModel
class HistoricoViewModel @Inject constructor(
    orcamentoRepository: OrcamentoRepository,
) : ViewModel() {

    val orcamentos: StateFlow<List<OrcamentoResumoUi>> =
        orcamentoRepository.observarTodosOrcamentos()
            .map { lista -> lista.map(::mapearResumo) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    private fun mapearResumo(orcamento: Orcamento): OrcamentoResumoUi = OrcamentoResumoUi(
        id = orcamento.id,
        clienteNome = orcamento.cliente.nome,
        status = orcamento.status.tituloExibicao,
        dataEntrega = Formatadores.formatarData(orcamento.cliente.dataEntregaTimestamp),
        subtituloModeloServico = "${orcamento.sofa.modelo.tituloCurto} • ${orcamento.tipoServico.tituloExibicao}",
        valorFormatado = Formatadores.formatarMoeda(orcamento.financeiro.valorFinalSalvo),
    )
}

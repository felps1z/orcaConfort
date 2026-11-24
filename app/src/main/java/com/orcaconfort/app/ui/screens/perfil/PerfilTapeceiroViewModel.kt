package com.orcaconfort.app.ui.screens.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orcaconfort.app.core.util.Formatadores
import com.orcaconfort.app.domain.model.PerfilTapeceiro
import com.orcaconfort.app.domain.repository.PerfilTapeceiroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PerfilTapeceiroUiState(
    val nomeNegocio: String = "",
    val telefone: String = "",
    val endereco: String = "",
    val instagram: String = "",
    val valorDiariaPadrao: String = "200,00",
    val margemExtrasPadrao: String = "10",
    val salvando: Boolean = false,
    val mensagemFeedback: String? = null,
)

/**
 * ViewModel da tela de Perfil da Tapeçaria.
 *
 * Gerencia o registro único da tabela `workshop_profile`, cujos dados abastecem:
 * 1. O cabeçalho institucional do PDF enviado ao cliente.
 * 2. Os valores padrão de diária e margem de materiais extras ao iniciar novos orçamentos.
 */
@HiltViewModel
class PerfilTapeceiroViewModel @Inject constructor(
    private val perfilTapeceiroRepository: PerfilTapeceiroRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilTapeceiroUiState())
    val uiState: StateFlow<PerfilTapeceiroUiState> = _uiState.asStateFlow()

    init {
        carregarPerfil()
    }

    private fun carregarPerfil() {
        viewModelScope.launch {
            val perfil = perfilTapeceiroRepository.obterPerfil()
            _uiState.update {
                it.copy(
                    nomeNegocio = perfil.nomeNegocio,
                    telefone = perfil.telefone,
                    endereco = perfil.endereco,
                    instagram = perfil.instagram,
                    valorDiariaPadrao = Formatadores.formatarValorSemSimbolo(perfil.valorDiariaPadrao),
                    margemExtrasPadrao = Formatadores.formatarQuantidade(perfil.margemExtrasPadrao),
                )
            }
        }
    }

    fun atualizarNomeNegocio(valor: String) {
        _uiState.update { it.copy(nomeNegocio = valor) }
    }

    fun atualizarTelefone(valor: String) {
        _uiState.update { it.copy(telefone = valor) }
    }

    fun atualizarEndereco(valor: String) {
        _uiState.update { it.copy(endereco = valor) }
    }

    fun atualizarInstagram(valor: String) {
        _uiState.update { it.copy(instagram = valor) }
    }

    fun atualizarValorDiariaPadrao(valor: String) {
        val filtrado = valor.filter { it.isDigit() || it == ',' || it == '.' }
        _uiState.update { it.copy(valorDiariaPadrao = filtrado) }
    }

    fun atualizarMargemExtrasPadrao(valor: String) {
        val filtrado = valor.filter { it.isDigit() || it == ',' || it == '.' }
        _uiState.update { it.copy(margemExtrasPadrao = filtrado) }
    }

    fun limparMensagemFeedback() {
        _uiState.update { it.copy(mensagemFeedback = null) }
    }

    fun salvarPerfil() {
        val estado = _uiState.value
        if (estado.salvando) return

        val nomeLimpo = estado.nomeNegocio.trim()
        if (nomeLimpo.isEmpty()) {
            _uiState.update {
                it.copy(mensagemFeedback = "Informe o nome da tapeçaria ou do profissional.")
            }
            return
        }

        val diaria = Formatadores.arredondar2Casas(
            Formatadores.parseNumeroPtBr(estado.valorDiariaPadrao).coerceAtLeast(0.0),
        )
        val margem = Formatadores.arredondar2Casas(
            Formatadores.parseNumeroPtBr(estado.margemExtrasPadrao).coerceAtLeast(0.0),
        )

        viewModelScope.launch {
            _uiState.update { it.copy(salvando = true) }
            perfilTapeceiroRepository.salvarPerfil(
                PerfilTapeceiro(
                    id = 1,
                    nomeNegocio = nomeLimpo,
                    telefone = estado.telefone.trim(),
                    endereco = estado.endereco.trim(),
                    instagram = estado.instagram.trim(),
                    valorDiariaPadrao = diaria,
                    margemExtrasPadrao = margem,
                ),
            )
            _uiState.update {
                it.copy(
                    salvando = false,
                    valorDiariaPadrao = Formatadores.formatarValorSemSimbolo(diaria),
                    margemExtrasPadrao = Formatadores.formatarQuantidade(margem),
                    mensagemFeedback = "Perfil da tapeçaria salvo com sucesso.",
                )
            }
        }
    }
}

package com.orcaconfort.app.ui.screens.orcamento

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class OrcamentoUiState(
    val passoAtual: Int = 1,
    val numeroOrcamento: String = "#024",

    // Passo 1 - Dados do cliente
    val nomeCliente: String = "Ana Souza",
    val telefone: String = "(11) 98765-4321",
    val dataEntrega: String = "20/11/2026",
    val observacoes: String = "Reformar assentos e trocar o tecido.\nManter a estrutura original.",
    val tipoServico: String = "Reforma", // "Reforma" ou "Sofá Novo"

    // Passo 2 - Modelo e acabamento
    val modeloSofa: String = "Retrátil", // "Fixo", "Retrátil", "Modular"
    val possuiAlmofadas: Boolean = true,
    val qtdAlmofadas: String = "4",
    val tecidoSelecionado: String = "Linho — R$ 45,00/m",
    val corSelecionada: String = "Cinza",

    // Passo 3 - Medidas e reforma
    val larguraCm: String = "220",
    val profundidadeCm: String = "95",
    val alturaCm: String = "90",
    val substituirMadeira: Boolean = true,
    val substituirEspuma: Boolean = true,
    val substituirMolas: Boolean = false,
    val substituirPercinta: Boolean = true,
    val substituirChapas: Boolean = false,

    // Passo 4 - Materiais e mão de obra
    val qtdTecido: String = "12 m",
    val qtdFibra: String = "4 kg",
    val qtdEspuma1cm: String = "1,5 m",
    val qtdEspuma2cm: String = "2 m",
    val qtdEspuma3cm: String = "3 m",
    val qtdPercinta: String = "16 m",
    val qtdTabuas: String = "6 m",
    val margemMateriaisExtras: String = "10",
    val diasTrabalhados: String = "3",
    val valorDiaria: String = "180,00",
    val valorFrete: String = "120,00",

    // Passo 5 - Resumo
    val valorFinalSobrescrito: String = "1.850,00"
)

@HiltViewModel
class OrcamentoViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(OrcamentoUiState())
    val uiState: StateFlow<OrcamentoUiState> = _uiState.asStateFlow()

    fun irParaPasso(passo: Int) {
        _uiState.update { it.copy(passoAtual = passo.coerceIn(1, 5)) }
    }

    fun proximoPasso() {
        _uiState.update { it.copy(passoAtual = (it.passoAtual + 1).coerceAtMost(5)) }
    }

    fun passoAnterior(): Boolean {
        val atual = _uiState.value.passoAtual
        return if (atual > 1) {
            _uiState.update { it.copy(passoAtual = atual - 1) }
            true
        } else {
            false
        }
    }

    fun atualizarDadosCliente(
        nome: String = _uiState.value.nomeCliente,
        telefone: String = _uiState.value.telefone,
        dataEntrega: String = _uiState.value.dataEntrega,
        observacoes: String = _uiState.value.observacoes,
        tipoServico: String = _uiState.value.tipoServico
    ) {
        _uiState.update {
            it.copy(
                nomeCliente = nome,
                telefone = telefone,
                dataEntrega = dataEntrega,
                observacoes = observacoes,
                tipoServico = tipoServico
            )
        }
    }

    fun atualizarModeloAcabamento(
        modeloSofa: String = _uiState.value.modeloSofa,
        possuiAlmofadas: Boolean = _uiState.value.possuiAlmofadas,
        qtdAlmofadas: String = _uiState.value.qtdAlmofadas,
        tecido: String = _uiState.value.tecidoSelecionado,
        cor: String = _uiState.value.corSelecionada
    ) {
        _uiState.update {
            it.copy(
                modeloSofa = modeloSofa,
                possuiAlmofadas = possuiAlmofadas,
                qtdAlmofadas = qtdAlmofadas,
                tecidoSelecionado = tecido,
                corSelecionada = cor
            )
        }
    }

    fun atualizarMedidasReforma(
        largura: String = _uiState.value.larguraCm,
        profundidade: String = _uiState.value.profundidadeCm,
        altura: String = _uiState.value.alturaCm,
        madeira: Boolean = _uiState.value.substituirMadeira,
        espuma: Boolean = _uiState.value.substituirEspuma,
        molas: Boolean = _uiState.value.substituirMolas,
        percinta: Boolean = _uiState.value.substituirPercinta,
        chapas: Boolean = _uiState.value.substituirChapas
    ) {
        _uiState.update {
            it.copy(
                larguraCm = largura,
                profundidadeCm = profundidade,
                alturaCm = altura,
                substituirMadeira = madeira,
                substituirEspuma = espuma,
                substituirMolas = molas,
                substituirPercinta = percinta,
                substituirChapas = chapas
            )
        }
    }

    fun atualizarMateriais(
        qtdTecido: String = _uiState.value.qtdTecido,
        qtdFibra: String = _uiState.value.qtdFibra,
        qtdEspuma1cm: String = _uiState.value.qtdEspuma1cm,
        qtdEspuma2cm: String = _uiState.value.qtdEspuma2cm,
        qtdEspuma3cm: String = _uiState.value.qtdEspuma3cm,
        qtdPercinta: String = _uiState.value.qtdPercinta,
        qtdTabuas: String = _uiState.value.qtdTabuas,
        margemExtras: String = _uiState.value.margemMateriaisExtras,
        diasTrab: String = _uiState.value.diasTrabalhados,
        valorDiaria: String = _uiState.value.valorDiaria,
        frete: String = _uiState.value.valorFrete
    ) {
        _uiState.update {
            it.copy(
                qtdTecido = qtdTecido,
                qtdFibra = qtdFibra,
                qtdEspuma1cm = qtdEspuma1cm,
                qtdEspuma2cm = qtdEspuma2cm,
                qtdEspuma3cm = qtdEspuma3cm,
                qtdPercinta = qtdPercinta,
                qtdTabuas = qtdTabuas,
                margemMateriaisExtras = margemExtras,
                diasTrabalhados = diasTrab,
                valorDiaria = valorDiaria,
                valorFrete = frete
            )
        }
    }

    fun atualizarValorFinal(valor: String) {
        _uiState.update { it.copy(valorFinalSobrescrito = valor) }
    }
}

package com.orcaconfort.app.ui.screens.orcamento

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orcaconfort.app.core.util.Formatadores
import com.orcaconfort.app.domain.model.CategoriaMaterial
import com.orcaconfort.app.domain.model.ChecklistReforma
import com.orcaconfort.app.domain.model.DadosCliente
import com.orcaconfort.app.domain.model.DimensaoPeca
import com.orcaconfort.app.domain.model.EspecificacoesSofa
import com.orcaconfort.app.domain.model.ItemMaterialOrcamento
import com.orcaconfort.app.domain.model.MaterialCatalogo
import com.orcaconfort.app.domain.model.ModeloSofa
import com.orcaconfort.app.domain.model.Orcamento
import com.orcaconfort.app.domain.model.ResumoFinanceiro
import com.orcaconfort.app.domain.model.StatusOrcamento
import com.orcaconfort.app.domain.model.TipoServico
import com.orcaconfort.app.domain.repository.CatalogoRepository
import com.orcaconfort.app.domain.repository.OrcamentoRepository
import com.orcaconfort.app.domain.repository.PerfilTapeceiroRepository
import com.orcaconfort.app.domain.usecase.CalcularMateriaisOrcamentoUseCase
import com.orcaconfort.app.domain.usecase.CalcularResumoFinanceiroUseCase
import com.orcaconfort.app.domain.usecase.GerarPdfOrcamentoUseCase
import com.orcaconfort.app.domain.usecase.PdfOrcamentoGerado
import com.orcaconfort.app.domain.usecase.SalvarOrcamentoUseCase
import com.orcaconfort.app.ui.navigation.Rotas
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Campos de entrada de uma peça individual na Etapa 3 (em cm). */
data class DimensaoPecaUi(
    val larguraCm: String = "",
    val profundidadeCm: String = "",
    val alturaCm: String = "",
)

/** Opção de tecido carregada dinamicamente da Tabela de Preços para o dropdown da Etapa 2. */
data class TecidoOpcaoUi(
    val id: Long,
    val nome: String,
    val precoUnitario: Double,
    val unidade: String = "m",
) {
    val rotuloDropdown: String
        get() = "$nome — ${Formatadores.formatarMoeda(precoUnitario)}/$unidade"
}

/**
 * Representação de um material editável na Etapa 4.
 *
 * Mantém `quantidadeTexto` separado do valor numérico em [item] para não atrapalhar
 * a digitação de decimais com vírgula (ex.: `"1,"` antes de digitar `"5"`).
 */
data class ItemMaterialUi(
    val item: ItemMaterialOrcamento,
    val quantidadeTexto: String,
)

data class OrcamentoUiState(
    val orcamentoId: Long = 0L,
    val criadoEm: Long = System.currentTimeMillis(),
    val status: StatusOrcamento = StatusOrcamento.PENDENTE,
    val passoAtual: Int = 1,
    val numeroOrcamento: String = "#NOVO",

    // Passo 1 - Dados do cliente
    val nomeCliente: String = "",
    val telefone: String = "",
    val dataEntrega: String = "",
    val observacoes: String = "",
    val tipoServico: TipoServico = TipoServico.REFORMA,

    // Passo 2 - Modelo e acabamento
    val modeloSofa: ModeloSofa = ModeloSofa.RETRATIL,
    val numeroPecas: String = "1",
    val possuiAlmofadas: Boolean = false,
    val qtdAlmofadas: String = "",
    val tecidosDisponiveis: List<TecidoOpcaoUi> = emptyList(),
    val tecidoSelecionado: TecidoOpcaoUi? = null,
    val corSelecionada: String = "Cinza",

    // Passo 3 - Medidas e reforma
    val dimensoesPecas: List<DimensaoPecaUi> = listOf(DimensaoPecaUi()),
    val substituirMadeira: Boolean = false,
    val substituirEspuma: Boolean = false,
    val substituirMolas: Boolean = false,
    val substituirPercinta: Boolean = false,
    val substituirChapas: Boolean = false,

    // Passo 4 - Materiais e mão de obra (calculados dinamicamente + edição aberta RN04)
    val itensMateriais: List<ItemMaterialUi> = emptyList(),
    val margemMateriaisExtras: String = "10",
    val diasTrabalhados: String = "1",
    val valorDiaria: String = "200,00",
    val valorFrete: String = "0,00",

    // Passo 5 - Resumo financeiro e sobrescrita manual
    val valorFinalSobrescrito: String = "",
    val resumoFinanceiro: ResumoFinanceiro = ResumoFinanceiro(),

    // Estado de persistência, geração de PDF e mensagens de validação
    val salvando: Boolean = false,
    val gerandoPdf: Boolean = false,
    val mensagemErro: String? = null,
) {
    val modoEdicao: Boolean
        get() = orcamentoId > 0L
}

/**
 * Assinatura imutável das entradas das Etapas 1 a 3.
 *
 * Evita recalcular (e perder edições manuais da Etapa 4) quando o usuário apenas navega
 * entre as etapas 4 e 5 ou abre um orçamento salvo em modo edição sem alterar especificações.
 */
private data class AssinaturaEspecificacoes(
    val tipoServico: TipoServico,
    val modeloSofa: ModeloSofa,
    val possuiAlmofadas: Boolean,
    val qtdAlmofadas: String,
    val nomeTecido: String,
    val precoTecido: Double,
    val corSelecionada: String,
    val dimensoesPecas: List<DimensaoPecaUi>,
    val checklistReforma: ChecklistReforma,
)

/**
 * ViewModel do fluxo de 5 etapas (Novo Orçamento e Edição de Orçamento).
 *
 * Orquestra os casos de uso de cálculo ([CalcularMateriaisOrcamentoUseCase],
 * [CalcularResumoFinanceiroUseCase]) e persistência ([SalvarOrcamentoUseCase]) mantendo
 * a UI puramente declarativa.
 */
@HiltViewModel
class OrcamentoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val catalogoRepository: CatalogoRepository,
    private val orcamentoRepository: OrcamentoRepository,
    private val perfilTapeceiroRepository: PerfilTapeceiroRepository,
    private val calcularMateriaisUseCase: CalcularMateriaisOrcamentoUseCase,
    private val calcularResumoFinanceiroUseCase: CalcularResumoFinanceiroUseCase,
    private val salvarOrcamentoUseCase: SalvarOrcamentoUseCase,
    private val gerarPdfOrcamentoUseCase: GerarPdfOrcamentoUseCase,
) : ViewModel() {

    private val orcamentoIdArg: Long =
        savedStateHandle[Rotas.ARG_ORCAMENTO_ID] ?: Rotas.ID_NOVO_ORCAMENTO

    private val _uiState = MutableStateFlow(OrcamentoUiState())
    val uiState: StateFlow<OrcamentoUiState> = _uiState.asStateFlow()

    /** Cache em memória do catálogo ativo para alimentar o motor de cálculo sem consultas redundantes. */
    private var catalogoAtual: List<MaterialCatalogo> = emptyList()

    /** Última assinatura de especificações usada para gerar a lista da Etapa 4. */
    private var ultimaAssinaturaCalculo: AssinaturaEspecificacoes? = null

    init {
        observarCatalogo()
        if (orcamentoIdArg > 0L) {
            carregarOrcamentoExistente(orcamentoIdArg)
        } else {
            carregarPadroesDoPerfil()
        }
    }

    private fun observarCatalogo() {
        viewModelScope.launch {
            catalogoRepository.observarMateriaisAtivos().collect { materiais ->
                catalogoAtual = materiais
                val tecidos = materiais
                    .filter { it.categoria == CategoriaMaterial.TECIDO }
                    .map {
                        TecidoOpcaoUi(
                            id = it.id,
                            nome = it.nome,
                            precoUnitario = it.precoUnitario,
                            unidade = it.unidade,
                        )
                    }

                _uiState.update { estado ->
                    val selecionadoAtual = estado.tecidoSelecionado
                    val tecidoAtualizado = when {
                        selecionadoAtual == null -> tecidos.firstOrNull()
                        // Em edição, preserva o preço snapshot do orçamento se o nome existir
                        estado.modoEdicao -> selecionadoAtual
                        else -> tecidos.firstOrNull { it.id == selecionadoAtual.id }
                            ?: tecidos.firstOrNull { it.nome.equals(selecionadoAtual.nome, ignoreCase = true) }
                            ?: tecidos.firstOrNull()
                    }
                    estado.copy(
                        tecidosDisponiveis = tecidos,
                        tecidoSelecionado = tecidoAtualizado,
                    )
                }
            }
        }
    }

    private fun carregarPadroesDoPerfil() {
        viewModelScope.launch {
            val perfil = perfilTapeceiroRepository.obterPerfil()
            _uiState.update { estado ->
                estado.copy(
                    margemMateriaisExtras = Formatadores.formatarQuantidade(perfil.margemExtrasPadrao),
                    valorDiaria = Formatadores.formatarValorSemSimbolo(perfil.valorDiariaPadrao),
                )
            }
        }
    }

    private fun carregarOrcamentoExistente(id: Long) {
        viewModelScope.launch {
            val orcamento = orcamentoRepository.buscarOrcamentoPorId(id) ?: return@launch
            val pecasUi = orcamento.sofa.pecas
                .ifEmpty { listOf(DimensaoPeca(1, 0.0, 0.0, 0.0)) }
                .map { peca ->
                    DimensaoPecaUi(
                        larguraCm = Formatadores.formatarQuantidade(peca.larguraCm),
                        profundidadeCm = Formatadores.formatarQuantidade(peca.profundidadeCm),
                        alturaCm = Formatadores.formatarQuantidade(peca.alturaCm),
                    )
                }

            val tecidoSalvo = if (orcamento.sofa.nomeTecidoSelecionado.isNotBlank()) {
                TecidoOpcaoUi(
                    id = 0L,
                    nome = orcamento.sofa.nomeTecidoSelecionado,
                    precoUnitario = orcamento.sofa.precoTecidoSelecionado,
                    unidade = "m",
                )
            } else {
                null
            }

            val itensUi = orcamento.itens.map { item ->
                ItemMaterialUi(
                    item = item,
                    quantidadeTexto = Formatadores.formatarQuantidade(item.quantidade),
                )
            }

            val novoEstado = _uiState.value.copy(
                orcamentoId = orcamento.id,
                criadoEm = orcamento.criadoEm,
                status = orcamento.status,
                numeroOrcamento = Formatadores.formatarCodigoOrcamento(orcamento.id),
                nomeCliente = orcamento.cliente.nome,
                telefone = orcamento.cliente.telefone,
                dataEntrega = if (orcamento.cliente.dataEntregaTimestamp != null) {
                    Formatadores.formatarData(orcamento.cliente.dataEntregaTimestamp)
                } else {
                    ""
                },
                observacoes = orcamento.cliente.observacoes,
                tipoServico = orcamento.tipoServico,
                modeloSofa = orcamento.sofa.modelo,
                numeroPecas = orcamento.sofa.numeroPecas.coerceAtLeast(1).toString(),
                possuiAlmofadas = orcamento.sofa.possuiAlmofadas,
                qtdAlmofadas = if (orcamento.sofa.qtdAlmofadas > 0) {
                    orcamento.sofa.qtdAlmofadas.toString()
                } else {
                    ""
                },
                tecidoSelecionado = tecidoSalvo ?: _uiState.value.tecidosDisponiveis.firstOrNull(),
                corSelecionada = orcamento.sofa.corTecido.ifBlank { "Cinza" },
                dimensoesPecas = pecasUi,
                substituirMadeira = orcamento.checklistReforma.substituirMadeira,
                substituirEspuma = orcamento.checklistReforma.substituirEspuma,
                substituirMolas = orcamento.checklistReforma.substituirMolas,
                substituirPercinta = orcamento.checklistReforma.substituirPercinta,
                substituirChapas = orcamento.checklistReforma.substituirChapas,
                itensMateriais = itensUi,
                margemMateriaisExtras = Formatadores.formatarQuantidade(orcamento.financeiro.margemExtrasPercentual),
                diasTrabalhados = Formatadores.formatarQuantidade(orcamento.financeiro.diasTrabalhados),
                valorDiaria = Formatadores.formatarValorSemSimbolo(orcamento.financeiro.valorDiaria),
                valorFrete = Formatadores.formatarValorSemSimbolo(orcamento.financeiro.custoFrete),
                valorFinalSobrescrito = orcamento.financeiro.valorSobrescritoManual
                    ?.let { Formatadores.formatarValorSemSimbolo(it) }
                    .orEmpty(),
                resumoFinanceiro = orcamento.financeiro,
            )

            // Congela a assinatura atual para preservar o snapshot salvo até que o usuário mude as Etapas 1-3.
            ultimaAssinaturaCalculo = extrairAssinatura(novoEstado)
            _uiState.value = novoEstado
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Navegação entre passos (1..5)
    // ---------------------------------------------------------------------------------------------

    fun irParaPasso(passo: Int) {
        val destino = passo.coerceIn(1, 5)
        if (destino >= 4) {
            recalcularMateriaisSeNecessario()
        }
        _uiState.update { it.copy(passoAtual = destino) }
    }

    fun proximoPasso() {
        val proximo = (_uiState.value.passoAtual + 1).coerceAtMost(5)
        if (proximo >= 4) {
            recalcularMateriaisSeNecessario()
        }
        _uiState.update { it.copy(passoAtual = proximo) }
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

    // ---------------------------------------------------------------------------------------------
    // Etapa 1 — Dados do cliente e tipo de serviço
    // ---------------------------------------------------------------------------------------------

    fun atualizarDadosCliente(
        nome: String = _uiState.value.nomeCliente,
        telefone: String = _uiState.value.telefone,
        dataEntrega: String = _uiState.value.dataEntrega,
        observacoes: String = _uiState.value.observacoes,
        tipoServico: TipoServico = _uiState.value.tipoServico,
    ) {
        _uiState.update {
            it.copy(
                nomeCliente = nome,
                telefone = telefone,
                dataEntrega = dataEntrega,
                observacoes = observacoes,
                tipoServico = tipoServico,
            )
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Etapa 2 — Modelo, número de peças e acabamento
    // ---------------------------------------------------------------------------------------------

    fun atualizarModeloAcabamento(
        modeloSofa: ModeloSofa = _uiState.value.modeloSofa,
        possuiAlmofadas: Boolean = _uiState.value.possuiAlmofadas,
        qtdAlmofadas: String = _uiState.value.qtdAlmofadas,
        tecido: TecidoOpcaoUi? = _uiState.value.tecidoSelecionado,
        cor: String = _uiState.value.corSelecionada,
    ) {
        val qtdLimpa = qtdAlmofadas.filter { it.isDigit() }
        _uiState.update {
            it.copy(
                modeloSofa = modeloSofa,
                possuiAlmofadas = possuiAlmofadas,
                qtdAlmofadas = qtdLimpa,
                tecidoSelecionado = tecido,
                corSelecionada = cor,
            )
        }
    }

    fun atualizarNumeroPecas(valor: String) {
        val apenasDigitos = valor.filter { it.isDigit() }
        val quantidade = (apenasDigitos.toIntOrNull() ?: 1).coerceIn(1, 20)
        _uiState.update { estadoAtual ->
            val listaAtual = estadoAtual.dimensoesPecas
            val novaLista = List(quantidade) { index ->
                listaAtual.getOrElse(index) { DimensaoPecaUi() }
            }
            estadoAtual.copy(
                numeroPecas = apenasDigitos,
                dimensoesPecas = novaLista,
            )
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Etapa 3 — Dimensões por peça e checklist de reforma
    // ---------------------------------------------------------------------------------------------

    fun atualizarDimensaoPeca(
        index: Int,
        largura: String,
        profundidade: String,
        altura: String,
    ) {
        _uiState.update { estadoAtual ->
            if (index !in estadoAtual.dimensoesPecas.indices) return@update estadoAtual
            val novaLista = estadoAtual.dimensoesPecas.toMutableList()
            novaLista[index] = DimensaoPecaUi(
                larguraCm = filtrarNumeroDecimal(largura),
                profundidadeCm = filtrarNumeroDecimal(profundidade),
                alturaCm = filtrarNumeroDecimal(altura),
            )
            estadoAtual.copy(dimensoesPecas = novaLista)
        }
    }

    fun atualizarItensSubstituidos(
        madeira: Boolean = _uiState.value.substituirMadeira,
        espuma: Boolean = _uiState.value.substituirEspuma,
        molas: Boolean = _uiState.value.substituirMolas,
        percinta: Boolean = _uiState.value.substituirPercinta,
        chapas: Boolean = _uiState.value.substituirChapas,
    ) {
        _uiState.update {
            it.copy(
                substituirMadeira = madeira,
                substituirEspuma = espuma,
                substituirMolas = molas,
                substituirPercinta = percinta,
                substituirChapas = chapas,
            )
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Etapa 4 — Materiais (RN04: edição aberta) e parâmetros financeiros
    // ---------------------------------------------------------------------------------------------

    /** Força o recálculo automático dos materiais pelas fórmulas da Seção 2. */
    fun recalcularMateriaisAutomaticamente() {
        ultimaAssinaturaCalculo = null
        recalcularMateriaisSeNecessario()
    }

    fun atualizarQuantidadeItemMaterial(index: Int, novoTexto: String) {
        val textoFiltrado = filtrarNumeroDecimal(novoTexto)
        val novaQtd = Formatadores.arredondar2Casas(
            Formatadores.parseNumeroPtBr(textoFiltrado).coerceAtLeast(0.0),
        )

        _uiState.update { estado ->
            if (index !in estado.itensMateriais.indices) return@update estado
            val listaAtualizada = estado.itensMateriais.toMutableList()
            val itemUiAtual = listaAtualizada[index]
            val itemAtualizado = itemUiAtual.item.copy(
                quantidade = novaQtd,
                custoTotal = Formatadores.arredondar2Casas(novaQtd * itemUiAtual.item.precoUnitarioSnapshot),
                editadoManualmente = true,
            )
            listaAtualizada[index] = ItemMaterialUi(
                item = itemAtualizado,
                quantidadeTexto = textoFiltrado,
            )

            val estadoComItens = estado.copy(itensMateriais = listaAtualizada)
            estadoComItens.copy(resumoFinanceiro = calcularFinanceiroPara(estadoComItens))
        }
    }

    fun atualizarParametrosFinanceiros(
        margemExtras: String = _uiState.value.margemMateriaisExtras,
        diasTrab: String = _uiState.value.diasTrabalhados,
        valorDiaria: String = _uiState.value.valorDiaria,
        frete: String = _uiState.value.valorFrete,
    ) {
        _uiState.update { estado ->
            val atualizado = estado.copy(
                margemMateriaisExtras = filtrarNumeroDecimal(margemExtras),
                diasTrabalhados = filtrarNumeroDecimal(diasTrab),
                valorDiaria = filtrarNumeroDecimal(valorDiaria),
                valorFrete = filtrarNumeroDecimal(frete),
            )
            atualizado.copy(resumoFinanceiro = calcularFinanceiroPara(atualizado))
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Etapa 5 — Sobrescrita manual do valor final e salvamento no Room
    // ---------------------------------------------------------------------------------------------

    fun atualizarValorFinal(valor: String) {
        val filtrado = filtrarNumeroDecimal(valor)
        _uiState.update { estado ->
            val atualizado = estado.copy(valorFinalSobrescrito = filtrado)
            atualizado.copy(resumoFinanceiro = calcularFinanceiroPara(atualizado))
        }
    }

    fun limparMensagemErro() {
        _uiState.update { it.copy(mensagemErro = null) }
    }

    fun salvarOrcamento(onSucesso: (Long) -> Unit) {
        recalcularMateriaisSeNecessario()
        val estadoAtual = _uiState.value
        if (estadoAtual.salvando || estadoAtual.gerandoPdf) return

        val orcamento = montarOrcamentoDominio(estadoAtual)
        viewModelScope.launch {
            _uiState.update { it.copy(salvando = true, mensagemErro = null) }
            val resultado = salvarOrcamentoUseCase(orcamento)
            resultado.fold(
                onSuccess = { idSalvo ->
                    _uiState.update {
                        it.copy(
                            salvando = false,
                            orcamentoId = idSalvo,
                            numeroOrcamento = Formatadores.formatarCodigoOrcamento(idSalvo),
                        )
                    }
                    onSucesso(idSalvo)
                },
                onFailure = { erro ->
                    _uiState.update {
                        it.copy(
                            salvando = false,
                            mensagemErro = erro.message ?: "Não foi possível salvar o orçamento.",
                        )
                    }
                },
            )
        }
    }

    /**
     * Salva (ou atualiza) o orçamento no banco local para garantir numeração oficial
     * e, em seguida, gera o arquivo PDF pronto para envio via WhatsApp.
     */
    fun salvarEGerarPdf(onSucesso: (PdfOrcamentoGerado) -> Unit) {
        recalcularMateriaisSeNecessario()
        val estadoAtual = _uiState.value
        if (estadoAtual.salvando || estadoAtual.gerandoPdf) return

        val orcamento = montarOrcamentoDominio(estadoAtual)
        viewModelScope.launch {
            _uiState.update { it.copy(gerandoPdf = true, mensagemErro = null) }

            val resultadoSalvo = salvarOrcamentoUseCase(orcamento)
            val idSalvo = resultadoSalvo.getOrElse { erro ->
                _uiState.update {
                    it.copy(
                        gerandoPdf = false,
                        mensagemErro = erro.message ?: "Não foi possível salvar o orçamento antes de gerar o PDF.",
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    orcamentoId = idSalvo,
                    numeroOrcamento = Formatadores.formatarCodigoOrcamento(idSalvo),
                )
            }

            val orcamentoPersistido = orcamento.copy(id = idSalvo)
            val resultadoPdf = gerarPdfOrcamentoUseCase(orcamentoPersistido)
            resultadoPdf.fold(
                onSuccess = { pdf ->
                    _uiState.update { it.copy(gerandoPdf = false) }
                    onSucesso(pdf)
                },
                onFailure = { erro ->
                    _uiState.update {
                        it.copy(
                            gerandoPdf = false,
                            mensagemErro = erro.message ?: "Não foi possível gerar o PDF do orçamento.",
                        )
                    }
                },
            )
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Motor interno de sincronização entre Etapas 1-3 -> Etapa 4 -> Etapa 5
    // ---------------------------------------------------------------------------------------------

    private fun recalcularMateriaisSeNecessario() {
        val estado = _uiState.value
        val assinaturaAtual = extrairAssinatura(estado)
        if (estado.itensMateriais.isNotEmpty() && assinaturaAtual == ultimaAssinaturaCalculo) {
            return
        }

        val itensCalculados = calcularMateriaisUseCase(
            tipoServico = estado.tipoServico,
            sofa = montarEspecificacoesSofa(estado),
            checklistReforma = montarChecklistReforma(estado),
            catalogo = catalogoAtual,
        )

        val itensUi = itensCalculados.map { item ->
            ItemMaterialUi(
                item = item,
                quantidadeTexto = Formatadores.formatarQuantidade(item.quantidade),
            )
        }

        ultimaAssinaturaCalculo = assinaturaAtual
        _uiState.update { atual ->
            val comItens = atual.copy(itensMateriais = itensUi)
            comItens.copy(resumoFinanceiro = calcularFinanceiroPara(comItens))
        }
    }

    private fun calcularFinanceiroPara(estado: OrcamentoUiState): ResumoFinanceiro {
        val manual = estado.valorFinalSobrescrito
            .takeIf { it.isNotBlank() }
            ?.let { Formatadores.parseNumeroPtBr(it) }
            ?.takeIf { it > 0.0 }

        return calcularResumoFinanceiroUseCase(
            itens = estado.itensMateriais.map { it.item },
            margemExtrasPercentual = Formatadores.parseNumeroPtBr(estado.margemMateriaisExtras),
            diasTrabalhados = Formatadores.parseNumeroPtBr(estado.diasTrabalhados),
            valorDiaria = Formatadores.parseNumeroPtBr(estado.valorDiaria),
            custoFrete = Formatadores.parseNumeroPtBr(estado.valorFrete),
            valorSobrescritoManual = manual,
        )
    }

    private fun montarOrcamentoDominio(estado: OrcamentoUiState): Orcamento {
        val financeiro = calcularFinanceiroPara(estado)
        return Orcamento(
            id = estado.orcamentoId,
            criadoEm = estado.criadoEm,
            status = estado.status,
            tipoServico = estado.tipoServico,
            cliente = DadosCliente(
                nome = estado.nomeCliente.trim(),
                telefone = estado.telefone.trim(),
                dataEntregaTimestamp = Formatadores.parseDataParaTimestamp(estado.dataEntrega),
                observacoes = estado.observacoes.trim(),
            ),
            sofa = montarEspecificacoesSofa(estado),
            checklistReforma = montarChecklistReforma(estado),
            itens = estado.itensMateriais.map { it.item },
            financeiro = financeiro,
        )
    }

    private fun montarEspecificacoesSofa(estado: OrcamentoUiState): EspecificacoesSofa {
        val pecas = estado.dimensoesPecas.mapIndexed { index, pecaUi ->
            DimensaoPeca(
                ordem = index + 1,
                larguraCm = Formatadores.parseNumeroPtBr(pecaUi.larguraCm),
                profundidadeCm = Formatadores.parseNumeroPtBr(pecaUi.profundidadeCm),
                alturaCm = Formatadores.parseNumeroPtBr(pecaUi.alturaCm),
            )
        }
        val tecido = estado.tecidoSelecionado
        return EspecificacoesSofa(
            modelo = estado.modeloSofa,
            numeroPecas = pecas.size.coerceAtLeast(1),
            pecas = pecas,
            possuiAlmofadas = estado.possuiAlmofadas,
            qtdAlmofadas = if (estado.possuiAlmofadas) {
                estado.qtdAlmofadas.toIntOrNull()?.coerceAtLeast(0) ?: 0
            } else {
                0
            },
            nomeTecidoSelecionado = tecido?.nome.orEmpty(),
            precoTecidoSelecionado = tecido?.precoUnitario ?: 0.0,
            corTecido = estado.corSelecionada,
        )
    }

    private fun montarChecklistReforma(estado: OrcamentoUiState): ChecklistReforma =
        ChecklistReforma(
            substituirMadeira = estado.substituirMadeira,
            substituirEspuma = estado.substituirEspuma,
            substituirMolas = estado.substituirMolas,
            substituirPercinta = estado.substituirPercinta,
            substituirChapas = estado.substituirChapas,
        )

    private fun extrairAssinatura(estado: OrcamentoUiState): AssinaturaEspecificacoes =
        AssinaturaEspecificacoes(
            tipoServico = estado.tipoServico,
            modeloSofa = estado.modeloSofa,
            possuiAlmofadas = estado.possuiAlmofadas,
            qtdAlmofadas = estado.qtdAlmofadas,
            nomeTecido = estado.tecidoSelecionado?.nome.orEmpty(),
            precoTecido = estado.tecidoSelecionado?.precoUnitario ?: 0.0,
            corSelecionada = estado.corSelecionada,
            dimensoesPecas = estado.dimensoesPecas,
            checklistReforma = montarChecklistReforma(estado),
        )

    private fun filtrarNumeroDecimal(texto: String): String =
        texto.filter { it.isDigit() || it == ',' || it == '.' }
}

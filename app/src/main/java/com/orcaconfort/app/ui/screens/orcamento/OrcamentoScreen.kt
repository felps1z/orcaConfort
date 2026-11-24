package com.orcaconfort.app.ui.screens.orcamento

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.orcaconfort.app.core.util.Formatadores
import com.orcaconfort.app.domain.model.CategoriaMaterial
import com.orcaconfort.app.domain.model.ItemMaterialOrcamento
import com.orcaconfort.app.domain.model.ModeloSofa
import com.orcaconfort.app.domain.model.ResumoFinanceiro
import com.orcaconfort.app.domain.model.TipoServico
import com.orcaconfort.app.domain.usecase.PdfOrcamentoGerado
import com.orcaconfort.app.ui.components.AppIcons
import com.orcaconfort.app.ui.components.BottomActionButtons
import com.orcaconfort.app.ui.components.CompartilhamentoPdfHelper
import com.orcaconfort.app.ui.components.DialogoEscolhaWhatsApp
import com.orcaconfort.app.ui.components.OpcaoWhatsApp
import com.orcaconfort.app.ui.components.OrcaDropdownField
import com.orcaconfort.app.ui.components.OrcaTextField
import com.orcaconfort.app.ui.components.ScreenTopBar
import com.orcaconfort.app.ui.components.StatusBadge
import com.orcaconfort.app.ui.components.StepperHeader
import com.orcaconfort.app.ui.theme.BackgroundGray
import com.orcaconfort.app.ui.theme.BadgeBackground
import com.orcaconfort.app.ui.theme.BorderDark
import com.orcaconfort.app.ui.theme.BorderGray
import com.orcaconfort.app.ui.theme.CardSelectedGray
import com.orcaconfort.app.ui.theme.DividerLight
import com.orcaconfort.app.ui.theme.OrcaConfortTheme
import com.orcaconfort.app.ui.theme.PrimaryBlack
import com.orcaconfort.app.ui.theme.SurfaceWhite
import com.orcaconfort.app.ui.theme.TextMuted
import com.orcaconfort.app.ui.theme.TextPrimary
import com.orcaconfort.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun OrcamentoScreen(
    onBackToHome: () -> Unit,
    viewModel: OrcamentoViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var dialogoWhatsApp by remember {
        mutableStateOf<Pair<PdfOrcamentoGerado, List<OpcaoWhatsApp>>?>(null)
    }

    val mostrarErro: (String) -> Unit = { erro ->
        coroutineScope.launch {
            snackbarHostState.showSnackbar(erro)
        }
    }

    LaunchedEffect(state.mensagemErro) {
        state.mensagemErro?.let { erro ->
            snackbarHostState.showSnackbar(erro)
            viewModel.limparMensagemErro()
        }
    }

    val handleVoltar = {
        if (!viewModel.passoAnterior()) {
            onBackToHome()
        }
    }

    BackHandler(onBack = handleVoltar)

    Box(modifier = Modifier.fillMaxSize()) {
        OrcamentoScreenContent(
            state = state,
            onBackClick = handleVoltar,
            onStepClick = viewModel::irParaPasso,
            onProximoClick = viewModel::proximoPasso,
            onSalvarOrcamento = {
                viewModel.salvarOrcamento(onSucesso = { onBackToHome() })
            },
            onGerarPdf = {
                viewModel.salvarEGerarPdf { pdfGerado ->
                    CompartilhamentoPdfHelper.enviarOuSelecionarWhatsApp(
                        context = context,
                        pdf = pdfGerado,
                        onSolicitarEscolha = { opcoes ->
                            dialogoWhatsApp = pdfGerado to opcoes
                        },
                        onErro = mostrarErro,
                    )
                }
            },
            onUpdateCliente = viewModel::atualizarDadosCliente,
            onUpdateModelo = viewModel::atualizarModeloAcabamento,
            onUpdateNumeroPecas = viewModel::atualizarNumeroPecas,
            onUpdateDimensaoPeca = viewModel::atualizarDimensaoPeca,
            onUpdateItensSubstituidos = viewModel::atualizarItensSubstituidos,
            onUpdateQuantidadeMaterial = viewModel::atualizarQuantidadeItemMaterial,
            onRecalcularMateriais = viewModel::recalcularMateriaisAutomaticamente,
            onUpdateParametrosFinanceiros = viewModel::atualizarParametrosFinanceiros,
            onUpdateValorFinal = viewModel::atualizarValorFinal,
        )

        dialogoWhatsApp?.let { (pdfGerado, opcoes) ->
            DialogoEscolhaWhatsApp(
                opcoes = opcoes,
                onSelecionarWhatsApp = { opcao ->
                    dialogoWhatsApp = null
                    CompartilhamentoPdfHelper.enviarParaPacote(
                        context = context,
                        pdf = pdfGerado,
                        pacoteDestino = opcao.pacote,
                        onErro = mostrarErro,
                    )
                },
                onCompartilharOutros = {
                    dialogoWhatsApp = null
                    CompartilhamentoPdfHelper.enviarParaPacote(
                        context = context,
                        pdf = pdfGerado,
                        pacoteDestino = null,
                        onErro = mostrarErro,
                    )
                },
                onDismiss = { dialogoWhatsApp = null },
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 76.dp),
        )
    }
}

@Composable
fun OrcamentoScreenContent(
    state: OrcamentoUiState,
    onBackClick: () -> Unit,
    onStepClick: (Int) -> Unit,
    onProximoClick: () -> Unit,
    onSalvarOrcamento: () -> Unit,
    onGerarPdf: () -> Unit,
    onUpdateCliente: (String, String, String, String, TipoServico) -> Unit,
    onUpdateModelo: (ModeloSofa, Boolean, String, TecidoOpcaoUi?, String) -> Unit,
    onUpdateNumeroPecas: (String) -> Unit,
    onUpdateDimensaoPeca: (Int, String, String, String) -> Unit,
    onUpdateItensSubstituidos: (Boolean, Boolean, Boolean, Boolean, Boolean) -> Unit,
    onUpdateQuantidadeMaterial: (Int, String) -> Unit,
    onRecalcularMateriais: () -> Unit,
    onUpdateParametrosFinanceiros: (String, String, String, String) -> Unit,
    onUpdateValorFinal: (String) -> Unit,
) {
    val tituloTopo = if (state.modoEdicao) "Editar orçamento" else "Novo orçamento"
    val subtituloTopo =
        "${state.numeroOrcamento} • ${state.nomeCliente.ifBlank { "Cliente" }} • ${state.tipoServico.tituloExibicao}"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray),
    ) {
        ScreenTopBar(
            title = tituloTopo,
            subtitle = subtituloTopo,
            onBackClick = onBackClick,
        )

        StepperHeader(
            currentStep = state.passoAtual,
            onStepClick = onStepClick,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            when (state.passoAtual) {
                1 -> PassoClienteContent(
                    state = state,
                    onUpdateCliente = onUpdateCliente,
                )
                2 -> PassoModeloContent(
                    state = state,
                    onUpdateModelo = onUpdateModelo,
                    onUpdateNumeroPecas = onUpdateNumeroPecas,
                )
                3 -> PassoDimensoesContent(
                    state = state,
                    onUpdateDimensaoPeca = onUpdateDimensaoPeca,
                    onUpdateItensSubstituidos = onUpdateItensSubstituidos,
                )
                4 -> PassoMateriaisContent(
                    state = state,
                    onUpdateQuantidadeMaterial = onUpdateQuantidadeMaterial,
                    onRecalcularMateriais = onRecalcularMateriais,
                    onUpdateParametrosFinanceiros = onUpdateParametrosFinanceiros,
                )
                5 -> PassoResumoContent(
                    state = state,
                    onUpdateValorFinal = onUpdateValorFinal,
                )
            }
        }

        when (state.passoAtual) {
            1 -> BottomActionButtons(
                primaryText = "Próximo",
                onPrimaryClick = onProximoClick,
            )
            in 2..4 -> BottomActionButtons(
                primaryText = "Próximo",
                onPrimaryClick = onProximoClick,
                secondaryText = "Voltar",
                onSecondaryClick = onBackClick,
                secondaryOnRight = false,
            )
            5 -> BottomActionButtons(
                primaryText = if (state.salvando) "Salvando..." else "Salvar Orçamento",
                onPrimaryClick = onSalvarOrcamento,
                secondaryText = if (state.gerandoPdf) "Gerando PDF..." else "Gerar PDF",
                onSecondaryClick = onGerarPdf,
                secondaryOnRight = true,
            )
        }
    }
}

@Composable
private fun PassoClienteContent(
    state: OrcamentoUiState,
    onUpdateCliente: (String, String, String, String, TipoServico) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Dados do cliente",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )

        OrcaTextField(
            label = "Nome",
            value = state.nomeCliente,
            onValueChange = {
                onUpdateCliente(it, state.telefone, state.dataEntrega, state.observacoes, state.tipoServico)
            },
            placeholder = "Ex.: Ana Souza",
        )

        OrcaTextField(
            label = "Telefone",
            value = state.telefone,
            onValueChange = {
                onUpdateCliente(state.nomeCliente, it, state.dataEntrega, state.observacoes, state.tipoServico)
            },
            placeholder = "(00) 00000-0000",
            keyboardType = KeyboardType.Phone,
        )

        OrcaTextField(
            label = "Data de entrega",
            value = state.dataEntrega,
            onValueChange = {
                onUpdateCliente(state.nomeCliente, state.telefone, it, state.observacoes, state.tipoServico)
            },
            placeholder = "dd/mm/aaaa",
            trailingIcon = {
                Icon(
                    imageVector = AppIcons.Calendar,
                    contentDescription = "Calendário",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp),
                )
            },
        )

        OrcaTextField(
            label = "Obs.",
            value = state.observacoes,
            onValueChange = {
                onUpdateCliente(state.nomeCliente, state.telefone, state.dataEntrega, it, state.tipoServico)
            },
            placeholder = "Detalhes combinados com o cliente",
            singleLine = false,
            minHeight = 68.dp,
        )

        Column {
            Text(
                text = "Tipo de serviço *",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, BorderDark, RoundedCornerShape(6.dp)),
            ) {
                val isReforma = state.tipoServico == TipoServico.REFORMA
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(if (isReforma) PrimaryBlack else SurfaceWhite)
                        .clickable {
                            onUpdateCliente(
                                state.nomeCliente,
                                state.telefone,
                                state.dataEntrega,
                                state.observacoes,
                                TipoServico.REFORMA,
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (isReforma) "✓ Reforma" else "Reforma",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isReforma) SurfaceWhite else TextPrimary,
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(if (!isReforma) PrimaryBlack else SurfaceWhite)
                        .clickable {
                            onUpdateCliente(
                                state.nomeCliente,
                                state.telefone,
                                state.dataEntrega,
                                state.observacoes,
                                TipoServico.SOFA_NOVO,
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (!isReforma) "✓ Sofá Novo" else "Sofá Novo",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (!isReforma) SurfaceWhite else TextPrimary,
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "* Selecione uma opção para continuar.",
                fontSize = 11.sp,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun PassoModeloContent(
    state: OrcamentoUiState,
    onUpdateModelo: (ModeloSofa, Boolean, String, TecidoOpcaoUi?, String) -> Unit,
    onUpdateNumeroPecas: (String) -> Unit,
) {
    val cores = listOf("Cinza", "Bege", "Marrom", "Preto", "Azul Marinho", "Terracota")
    val opcoesTecidoRotulos = state.tecidosDisponiveis.map { it.rotuloDropdown }
    val rotuloTecidoAtual = state.tecidoSelecionado?.rotuloDropdown ?: "Selecione um tecido"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column {
            Text(
                text = "Modelo e acabamento",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (state.tipoServico == TipoServico.REFORMA) {
                    "Selecione o modelo do sofá a reformar."
                } else {
                    "Selecione o modelo do sofá a fabricar."
                },
                fontSize = 12.sp,
                color = TextSecondary,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ModeloSofa.entries.forEach { modelo ->
                val selecionado = state.modeloSofa == modelo
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (selecionado) CardSelectedGray else SurfaceWhite,
                    border = BorderStroke(
                        width = if (selecionado) 1.5.dp else 1.dp,
                        color = if (selecionado) BorderDark else BorderGray,
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(106.dp)
                        .clickable {
                            onUpdateModelo(
                                modelo,
                                state.possuiAlmofadas,
                                state.qtdAlmofadas,
                                state.tecidoSelecionado,
                                state.corSelecionada,
                            )
                        },
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (selecionado) 1.5.dp else 1.dp,
                                    color = if (selecionado) PrimaryBlack else TextMuted,
                                    shape = CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (selecionado) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryBlack),
                                )
                            }
                        }

                        Column {
                            Text(
                                text = modelo.tituloCurto,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = modelo.descricaoCurta,
                                fontSize = 11.sp,
                                color = TextSecondary,
                            )
                        }
                    }
                }
            }
        }

        OrcaTextField(
            label = "Número de peças",
            value = state.numeroPecas,
            onValueChange = onUpdateNumeroPecas,
            keyboardType = KeyboardType.Number,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Almofadas?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        onUpdateModelo(
                            state.modeloSofa,
                            !state.possuiAlmofadas,
                            state.qtdAlmofadas,
                            state.tecidoSelecionado,
                            state.corSelecionada,
                        )
                    },
                ) {
                    CustomSwitch(
                        checked = state.possuiAlmofadas,
                        onCheckedChange = {
                            onUpdateModelo(
                                state.modeloSofa,
                                it,
                                state.qtdAlmofadas,
                                state.tecidoSelecionado,
                                state.corSelecionada,
                            )
                        },
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (state.possuiAlmofadas) "Sim" else "Não",
                        fontSize = 13.sp,
                        color = TextPrimary,
                    )
                }
            }

            if (state.possuiAlmofadas) {
                OrcaTextField(
                    label = "Qtd. de Almofadas",
                    value = state.qtdAlmofadas,
                    onValueChange = {
                        onUpdateModelo(
                            state.modeloSofa,
                            state.possuiAlmofadas,
                            it,
                            state.tecidoSelecionado,
                            state.corSelecionada,
                        )
                    },
                    placeholder = "0",
                    suffix = "un.",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.width(140.dp),
                )
            }
        }

        OrcaDropdownField(
            label = "Tecido",
            selectedValue = rotuloTecidoAtual,
            options = opcoesTecidoRotulos,
            onOptionSelected = { rotuloEscolhido ->
                val tecidoEscolhido = state.tecidosDisponiveis
                    .firstOrNull { it.rotuloDropdown == rotuloEscolhido }
                onUpdateModelo(
                    state.modeloSofa,
                    state.possuiAlmofadas,
                    state.qtdAlmofadas,
                    tecidoEscolhido,
                    state.corSelecionada,
                )
            },
        )

        OrcaDropdownField(
            label = "Cor",
            selectedValue = state.corSelecionada,
            options = cores,
            onOptionSelected = {
                onUpdateModelo(
                    state.modeloSofa,
                    state.possuiAlmofadas,
                    state.qtdAlmofadas,
                    state.tecidoSelecionado,
                    it,
                )
            },
        )
    }
}

@Composable
private fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Box(
        modifier = Modifier
            .width(42.dp)
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (checked) PrimaryBlack else BorderGray)
            .clickable { onCheckedChange(!checked) }
            .padding(3.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(SurfaceWhite),
        )
    }
}

@Composable
private fun PassoDimensoesContent(
    state: OrcamentoUiState,
    onUpdateDimensaoPeca: (Int, String, String, String) -> Unit,
    onUpdateItensSubstituidos: (Boolean, Boolean, Boolean, Boolean, Boolean) -> Unit,
) {
    val isReforma = state.tipoServico == TipoServico.REFORMA
    val multiplasPecas = state.dimensoesPecas.size > 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column {
            Text(
                text = if (isReforma) "Medidas e reforma" else "Medidas",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (multiplasPecas) {
                    "Informe as medidas individuais de cada peça com o sofá fechado."
                } else {
                    "Informe as medidas totais com o sofá fechado."
                },
                fontSize = 12.sp,
                color = TextSecondary,
            )
        }

        if (!multiplasPecas) {
            val peca = state.dimensoesPecas.firstOrNull() ?: DimensaoPecaUi()
            CamposDimensaoPecaRow(
                peca = peca,
                onLarguraChange = { onUpdateDimensaoPeca(0, it, peca.profundidadeCm, peca.alturaCm) },
                onProfundidadeChange = { onUpdateDimensaoPeca(0, peca.larguraCm, it, peca.alturaCm) },
                onAlturaChange = { onUpdateDimensaoPeca(0, peca.larguraCm, peca.profundidadeCm, it) },
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.dimensoesPecas.forEachIndexed { index, peca ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SurfaceWhite,
                        border = BorderStroke(1.dp, BorderGray),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                text = "Peça ${index + 1}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                            )
                            CamposDimensaoPecaRow(
                                peca = peca,
                                onLarguraChange = {
                                    onUpdateDimensaoPeca(index, it, peca.profundidadeCm, peca.alturaCm)
                                },
                                onProfundidadeChange = {
                                    onUpdateDimensaoPeca(index, peca.larguraCm, it, peca.alturaCm)
                                },
                                onAlturaChange = {
                                    onUpdateDimensaoPeca(index, peca.larguraCm, peca.profundidadeCm, it)
                                },
                            )
                        }
                    }
                }
            }
        }

        // Regra condicional: "O que será substituído?" aparece somente em Reformas.
        if (isReforma) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SurfaceWhite,
                border = BorderStroke(1.dp, BorderGray),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "O que será substituído?",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                        )
                        StatusBadge(text = state.tipoServico.tituloExibicao)
                    }

                    Text(
                        text = "Opções exibidas para reformas. Marque os materiais que entram neste orçamento.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp,
                    )

                    OrcaCheckboxRow(
                        label = "Madeira",
                        checked = state.substituirMadeira,
                        onCheckedChange = {
                            onUpdateItensSubstituidos(
                                it,
                                state.substituirEspuma,
                                state.substituirMolas,
                                state.substituirPercinta,
                                state.substituirChapas,
                            )
                        },
                    )
                    OrcaCheckboxRow(
                        label = "Espuma",
                        checked = state.substituirEspuma,
                        onCheckedChange = {
                            onUpdateItensSubstituidos(
                                state.substituirMadeira,
                                it,
                                state.substituirMolas,
                                state.substituirPercinta,
                                state.substituirChapas,
                            )
                        },
                    )
                    OrcaCheckboxRow(
                        label = "Molas",
                        checked = state.substituirMolas,
                        onCheckedChange = {
                            onUpdateItensSubstituidos(
                                state.substituirMadeira,
                                state.substituirEspuma,
                                it,
                                state.substituirPercinta,
                                state.substituirChapas,
                            )
                        },
                    )
                    OrcaCheckboxRow(
                        label = "Percinta",
                        checked = state.substituirPercinta,
                        onCheckedChange = {
                            onUpdateItensSubstituidos(
                                state.substituirMadeira,
                                state.substituirEspuma,
                                state.substituirMolas,
                                it,
                                state.substituirChapas,
                            )
                        },
                    )
                    OrcaCheckboxRow(
                        label = "Chapas",
                        checked = state.substituirChapas,
                        onCheckedChange = {
                            onUpdateItensSubstituidos(
                                state.substituirMadeira,
                                state.substituirEspuma,
                                state.substituirMolas,
                                state.substituirPercinta,
                                it,
                            )
                        },
                    )
                }
            }

            Text(
                text = "Tecido e fibra entram automaticamente no cálculo do revestimento. Itens desmarcados permanecem zerados na próxima etapa.",
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp,
            )
        }
    }
}

@Composable
private fun CamposDimensaoPecaRow(
    peca: DimensaoPecaUi,
    onLarguraChange: (String) -> Unit,
    onProfundidadeChange: (String) -> Unit,
    onAlturaChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        OrcaTextField(
            label = "L • Largura",
            value = peca.larguraCm,
            onValueChange = onLarguraChange,
            placeholder = "0",
            suffix = "cm",
            keyboardType = KeyboardType.Decimal,
            modifier = Modifier.weight(1f),
        )
        OrcaTextField(
            label = "P • Profund.",
            value = peca.profundidadeCm,
            onValueChange = onProfundidadeChange,
            placeholder = "0",
            suffix = "cm",
            keyboardType = KeyboardType.Decimal,
            modifier = Modifier.weight(1f),
        )
        OrcaTextField(
            label = "A • Altura",
            value = peca.alturaCm,
            onValueChange = onAlturaChange,
            placeholder = "0",
            suffix = "cm",
            keyboardType = KeyboardType.Decimal,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun OrcaCheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (checked) PrimaryBlack else SurfaceWhite)
                .border(
                    width = 1.dp,
                    color = if (checked) PrimaryBlack else BorderGray,
                    shape = RoundedCornerShape(4.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Icon(
                    imageVector = AppIcons.Check,
                    contentDescription = null,
                    tint = SurfaceWhite,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = TextPrimary,
        )
    }
}

@Composable
private fun PassoMateriaisContent(
    state: OrcamentoUiState,
    onUpdateQuantidadeMaterial: (Int, String) -> Unit,
    onRecalcularMateriais: () -> Unit,
    onUpdateParametrosFinanceiros: (String, String, String, String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Materiais",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Text(
                text = "Recalcular",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { onRecalcularMateriais() },
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Material",
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "Qtd.",
                fontSize = 11.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(86.dp),
            )
            Text(
                text = "Custo (R$)",
                fontSize = 11.sp,
                color = TextSecondary,
                textAlign = TextAlign.End,
                modifier = Modifier.width(74.dp),
            )
        }
        HorizontalDivider(color = DividerLight, thickness = 1.dp)

        state.itensMateriais.forEachIndexed { index, itemUi ->
            val item = itemUi.item
            val subtituloPreco =
                "${Formatadores.formatarMoeda(item.precoUnitarioSnapshot)}/${item.unidade}"
            val custoTexto = if (item.custoTotal > 0.0) {
                Formatadores.formatarValorSemSimbolo(item.custoTotal)
            } else {
                "--"
            }

            MaterialItemRow(
                titulo = item.descricao,
                subtitulo = subtituloPreco,
                quantidadeTexto = itemUi.quantidadeTexto,
                unidade = item.unidade,
                onQuantidadeChange = { novoTexto ->
                    onUpdateQuantidadeMaterial(index, novoTexto)
                },
                custo = custoTexto,
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            OrcaTextField(
                label = "Margem de materiais\nextras (%)",
                value = state.margemMateriaisExtras,
                onValueChange = {
                    onUpdateParametrosFinanceiros(
                        it,
                        state.diasTrabalhados,
                        state.valorDiaria,
                        state.valorFrete,
                    )
                },
                suffix = "%",
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1f),
            )

            OrcaTextField(
                label = "Dias Trab",
                value = state.diasTrabalhados,
                onValueChange = {
                    onUpdateParametrosFinanceiros(
                        state.margemMateriaisExtras,
                        it,
                        state.valorDiaria,
                        state.valorFrete,
                    )
                },
                suffix = "dias",
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OrcaTextField(
                label = "Valor Diária (R$)",
                value = state.valorDiaria,
                onValueChange = {
                    onUpdateParametrosFinanceiros(
                        state.margemMateriaisExtras,
                        state.diasTrabalhados,
                        it,
                        state.valorFrete,
                    )
                },
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1f),
            )

            OrcaTextField(
                label = "Frete (R$)",
                value = state.valorFrete,
                onValueChange = {
                    onUpdateParametrosFinanceiros(
                        state.margemMateriaisExtras,
                        state.diasTrabalhados,
                        state.valorDiaria,
                        it,
                    )
                },
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MaterialItemRow(
    titulo: String,
    subtitulo: String?,
    quantidadeTexto: String,
    unidade: String,
    onQuantidadeChange: (String) -> Unit,
    custo: String,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
            ) {
                Text(
                    text = titulo,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                )
                if (subtitulo != null) {
                    Text(
                        text = subtitulo,
                        fontSize = 11.sp,
                        color = TextSecondary,
                    )
                }
            }

            Row(
                modifier = Modifier
                    .width(86.dp)
                    .height(34.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceWhite)
                    .border(1.dp, BorderGray, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = quantidadeTexto,
                    onValueChange = onQuantidadeChange,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = TextStyle(
                        fontSize = 13.sp,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                    ),
                    cursorBrush = SolidColor(PrimaryBlack),
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = unidade,
                    fontSize = 11.sp,
                    color = TextSecondary,
                )
            }

            Text(
                text = custo,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                textAlign = TextAlign.End,
                modifier = Modifier.width(74.dp),
            )
        }
        HorizontalDivider(color = DividerLight, thickness = 1.dp)
    }
}

@Composable
private fun PassoResumoContent(
    state: OrcamentoUiState,
    onUpdateValorFinal: (String) -> Unit,
) {
    val resumo = state.resumoFinanceiro

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Resumo do orçamento",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )

        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            val resumoDimensoes = if (state.dimensoesPecas.size == 1) {
                val peca = state.dimensoesPecas.first()
                "${peca.larguraCm.ifBlank { "-" }} × ${peca.profundidadeCm.ifBlank { "-" }} × ${peca.alturaCm.ifBlank { "-" }} cm"
            } else {
                val somaLarguraCm = state.dimensoesPecas.sumOf { Formatadores.parseNumeroPtBr(it.larguraCm) }
                "${state.dimensoesPecas.size} peças (${Formatadores.formatarQuantidade(somaLarguraCm)} cm total)"
            }
            val tecidoTexto = listOfNotNull(
                state.tecidoSelecionado?.nome?.takeIf { it.isNotBlank() },
                state.corSelecionada.lowercase().takeIf { it.isNotBlank() },
            ).joinToString(" ").ifBlank { "Tecido não selecionado" }
            val almofadasTexto = if (state.possuiAlmofadas) {
                "${state.qtdAlmofadas.ifBlank { "0" }} almofadas"
            } else {
                "sem almofadas"
            }

            Text(
                text = "${state.nomeCliente.ifBlank { "Cliente" }} • ${state.modeloSofa.tituloCurto} • ${state.tipoServico.tituloExibicao}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Text(
                text = "$resumoDimensoes • $tecidoTexto • $almofadasTexto",
                fontSize = 12.sp,
                color = TextSecondary,
            )
            Text(
                text = "Entrega: ${state.dataEntrega.ifBlank { "Não informada" }}",
                fontSize = 12.sp,
                color = TextSecondary,
            )
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = SurfaceWhite,
            border = BorderStroke(1.dp, BorderGray),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ResumoLinhaCusto(
                    label = "Materiais",
                    valor = Formatadores.formatarMoeda(resumo.subtotalMateriais),
                )
                ResumoLinhaCusto(
                    label = "Margem Extras (${Formatadores.formatarQuantidade(resumo.margemExtrasPercentual)}%)",
                    valor = Formatadores.formatarMoeda(resumo.valorMargemExtras),
                )
                ResumoLinhaCusto(
                    label = "Mão de obra (${Formatadores.formatarQuantidade(resumo.diasTrabalhados)} × ${Formatadores.formatarMoeda(resumo.valorDiaria)})",
                    valor = Formatadores.formatarMoeda(resumo.totalMaoDeObra),
                )
                ResumoLinhaCusto(
                    label = "Frete",
                    valor = Formatadores.formatarMoeda(resumo.custoFrete),
                )

                HorizontalDivider(
                    color = DividerLight,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 4.dp),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Valor Total",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                    Text(
                        text = Formatadores.formatarMoeda(resumo.totalCalculado),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            OrcaTextField(
                label = "Valor Final (R$) • sobrescrever total",
                value = state.valorFinalSobrescrito,
                onValueChange = onUpdateValorFinal,
                placeholder = Formatadores.formatarValorSemSimbolo(resumo.totalCalculado),
                keyboardType = KeyboardType.Decimal,
            )

            val ajuste = resumo.ajusteComercial
            val textoAjuste = when {
                ajuste == null -> "Sem ajuste comercial. Será salvo o valor total calculado."
                ajuste > 0.0 -> "Ajuste comercial: + ${Formatadores.formatarMoeda(ajuste)} sobre o total."
                else -> "Ajuste comercial: ${Formatadores.formatarMoeda(ajuste)} sobre o total."
            }
            Text(
                text = textoAjuste,
                fontSize = 11.sp,
                color = TextSecondary,
            )
        }

        Surface(
            shape = RoundedCornerShape(6.dp),
            color = BadgeBackground,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Valor a salvar",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                )
                Text(
                    text = Formatadores.formatarMoeda(resumo.valorFinalSalvo),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
            }
        }
    }
}

@Composable
private fun ResumoLinhaCusto(label: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextSecondary,
        )
        Text(
            text = valor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
        )
    }
}

private fun estadoPreview(passo: Int): OrcamentoUiState {
    val linho = TecidoOpcaoUi(1L, "Linho", 45.0, "m")
    return OrcamentoUiState(
        passoAtual = passo,
        numeroOrcamento = "#024",
        nomeCliente = "Ana Souza",
        telefone = "(11) 98765-4321",
        dataEntrega = "20/11/2026",
        observacoes = "Reformar assentos e trocar o tecido.",
        tipoServico = TipoServico.REFORMA,
        modeloSofa = ModeloSofa.RETRATIL,
        numeroPecas = "1",
        possuiAlmofadas = true,
        qtdAlmofadas = "4",
        tecidosDisponiveis = listOf(linho),
        tecidoSelecionado = linho,
        corSelecionada = "Cinza",
        dimensoesPecas = listOf(DimensaoPecaUi("220", "95", "90")),
        substituirMadeira = true,
        substituirEspuma = true,
        substituirPercinta = true,
        itensMateriais = listOf(
            ItemMaterialUi(
                ItemMaterialOrcamento(
                    categoria = CategoriaMaterial.TECIDO,
                    chaveMaterial = "TECIDO",
                    descricao = "Tecido: Linho / Cor: Cinza",
                    unidade = "m",
                    precoUnitarioSnapshot = 45.0,
                    quantidade = 15.0,
                    custoTotal = 675.0,
                ),
                quantidadeTexto = "15",
            ),
            ItemMaterialUi(
                ItemMaterialOrcamento(
                    categoria = CategoriaMaterial.FIBRA,
                    chaveMaterial = "FIBRA",
                    descricao = "Fibra siliconada",
                    unidade = "kg",
                    precoUnitarioSnapshot = 28.0,
                    quantidade = 8.0,
                    custoTotal = 224.0,
                ),
                quantidadeTexto = "8",
            ),
        ),
        margemMateriaisExtras = "10",
        diasTrabalhados = "3",
        valorDiaria = "200,00",
        valorFrete = "120,00",
        valorFinalSobrescrito = "1.650,00",
        resumoFinanceiro = ResumoFinanceiro(
            subtotalMateriais = 899.0,
            margemExtrasPercentual = 10.0,
            valorMargemExtras = 89.9,
            valorDiaria = 200.0,
            diasTrabalhados = 3.0,
            totalMaoDeObra = 600.0,
            custoFrete = 120.0,
            totalCalculado = 1708.9,
            valorSobrescritoManual = 1650.0,
            valorFinalSalvo = 1650.0,
        ),
    )
}

@Preview(showBackground = true, heightDp = 840)
@Composable
private fun Passo4MateriaisPreview() {
    OrcaConfortTheme {
        OrcamentoScreenContent(
            state = estadoPreview(4),
            onBackClick = {},
            onStepClick = {},
            onProximoClick = {},
            onSalvarOrcamento = {},
            onGerarPdf = {},
            onUpdateCliente = { _, _, _, _, _ -> },
            onUpdateModelo = { _, _, _, _, _ -> },
            onUpdateNumeroPecas = {},
            onUpdateDimensaoPeca = { _, _, _, _ -> },
            onUpdateItensSubstituidos = { _, _, _, _, _ -> },
            onUpdateQuantidadeMaterial = { _, _ -> },
            onRecalcularMateriais = {},
            onUpdateParametrosFinanceiros = { _, _, _, _ -> },
            onUpdateValorFinal = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 840)
@Composable
private fun Passo5ResumoPreview() {
    OrcaConfortTheme {
        OrcamentoScreenContent(
            state = estadoPreview(5),
            onBackClick = {},
            onStepClick = {},
            onProximoClick = {},
            onSalvarOrcamento = {},
            onGerarPdf = {},
            onUpdateCliente = { _, _, _, _, _ -> },
            onUpdateModelo = { _, _, _, _, _ -> },
            onUpdateNumeroPecas = {},
            onUpdateDimensaoPeca = { _, _, _, _ -> },
            onUpdateItensSubstituidos = { _, _, _, _, _ -> },
            onUpdateQuantidadeMaterial = { _, _ -> },
            onRecalcularMateriais = {},
            onUpdateParametrosFinanceiros = { _, _, _, _ -> },
            onUpdateValorFinal = {},
        )
    }
}

package com.orcaconfort.app.ui.screens.detalhes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.orcaconfort.app.domain.model.StatusOrcamento
import com.orcaconfort.app.domain.usecase.PdfOrcamentoGerado
import com.orcaconfort.app.ui.components.BottomActionButtons
import com.orcaconfort.app.ui.components.CompartilhamentoPdfHelper
import com.orcaconfort.app.ui.components.DialogoEscolhaWhatsApp
import com.orcaconfort.app.ui.components.OpcaoWhatsApp
import com.orcaconfort.app.ui.components.ScreenTopBar
import com.orcaconfort.app.ui.components.StatusBadge
import com.orcaconfort.app.ui.theme.BackgroundGray
import com.orcaconfort.app.ui.theme.BadgeBackground
import com.orcaconfort.app.ui.theme.BorderGray
import com.orcaconfort.app.ui.theme.DividerLight
import com.orcaconfort.app.ui.theme.OrcaConfortTheme
import com.orcaconfort.app.ui.theme.PrimaryBlack
import com.orcaconfort.app.ui.theme.SurfaceWhite
import com.orcaconfort.app.ui.theme.TextPrimary
import com.orcaconfort.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

private val IllustrationSurface = Color(0xFFF6F6F6)

@Composable
fun DetalhesOrcamentoScreen(
    onBackClick: () -> Unit,
    onEditarOrcamentoClick: (Long) -> Unit,
    viewModel: DetalhesOrcamentoViewModel = hiltViewModel(),
) {
    val screenState by viewModel.screenState.collectAsState()
    val gerandoPdf by viewModel.gerandoPdf.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var dialogoWhatsApp by remember {
        mutableStateOf<Pair<PdfOrcamentoGerado, List<OpcaoWhatsApp>>?>(null)
    }

    val mostrarErro: (String) -> Unit = { mensagem ->
        coroutineScope.launch {
            snackbarHostState.showSnackbar(mensagem)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val estado = screenState) {
            DetalhesScreenState.Carregando -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BackgroundGray),
                ) {
                    ScreenTopBar(
                        title = "Detalhes do orçamento",
                        subtitle = "Carregando...",
                        onBackClick = onBackClick,
                    )
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = PrimaryBlack)
                    }
                }
            }

            DetalhesScreenState.NaoEncontrado -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BackgroundGray),
                ) {
                    ScreenTopBar(
                        title = "Detalhes do orçamento",
                        subtitle = "Não encontrado",
                        onBackClick = onBackClick,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Orçamento não encontrado no banco local.",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            is DetalhesScreenState.Sucesso -> {
                val detalhes = estado.detalhes
                DetalhesOrcamentoContent(
                    state = detalhes,
                    gerandoPdf = gerandoPdf,
                    onBackClick = onBackClick,
                    onStatusSelected = { novoStatus ->
                        viewModel.atualizarStatus(novoStatus)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Status atualizado para ${novoStatus.tituloExibicao}.")
                        }
                    },
                    onEditarOrcamentoClick = { onEditarOrcamentoClick(detalhes.id) },
                    onGerarPdfClick = {
                        viewModel.gerarPdf(
                            onSucesso = { pdfGerado ->
                                CompartilhamentoPdfHelper.enviarOuSelecionarWhatsApp(
                                    context = context,
                                    pdf = pdfGerado,
                                    onSolicitarEscolha = { opcoes ->
                                        dialogoWhatsApp = pdfGerado to opcoes
                                    },
                                    onErro = mostrarErro,
                                )
                            },
                            onErro = mostrarErro,
                        )
                    },
                )
            }
        }

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
fun DetalhesOrcamentoContent(
    state: DetalhesOrcamentoUiState,
    gerandoPdf: Boolean = false,
    onBackClick: () -> Unit,
    onStatusSelected: (StatusOrcamento) -> Unit,
    onEditarOrcamentoClick: () -> Unit,
    onGerarPdfClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray),
    ) {
        ScreenTopBar(
            title = "Detalhes do orçamento",
            subtitle = "${state.codigoFormatado} • ${state.tipoServico}",
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ClienteResumoSection(
                state = state,
                onStatusSelected = onStatusSelected,
            )
            EstofadoSection(state = state)
            MateriaisUtilizadosSection(state = state)
            DetalhamentoFinanceiroSection(state = state)
            Spacer(modifier = Modifier.height(4.dp))
        }

        BottomActionButtons(
            primaryText = if (gerandoPdf) "Gerando PDF..." else "Gerar PDF",
            onPrimaryClick = onGerarPdfClick,
            secondaryText = "Editar orçamento",
            onSecondaryClick = onEditarOrcamentoClick,
            secondaryOnRight = false,
        )
    }
}

@Composable
private fun ClienteResumoSection(
    state: DetalhesOrcamentoUiState,
    onStatusSelected: (StatusOrcamento) -> Unit,
) {
    var menuStatusAberto by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = state.clienteNome,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
            )

            Box {
                Box(modifier = Modifier.clickable { menuStatusAberto = true }) {
                    StatusBadge(text = state.status.tituloExibicao)
                }
                DropdownMenu(
                    expanded = menuStatusAberto,
                    onDismissRequest = { menuStatusAberto = false },
                    modifier = Modifier.background(SurfaceWhite),
                ) {
                    StatusOrcamento.entries.forEach { opcao ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = opcao.tituloExibicao,
                                    fontSize = 13.sp,
                                    fontWeight = if (opcao == state.status) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    },
                                    color = TextPrimary,
                                )
                            },
                            onClick = {
                                menuStatusAberto = false
                                if (opcao != state.status) {
                                    onStatusSelected(opcao)
                                }
                            },
                        )
                    }
                }
            }
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
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                InfoField(
                    label = "Tipo de serviço",
                    value = state.tipoServico,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    InfoField(
                        label = "Data do orçamento",
                        value = state.dataOrcamento,
                        modifier = Modifier.weight(1f),
                    )
                    InfoField(
                        label = "Entrega prevista",
                        value = state.dataEntregaPrevista,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun EstofadoSection(state: DetalhesOrcamentoUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Estofado",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )

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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(IllustrationSurface)
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = painterResource(id = state.modelo.imagemRes),
                        contentDescription = "Imagem ilustrativa • ${state.modelo.tituloModelo}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Imagem ilustrativa • ${state.modelo.tituloModelo}",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                    )
                }

                InfoField(
                    label = "Modelo",
                    value = state.modelo.tituloModelo,
                )

                InfoField(
                    label = "Dimensões • L × P × A",
                    value = state.dimensoes,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    InfoField(
                        label = "Tecido e cor",
                        value = state.tecidoECor,
                        modifier = Modifier.weight(1f),
                    )
                    InfoField(
                        label = "Almofadas",
                        value = state.almofadas,
                        modifier = Modifier.weight(1f),
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "Observações acordadas",
                        fontSize = 11.sp,
                        color = TextSecondary,
                    )
                    Text(
                        text = state.observacoesAcordadas,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 19.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun MateriaisUtilizadosSection(state: DetalhesOrcamentoUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Materiais utilizados",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = SurfaceWhite,
            border = BorderStroke(1.dp, BorderGray),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BadgeBackground)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Material",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary,
                    )
                    Text(
                        text = "Quantidade",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary,
                    )
                }
                HorizontalDivider(color = DividerLight, thickness = 1.dp)

                state.materiais.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp),
                        ) {
                            Text(
                                text = item.nome,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                            )
                            item.observacao?.let { obs ->
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = obs,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                )
                            }
                        }

                        Text(
                            text = item.quantidade,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            textAlign = TextAlign.End,
                        )
                    }
                    if (index < state.materiais.lastIndex) {
                        HorizontalDivider(color = DividerLight, thickness = 1.dp)
                    }
                }
            }
        }

        Text(
            text = state.notaMateriais,
            fontSize = 11.sp,
            color = TextSecondary,
            lineHeight = 15.sp,
        )
    }
}

@Composable
private fun DetalhamentoFinanceiroSection(state: DetalhesOrcamentoUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Detalhamento financeiro",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )

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
                state.itensCustosMateriais.forEach { linha ->
                    LinhaFinanceiraRow(
                        label = linha.label,
                        valor = linha.valor,
                        destaque = false,
                    )
                }

                HorizontalDivider(
                    color = DividerLight,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 2.dp),
                )

                LinhaFinanceiraRow(
                    label = state.subtotalMateriais.label,
                    valor = state.subtotalMateriais.valor,
                    destaque = true,
                )

                state.itensCustosServicos.forEach { linha ->
                    LinhaFinanceiraRow(
                        label = linha.label,
                        valor = linha.valor,
                        destaque = false,
                    )
                }

                HorizontalDivider(
                    color = DividerLight,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 2.dp),
                )

                LinhaFinanceiraRow(
                    label = state.totalCalculado.label,
                    valor = state.totalCalculado.valor,
                    destaque = true,
                )

                state.ajusteComercial?.let { ajuste ->
                    LinhaFinanceiraRow(
                        label = ajuste.label,
                        valor = ajuste.valor,
                        destaque = false,
                    )
                }
            }
        }

        state.notaFinanceira?.let { nota ->
            Text(
                text = nota,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp,
            )
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = PrimaryBlack,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Valor total do orçamento",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SurfaceWhite.copy(alpha = 0.9f),
                )
                Text(
                    text = state.valorTotalFinal,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = SurfaceWhite,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = state.legendaValorFinal,
                    fontSize = 11.sp,
                    color = SurfaceWhite.copy(alpha = 0.85f),
                )
            }
        }
    }
}

@Composable
private fun InfoField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary,
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
        )
    }
}

@Composable
private fun LinhaFinanceiraRow(
    label: String,
    valor: String,
    destaque: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (destaque) FontWeight.Bold else FontWeight.Normal,
            color = if (destaque) TextPrimary else TextSecondary,
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
        )
        Text(
            text = valor,
            fontSize = 13.sp,
            fontWeight = if (destaque) FontWeight.Bold else FontWeight.Medium,
            color = TextPrimary,
            textAlign = TextAlign.End,
        )
    }
}

private val detalhesPreviewSample = DetalhesOrcamentoUiState(
    id = 24L,
    codigoFormatado = "#024",
    clienteNome = "Ana Souza",
    status = StatusOrcamento.PENDENTE,
    tipoServico = "Reforma",
    dataOrcamento = "10/11/2026",
    dataEntregaPrevista = "20/11/2026",
    modelo = ModeloSofaIlustrativo.RETRATIL,
    dimensoes = "220 × 95 × 90 cm",
    tecidoECor = "Linho cinza",
    almofadas = "Sim • 4 unidades",
    observacoesAcordadas = "Reformar assentos e trocar o tecido.\nManter a estrutura original.",
    materiais = listOf(
        MaterialDetalheUi("Tecido: Linho / Cor: Cinza", "15 m"),
        MaterialDetalheUi("Fibra siliconada", "8 kg"),
        MaterialDetalheUi("Espuma 10cm", "1,47 m"),
        MaterialDetalheUi("Percinta elástica", "27 m"),
    ),
    notaMateriais = "Quantidades conforme o orçamento salvo.",
    itensCustosMateriais = listOf(
        LinhaFinanceiraUi("Total em tecido", "R$ 675,00"),
        LinhaFinanceiraUi("Espuma / enchimento", "R$ 260,00"),
        LinhaFinanceiraUi("Estrutura / materiais adicionais", "R$ 162,00"),
    ),
    subtotalMateriais = LinhaFinanceiraUi("Subtotal dos materiais", "R$ 1.097,00"),
    itensCustosServicos = listOf(
        LinhaFinanceiraUi("Reserva para extras • 10%", "R$ 109,70"),
        LinhaFinanceiraUi("Mão de obra • 3 × R$ 200,00", "R$ 600,00"),
        LinhaFinanceiraUi("Frete", "R$ 120,00"),
    ),
    totalCalculado = LinhaFinanceiraUi("Total calculado", "R$ 1.926,70"),
    ajusteComercial = LinhaFinanceiraUi("Ajuste comercial", "- R$ 76,70"),
    notaFinanceira = null,
    valorTotalFinal = "R$ 1.850,00",
    legendaValorFinal = "Valor final com ajuste comercial aplicado",
)

@Preview(showBackground = true, heightDp = 1600)
@Composable
private fun DetalhesOrcamentoPreview() {
    OrcaConfortTheme {
        DetalhesOrcamentoContent(
            state = detalhesPreviewSample,
            onBackClick = {},
            onStatusSelected = {},
            onEditarOrcamentoClick = {},
            onGerarPdfClick = {},
        )
    }
}

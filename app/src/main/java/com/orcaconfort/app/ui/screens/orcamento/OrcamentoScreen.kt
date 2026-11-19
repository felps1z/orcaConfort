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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.orcaconfort.app.ui.components.AppIcons
import com.orcaconfort.app.ui.components.BottomActionButtons
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

@Composable
fun OrcamentoScreen(
    onBackToHome: () -> Unit,
    viewModel: OrcamentoViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val handleVoltar = {
        if (!viewModel.passoAnterior()) {
            onBackToHome()
        }
    }

    BackHandler(onBack = handleVoltar)

    OrcamentoScreenContent(
        state = state,
        onBackClick = handleVoltar,
        onStepClick = viewModel::irParaPasso,
        onProximoClick = viewModel::proximoPasso,
        onSalvarOrcamento = onBackToHome,
        onGerarPdf = onBackToHome,
        onUpdateCliente = viewModel::atualizarDadosCliente,
        onUpdateModelo = viewModel::atualizarModeloAcabamento,
        onUpdateMedidas = viewModel::atualizarMedidasReforma,
        onUpdateMateriais = viewModel::atualizarMateriais,
        onUpdateValorFinal = viewModel::atualizarValorFinal
    )
}

@Composable
fun OrcamentoScreenContent(
    state: OrcamentoUiState,
    onBackClick: () -> Unit,
    onStepClick: (Int) -> Unit,
    onProximoClick: () -> Unit,
    onSalvarOrcamento: () -> Unit,
    onGerarPdf: () -> Unit,
    onUpdateCliente: (String, String, String, String, String) -> Unit,
    onUpdateModelo: (String, Boolean, String, String, String) -> Unit,
    onUpdateMedidas: (String, String, String, Boolean, Boolean, Boolean, Boolean, Boolean) -> Unit,
    onUpdateMateriais: (String, String, String, String, String, String, String, String, String, String, String) -> Unit,
    onUpdateValorFinal: (String) -> Unit
) {
    val subtituloTopo = "${state.numeroOrcamento} • ${state.nomeCliente.ifBlank { "Cliente" }} • ${state.tipoServico}"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        ScreenTopBar(
            title = "Novo orçamento",
            subtitle = subtituloTopo,
            onBackClick = onBackClick
        )

        StepperHeader(
            currentStep = state.passoAtual,
            onStepClick = onStepClick
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (state.passoAtual) {
                1 -> PassoClienteContent(
                    state = state,
                    onUpdateCliente = onUpdateCliente
                )
                2 -> PassoModeloContent(
                    state = state,
                    onUpdateModelo = onUpdateModelo
                )
                3 -> PassoDimensoesContent(
                    state = state,
                    onUpdateMedidas = onUpdateMedidas
                )
                4 -> PassoMateriaisContent(
                    state = state,
                    onUpdateMateriais = onUpdateMateriais
                )
                5 -> PassoResumoContent(
                    state = state,
                    onUpdateValorFinal = onUpdateValorFinal
                )
            }
        }

        when (state.passoAtual) {
            1 -> BottomActionButtons(
                primaryText = "Próximo",
                onPrimaryClick = onProximoClick
            )
            in 2..4 -> BottomActionButtons(
                primaryText = "Próximo",
                onPrimaryClick = onProximoClick,
                secondaryText = "Voltar",
                onSecondaryClick = onBackClick,
                secondaryOnRight = false
            )
            5 -> BottomActionButtons(
                primaryText = "Salvar Orçamento",
                onPrimaryClick = onSalvarOrcamento,
                secondaryText = "Gerar PDF",
                onSecondaryClick = onGerarPdf,
                secondaryOnRight = true
            )
        }
    }
}

@Composable
private fun PassoClienteContent(
    state: OrcamentoUiState,
    onUpdateCliente: (String, String, String, String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Dados do cliente",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        OrcaTextField(
            label = "Nome",
            value = state.nomeCliente,
            onValueChange = {
                onUpdateCliente(it, state.telefone, state.dataEntrega, state.observacoes, state.tipoServico)
            }
        )

        OrcaTextField(
            label = "Telefone",
            value = state.telefone,
            onValueChange = {
                onUpdateCliente(state.nomeCliente, it, state.dataEntrega, state.observacoes, state.tipoServico)
            },
            keyboardType = KeyboardType.Phone
        )

        OrcaTextField(
            label = "Data de entrega",
            value = state.dataEntrega,
            onValueChange = {
                onUpdateCliente(state.nomeCliente, state.telefone, it, state.observacoes, state.tipoServico)
            },
            trailingIcon = {
                Icon(
                    imageVector = AppIcons.Calendar,
                    contentDescription = "Calendário",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        )

        OrcaTextField(
            label = "Obs.",
            value = state.observacoes,
            onValueChange = {
                onUpdateCliente(state.nomeCliente, state.telefone, state.dataEntrega, it, state.tipoServico)
            },
            singleLine = false,
            minHeight = 68.dp
        )

        Column {
            Text(
                text = "Tipo de serviço *",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, BorderDark, RoundedCornerShape(6.dp))
            ) {
                val isReforma = state.tipoServico == "Reforma"
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
                                "Reforma"
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isReforma) "✓ Reforma" else "Reforma",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isReforma) SurfaceWhite else TextPrimary
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
                                "Sofá Novo"
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (!isReforma) "✓ Sofá Novo" else "Sofá Novo",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (!isReforma) SurfaceWhite else TextPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "* Selecione uma opção para continuar.",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun PassoModeloContent(
    state: OrcamentoUiState,
    onUpdateModelo: (String, Boolean, String, String, String) -> Unit
) {
    val modelos = listOf(
        "Fixo" to "Assento fixo",
        "Retrátil" to "Assento móvel",
        "Modular" to "Por módulos"
    )
    val tecidos = listOf(
        "Linho — R$ 45,00/m",
        "Suede liso — R$ 32,00/m",
        "Veludo — R$ 58,00/m"
    )
    val cores = listOf("Cinza", "Bege", "Marrom", "Preto", "Azul Marinho", "Terracota")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Modelo e acabamento",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Selecione o modelo do sofá a reformar.",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            modelos.forEach { (titulo, descricao) ->
                val selecionado = state.modeloSofa == titulo
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (selecionado) CardSelectedGray else SurfaceWhite,
                    border = BorderStroke(
                        width = if (selecionado) 1.5.dp else 1.dp,
                        color = if (selecionado) BorderDark else BorderGray
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(106.dp)
                        .clickable {
                            onUpdateModelo(
                                titulo,
                                state.possuiAlmofadas,
                                state.qtdAlmofadas,
                                state.tecidoSelecionado,
                                state.corSelecionada
                            )
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (selecionado) 1.5.dp else 1.dp,
                                    color = if (selecionado) PrimaryBlack else TextMuted,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selecionado) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryBlack)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = titulo,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = descricao,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Almofadas?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
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
                            state.corSelecionada
                        )
                    }
                ) {
                    CustomSwitch(
                        checked = state.possuiAlmofadas,
                        onCheckedChange = {
                            onUpdateModelo(
                                state.modeloSofa,
                                it,
                                state.qtdAlmofadas,
                                state.tecidoSelecionado,
                                state.corSelecionada
                            )
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (state.possuiAlmofadas) "Sim" else "Não",
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                }
            }

            OrcaTextField(
                label = "Qtd. de Almofadas",
                value = state.qtdAlmofadas,
                onValueChange = {
                    onUpdateModelo(
                        state.modeloSofa,
                        state.possuiAlmofadas,
                        it,
                        state.tecidoSelecionado,
                        state.corSelecionada
                    )
                },
                suffix = "un.",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.width(140.dp)
            )
        }

        OrcaDropdownField(
            label = "Tecido",
            selectedValue = state.tecidoSelecionado,
            options = tecidos,
            onOptionSelected = {
                onUpdateModelo(
                    state.modeloSofa,
                    state.possuiAlmofadas,
                    state.qtdAlmofadas,
                    it,
                    state.corSelecionada
                )
            }
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
                    it
                )
            }
        )
    }
}

@Composable
private fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .width(42.dp)
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (checked) PrimaryBlack else BorderGray)
            .clickable { onCheckedChange(!checked) }
            .padding(3.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(SurfaceWhite)
        )
    }
}

@Composable
private fun PassoDimensoesContent(
    state: OrcamentoUiState,
    onUpdateMedidas: (String, String, String, Boolean, Boolean, Boolean, Boolean, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Medidas e reforma",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Informe as medidas totais com o sofá fechado.",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OrcaTextField(
                label = "L • Largura",
                value = state.larguraCm,
                onValueChange = {
                    onUpdateMedidas(
                        it,
                        state.profundidadeCm,
                        state.alturaCm,
                        state.substituirMadeira,
                        state.substituirEspuma,
                        state.substituirMolas,
                        state.substituirPercinta,
                        state.substituirChapas
                    )
                },
                suffix = "cm",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )
            OrcaTextField(
                label = "P • Profund.",
                value = state.profundidadeCm,
                onValueChange = {
                    onUpdateMedidas(
                        state.larguraCm,
                        it,
                        state.alturaCm,
                        state.substituirMadeira,
                        state.substituirEspuma,
                        state.substituirMolas,
                        state.substituirPercinta,
                        state.substituirChapas
                    )
                },
                suffix = "cm",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )
            OrcaTextField(
                label = "A • Altura",
                value = state.alturaCm,
                onValueChange = {
                    onUpdateMedidas(
                        state.larguraCm,
                        state.profundidadeCm,
                        it,
                        state.substituirMadeira,
                        state.substituirEspuma,
                        state.substituirMolas,
                        state.substituirPercinta,
                        state.substituirChapas
                    )
                },
                suffix = "cm",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = SurfaceWhite,
            border = BorderStroke(1.dp, BorderGray),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "O que será substituído?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    StatusBadge(text = state.tipoServico)
                }

                Text(
                    text = "Opções exibidas para reformas. Marque os materiais que entram neste orçamento.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                OrcaCheckboxRow(
                    label = "Madeira",
                    checked = state.substituirMadeira,
                    onCheckedChange = {
                        onUpdateMedidas(
                            state.larguraCm,
                            state.profundidadeCm,
                            state.alturaCm,
                            it,
                            state.substituirEspuma,
                            state.substituirMolas,
                            state.substituirPercinta,
                            state.substituirChapas
                        )
                    }
                )
                OrcaCheckboxRow(
                    label = "Espuma",
                    checked = state.substituirEspuma,
                    onCheckedChange = {
                        onUpdateMedidas(
                            state.larguraCm,
                            state.profundidadeCm,
                            state.alturaCm,
                            state.substituirMadeira,
                            it,
                            state.substituirMolas,
                            state.substituirPercinta,
                            state.substituirChapas
                        )
                    }
                )
                OrcaCheckboxRow(
                    label = "Molas",
                    checked = state.substituirMolas,
                    onCheckedChange = {
                        onUpdateMedidas(
                            state.larguraCm,
                            state.profundidadeCm,
                            state.alturaCm,
                            state.substituirMadeira,
                            state.substituirEspuma,
                            it,
                            state.substituirPercinta,
                            state.substituirChapas
                        )
                    }
                )
                OrcaCheckboxRow(
                    label = "Percinta",
                    checked = state.substituirPercinta,
                    onCheckedChange = {
                        onUpdateMedidas(
                            state.larguraCm,
                            state.profundidadeCm,
                            state.alturaCm,
                            state.substituirMadeira,
                            state.substituirEspuma,
                            state.substituirMolas,
                            it,
                            state.substituirChapas
                        )
                    }
                )
                OrcaCheckboxRow(
                    label = "Chapas",
                    checked = state.substituirChapas,
                    onCheckedChange = {
                        onUpdateMedidas(
                            state.larguraCm,
                            state.profundidadeCm,
                            state.alturaCm,
                            state.substituirMadeira,
                            state.substituirEspuma,
                            state.substituirMolas,
                            state.substituirPercinta,
                            it
                        )
                    }
                )
            }
        }

        Text(
            text = "Tecido e fibra entram no cálculo do revestimento. Molas e chapas originais serão mantidas.",
            fontSize = 11.sp,
            color = TextSecondary,
            lineHeight = 15.sp
        )
    }
}

@Composable
private fun OrcaCheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (checked) PrimaryBlack else SurfaceWhite)
                .border(
                    width = 1.dp,
                    color = if (checked) PrimaryBlack else BorderGray,
                    shape = RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    imageVector = AppIcons.Check,
                    contentDescription = null,
                    tint = SurfaceWhite,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = TextPrimary
        )
    }
}

@Composable
private fun PassoMateriaisContent(
    state: OrcamentoUiState,
    onUpdateMateriais: (String, String, String, String, String, String, String, String, String, String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Materiais",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Material",
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Qtd.",
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier.width(68.dp)
            )
            Text(
                text = "Custo (R$)",
                fontSize = 11.sp,
                color = TextSecondary,
                textAlign = TextAlign.End,
                modifier = Modifier.width(72.dp)
            )
        }
        HorizontalDivider(color = DividerLight, thickness = 1.dp)

        MaterialItemRow(
            titulo = "Tecido: Linho / Cor: ${state.corSelecionada}",
            subtitulo = "R$ 45,00/m",
            quantidade = state.qtdTecido,
            onQuantidadeChange = {
                onUpdateMateriais(
                    it, state.qtdFibra, state.qtdEspuma1cm, state.qtdEspuma2cm, state.qtdEspuma3cm,
                    state.qtdPercinta, state.qtdTabuas, state.margemMateriaisExtras,
                    state.diasTrabalhados, state.valorDiaria, state.valorFrete
                )
            },
            custo = "540,00"
        )

        MaterialItemRow(
            titulo = "Fibra",
            subtitulo = "R$ 28,00/kg",
            quantidade = state.qtdFibra,
            onQuantidadeChange = {
                onUpdateMateriais(
                    state.qtdTecido, it, state.qtdEspuma1cm, state.qtdEspuma2cm, state.qtdEspuma3cm,
                    state.qtdPercinta, state.qtdTabuas, state.margemMateriaisExtras,
                    state.diasTrabalhados, state.valorDiaria, state.valorFrete
                )
            },
            custo = "112,00"
        )

        MaterialItemRow(
            titulo = "Espuma 1cm",
            subtitulo = "R$ 6,00/m",
            quantidade = state.qtdEspuma1cm,
            onQuantidadeChange = {
                onUpdateMateriais(
                    state.qtdTecido, state.qtdFibra, it, state.qtdEspuma2cm, state.qtdEspuma3cm,
                    state.qtdPercinta, state.qtdTabuas, state.margemMateriaisExtras,
                    state.diasTrabalhados, state.valorDiaria, state.valorFrete
                )
            },
            custo = "--"
        )

        MaterialDropdownRow(
            titulo = "Espuma 2cm",
            selecionado = state.qtdEspuma2cm,
            opcoes = listOf("1 m", "2 m", "3 m", "4 m"),
            onSelecionado = {
                onUpdateMateriais(
                    state.qtdTecido, state.qtdFibra, state.qtdEspuma1cm, it, state.qtdEspuma3cm,
                    state.qtdPercinta, state.qtdTabuas, state.margemMateriaisExtras,
                    state.diasTrabalhados, state.valorDiaria, state.valorFrete
                )
            },
            custo = "10,00"
        )

        MaterialDropdownRow(
            titulo = "Espuma 3cm",
            selecionado = state.qtdEspuma3cm,
            opcoes = listOf("1 m", "2 m", "3 m", "4 m"),
            onSelecionado = {
                onUpdateMateriais(
                    state.qtdTecido, state.qtdFibra, state.qtdEspuma1cm, state.qtdEspuma2cm, it,
                    state.qtdPercinta, state.qtdTabuas, state.margemMateriaisExtras,
                    state.diasTrabalhados, state.valorDiaria, state.valorFrete
                )
            },
            custo = "15,00"
        )

        MaterialItemRow(
            titulo = "Percinta",
            subtitulo = "R$ 6,00/m",
            quantidade = state.qtdPercinta,
            onQuantidadeChange = {
                onUpdateMateriais(
                    state.qtdTecido, state.qtdFibra, state.qtdEspuma1cm, state.qtdEspuma2cm, state.qtdEspuma3cm,
                    it, state.qtdTabuas, state.margemMateriaisExtras,
                    state.diasTrabalhados, state.valorDiaria, state.valorFrete
                )
            },
            custo = "96,00"
        )

        MaterialItemRow(
            titulo = "Tábuas",
            subtitulo = "R$ 18,00/m",
            quantidade = state.qtdTabuas,
            onQuantidadeChange = {
                onUpdateMateriais(
                    state.qtdTecido, state.qtdFibra, state.qtdEspuma1cm, state.qtdEspuma2cm, state.qtdEspuma3cm,
                    state.qtdPercinta, it, state.margemMateriaisExtras,
                    state.diasTrabalhados, state.valorDiaria, state.valorFrete
                )
            },
            custo = "108,00"
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OrcaTextField(
                label = "Margem de mateirias\nextras (%)",
                value = state.margemMateriaisExtras,
                onValueChange = {
                    onUpdateMateriais(
                        state.qtdTecido, state.qtdFibra, state.qtdEspuma1cm, state.qtdEspuma2cm, state.qtdEspuma3cm,
                        state.qtdPercinta, state.qtdTabuas, it,
                        state.diasTrabalhados, state.valorDiaria, state.valorFrete
                    )
                },
                suffix = "%",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )

            OrcaTextField(
                label = "Dias Trab",
                value = state.diasTrabalhados,
                onValueChange = {
                    onUpdateMateriais(
                        state.qtdTecido, state.qtdFibra, state.qtdEspuma1cm, state.qtdEspuma2cm, state.qtdEspuma3cm,
                        state.qtdPercinta, state.qtdTabuas, state.margemMateriaisExtras,
                        it, state.valorDiaria, state.valorFrete
                    )
                },
                suffix = "dias",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OrcaTextField(
                label = "Valor Diária (R$)",
                value = state.valorDiaria,
                onValueChange = {
                    onUpdateMateriais(
                        state.qtdTecido, state.qtdFibra, state.qtdEspuma1cm, state.qtdEspuma2cm, state.qtdEspuma3cm,
                        state.qtdPercinta, state.qtdTabuas, state.margemMateriaisExtras,
                        state.diasTrabalhados, it, state.valorFrete
                    )
                },
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )

            OrcaTextField(
                label = "Frete (R$)",
                value = state.valorFrete,
                onValueChange = {
                    onUpdateMateriais(
                        state.qtdTecido, state.qtdFibra, state.qtdEspuma1cm, state.qtdEspuma2cm, state.qtdEspuma3cm,
                        state.qtdPercinta, state.qtdTabuas, state.margemMateriaisExtras,
                        state.diasTrabalhados, state.valorDiaria, it
                    )
                },
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MaterialItemRow(
    titulo: String,
    subtitulo: String?,
    quantidade: String,
    onQuantidadeChange: (String) -> Unit,
    custo: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                if (subtitulo != null) {
                    Text(
                        text = subtitulo,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(64.dp)
                    .height(34.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceWhite)
                    .border(1.dp, BorderGray, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = quantidade,
                    onValueChange = onQuantidadeChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 13.sp,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    ),
                    cursorBrush = SolidColor(PrimaryBlack),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text(
                text = custo,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                textAlign = TextAlign.End,
                modifier = Modifier.width(72.dp)
            )
        }
        HorizontalDivider(color = DividerLight, thickness = 1.dp)
    }
}

@Composable
private fun MaterialDropdownRow(
    titulo: String,
    selecionado: String,
    opcoes: List<String>,
    onSelecionado: (String) -> Unit,
    custo: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = titulo,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )

            OrcaDropdownField(
                label = "",
                selectedValue = selecionado,
                options = opcoes,
                onOptionSelected = onSelecionado,
                compact = true,
                modifier = Modifier.width(64.dp)
            )

            Text(
                text = custo,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                textAlign = TextAlign.End,
                modifier = Modifier.width(72.dp)
            )
        }
        HorizontalDivider(color = DividerLight, thickness = 1.dp)
    }
}

@Composable
private fun PassoResumoContent(
    state: OrcamentoUiState,
    onUpdateValorFinal: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Resumo do orçamento",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = "${state.nomeCliente} • ${state.modeloSofa} • ${state.tipoServico}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "${state.larguraCm} × ${state.profundidadeCm} × ${state.alturaCm} cm • Linho ${state.corSelecionada.lowercase()} • ${state.qtdAlmofadas} almofadas",
                fontSize = 12.sp,
                color = TextSecondary
            )
            Text(
                text = "Entrega: ${state.dataEntrega}",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = SurfaceWhite,
            border = BorderStroke(1.dp, BorderGray),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ResumoLinhaCusto(label = "Materiais", valor = "R$ 1.136,00")
                ResumoLinhaCusto(label = "Margem Extras (${state.margemMateriaisExtras}%)", valor = "R$ 113,60")
                ResumoLinhaCusto(label = "Mão de obra (${state.diasTrabalhados} × R$ 180)", valor = "R$ 540,00")
                ResumoLinhaCusto(label = "Frete", valor = "R$ ${state.valorFrete}")

                HorizontalDivider(
                    color = DividerLight,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Valor Total",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "R$ 1.909,60",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            OrcaTextField(
                label = "Valor Final (R$) • sobrescrever total",
                value = state.valorFinalSobrescrito,
                onValueChange = onUpdateValorFinal,
                keyboardType = KeyboardType.Number
            )
            Text(
                text = "Ajuste comercial: - R$ 59,60 sobre o total.",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        Surface(
            shape = RoundedCornerShape(6.dp),
            color = BadgeBackground,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Valor a salvar",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "R$ ${state.valorFinalSobrescrito}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextSecondary
        )
        Text(
            text = valor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}

@Preview(showBackground = true, heightDp = 840)
@Composable
private fun Passo1ClientePreview() {
    OrcaConfortTheme {
        OrcamentoScreenContent(
            state = OrcamentoUiState(passoAtual = 1),
            onBackClick = {},
            onStepClick = {},
            onProximoClick = {},
            onSalvarOrcamento = {},
            onGerarPdf = {},
            onUpdateCliente = { _, _, _, _, _ -> },
            onUpdateModelo = { _, _, _, _, _ -> },
            onUpdateMedidas = { _, _, _, _, _, _, _, _ -> },
            onUpdateMateriais = { _, _, _, _, _, _, _, _, _, _, _ -> },
            onUpdateValorFinal = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 840)
@Composable
private fun Passo2ModeloPreview() {
    OrcaConfortTheme {
        OrcamentoScreenContent(
            state = OrcamentoUiState(passoAtual = 2),
            onBackClick = {},
            onStepClick = {},
            onProximoClick = {},
            onSalvarOrcamento = {},
            onGerarPdf = {},
            onUpdateCliente = { _, _, _, _, _ -> },
            onUpdateModelo = { _, _, _, _, _ -> },
            onUpdateMedidas = { _, _, _, _, _, _, _, _ -> },
            onUpdateMateriais = { _, _, _, _, _, _, _, _, _, _, _ -> },
            onUpdateValorFinal = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 840)
@Composable
private fun Passo3DimensoesPreview() {
    OrcaConfortTheme {
        OrcamentoScreenContent(
            state = OrcamentoUiState(passoAtual = 3),
            onBackClick = {},
            onStepClick = {},
            onProximoClick = {},
            onSalvarOrcamento = {},
            onGerarPdf = {},
            onUpdateCliente = { _, _, _, _, _ -> },
            onUpdateModelo = { _, _, _, _, _ -> },
            onUpdateMedidas = { _, _, _, _, _, _, _, _ -> },
            onUpdateMateriais = { _, _, _, _, _, _, _, _, _, _, _ -> },
            onUpdateValorFinal = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 840)
@Composable
private fun Passo4MateriaisPreview() {
    OrcaConfortTheme {
        OrcamentoScreenContent(
            state = OrcamentoUiState(passoAtual = 4),
            onBackClick = {},
            onStepClick = {},
            onProximoClick = {},
            onSalvarOrcamento = {},
            onGerarPdf = {},
            onUpdateCliente = { _, _, _, _, _ -> },
            onUpdateModelo = { _, _, _, _, _ -> },
            onUpdateMedidas = { _, _, _, _, _, _, _, _ -> },
            onUpdateMateriais = { _, _, _, _, _, _, _, _, _, _, _ -> },
            onUpdateValorFinal = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 840)
@Composable
private fun Passo5ResumoPreview() {
    OrcaConfortTheme {
        OrcamentoScreenContent(
            state = OrcamentoUiState(passoAtual = 5),
            onBackClick = {},
            onStepClick = {},
            onProximoClick = {},
            onSalvarOrcamento = {},
            onGerarPdf = {},
            onUpdateCliente = { _, _, _, _, _ -> },
            onUpdateModelo = { _, _, _, _, _ -> },
            onUpdateMedidas = { _, _, _, _, _, _, _, _ -> },
            onUpdateMateriais = { _, _, _, _, _, _, _, _, _, _, _ -> },
            onUpdateValorFinal = {}
        )
    }
}

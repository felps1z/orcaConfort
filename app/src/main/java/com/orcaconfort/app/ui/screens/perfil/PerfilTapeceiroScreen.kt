package com.orcaconfort.app.ui.screens.perfil

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.orcaconfort.app.ui.components.BottomActionButtons
import com.orcaconfort.app.ui.components.OrcaTextField
import com.orcaconfort.app.ui.components.ScreenTopBar
import com.orcaconfort.app.ui.theme.BackgroundGray
import com.orcaconfort.app.ui.theme.BorderGray
import com.orcaconfort.app.ui.theme.OrcaConfortTheme
import com.orcaconfort.app.ui.theme.SurfaceWhite
import com.orcaconfort.app.ui.theme.TextPrimary
import com.orcaconfort.app.ui.theme.TextSecondary

@Composable
fun PerfilTapeceiroScreen(
    onBackClick: () -> Unit,
    viewModel: PerfilTapeceiroViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.mensagemFeedback) {
        state.mensagemFeedback?.let { mensagem ->
            snackbarHostState.showSnackbar(mensagem)
            viewModel.limparMensagemFeedback()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PerfilTapeceiroScreenContent(
            state = state,
            onBackClick = onBackClick,
            onNomeNegocioChange = viewModel::atualizarNomeNegocio,
            onTelefoneChange = viewModel::atualizarTelefone,
            onEnderecoChange = viewModel::atualizarEndereco,
            onInstagramChange = viewModel::atualizarInstagram,
            onValorDiariaChange = viewModel::atualizarValorDiariaPadrao,
            onMargemExtrasChange = viewModel::atualizarMargemExtrasPadrao,
            onSalvarClick = viewModel::salvarPerfil,
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 76.dp),
        )
    }
}

@Composable
fun PerfilTapeceiroScreenContent(
    state: PerfilTapeceiroUiState,
    onBackClick: () -> Unit,
    onNomeNegocioChange: (String) -> Unit,
    onTelefoneChange: (String) -> Unit,
    onEnderecoChange: (String) -> Unit,
    onInstagramChange: (String) -> Unit,
    onValorDiariaChange: (String) -> Unit,
    onMargemExtrasChange: (String) -> Unit,
    onSalvarClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray),
    ) {
        ScreenTopBar(
            title = "Perfil da Tapeçaria",
            subtitle = "Cabeçalho do PDF e valores padrão",
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Dados institucionais (cabeçalho do PDF)",
                    fontSize = 15.sp,
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
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        OrcaTextField(
                            label = "Nome do profissional / Tapeçaria",
                            value = state.nomeNegocio,
                            onValueChange = onNomeNegocioChange,
                            placeholder = "Ex.: Estofados Conforto",
                        )

                        OrcaTextField(
                            label = "Telefone / WhatsApp",
                            value = state.telefone,
                            onValueChange = onTelefoneChange,
                            placeholder = "(00) 00000-0000",
                            keyboardType = KeyboardType.Phone,
                        )

                        OrcaTextField(
                            label = "Endereço físico da oficina",
                            value = state.endereco,
                            onValueChange = onEnderecoChange,
                            placeholder = "Rua, número, bairro - cidade",
                            singleLine = false,
                            minHeight = 60.dp,
                        )

                        OrcaTextField(
                            label = "Instagram",
                            value = state.instagram,
                            onValueChange = onInstagramChange,
                            placeholder = "@suatapecaria",
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Padrões para novos orçamentos",
                    fontSize = 15.sp,
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            OrcaTextField(
                                label = "Valor da diária (R$)",
                                value = state.valorDiariaPadrao,
                                onValueChange = onValorDiariaChange,
                                placeholder = "200,00",
                                keyboardType = KeyboardType.Decimal,
                                modifier = Modifier.weight(1f),
                            )
                            OrcaTextField(
                                label = "Margem extras (%)",
                                value = state.margemExtrasPadrao,
                                onValueChange = onMargemExtrasChange,
                                placeholder = "10",
                                suffix = "%",
                                keyboardType = KeyboardType.Decimal,
                                modifier = Modifier.weight(1f),
                            )
                        }

                        Text(
                            text = "Esses valores são preenchidos automaticamente ao iniciar um novo orçamento (podendo ser ajustados caso a caso). Orçamentos já salvos não são alterados.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }

        BottomActionButtons(
            primaryText = if (state.salvando) "Salvando..." else "Salvar Perfil",
            onPrimaryClick = onSalvarClick,
        )
    }
}

@Preview(showBackground = true, heightDp = 840)
@Composable
private fun PerfilTapeceiroScreenPreview() {
    OrcaConfortTheme {
        PerfilTapeceiroScreenContent(
            state = PerfilTapeceiroUiState(
                nomeNegocio = "Estofados Conforto",
                telefone = "(11) 99876-5432",
                endereco = "Av. Brasil, 450 - Centro",
                instagram = "@estofadosconforto",
                valorDiariaPadrao = "200,00",
                margemExtrasPadrao = "10",
            ),
            onBackClick = {},
            onNomeNegocioChange = {},
            onTelefoneChange = {},
            onEnderecoChange = {},
            onInstagramChange = {},
            onValorDiariaChange = {},
            onMargemExtrasChange = {},
            onSalvarClick = {},
        )
    }
}

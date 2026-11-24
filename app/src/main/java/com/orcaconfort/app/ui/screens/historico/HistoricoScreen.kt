package com.orcaconfort.app.ui.screens.historico

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.orcaconfort.app.ui.components.AppIcons
import com.orcaconfort.app.ui.components.HomeTopHeader
import com.orcaconfort.app.ui.components.StatusBadge
import com.orcaconfort.app.ui.theme.BackgroundGray
import com.orcaconfort.app.ui.theme.BorderDark
import com.orcaconfort.app.ui.theme.BorderGray
import com.orcaconfort.app.ui.theme.OrcaConfortTheme
import com.orcaconfort.app.ui.theme.PrimaryBlack
import com.orcaconfort.app.ui.theme.SurfaceWhite
import com.orcaconfort.app.ui.theme.TextPrimary
import com.orcaconfort.app.ui.theme.TextSecondary

@Composable
fun HistoricoScreen(
    onNavigateToTabelaPrecos: () -> Unit,
    onNavigateToPerfil: () -> Unit,
    onNavigateToNovoOrcamento: () -> Unit,
    onDetalhesClick: (Long) -> Unit,
    viewModel: HistoricoViewModel = hiltViewModel(),
) {
    val orcamentos by viewModel.orcamentos.collectAsState()

    HistoricoScreenContent(
        orcamentos = orcamentos,
        onNavigateToTabelaPrecos = onNavigateToTabelaPrecos,
        onNavigateToPerfil = onNavigateToPerfil,
        onNavigateToNovoOrcamento = onNavigateToNovoOrcamento,
        onDetalhesClick = onDetalhesClick,
    )
}

@Composable
fun HistoricoScreenContent(
    orcamentos: List<OrcamentoResumoUi>,
    onNavigateToTabelaPrecos: () -> Unit,
    onNavigateToPerfil: () -> Unit,
    onNavigateToNovoOrcamento: () -> Unit,
    onDetalhesClick: (Long) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HomeTopHeader(
                trailingContent = {
                    Surface(
                        onClick = onNavigateToPerfil,
                        shape = RoundedCornerShape(6.dp),
                        color = SurfaceWhite,
                        border = BorderStroke(1.dp, BorderGray),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = AppIcons.Profile,
                                contentDescription = "Perfil da tapeçaria",
                                tint = PrimaryBlack,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Perfil",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                            )
                        }
                    }
                },
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val contagemTexto = if (orcamentos.size == 1) {
                    "1 orçamento"
                } else {
                    "${orcamentos.size} orçamentos"
                }
                Text(
                    text = contagemTexto,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Text(
                    text = "Tabela de Preços",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onNavigateToTabelaPrecos() },
                )
            }

            if (orcamentos.isEmpty()) {
                EstadoVazioHistorico(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 92.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(orcamentos, key = { it.id }) { item ->
                        OrcamentoCard(
                            item = item,
                            onDetalhesClick = { onDetalhesClick(item.id) },
                        )
                    }
                }
            }
        }

        Button(
            onClick = onNavigateToNovoOrcamento,
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryBlack,
                contentColor = SurfaceWhite,
            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp)
                .height(48.dp),
        ) {
            Text(
                text = "+ Novo Orçamento",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun EstadoVazioHistorico(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderGray),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Nenhum orçamento salvo",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Text(
                text = "Toque em \"+ Novo Orçamento\" para criar seu primeiro cálculo e salvar no histórico.",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp,
            )
        }
    }
}

@Composable
private fun OrcamentoCard(
    item: OrcamentoResumoUi,
    onDetalhesClick: () -> Unit,
) {
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
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.clienteNome,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                StatusBadge(text = item.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Entrega: ${item.dataEntrega}",
                fontSize = 12.sp,
                color = TextSecondary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    Text(
                        text = item.subtituloModeloServico,
                        fontSize = 12.sp,
                        color = TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.valorFormatado,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                }

                OutlinedButton(
                    onClick = onDetalhesClick,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, BorderDark),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = SurfaceWhite,
                        contentColor = TextPrimary,
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp),
                ) {
                    Text(
                        text = "Detalhes",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

private val orcamentosPreview = listOf(
    OrcamentoResumoUi(
        id = 24L,
        clienteNome = "Ana Souza",
        status = "Pendente",
        dataEntrega = "20/11/2026",
        subtituloModeloServico = "Retrátil • Reforma",
        valorFormatado = "R$ 1.850,00",
    ),
    OrcamentoResumoUi(
        id = 23L,
        clienteNome = "Carlos Lima",
        status = "Aprovado",
        dataEntrega = "24/11/2026",
        subtituloModeloServico = "Fixo • Sofá Novo",
        valorFormatado = "R$ 2.400,00",
    ),
)

@Preview(showBackground = true, heightDp = 840)
@Composable
private fun HistoricoScreenPreview() {
    OrcaConfortTheme {
        HistoricoScreenContent(
            orcamentos = orcamentosPreview,
            onNavigateToTabelaPrecos = {},
            onNavigateToPerfil = {},
            onNavigateToNovoOrcamento = {},
            onDetalhesClick = {},
        )
    }
}

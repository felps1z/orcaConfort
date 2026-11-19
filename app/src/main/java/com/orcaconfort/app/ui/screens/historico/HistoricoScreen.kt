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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

data class OrcamentoResumoUi(
    val id: String,
    val clienteNome: String,
    val status: String,
    val dataEntrega: String,
    val subtituloModeloServico: String,
    val valorFormatado: String
)

private val orcamentosMock = listOf(
    OrcamentoResumoUi(
        id = "024",
        clienteNome = "Ana Souza",
        status = "Pendente",
        dataEntrega = "20/11/2026",
        subtituloModeloServico = "Retrátil • Reforma",
        valorFormatado = "R$ 1.850,00"
    ),
    OrcamentoResumoUi(
        id = "023",
        clienteNome = "Carlos Lima",
        status = "Aprovado",
        dataEntrega = "24/11/2026",
        subtituloModeloServico = "Fixo • Sofá novo",
        valorFormatado = "R$ 2.400,00"
    ),
    OrcamentoResumoUi(
        id = "022",
        clienteNome = "Marina Costa",
        status = "Produção",
        dataEntrega = "12/11/2026",
        subtituloModeloServico = "Modular • Sofá novo",
        valorFormatado = "R$ 4.650,00"
    ),
    OrcamentoResumoUi(
        id = "021",
        clienteNome = "Paulo Alves",
        status = "Concluído",
        dataEntrega = "02/11/2026",
        subtituloModeloServico = "Fixo • Reforma",
        valorFormatado = "R$ 1.320,00"
    )
)

@Composable
fun HistoricoScreen(
    onNavigateToTabelaPrecos: () -> Unit,
    onNavigateToNovoOrcamento: () -> Unit,
    onDetalhesClick: (OrcamentoResumoUi) -> Unit = { onNavigateToNovoOrcamento() }
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HomeTopHeader()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${orcamentosMock.size} orçamentos",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Tabela de Preços",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onNavigateToTabelaPrecos() }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 92.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orcamentosMock, key = { it.id }) { item ->
                    OrcamentoCard(
                        item = item,
                        onDetalhesClick = { onDetalhesClick(item) }
                    )
                }
            }
        }

        Button(
            onClick = onNavigateToNovoOrcamento,
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryBlack,
                contentColor = SurfaceWhite
            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp)
                .height(48.dp)
        ) {
            Text(
                text = "+ Novo Orçamento",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun OrcamentoCard(
    item: OrcamentoResumoUi,
    onDetalhesClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.clienteNome,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                StatusBadge(text = item.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Entrega: ${item.dataEntrega}",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = item.subtituloModeloServico,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.valorFormatado,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                OutlinedButton(
                    onClick = onDetalhesClick,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, BorderDark),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = SurfaceWhite,
                        contentColor = TextPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = "Detalhes",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 840)
@Composable
private fun HistoricoScreenPreview() {
    OrcaConfortTheme {
        HistoricoScreen(
            onNavigateToTabelaPrecos = {},
            onNavigateToNovoOrcamento = {}
        )
    }
}

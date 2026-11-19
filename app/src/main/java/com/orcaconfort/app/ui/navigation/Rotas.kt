package com.orcaconfort.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.orcaconfort.app.ui.screens.detalhes.DetalhesOrcamentoScreen
import com.orcaconfort.app.ui.screens.historico.HistoricoScreen
import com.orcaconfort.app.ui.screens.orcamento.OrcamentoScreen
import com.orcaconfort.app.ui.screens.precos.TabelaPrecosScreen

enum class Rotas(val rota: String) {
    HISTORICO("historico"),
    TABELA_PRECOS("tabela_precos"),
    NOVO_ORCAMENTO("novo_orcamento"),
    DETALHES_ORCAMENTO("detalhes_orcamento/{orcamentoId}");

    companion object {
        const val ARG_ORCAMENTO_ID = "orcamentoId"

        fun detalhesOrcamento(orcamentoId: String): String =
            "detalhes_orcamento/$orcamentoId"
    }
}

@Composable
fun OrcaConfortNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Rotas.HISTORICO.rota,
        modifier = modifier,
    ) {
        composable(Rotas.HISTORICO.rota) {
            HistoricoScreen(
                onNavigateToTabelaPrecos = {
                    navController.navigate(Rotas.TABELA_PRECOS.rota)
                },
                onNavigateToNovoOrcamento = {
                    navController.navigate(Rotas.NOVO_ORCAMENTO.rota)
                },
                onDetalhesClick = { orcamento ->
                    navController.navigate(Rotas.detalhesOrcamento(orcamento.id))
                },
            )
        }

        composable(Rotas.TABELA_PRECOS.rota) {
            TabelaPrecosScreen(
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }

        composable(Rotas.NOVO_ORCAMENTO.rota) {
            OrcamentoScreen(
                onBackToHome = {
                    navController.popBackStack()
                },
            )
        }

        composable(
            route = Rotas.DETALHES_ORCAMENTO.rota,
            arguments = listOf(
                navArgument(Rotas.ARG_ORCAMENTO_ID) {
                    type = NavType.StringType
                },
            ),
        ) {
            DetalhesOrcamentoScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onEditarOrcamentoClick = {
                    navController.navigate(Rotas.NOVO_ORCAMENTO.rota)
                },
            )
        }
    }
}

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
import com.orcaconfort.app.ui.screens.perfil.PerfilTapeceiroScreen
import com.orcaconfort.app.ui.screens.precos.TabelaPrecosScreen

enum class Rotas(val rota: String) {
    HISTORICO("historico"),
    TABELA_PRECOS("tabela_precos"),
    PERFIL_TAPECEIRO("perfil_tapeceiro"),
    NOVO_ORCAMENTO("novo_orcamento?orcamentoId={orcamentoId}"),
    DETALHES_ORCAMENTO("detalhes_orcamento/{orcamentoId}");

    companion object {
        const val ARG_ORCAMENTO_ID = "orcamentoId"
        const val ID_NOVO_ORCAMENTO = -1L

        fun novoOrcamento(): String = "novo_orcamento"

        fun editarOrcamento(orcamentoId: Long): String =
            "novo_orcamento?$ARG_ORCAMENTO_ID=$orcamentoId"

        fun detalhesOrcamento(orcamentoId: Long): String =
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
                onNavigateToPerfil = {
                    navController.navigate(Rotas.PERFIL_TAPECEIRO.rota)
                },
                onNavigateToNovoOrcamento = {
                    navController.navigate(Rotas.novoOrcamento())
                },
                onDetalhesClick = { orcamentoId ->
                    navController.navigate(Rotas.detalhesOrcamento(orcamentoId))
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

        composable(Rotas.PERFIL_TAPECEIRO.rota) {
            PerfilTapeceiroScreen(
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }

        composable(
            route = Rotas.NOVO_ORCAMENTO.rota,
            arguments = listOf(
                navArgument(Rotas.ARG_ORCAMENTO_ID) {
                    type = NavType.LongType
                    defaultValue = Rotas.ID_NOVO_ORCAMENTO
                },
            ),
        ) {
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
                    type = NavType.LongType
                },
            ),
        ) {
            DetalhesOrcamentoScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onEditarOrcamentoClick = { orcamentoId ->
                    navController.navigate(Rotas.editarOrcamento(orcamentoId))
                },
            )
        }
    }
}

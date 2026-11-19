package com.orcaconfort.app.ui.screens.detalhes

import androidx.annotation.DrawableRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.orcaconfort.app.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

enum class ModeloSofaIlustrativo(
    val tituloModelo: String,
    @param:DrawableRes val imagemRes: Int,
) {
    FIXO(
        tituloModelo = "Sofá fixo",
        imagemRes = R.drawable.img_sofa_fixo,
    ),
    RETRATIL(
        tituloModelo = "Sofá retrátil",
        imagemRes = R.drawable.img_sofa_retratil,
    ),
    MODULAR(
        tituloModelo = "Sofá modular",
        imagemRes = R.drawable.img_sofa_modular,
    ),
}

data class MaterialDetalheUi(
    val nome: String,
    val quantidade: String,
    val observacao: String? = null,
)

data class LinhaFinanceiraUi(
    val label: String,
    val valor: String,
)

data class DetalhesOrcamentoUiState(
    val id: String,
    val clienteNome: String,
    val status: String,
    val tipoServico: String,
    val dataOrcamento: String,
    val dataEntregaPrevista: String,
    val modelo: ModeloSofaIlustrativo,
    val dimensoes: String,
    val tecidoECor: String,
    val almofadas: String,
    val observacoesAcordadas: String,
    val materiais: List<MaterialDetalheUi>,
    val notaMateriais: String,
    val itensCustosMateriais: List<LinhaFinanceiraUi>,
    val subtotalMateriais: LinhaFinanceiraUi,
    val itensCustosServicos: List<LinhaFinanceiraUi>,
    val totalCalculado: LinhaFinanceiraUi,
    val ajusteComercial: LinhaFinanceiraUi?,
    val notaFinanceira: String?,
    val valorTotalFinal: String,
    val legendaValorFinal: String,
)

@HiltViewModel
class DetalhesOrcamentoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val orcamentoId: String = savedStateHandle["orcamentoId"] ?: "024"

    private val _uiState = MutableStateFlow(detalhesMockPorId(orcamentoId))
    val uiState: StateFlow<DetalhesOrcamentoUiState> = _uiState.asStateFlow()
}

internal fun detalhesMockPorId(id: String): DetalhesOrcamentoUiState =
    when (id) {
        "023" -> DetalhesOrcamentoUiState(
            id = "023",
            clienteNome = "Carlos Lima",
            status = "Aprovado",
            tipoServico = "Sofá novo",
            dataOrcamento = "05/11/2026",
            dataEntregaPrevista = "24/11/2026",
            modelo = ModeloSofaIlustrativo.FIXO,
            dimensoes = "200 × 88 × 85 cm",
            tecidoECor = "Suede liso bege",
            almofadas = "Sim • 2 unidades",
            observacoesAcordadas = "Conjunto fixo com acabamento em pesponto reforçado e pés em madeira escura.",
            materiais = listOf(
                MaterialDetalheUi("Tecido • Suede liso bege", "14 m"),
                MaterialDetalheUi("Espuma • 1 cm", "2 m"),
                MaterialDetalheUi("Espuma • 2 cm", "3 m"),
                MaterialDetalheUi("Espuma • 3 cm", "4 m"),
                MaterialDetalheUi("Fibra siliconada", "3 kg"),
                MaterialDetalheUi("Percinta elástica", "18 m"),
                MaterialDetalheUi("Tábuas de madeira", "10 m", "Estrutura completa nova"),
                MaterialDetalheUi("Duratex / Compensado", "3 folhas"),
                MaterialDetalheUi("Molas", "12 un."),
                MaterialDetalheUi("Pés", "8 un."),
            ),
            notaMateriais = "Quantidades conforme o orçamento. Itens mantidos não representam material novo.",
            itensCustosMateriais = listOf(
                LinhaFinanceiraUi("Total em tecido", "R$ 448,00"),
                LinhaFinanceiraUi("Espuma / enchimento*", "R$ 480,00"),
                LinhaFinanceiraUi("Estrutura / materiais adicionais", "R$ 560,00"),
            ),
            subtotalMateriais = LinhaFinanceiraUi("Subtotal dos materiais", "R$ 1.488,00"),
            itensCustosServicos = listOf(
                LinhaFinanceiraUi("Reserva para extras • 10%", "R$ 148,80"),
                LinhaFinanceiraUi("Mão de obra • 4 × R$ 180", "R$ 720,00"),
                LinhaFinanceiraUi("Frete", "R$ 120,00"),
            ),
            totalCalculado = LinhaFinanceiraUi("Total calculado", "R$ 2.476,80"),
            ajusteComercial = LinhaFinanceiraUi("Ajuste comercial", "– R$ 76,80"),
            notaFinanceira = "*Espuma / enchimento: saldo do total de materiais informado, incluindo R$ 84,00 de fibra. Rateio a confirmar.",
            valorTotalFinal = "R$ 2.400,00",
            legendaValorFinal = "Valor final com ajuste comercial aplicado",
        )

        "022" -> DetalhesOrcamentoUiState(
            id = "022",
            clienteNome = "Marina Costa",
            status = "Produção",
            tipoServico = "Sofá novo",
            dataOrcamento = "01/11/2026",
            dataEntregaPrevista = "12/11/2026",
            modelo = ModeloSofaIlustrativo.MODULAR,
            dimensoes = "280 × 190 × 88 cm",
            tecidoECor = "Veludo cinza claro",
            almofadas = "Sim • 6 unidades",
            observacoesAcordadas = "Sofá modular em L com 4 módulos acopláveis e costura capitonê nos assentos.",
            materiais = listOf(
                MaterialDetalheUi("Tecido • Veludo cinza claro", "24 m"),
                MaterialDetalheUi("Espuma • 1 cm", "3 m"),
                MaterialDetalheUi("Espuma • 2 cm", "4 m"),
                MaterialDetalheUi("Espuma • 3 cm", "6 m"),
                MaterialDetalheUi("Fibra siliconada", "7 kg"),
                MaterialDetalheUi("Percinta elástica", "26 m"),
                MaterialDetalheUi("Tábuas de madeira", "16 m", "Estrutura modular independente"),
                MaterialDetalheUi("Duratex / Compensado", "5 folhas"),
                MaterialDetalheUi("Molas", "20 un."),
                MaterialDetalheUi("Pés", "16 un."),
            ),
            notaMateriais = "Quantidades conforme o orçamento. Itens mantidos não representam material novo.",
            itensCustosMateriais = listOf(
                LinhaFinanceiraUi("Total em tecido", "R$ 1.392,00"),
                LinhaFinanceiraUi("Espuma / enchimento*", "R$ 920,00"),
                LinhaFinanceiraUi("Estrutura / materiais adicionais", "R$ 890,00"),
            ),
            subtotalMateriais = LinhaFinanceiraUi("Subtotal dos materiais", "R$ 3.202,00"),
            itensCustosServicos = listOf(
                LinhaFinanceiraUi("Reserva para extras • 10%", "R$ 320,20"),
                LinhaFinanceiraUi("Mão de obra • 5 × R$ 180", "R$ 900,00"),
                LinhaFinanceiraUi("Frete", "R$ 180,00"),
            ),
            totalCalculado = LinhaFinanceiraUi("Total calculado", "R$ 4.602,20"),
            ajusteComercial = LinhaFinanceiraUi("Ajuste comercial", "+ R$ 47,80"),
            notaFinanceira = "*Espuma / enchimento: saldo do total de materiais informado, incluindo R$ 196,00 de fibra. Rateio a confirmar.",
            valorTotalFinal = "R$ 4.650,00",
            legendaValorFinal = "Valor final com ajuste comercial aplicado",
        )

        "021" -> DetalhesOrcamentoUiState(
            id = "021",
            clienteNome = "Paulo Alves",
            status = "Concluído",
            tipoServico = "Reforma",
            dataOrcamento = "22/10/2026",
            dataEntregaPrevista = "02/11/2026",
            modelo = ModeloSofaIlustrativo.FIXO,
            dimensoes = "185 × 85 × 82 cm",
            tecidoECor = "Suede liso marrom",
            almofadas = "Não",
            observacoesAcordadas = "Troca completa de tecido e reforço nas percintas dos assentos.",
            materiais = listOf(
                MaterialDetalheUi("Tecido • Suede liso marrom", "10 m"),
                MaterialDetalheUi("Espuma • 1 cm", "1 m"),
                MaterialDetalheUi("Espuma • 2 cm", "2 m"),
                MaterialDetalheUi("Espuma • 3 cm", "2 m"),
                MaterialDetalheUi("Fibra siliconada", "2 kg"),
                MaterialDetalheUi("Percinta elástica", "12 m"),
                MaterialDetalheUi("Tábuas de madeira", "0 m", "Estrutura original mantida"),
                MaterialDetalheUi("Duratex / Compensado", "0 folhas", "Chapas originais mantidas"),
                MaterialDetalheUi("Molas", "0 un.", "Molas originais mantidas"),
                MaterialDetalheUi("Pés", "4 un.", "Pés originais mantidos"),
            ),
            notaMateriais = "Quantidades conforme o orçamento. Itens mantidos não representam material novo.",
            itensCustosMateriais = listOf(
                LinhaFinanceiraUi("Total em tecido", "R$ 320,00"),
                LinhaFinanceiraUi("Espuma / enchimento*", "R$ 260,00"),
                LinhaFinanceiraUi("Estrutura / materiais adicionais", "R$ 140,00"),
            ),
            subtotalMateriais = LinhaFinanceiraUi("Subtotal dos materiais", "R$ 720,00"),
            itensCustosServicos = listOf(
                LinhaFinanceiraUi("Reserva para extras • 10%", "R$ 72,00"),
                LinhaFinanceiraUi("Mão de obra • 2 × R$ 180", "R$ 360,00"),
                LinhaFinanceiraUi("Frete", "R$ 120,00"),
            ),
            totalCalculado = LinhaFinanceiraUi("Total calculado", "R$ 1.272,00"),
            ajusteComercial = LinhaFinanceiraUi("Ajuste comercial", "+ R$ 48,00"),
            notaFinanceira = "*Espuma / enchimento: saldo do total de materiais informado, incluindo R$ 56,00 de fibra. Rateio a confirmar.",
            valorTotalFinal = "R$ 1.320,00",
            legendaValorFinal = "Valor final com ajuste comercial aplicado",
        )

        else -> DetalhesOrcamentoUiState(
            id = "024",
            clienteNome = "Ana Souza",
            status = "Pendente",
            tipoServico = "Reforma",
            dataOrcamento = "Não informada",
            dataEntregaPrevista = "20/11/2026",
            modelo = ModeloSofaIlustrativo.RETRATIL,
            dimensoes = "220 × 95 × 90 cm",
            tecidoECor = "Linho cinza",
            almofadas = "Sim • 4 unidades",
            observacoesAcordadas = "Reformar assentos e trocar o tecido.\nManter a estrutura original.",
            materiais = listOf(
                MaterialDetalheUi("Tecido • Linho cinza", "12 m"),
                MaterialDetalheUi("Espuma • 1 cm", "1,5 m"),
                MaterialDetalheUi("Espuma • 2 cm", "2 m"),
                MaterialDetalheUi("Espuma • 3 cm", "3 m"),
                MaterialDetalheUi("Fibra siliconada", "4 kg"),
                MaterialDetalheUi("Percinta elástica", "16 m"),
                MaterialDetalheUi("Tábuas de madeira", "6 m", "Unidades não informadas"),
                MaterialDetalheUi("Duratex / Compensado", "0 folhas", "Chapas originais mantidas"),
                MaterialDetalheUi("Molas", "0 un.", "Molas originais mantidas"),
                MaterialDetalheUi("Pés", "Não informado", "Quantidade em unidades a confirmar"),
            ),
            notaMateriais = "Quantidades conforme o orçamento. Itens mantidos não representam material novo.",
            itensCustosMateriais = listOf(
                LinhaFinanceiraUi("Total em tecido", "R$ 540,00"),
                LinhaFinanceiraUi("Espuma / enchimento*", "R$ 392,00"),
                LinhaFinanceiraUi("Estrutura / materiais adicionais", "R$ 204,00"),
            ),
            subtotalMateriais = LinhaFinanceiraUi("Subtotal dos materiais", "R$ 1.136,00"),
            itensCustosServicos = listOf(
                LinhaFinanceiraUi("Reserva para extras • 10%", "R$ 113,60"),
                LinhaFinanceiraUi("Mão de obra • 3 × R$ 180", "R$ 540,00"),
                LinhaFinanceiraUi("Frete", "R$ 120,00"),
            ),
            totalCalculado = LinhaFinanceiraUi("Total calculado", "R$ 1.909,60"),
            ajusteComercial = LinhaFinanceiraUi("Ajuste comercial", "– R$ 59,60"),
            notaFinanceira = "*Espuma / enchimento: saldo do total de materiais informado, incluindo R$ 112,00 de fibra. Rateio a confirmar.",
            valorTotalFinal = "R$ 1.850,00",
            legendaValorFinal = "Valor final com ajuste comercial aplicado",
        )
    }

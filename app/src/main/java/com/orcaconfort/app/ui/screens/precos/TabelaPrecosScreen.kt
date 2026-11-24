package com.orcaconfort.app.ui.screens.precos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.orcaconfort.app.domain.model.CategoriaMaterial
import com.orcaconfort.app.ui.components.AppIcons
import com.orcaconfort.app.ui.components.OrcaTextField
import com.orcaconfort.app.ui.components.ScreenTopBar
import com.orcaconfort.app.ui.theme.BackgroundGray
import com.orcaconfort.app.ui.theme.BadgeBackground
import com.orcaconfort.app.ui.theme.BorderDark
import com.orcaconfort.app.ui.theme.BorderGray
import com.orcaconfort.app.ui.theme.DividerLight
import com.orcaconfort.app.ui.theme.OrcaConfortTheme
import com.orcaconfort.app.ui.theme.PrimaryBlack
import com.orcaconfort.app.ui.theme.SurfaceWhite
import com.orcaconfort.app.ui.theme.TextPrimary
import com.orcaconfort.app.ui.theme.TextSecondary

@Composable
fun TabelaPrecosScreen(
    onBackClick: () -> Unit,
    viewModel: TabelaPrecosViewModel = hiltViewModel(),
) {
    val categorias by viewModel.categorias.collectAsState()

    TabelaPrecosScreenContent(
        categorias = categorias,
        onBackClick = onBackClick,
        onSalvarPreco = viewModel::atualizarPreco,
        onAdicionarMaterial = viewModel::adicionarMaterial,
    )
}

@Composable
fun TabelaPrecosScreenContent(
    categorias: List<CategoriaPrecoUi>,
    onBackClick: () -> Unit,
    onSalvarPreco: (materialId: Long, novoValorTexto: String) -> Unit,
    onAdicionarMaterial: (categoria: CategoriaMaterial, nome: String, unidade: String, valorTexto: String) -> Unit,
) {
    val expandidas = remember {
        mutableStateMapOf(CategoriaMaterial.TECIDO to true)
    }

    var itemEmEdicao by remember { mutableStateOf<ItemPrecoUi?>(null) }
    var textoPrecoEditado by remember { mutableStateOf("") }

    var categoriaParaAdicionar by remember { mutableStateOf<CategoriaPrecoUi?>(null) }
    var novoNomeMaterial by remember { mutableStateOf("") }
    var novaUnidadeMaterial by remember { mutableStateOf("") }
    var novoPrecoMaterial by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray),
    ) {
        ScreenTopBar(
            title = "Tabela de Preços",
            subtitle = "Valores unitários dos materiais",
            onBackClick = onBackClick,
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(
                    text = "Abra uma categoria para consultar os valores. Use o lápis para editar um preço.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }

            items(categorias, key = { it.categoria.name }) { categoria ->
                val expandida = expandidas[categoria.categoria] == true
                CategoriaAccordionCard(
                    categoria = categoria,
                    expandida = expandida,
                    onToggleExpand = {
                        expandidas[categoria.categoria] = !expandida
                    },
                    onEditItem = { item ->
                        // Campo recebe apenas o valor numérico (sem a unidade de medida).
                        textoPrecoEditado = item.valor
                        itemEmEdicao = item
                    },
                    onAdicionarItemClick = {
                        novoNomeMaterial = ""
                        novaUnidadeMaterial = categoria.unidadePadrao
                        novoPrecoMaterial = ""
                        categoriaParaAdicionar = categoria
                    },
                )
            }
        }
    }

    // Diálogo de edição de preço unitário
    itemEmEdicao?.let { item ->
        AlertDialog(
            onDismissRequest = { itemEmEdicao = null },
            containerColor = SurfaceWhite,
            title = {
                Text(
                    text = "Editar preço — ${item.nome}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
            },
            text = {
                OrcaTextField(
                    label = "Valor unitário (R$)",
                    value = textoPrecoEditado,
                    onValueChange = { novoValor ->
                        textoPrecoEditado = novoValor.filter { it.isDigit() || it == ',' || it == '.' }
                    },
                    keyboardType = KeyboardType.Decimal,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val valorLimpo = textoPrecoEditado.trim()
                        if (valorLimpo.isNotEmpty()) {
                            onSalvarPreco(item.id, valorLimpo)
                        }
                        itemEmEdicao = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlack,
                        contentColor = SurfaceWhite,
                    ),
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text("Salvar", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { itemEmEdicao = null },
                    border = BorderStroke(1.dp, BorderDark),
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text("Cancelar", fontSize = 13.sp, color = TextPrimary)
                }
            },
        )
    }

    // Diálogo de cadastro de nova variação de material na categoria
    categoriaParaAdicionar?.let { categoria ->
        AlertDialog(
            onDismissRequest = { categoriaParaAdicionar = null },
            containerColor = SurfaceWhite,
            title = {
                Text(
                    text = "Novo item — ${categoria.nome}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OrcaTextField(
                        label = "Nome do material",
                        value = novoNomeMaterial,
                        onValueChange = { novoNomeMaterial = it },
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        OrcaTextField(
                            label = "Valor (R$)",
                            value = novoPrecoMaterial,
                            onValueChange = { novoValor ->
                                novoPrecoMaterial = novoValor.filter { it.isDigit() || it == ',' || it == '.' }
                            },
                            keyboardType = KeyboardType.Decimal,
                            modifier = Modifier.weight(1f),
                        )
                        OrcaTextField(
                            label = "Unidade",
                            value = novaUnidadeMaterial,
                            onValueChange = { novaUnidadeMaterial = it },
                            modifier = Modifier.width(96.dp),
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (novoNomeMaterial.isNotBlank() && novoPrecoMaterial.isNotBlank()) {
                            onAdicionarMaterial(
                                categoria.categoria,
                                novoNomeMaterial,
                                novaUnidadeMaterial,
                                novoPrecoMaterial,
                            )
                            categoriaParaAdicionar = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlack,
                        contentColor = SurfaceWhite,
                    ),
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text("Adicionar", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { categoriaParaAdicionar = null },
                    border = BorderStroke(1.dp, BorderDark),
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text("Cancelar", fontSize = 13.sp, color = TextPrimary)
                }
            },
        )
    }
}

@Composable
private fun CategoriaAccordionCard(
    categoria: CategoriaPrecoUi,
    expandida: Boolean,
    onToggleExpand: () -> Unit,
    onEditItem: (ItemPrecoUi) -> Unit,
    onAdicionarItemClick: () -> Unit,
) {
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
                    .background(if (expandida) BadgeBackground else SurfaceWhite)
                    .clickable { onToggleExpand() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = categoria.nome,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                    if (expandida) {
                        Spacer(modifier = Modifier.width(8.dp))
                        val textoItens = if (categoria.itens.size == 1) {
                            "1 item"
                        } else {
                            "${categoria.itens.size} itens"
                        }
                        Text(
                            text = textoItens,
                            fontSize = 11.sp,
                            color = TextSecondary,
                        )
                    }
                }
                Icon(
                    imageVector = if (expandida) AppIcons.ChevronUp else AppIcons.ChevronDown,
                    contentDescription = if (expandida) "Recolher" else "Expandir",
                    tint = PrimaryBlack,
                    modifier = Modifier.size(16.dp),
                )
            }

            if (expandida) {
                HorizontalDivider(color = DividerLight, thickness = 1.dp)
                categoria.itens.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.nome,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Atualizado em ${item.atualizadoEm}",
                                fontSize = 11.sp,
                                color = TextSecondary,
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.precoFormatado,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { onEditItem(item) },
                                modifier = Modifier.size(28.dp),
                            ) {
                                Icon(
                                    imageVector = AppIcons.EditPencil,
                                    contentDescription = "Editar preço",
                                    tint = PrimaryBlack,
                                    modifier = Modifier.size(15.dp),
                                )
                            }
                        }
                    }
                    if (index < categoria.itens.lastIndex) {
                        HorizontalDivider(color = DividerLight, thickness = 1.dp)
                    }
                }

                HorizontalDivider(color = DividerLight, thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAdicionarItemClick() }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "+ Adicionar em ${categoria.nome}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary,
                    )
                }
            }
        }
    }
}

private val categoriasPreview = listOf(
    CategoriaPrecoUi(
        categoria = CategoriaMaterial.TECIDO,
        nome = "Tecidos",
        unidadePadrao = "m",
        itens = listOf(
            ItemPrecoUi(1L, "Linho", "01/11/2026", "45,00", "m"),
            ItemPrecoUi(2L, "Suede liso", "01/11/2026", "32,00", "m"),
            ItemPrecoUi(3L, "Veludo", "01/11/2026", "58,00", "m"),
        ),
    ),
    CategoriaPrecoUi(
        categoria = CategoriaMaterial.ESPUMA,
        nome = "Espumas",
        unidadePadrao = "m",
        itens = listOf(
            ItemPrecoUi(4L, "Espuma 1cm", "01/11/2026", "6,00", "m"),
            ItemPrecoUi(5L, "Espuma 2cm", "01/11/2026", "5,00", "m"),
            ItemPrecoUi(6L, "Espuma 3cm", "01/11/2026", "5,00", "m"),
            ItemPrecoUi(7L, "Espuma 5cm", "01/11/2026", "12,00", "m"),
            ItemPrecoUi(8L, "Espuma 10cm", "01/11/2026", "22,00", "m"),
        ),
    ),
)

@Preview(showBackground = true, heightDp = 840)
@Composable
private fun TabelaPrecosScreenPreview() {
    OrcaConfortTheme {
        TabelaPrecosScreenContent(
            categorias = categoriasPreview,
            onBackClick = {},
            onSalvarPreco = { _, _ -> },
            onAdicionarMaterial = { _, _, _, _ -> },
        )
    }
}

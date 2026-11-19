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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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

data class ItemPrecoUi(
    val nome: String,
    val atualizadoEm: String,
    val precoUnidade: String
)

data class CategoriaPrecoUi(
    val nome: String,
    val itens: List<ItemPrecoUi>,
    val expandidaInicialmente: Boolean = false
)

private val categoriasIniciais = listOf(
    CategoriaPrecoUi(
        nome = "Tecidos",
        expandidaInicialmente = true,
        itens = listOf(
            ItemPrecoUi("Linho", "01/11/2026", "R$ 45,00 / m"),
            ItemPrecoUi("Suede liso", "01/11/2026", "R$ 32,00 / m"),
            ItemPrecoUi("Veludo", "01/11/2026", "R$ 58,00 / m")
        )
    ),
    CategoriaPrecoUi(
        nome = "Espumas",
        itens = listOf(
            ItemPrecoUi("Espuma 1cm", "01/11/2026", "R$ 6,00 / m"),
            ItemPrecoUi("Espuma 2cm", "01/11/2026", "R$ 5,00 / m"),
            ItemPrecoUi("Espuma 3cm", "01/11/2026", "R$ 5,00 / m")
        )
    ),
    CategoriaPrecoUi(
        nome = "Fibra",
        itens = listOf(
            ItemPrecoUi("Fibra siliconada", "01/11/2026", "R$ 28,00 / kg")
        )
    ),
    CategoriaPrecoUi(
        nome = "Percinta",
        itens = listOf(
            ItemPrecoUi("Percinta elástica", "01/11/2026", "R$ 6,00 / m")
        )
    ),
    CategoriaPrecoUi(
        nome = "Madeira",
        itens = listOf(
            ItemPrecoUi("Tábuas de eucalipto", "01/11/2026", "R$ 18,00 / m")
        )
    ),
    CategoriaPrecoUi(
        nome = "Folhas",
        itens = listOf(
            ItemPrecoUi("Chapa compensado", "01/11/2026", "R$ 65,00 / un")
        )
    ),
    CategoriaPrecoUi(
        nome = "Pés",
        itens = listOf(
            ItemPrecoUi("Pé de madeira", "01/11/2026", "R$ 15,00 / un")
        )
    ),
    CategoriaPrecoUi(
        nome = "Molas",
        itens = listOf(
            ItemPrecoUi("Mola espiral", "01/11/2026", "R$ 12,00 / un")
        )
    )
)

@Composable
fun TabelaPrecosScreen(
    onBackClick: () -> Unit
) {
    val categorias = remember { mutableStateListOf(*categoriasIniciais.toTypedArray()) }
    val expandidas = remember {
        mutableStateListOf(*categoriasIniciais.map { it.expandidaInicialmente }.toTypedArray())
    }
    var itemEmEdicao by remember { mutableStateOf<Triple<Int, Int, ItemPrecoUi>?>(null) }
    var textoPrecoEditado by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        ScreenTopBar(
            title = "Tabela de Preços",
            subtitle = "Valores unitários dos materiais",
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "Abra uma categoria para consultar os valores. Use o lápis para editar um preço.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            itemsIndexed(categorias) { catIndex, categoria ->
                val expandida = expandidas[catIndex]
                CategoriaAccordionCard(
                    categoria = categoria,
                    expandida = expandida,
                    onToggleExpand = { expandidas[catIndex] = !expandida },
                    onEditItem = { itemIndex, item ->
                        textoPrecoEditado = item.precoUnidade
                        itemEmEdicao = Triple(catIndex, itemIndex, item)
                    }
                )
            }
        }
    }

    itemEmEdicao?.let { (catIndex, itemIndex, item) ->
        AlertDialog(
            onDismissRequest = { itemEmEdicao = null },
            containerColor = SurfaceWhite,
            title = {
                Text(
                    text = "Editar preço — ${item.nome}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                OrcaTextField(
                    label = "Valor unitário",
                    value = textoPrecoEditado,
                    onValueChange = { textoPrecoEditado = it },
                    keyboardType = KeyboardType.Text
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val catAtual = categorias[catIndex]
                        val novosItens = catAtual.itens.toMutableList()
                        novosItens[itemIndex] = item.copy(precoUnidade = textoPrecoEditado)
                        categorias[catIndex] = catAtual.copy(itens = novosItens)
                        itemEmEdicao = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlack,
                        contentColor = SurfaceWhite
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Salvar", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { itemEmEdicao = null },
                    border = BorderStroke(1.dp, BorderDark),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Cancelar", fontSize = 13.sp, color = TextPrimary)
                }
            }
        )
    }
}

@Composable
private fun CategoriaAccordionCard(
    categoria: CategoriaPrecoUi,
    expandida: Boolean,
    onToggleExpand: () -> Unit,
    onEditItem: (Int, ItemPrecoUi) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (expandida) BadgeBackground else SurfaceWhite)
                    .clickable { onToggleExpand() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = categoria.nome,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (expandida) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${categoria.itens.size} itens",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
                Icon(
                    imageVector = if (expandida) AppIcons.ChevronUp else AppIcons.ChevronDown,
                    contentDescription = if (expandida) "Recolher" else "Expandir",
                    tint = PrimaryBlack,
                    modifier = Modifier.size(16.dp)
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.nome,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Atualizado em ${item.atualizadoEm}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.precoUnidade,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { onEditItem(index, item) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = AppIcons.EditPencil,
                                    contentDescription = "Editar preço",
                                    tint = PrimaryBlack,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                    if (index < categoria.itens.lastIndex) {
                        HorizontalDivider(color = DividerLight, thickness = 1.dp)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 840)
@Composable
private fun TabelaPrecosScreenPreview() {
    OrcaConfortTheme {
        TabelaPrecosScreen(onBackClick = {})
    }
}

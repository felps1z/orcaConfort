package com.orcaconfort.app.data.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.withTranslation
import com.orcaconfort.app.R
import com.orcaconfort.app.core.util.Formatadores
import com.orcaconfort.app.domain.model.CategoriaMaterial
import com.orcaconfort.app.domain.model.ItemMaterialOrcamento
import com.orcaconfort.app.domain.model.ModeloSofa
import com.orcaconfort.app.domain.model.Orcamento
import com.orcaconfort.app.domain.model.PerfilTapeceiro
import com.orcaconfort.app.domain.model.TipoServico
import com.orcaconfort.app.domain.repository.GeradorPdfOrcamento
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação nativa do gerador de PDF de orçamento usando [PdfDocument].
 *
 * Layout fiel à identidade visual da tela de Detalhes do app (cartões brancos com borda sutil,
 * cor de marca vermelha `#CE2C2C` e tipografia limpa), organizado em 5 blocos para o cliente:
 * 1. Cabeçalho Institucional da Tapeçaria (nome, telefone, endereço, Instagram e logo).
 * 2. Identificação do Cliente e Serviço.
 * 3. Bloco Visual e Técnico do Estofado (imagem ilustrativa do modelo + especificações).
 * 4. Tabela de Materiais Utilizados.
 * 5. Detalhamento Financeiro por Grupo e Valor Total em destaque.
 */
@Singleton
class AndroidPdfOrcamentoGenerator @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : GeradorPdfOrcamento {

    companion object {
        // Tamanho A4 em pontos PostScript (72 dpi)
        private const val PAGE_WIDTH = 595
        private const val PAGE_HEIGHT = 842
        private const val MARGIN_X = 32f
        private const val MARGIN_TOP = 32f
        private const val MARGIN_BOTTOM = 36f
        private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN_X * 2)

        // Paleta alinhada ao Color.kt do aplicativo
        private val COLOR_BRAND = Color.parseColor("#CE2C2C")
        private val COLOR_BG_PAGE = Color.parseColor("#FAF8F8")
        private val COLOR_CARD = Color.WHITE
        private val COLOR_ILLUSTRATION_BG = Color.parseColor("#F6F6F6")
        private val COLOR_HEADER_ROW = Color.parseColor("#F3F0F0")
        private val COLOR_BORDER = Color.parseColor("#DADADA")
        private val COLOR_DIVIDER = Color.parseColor("#E6E6E6")
        private val COLOR_TEXT_PRIMARY = Color.parseColor("#1A1A1A")
        private val COLOR_TEXT_SECONDARY = Color.parseColor("#666666")
    }

    override suspend fun gerarPdf(
        orcamento: Orcamento,
        perfil: PerfilTapeceiro,
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val pastaDestino = File(context.cacheDir, "orcamentos").apply { mkdirs() }
            val codigo = Formatadores.formatarCodigoOrcamento(orcamento.id).replace("#", "")
            val slugCliente = orcamento.cliente.nome
                .lowercase()
                .replace(Regex("[^a-z0-9]+"), "_")
                .trim('_')
                .ifEmpty { "cliente" }
                .take(24)
            val arquivoPdf = File(pastaDestino, "orcamento_${codigo}_${slugCliente}.pdf")

            val document = PdfDocument()
            val writer = PdfPageWriter(document)
            try {
                desenharCabecalhoInstitucional(writer, orcamento, perfil)
                desenharIdentificacaoCliente(writer, orcamento)
                desenharBlocoEstofado(writer, orcamento)
                desenharTabelaMateriais(writer, orcamento)
                desenharDetalhamentoFinanceiro(writer, orcamento)
                writer.finalizar()

                FileOutputStream(arquivoPdf).use { output ->
                    document.writeTo(output)
                }
            } finally {
                document.close()
            }

            arquivoPdf.absolutePath
        }
    }

    // =============================================================================================
    // 1. Cabeçalho Institucional da Tapeçaria
    // =============================================================================================

    private fun desenharCabecalhoInstitucional(
        writer: PdfPageWriter,
        orcamento: Orcamento,
        perfil: PerfilTapeceiro,
    ) {
        val linhasContato = buildList {
            if (perfil.telefone.isNotBlank()) add("WhatsApp / Tel.: ${perfil.telefone.trim()}")
            if (perfil.endereco.isNotBlank()) add("Endereço: ${perfil.endereco.trim()}")
            if (perfil.instagram.isNotBlank()) {
                val handle = perfil.instagram.trim().let { if (it.startsWith("@")) it else "@$it" }
                add("Instagram: $handle")
            }
        }

        val alturaCabecalho = 48f + (linhasContato.size * 14f) + 12f
        writer.garantirEspaco(alturaCabecalho + 14f)

        val topo = writer.cursorY
        val cardRect = RectF(MARGIN_X, topo, PAGE_WIDTH - MARGIN_X, topo + alturaCabecalho)
        desenharCard(writer.canvas, cardRect)

        // Faixa lateral esquerda na cor da marca
        val faixaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_BRAND }
        val faixaRect = RectF(cardRect.left, cardRect.top, cardRect.left + 5f, cardRect.bottom)
        writer.canvas.drawRoundRect(faixaRect, 4f, 4f, faixaPaint)

        // Logo vetorial do app (ic_logo_estofados)
        desenharLogoEstofados(
            canvas = writer.canvas,
            left = (cardRect.left + 18f).toInt(),
            top = (topo + 16f).toInt(),
            width = 34,
            height = 26,
        )

        // Nome da tapeçaria
        val tituloPaint = criarTextPaint(16f, COLOR_TEXT_PRIMARY, bold = true)
        val nomeNegocio = perfil.nomeNegocio.ifBlank { "OrçaConfort Tapeçaria" }
        writer.canvas.drawText(nomeNegocio, cardRect.left + 60f, topo + 30f, tituloPaint)

        // Badge com o número do orçamento à direita
        val codigo = "Orçamento ${Formatadores.formatarCodigoOrcamento(orcamento.id)}"
        val badgeTextPaint = criarTextPaint(10f, COLOR_BRAND, bold = true)
        val badgeTextWidth = badgeTextPaint.measureText(codigo)
        val badgeRect = RectF(
            cardRect.right - badgeTextWidth - 32f,
            topo + 14f,
            cardRect.right - 14f,
            topo + 34f,
        )
        val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FDECEC") }
        writer.canvas.drawRoundRect(badgeRect, 5f, 5f, badgeBgPaint)
        writer.canvas.drawText(codigo, badgeRect.left + 9f, badgeRect.top + 13.5f, badgeTextPaint)

        // Linhas de contato da tapeçaria
        val infoPaint = criarTextPaint(9.5f, COLOR_TEXT_SECONDARY, bold = false)
        var yInfo = topo + 50f
        linhasContato.forEach { linha ->
            writer.canvas.drawText(linha, cardRect.left + 18f, yInfo, infoPaint)
            yInfo += 14f
        }

        writer.avancar(alturaCabecalho + 14f)
    }

    // =============================================================================================
    // 2. Identificação do Cliente e Serviço
    // =============================================================================================

    private fun desenharIdentificacaoCliente(
        writer: PdfPageWriter,
        orcamento: Orcamento,
    ) {
        val alturaCard = 82f
        writer.garantirEspaco(alturaCard + 16f)

        val topo = writer.cursorY
        val cardRect = RectF(MARGIN_X, topo, PAGE_WIDTH - MARGIN_X, topo + alturaCard)
        desenharCard(writer.canvas, cardRect)

        // Nome do cliente + badge do tipo de serviço
        val clientePaint = criarTextPaint(15f, COLOR_TEXT_PRIMARY, bold = true)
        writer.canvas.drawText(orcamento.cliente.nome, cardRect.left + 14f, topo + 25f, clientePaint)

        val tipoServicoTexto = if (orcamento.tipoServico == TipoServico.REFORMA) {
            "Reforma"
        } else {
            "Fabricação Nova"
        }
        val badgePaint = criarTextPaint(9.5f, COLOR_TEXT_PRIMARY, bold = true)
        val larguraBadge = badgePaint.measureText(tipoServicoTexto) + 18f
        val badgeRect = RectF(
            cardRect.right - larguraBadge - 14f,
            topo + 11f,
            cardRect.right - 14f,
            topo + 29f,
        )
        val badgeBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_HEADER_ROW }
        val badgeBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_BORDER
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }
        writer.canvas.drawRoundRect(badgeRect, 4f, 4f, badgeBg)
        writer.canvas.drawRoundRect(badgeRect, 4f, 4f, badgeBorder)
        writer.canvas.drawText(tipoServicoTexto, badgeRect.left + 9f, badgeRect.top + 12.5f, badgePaint)

        // Divisor horizontal
        val dividerPaint = Paint().apply { color = COLOR_DIVIDER; strokeWidth = 0.8f }
        writer.canvas.drawLine(cardRect.left + 14f, topo + 38f, cardRect.right - 14f, topo + 38f, dividerPaint)

        // Três colunas: Tipo de Serviço | Data do Orçamento | Entrega Prevista
        val colWidth = (CONTENT_WIDTH - 28f) / 3f
        val yLabel = topo + 53f
        val yValue = topo + 68f
        desenharCampoInfo(writer.canvas, "Tipo de serviço", tipoServicoTexto, cardRect.left + 14f, yLabel, yValue)
        desenharCampoInfo(
            writer.canvas,
            "Data do orçamento",
            Formatadores.formatarData(orcamento.criadoEm),
            cardRect.left + 14f + colWidth,
            yLabel,
            yValue,
        )
        desenharCampoInfo(
            writer.canvas,
            "Entrega prevista",
            Formatadores.formatarData(orcamento.cliente.dataEntregaTimestamp),
            cardRect.left + 14f + (colWidth * 2f),
            yLabel,
            yValue,
        )

        writer.avancar(alturaCard + 16f)
    }

    // =============================================================================================
    // 3. Bloco Visual e Técnico do Estofado
    // =============================================================================================

    private fun desenharBlocoEstofado(
        writer: PdfPageWriter,
        orcamento: Orcamento,
    ) {
        val dimensoesTexto = formatarDimensoes(orcamento)
        val tecidoTexto = listOfNotNull(
            orcamento.sofa.nomeTecidoSelecionado.takeIf { it.isNotBlank() },
            orcamento.sofa.corTecido.lowercase().takeIf { it.isNotBlank() },
        ).joinToString(" ").ifBlank { "Não informado" }

        val almofadasTexto = if (orcamento.sofa.possuiAlmofadas) {
            val qtd = orcamento.sofa.qtdAlmofadas
            "Sim • $qtd ${if (qtd == 1) "unidade" else "unidades"}"
        } else {
            "Não"
        }

        val obsTexto = orcamento.cliente.observacoes.ifBlank { "Sem observações adicionais." }
        val larguraInterna = (CONTENT_WIDTH - 28f).toInt()
        val dimensoesLayout = criarStaticLayout(dimensoesTexto, criarTextPaint(10.5f, COLOR_TEXT_PRIMARY, bold = true), larguraInterna)
        val obsLayout = criarStaticLayout(obsTexto, criarTextPaint(10f, COLOR_TEXT_PRIMARY, bold = false), larguraInterna)

        val alturaTituloSecao = 20f
        val alturaCaixaImagem = 132f
        val alturaCard = 14f + alturaCaixaImagem + 14f +
            32f + // Modelo
            (16f + dimensoesLayout.height) + 10f + // Dimensões
            32f + // Tecido e Almofadas
            (16f + obsLayout.height) + 14f

        writer.garantirEspaco(alturaTituloSecao + alturaCard + 16f)

        desenharTituloSecao(writer, "Estofado")

        val topo = writer.cursorY
        val cardRect = RectF(MARGIN_X, topo, PAGE_WIDTH - MARGIN_X, topo + alturaCard)
        desenharCard(writer.canvas, cardRect)

        // Container da imagem ilustrativa do modelo
        val imgContainer = RectF(
            cardRect.left + 14f,
            topo + 14f,
            cardRect.right - 14f,
            topo + 14f + alturaCaixaImagem,
        )
        val imgBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_ILLUSTRATION_BG }
        writer.canvas.drawRoundRect(imgContainer, 6f, 6f, imgBgPaint)

        val imagemRes = drawableParaModelo(orcamento.sofa.modelo)
        desenharImagemCentralizada(
            canvas = writer.canvas,
            drawableRes = imagemRes,
            area = RectF(
                imgContainer.left + 16f,
                imgContainer.top + 8f,
                imgContainer.right - 16f,
                imgContainer.bottom - 22f,
            ),
        )

        val legendaPaint = criarTextPaint(8.5f, COLOR_TEXT_SECONDARY, bold = false).apply {
            textAlign = Paint.Align.CENTER
        }
        writer.canvas.drawText(
            "Imagem ilustrativa • ${orcamento.sofa.modelo.tituloCompleto}",
            imgContainer.centerX(),
            imgContainer.bottom - 7f,
            legendaPaint,
        )

        var yAtual = imgContainer.bottom + 14f
        val xEsquerda = cardRect.left + 14f
        val xMeio = cardRect.left + 14f + ((CONTENT_WIDTH - 28f) / 2f)

        // Modelo
        desenharCampoInfo(writer.canvas, "Modelo", orcamento.sofa.modelo.tituloCompleto, xEsquerda, yAtual + 9f, yAtual + 23f)
        yAtual += 32f

        // Dimensões (suporta 1 ou várias peças)
        val labelPaint = criarTextPaint(8.5f, COLOR_TEXT_SECONDARY, bold = false)
        writer.canvas.drawText("Dimensões • L × P × A", xEsquerda, yAtual + 9f, labelPaint)
        writer.canvas.withTranslation(xEsquerda, yAtual + 13f) {
            dimensoesLayout.draw(this)
        }
        yAtual += 16f + dimensoesLayout.height + 10f

        // Tecido e cor | Almofadas
        desenharCampoInfo(writer.canvas, "Tecido e cor", tecidoTexto, xEsquerda, yAtual + 9f, yAtual + 23f)
        desenharCampoInfo(writer.canvas, "Almofadas", almofadasTexto, xMeio, yAtual + 9f, yAtual + 23f)
        yAtual += 32f

        // Observações acordadas
        writer.canvas.drawText("Observações acordadas", xEsquerda, yAtual + 9f, labelPaint)
        writer.canvas.withTranslation(xEsquerda, yAtual + 13f) {
            obsLayout.draw(this)
        }

        writer.avancar(alturaCard + 16f)
    }

    // =============================================================================================
    // 4. Tabela de Materiais Utilizados
    // =============================================================================================

    private fun desenharTabelaMateriais(
        writer: PdfPageWriter,
        orcamento: Orcamento,
    ) {
        val itensParaPdf = selecionarItensParaTabelaCliente(orcamento)
        val alturaHeaderTabela = 26f
        val alturaLinha = 24f

        writer.garantirEspaco(20f + alturaHeaderTabela + (alturaLinha * 2))
        desenharTituloSecao(writer, "Materiais utilizados")

        // Cabeçalho da tabela
        val topoHeader = writer.cursorY
        val headerRect = RectF(MARGIN_X, topoHeader, PAGE_WIDTH - MARGIN_X, topoHeader + alturaHeaderTabela)
        val headerBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_HEADER_ROW }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_BORDER
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }
        writer.canvas.drawRect(headerRect, headerBg)
        writer.canvas.drawRect(headerRect, borderPaint)

        val headerTextPaint = criarTextPaint(9.5f, COLOR_TEXT_SECONDARY, bold = true)
        val headerTextRight = criarTextPaint(9.5f, COLOR_TEXT_SECONDARY, bold = true).apply {
            textAlign = Paint.Align.RIGHT
        }
        writer.canvas.drawText("Material", headerRect.left + 12f, topoHeader + 16.5f, headerTextPaint)
        writer.canvas.drawText("Quantidade", headerRect.right - 12f, topoHeader + 16.5f, headerTextRight)
        writer.avancar(alturaHeaderTabela)

        val nomePaint = criarTextPaint(10f, COLOR_TEXT_PRIMARY, bold = false)
        val qtdPaint = criarTextPaint(10f, COLOR_TEXT_PRIMARY, bold = true).apply {
            textAlign = Paint.Align.RIGHT
        }
        val qtdZeroPaint = criarTextPaint(9.5f, COLOR_TEXT_SECONDARY, bold = false).apply {
            textAlign = Paint.Align.RIGHT
        }
        val rowBg = Paint().apply { color = COLOR_CARD }

        itensParaPdf.forEach { linha ->
            if (writer.espacoRestante() < alturaLinha + 8f) {
                writer.novaPagina()
            }
            val yLinha = writer.cursorY
            val rowRect = RectF(MARGIN_X, yLinha, PAGE_WIDTH - MARGIN_X, yLinha + alturaLinha)
            writer.canvas.drawRect(rowRect, rowBg)
            writer.canvas.drawRect(rowRect, borderPaint)

            writer.canvas.drawText(linha.descricao, rowRect.left + 12f, yLinha + 15.5f, nomePaint)
            writer.canvas.drawText(
                linha.quantidadeTexto,
                rowRect.right - 12f,
                yLinha + 15.5f,
                if (linha.destaqueQuantidade) qtdPaint else qtdZeroPaint,
            )
            writer.avancar(alturaLinha)
        }

        val notaPaint = criarTextPaint(8.5f, COLOR_TEXT_SECONDARY, bold = false)
        writer.avancar(12f)
        writer.canvas.drawText(
            "Quantidades estimadas conforme as especificações do orçamento. Itens mantidos não representam material novo.",
            MARGIN_X,
            writer.cursorY,
            notaPaint,
        )
        writer.avancar(16f)
    }

    // =============================================================================================
    // 5. Detalhamento Financeiro por Grupo e Valor Total em Destaque
    // =============================================================================================

    private fun desenharDetalhamentoFinanceiro(
        writer: PdfPageWriter,
        orcamento: Orcamento,
    ) {
        val custoTecido = orcamento.itens
            .filter { it.categoria == CategoriaMaterial.TECIDO }
            .sumOf { it.custoTotal }
        val custoEspumaEnchimento = orcamento.itens
            .filter { it.categoria == CategoriaMaterial.ESPUMA || it.categoria == CategoriaMaterial.FIBRA }
            .sumOf { it.custoTotal }
        val custoEstruturaBase = orcamento.itens
            .filter {
                it.categoria != CategoriaMaterial.TECIDO &&
                    it.categoria != CategoriaMaterial.ESPUMA &&
                    it.categoria != CategoriaMaterial.FIBRA
            }
            .sumOf { it.custoTotal }

        val fin = orcamento.financeiro
        // A margem de materiais extras (cola, grampos, linhas, zíperes) compõe "Estrutura / Materiais Adicionais".
        val custoEstruturaEAdicionais = Formatadores.arredondar2Casas(custoEstruturaBase + fin.valorMargemExtras)
        val ajuste = fin.ajusteComercial

        val linhas = buildList {
            add("Total em Tecido" to Formatadores.formatarMoeda(custoTecido))
            add("Total em Espuma / Enchimento" to Formatadores.formatarMoeda(custoEspumaEnchimento))
            add("Total em Estrutura / Materiais Adicionais*" to Formatadores.formatarMoeda(custoEstruturaEAdicionais))
            add("Custo de Mão de Obra" to Formatadores.formatarMoeda(fin.totalMaoDeObra))
            add("Frete" to Formatadores.formatarMoeda(fin.custoFrete))
            if (ajuste != null) {
                val textoAjuste = if (ajuste > 0.0) {
                    "+ ${Formatadores.formatarMoeda(ajuste)}"
                } else {
                    Formatadores.formatarMoeda(ajuste)
                }
                add("Ajuste comercial" to textoAjuste)
            }
        }

        val alturaCardLinhas = 16f + (linhas.size * 20f) + 8f
        val alturaBoxTotal = 72f
        val alturaTotalBloco = 20f + alturaCardLinhas + 14f + 12f + alturaBoxTotal + 12f

        writer.garantirEspaco(alturaTotalBloco)
        desenharTituloSecao(writer, "Detalhamento financeiro")

        // Cartão de custos agrupados
        val topoCard = writer.cursorY
        val cardRect = RectF(MARGIN_X, topoCard, PAGE_WIDTH - MARGIN_X, topoCard + alturaCardLinhas)
        desenharCard(writer.canvas, cardRect)

        val labelPaint = criarTextPaint(10.5f, COLOR_TEXT_SECONDARY, bold = false)
        val valorPaint = criarTextPaint(10.5f, COLOR_TEXT_PRIMARY, bold = true).apply {
            textAlign = Paint.Align.RIGHT
        }

        var yLinha = topoCard + 22f
        linhas.forEach { (rotulo, valor) ->
            writer.canvas.drawText(rotulo, cardRect.left + 14f, yLinha, labelPaint)
            writer.canvas.drawText(valor, cardRect.right - 14f, yLinha, valorPaint)
            yLinha += 20f
        }
        writer.avancar(alturaCardLinhas + 12f)

        val notaInsumos = criarTextPaint(8f, COLOR_TEXT_SECONDARY, bold = false)
        writer.canvas.drawText(
            "*Inclui estrutura e insumos adicionais necessários ao acabamento (cola, grampos, linhas e aviamentos).",
            MARGIN_X,
            writer.cursorY,
            notaInsumos,
        )
        writer.avancar(14f)

        // Box em destaque com o Valor Total do Orçamento (cor da marca #CE2C2C)
        val topoTotal = writer.cursorY
        val totalRect = RectF(MARGIN_X, topoTotal, PAGE_WIDTH - MARGIN_X, topoTotal + alturaBoxTotal)
        val totalBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_BRAND }
        writer.canvas.drawRoundRect(totalRect, 8f, 8f, totalBgPaint)

        val tituloTotalPaint = criarTextPaint(10.5f, Color.parseColor("#FCEAEA"), bold = true)
        val valorTotalPaint = criarTextPaint(23f, Color.WHITE, bold = true)
        val legendaTotalPaint = criarTextPaint(8.5f, Color.parseColor("#FBE2E2"), bold = false)

        writer.canvas.drawText("Valor total do orçamento", totalRect.left + 16f, topoTotal + 21f, tituloTotalPaint)
        writer.canvas.drawText(
            Formatadores.formatarMoeda(fin.valorFinalSalvo),
            totalRect.left + 16f,
            topoTotal + 47f,
            valorTotalPaint,
        )
        val legendaFinal = if (ajuste != null) {
            "Valor final com ajuste comercial aplicado."
        } else {
            "Valor final sujeito à confirmação de disponibilidade de agenda."
        }
        writer.canvas.drawText(legendaFinal, totalRect.left + 16f, topoTotal + 62f, legendaTotalPaint)

        writer.avancar(alturaBoxTotal + 10f)
    }

    // =============================================================================================
    // Auxiliares de dados e desenho
    // =============================================================================================

    private data class LinhaMaterialPdf(
        val descricao: String,
        val quantidadeTexto: String,
        val destaqueQuantidade: Boolean,
    )

    private fun selecionarItensParaTabelaCliente(orcamento: Orcamento): List<LinhaMaterialPdf> {
        val isReforma = orcamento.tipoServico == TipoServico.REFORMA
        val categoriasEstruturais = setOf(
            CategoriaMaterial.MADEIRA,
            CategoriaMaterial.FOLHA,
            CategoriaMaterial.MOLA,
            CategoriaMaterial.PE,
        )

        return orcamento.itens.mapNotNull { item: ItemMaterialOrcamento ->
            when {
                item.quantidade > 0.0 -> LinhaMaterialPdf(
                    descricao = item.descricao,
                    quantidadeTexto = "${Formatadores.formatarQuantidade(item.quantidade)} ${item.unidade}",
                    destaqueQuantidade = true,
                )
                isReforma && item.categoria in categoriasEstruturais -> LinhaMaterialPdf(
                    descricao = item.descricao,
                    quantidadeTexto = "Original mantido",
                    destaqueQuantidade = false,
                )
                else -> null
            }
        }
    }

    private fun formatarDimensoes(orcamento: Orcamento): String {
        val pecas = orcamento.sofa.pecas
        if (pecas.isEmpty()) return "Não informadas"
        if (pecas.size == 1) {
            val p = pecas.first()
            return "${Formatadores.formatarQuantidade(p.larguraCm)} × ${Formatadores.formatarQuantidade(p.profundidadeCm)} × ${Formatadores.formatarQuantidade(p.alturaCm)} cm"
        }
        return pecas.joinToString("\n") { p ->
            "Peça ${p.ordem}: ${Formatadores.formatarQuantidade(p.larguraCm)} × ${Formatadores.formatarQuantidade(p.profundidadeCm)} × ${Formatadores.formatarQuantidade(p.alturaCm)} cm"
        }
    }

    @DrawableRes
    private fun drawableParaModelo(modelo: ModeloSofa): Int = when (modelo) {
        ModeloSofa.FIXO -> R.drawable.img_sofa_fixo
        ModeloSofa.RETRATIL -> R.drawable.img_sofa_retratil
        ModeloSofa.MODULAR -> R.drawable.img_sofa_modular
    }

    private fun desenharLogoEstofados(
        canvas: Canvas,
        left: Int,
        top: Int,
        width: Int,
        height: Int,
    ) {
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_logo_estofados)?.mutate() ?: return
        DrawableCompat.setTint(drawable, COLOR_BRAND)
        drawable.setBounds(left, top, left + width, top + height)
        drawable.draw(canvas)
    }

    private fun desenharImagemCentralizada(
        canvas: Canvas,
        @DrawableRes drawableRes: Int,
        area: RectF,
    ) {
        val bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, drawableRes) ?: return
        try {
            val scaleX = area.width() / bitmap.width.toFloat()
            val scaleY = area.height() / bitmap.height.toFloat()
            val scale = minOf(scaleX, scaleY)
            val destW = bitmap.width * scale
            val destH = bitmap.height * scale
            val left = area.centerX() - (destW / 2f)
            val top = area.centerY() - (destH / 2f)
            val destino = RectF(left, top, left + destW, top + destH)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            canvas.drawBitmap(bitmap, Rect(0, 0, bitmap.width, bitmap.height), destino, paint)
        } finally {
            bitmap.recycle()
        }
    }

    private fun desenharTituloSecao(writer: PdfPageWriter, titulo: String) {
        val paint = criarTextPaint(13f, COLOR_TEXT_PRIMARY, bold = true)
        writer.canvas.drawText(titulo, MARGIN_X, writer.cursorY + 13f, paint)
        writer.avancar(20f)
    }

    private fun desenharCard(canvas: Canvas, rect: RectF) {
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_CARD }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_BORDER
            style = Paint.Style.STROKE
            strokeWidth = 0.9f
        }
        canvas.drawRoundRect(rect, 7f, 7f, bgPaint)
        canvas.drawRoundRect(rect, 7f, 7f, borderPaint)
    }

    private fun desenharCampoInfo(
        canvas: Canvas,
        label: String,
        value: String,
        x: Float,
        yLabel: Float,
        yValue: Float,
    ) {
        val labelPaint = criarTextPaint(8.5f, COLOR_TEXT_SECONDARY, bold = false)
        val valuePaint = criarTextPaint(10.5f, COLOR_TEXT_PRIMARY, bold = true)
        canvas.drawText(label, x, yLabel, labelPaint)
        canvas.drawText(value, x, yValue, valuePaint)
    }

    private fun criarTextPaint(sizePt: Float, color: Int, bold: Boolean): TextPaint =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = sizePt
            this.color = color
            typeface = Typeface.create(Typeface.SANS_SERIF, if (bold) Typeface.BOLD else Typeface.NORMAL)
        }

    private fun criarStaticLayout(texto: String, paint: TextPaint, widthPx: Int): StaticLayout =
        StaticLayout.Builder.obtain(texto, 0, texto.length, paint, widthPx.coerceAtLeast(40))
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(2f, 1.0f)
            .setIncludePad(false)
            .build()

    /**
     * Controlador simples de paginação A4: abre nova página automaticamente quando o conteúdo
     * seguinte ultrapassa a margem inferior, evitando cortes em orçamentos longos.
     */
    private class PdfPageWriter(private val document: PdfDocument) {
        private var pageNumber = 0
        private var currentPage: PdfDocument.Page? = null
        var cursorY: Float = MARGIN_TOP
            private set

        val canvas: Canvas
            get() = checkNotNull(currentPage).canvas

        init {
            novaPagina()
        }

        fun espacoRestante(): Float = (PAGE_HEIGHT - MARGIN_BOTTOM) - cursorY

        fun garantirEspaco(alturaNecessaria: Float) {
            if (espacoRestante() < alturaNecessaria && cursorY > MARGIN_TOP + 1f) {
                novaPagina()
            }
        }

        fun avancar(deltaY: Float) {
            cursorY += deltaY
        }

        fun novaPagina() {
            currentPage?.let { document.finishPage(it) }
            pageNumber += 1
            val info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            val page = document.startPage(info)
            page.canvas.drawColor(COLOR_BG_PAGE)
            currentPage = page
            cursorY = MARGIN_TOP
        }

        fun finalizar() {
            currentPage?.let {
                document.finishPage(it)
                currentPage = null
            }
        }
    }
}

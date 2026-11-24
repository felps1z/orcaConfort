package com.orcaconfort.app.core.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.round

/**
 * Utilitários transversais de formatação e conversão (moeda pt-BR, quantidades e datas).
 *
 * Mantido em `core/util` sem dependências de `ui`, `domain` ou `data`,
 * conforme definido em `docs/ARCHITECTURE.md`.
 */
object Formatadores {

    private val localePtBr = Locale.forLanguageTag("pt-BR")

    private val simbolosPtBr = DecimalFormatSymbols(localePtBr).apply {
        decimalSeparator = ','
        groupingSeparator = '.'
    }

    /** Formata um valor em Reais com símbolo. Ex.: `1850.0` -> `"R$ 1.850,00"`. */
    fun formatarMoeda(valor: Double): String {
        val negativo = valor < -0.005
        val formatado = formatarValorSemSimbolo(abs(valor))
        return if (negativo) "- R$ $formatado" else "R$ $formatado"
    }

    /** Formata um valor decimal com duas casas e separador brasileiro. Ex.: `1850.0` -> `"1.850,00"`. */
    fun formatarValorSemSimbolo(valor: Double): String {
        val formato = DecimalFormat("#,##0.00", simbolosPtBr)
        return formato.format(valor)
    }

    /**
     * Formata uma quantidade de material para exibição limpa.
     * Ex.: `12.0` -> `"12"`, `1.5` -> `"1,5"`, `1.47` -> `"1,47"`.
     */
    fun formatarQuantidade(valor: Double): String {
        val arredondado = arredondar2Casas(valor)
        val formato = DecimalFormat("#.##", simbolosPtBr)
        return formato.format(arredondado)
    }

    /**
     * Converte um texto digitado pelo usuário (pt-BR ou ponto decimal) para [Double].
     * Aceita formatos como `"1.850,50"`, `"45,00"`, `"45.00"` ou `"12"`.
     * Retorna `0.0` se vazio ou inválido.
     */
    fun parseNumeroPtBr(texto: String): Double {
        val limpo = texto.trim()
            .replace("R$", "")
            .replace(" ", "")
        if (limpo.isEmpty()) return 0.0

        val normalizado = when {
            limpo.contains(',') && limpo.contains('.') -> {
                // Ex.: "1.850,50" -> remove separador de milhar e troca vírgula por ponto
                limpo.replace(".", "").replace(",", ".")
            }
            limpo.contains(',') -> {
                // Ex.: "45,50" -> "45.50"
                limpo.replace(",", ".")
            }
            else -> limpo
        }
        return normalizado.toDoubleOrNull() ?: 0.0
    }

    /** Arredonda para 2 casas decimais evitando ruído de ponto flutuante. */
    fun arredondar2Casas(valor: Double): Double = round(valor * 100.0) / 100.0

    /** Formata um timestamp (ms) para `"dd/MM/yyyy"`. Retorna `"Não informada"` se nulo. */
    fun formatarData(timestampMillis: Long?): String {
        if (timestampMillis == null || timestampMillis <= 0L) return "Não informada"
        val formatter = SimpleDateFormat("dd/MM/yyyy", localePtBr)
        return formatter.format(Date(timestampMillis))
    }

    /**
     * Converte uma data no formato `"dd/MM/yyyy"` para timestamp em milissegundos.
     * Retorna `null` se o texto estiver vazio ou fora do formato esperado.
     */
    fun parseDataParaTimestamp(dataTexto: String): Long? {
        val textoLimpo = dataTexto.trim()
        if (textoLimpo.length != 10) return null
        return try {
            val formatter = SimpleDateFormat("dd/MM/yyyy", localePtBr).apply {
                isLenient = false
            }
            formatter.parse(textoLimpo)?.time
        } catch (_: Exception) {
            null
        }
    }

    /** Formata o ID numérico do orçamento no padrão visual do app (ex.: `24` -> `"#024"`). */
    fun formatarCodigoOrcamento(id: Long): String {
        if (id <= 0L) return "#NOVO"
        return "#${id.toString().padStart(3, '0')}"
    }
}

package com.orcaconfort.app.ui.components

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.orcaconfort.app.core.util.Formatadores
import com.orcaconfort.app.domain.usecase.PdfOrcamentoGerado
import com.orcaconfort.app.ui.theme.BorderDark
import com.orcaconfort.app.ui.theme.PrimaryBlack
import com.orcaconfort.app.ui.theme.SurfaceWhite
import com.orcaconfort.app.ui.theme.TextPrimary
import com.orcaconfort.app.ui.theme.TextSecondary
import java.io.File

/** Opção de aplicativo WhatsApp instalada no dispositivo. */
enum class OpcaoWhatsApp(val pacote: String, val titulo: String) {
    WHATSAPP("com.whatsapp", "WhatsApp"),
    WHATSAPP_BUSINESS("com.whatsapp.w4b", "WhatsApp Business"),
}

/**
 * Utilitário responsável por expor o PDF via [FileProvider] e abrir o WhatsApp
 * (ou o seletor nativo de compartilhamento do Android caso nenhum WhatsApp esteja instalado).
 */
object CompartilhamentoPdfHelper {

    fun detectarWhatsAppsInstalados(context: Context): List<OpcaoWhatsApp> {
        val pm = context.packageManager
        return OpcaoWhatsApp.entries.filter { opcao ->
            isPacoteInstalado(pm, opcao.pacote)
        }
    }

    /**
     * Decide o envio imediato ou solicita seleção quando WhatsApp e WhatsApp Business
     * estão ambos instalados no aparelho.
     */
    fun enviarOuSelecionarWhatsApp(
        context: Context,
        pdf: PdfOrcamentoGerado,
        onSolicitarEscolha: (List<OpcaoWhatsApp>) -> Unit,
        onErro: (String) -> Unit,
    ) {
        val instalados = detectarWhatsAppsInstalados(context)
        when {
            instalados.size > 1 -> onSolicitarEscolha(instalados)
            instalados.size == 1 -> enviarParaPacote(
                context = context,
                pdf = pdf,
                pacoteDestino = instalados.first().pacote,
                onErro = onErro,
            )
            else -> enviarParaPacote(
                context = context,
                pdf = pdf,
                pacoteDestino = null,
                onErro = onErro,
            )
        }
    }

    fun enviarParaPacote(
        context: Context,
        pdf: PdfOrcamentoGerado,
        pacoteDestino: String?,
        onErro: (String) -> Unit,
    ) {
        val arquivo = File(pdf.caminhoArquivo)
        if (!arquivo.exists()) {
            onErro("Arquivo PDF não encontrado para compartilhamento.")
            return
        }

        val uri = try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                arquivo,
            )
        } catch (e: Exception) {
            onErro("Não foi possível preparar o PDF para envio.")
            return
        }

        val codigo = Formatadores.formatarCodigoOrcamento(pdf.idOrcamento)
        val nomeCliente = pdf.nomeCliente.ifBlank { "cliente" }
        val nomeNegocio = pdf.nomeNegocio.ifBlank { "nossa tapeçaria" }
        val mensagem = "Olá, $nomeCliente! Segue o orçamento $codigo de $nomeNegocio."

        val intentEnvio = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, mensagem)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (pacoteDestino != null) {
                setPackage(pacoteDestino)
            }
        }

        try {
            if (pacoteDestino != null) {
                context.startActivity(intentEnvio)
            } else {
                val chooser = Intent.createChooser(intentEnvio, "Enviar orçamento em PDF")
                context.startActivity(chooser)
            }
        } catch (_: Exception) {
            // Fallback seguro para o seletor do sistema caso o pacote direto falhe
            try {
                intentEnvio.setPackage(null)
                context.startActivity(Intent.createChooser(intentEnvio, "Enviar orçamento em PDF"))
            } catch (_: Exception) {
                onErro("Nenhum aplicativo compatível encontrado para compartilhar o PDF.")
            }
        }
    }

    private fun isPacoteInstalado(pm: PackageManager, pacote: String): Boolean =
        try {
            pm.getPackageInfo(pacote, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
}

/**
 * Diálogo exibido quando o aparelho possui mais de um WhatsApp (pessoal e Business)
 * para que o tapeceiro selecione qual deseja utilizar no envio do PDF.
 */
@Composable
fun DialogoEscolhaWhatsApp(
    opcoes: List<OpcaoWhatsApp>,
    onSelecionarWhatsApp: (OpcaoWhatsApp) -> Unit,
    onCompartilharOutros: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        title = {
            Text(
                text = "Enviar PDF do orçamento",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Escolha em qual WhatsApp deseja selecionar o contato para envio:",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp,
                )
                opcoes.forEach { opcao ->
                    Button(
                        onClick = { onSelecionarWhatsApp(opcao) },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlack,
                            contentColor = SurfaceWhite,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = opcao.titulo,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                OutlinedButton(
                    onClick = onCompartilharOutros,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Outros aplicativos",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, BorderDark),
                shape = RoundedCornerShape(6.dp),
            ) {
                Text("Cancelar", fontSize = 13.sp, color = TextPrimary)
            }
        },
    )
}

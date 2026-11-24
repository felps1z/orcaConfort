package com.orcaconfort.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.orcaconfort.app.domain.model.ModeloSofa
import com.orcaconfort.app.domain.model.StatusOrcamento
import com.orcaconfort.app.domain.model.TipoServico

/**
 * Tabela 2 (`quotes`): Cabeçalho, especificações e resumo financeiro do orçamento.
 *
 * Organizada em subblocos `@Embedded` correspondentes às etapas do Wizard para facilitar
 * a adição de novos campos no futuro sem desorganizar o esquema.
 */
@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey(autoGenerate = true)
    val quoteId: Long = 0L,

    val createdAt: Long = System.currentTimeMillis(),
    val status: StatusOrcamento = StatusOrcamento.PENDENTE,
    val serviceType: TipoServico,

    @Embedded(prefix = "client_")
    val client: ClientData,

    @Embedded(prefix = "sofa_")
    val sofa: SofaSpecifications,

    @Embedded(prefix = "reform_")
    val reformChecklist: ReformChecklist,

    @Embedded(prefix = "fin_")
    val financials: FinancialData,
)

/** Bloco Etapa 1: Dados do Cliente. Para incluir CPF/endereço no futuro, adicione aqui com `defaultValue`. */
data class ClientData(
    val name: String,
    @ColumnInfo(defaultValue = "")
    val phone: String = "",
    val deliveryDate: Long? = null,
    @ColumnInfo(defaultValue = "")
    val observations: String = "",
)

/**
 * Bloco Etapas 2 e 3: Modelo, acabamento e resumo de medidas.
 *
 * As dimensões individuais de cada peça ficam em [QuotePieceEntity] (`quote_pieces`),
 * enquanto `pieceCount`, `widthCm` (largura total), `depthCm` e `heightCm` mantêm
 * acesso direto para listagens rápidas e compatibilidade com a modelagem original.
 */
data class SofaSpecifications(
    val model: ModeloSofa,
    @ColumnInfo(defaultValue = "1")
    val pieceCount: Int = 1,
    @ColumnInfo(defaultValue = "0")
    val hasPillows: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val pillowCount: Int = 0,
    @ColumnInfo(defaultValue = "")
    val selectedFabricName: String = "",
    @ColumnInfo(defaultValue = "0.0")
    val selectedFabricPrice: Double = 0.0,
    @ColumnInfo(defaultValue = "")
    val fabricColor: String = "",
    @ColumnInfo(defaultValue = "0.0")
    val widthCm: Double = 0.0,
    @ColumnInfo(defaultValue = "0.0")
    val depthCm: Double = 0.0,
    @ColumnInfo(defaultValue = "0.0")
    val heightCm: Double = 0.0,
)

/** Bloco Etapa 3: Checklist condicional de substituição (somente para `REFORMA`). */
data class ReformChecklist(
    @ColumnInfo(defaultValue = "0")
    val replaceWood: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val replaceFoam: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val replaceSprings: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val replaceWebbing: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val replaceSheets: Boolean = false,
)

/** Bloco Etapas 4 e 5: Composição financeira e sobrescrita manual. */
data class FinancialData(
    @ColumnInfo(defaultValue = "0.0")
    val materialsSubtotal: Double = 0.0,
    @ColumnInfo(defaultValue = "10.0")
    val extraMarginPercent: Double = 10.0,
    @ColumnInfo(defaultValue = "0.0")
    val extraMarginValue: Double = 0.0,
    @ColumnInfo(defaultValue = "200.0")
    val dailyRate: Double = 200.0,
    @ColumnInfo(defaultValue = "1.0")
    val workDays: Double = 1.0,
    @ColumnInfo(defaultValue = "0.0")
    val laborTotal: Double = 0.0,
    @ColumnInfo(defaultValue = "0.0")
    val freightCost: Double = 0.0,
    @ColumnInfo(defaultValue = "0.0")
    val calculatedTotal: Double = 0.0,
    val manualOverrideTotal: Double? = null,
    @ColumnInfo(defaultValue = "0.0")
    val finalSavedValue: Double = 0.0,
)

package com.orcaconfort.app.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.orcaconfort.app.domain.model.CategoriaMaterial

/**
 * Pré-população inicial do banco (Seção 6.3 do Documento de Modelagem Room).
 *
 * Executada de forma síncrona dentro de [onCreate] na primeira criação do arquivo SQLite,
 * garantindo que a Tabela de Preços e o Perfil da Tapeçaria já estejam prontos antes da
 * primeira leitura das telas.
 */
class DatabaseSeedCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        val agora = System.currentTimeMillis()

        // 1. Perfil padrão da tapeçaria (Singleton id = 1)
        db.execSQL(
            """
            INSERT OR IGNORE INTO workshop_profile
                (id, businessName, phone, address, instagram, defaultDailyRate, defaultExtraMarginPercent)
            VALUES
                (1, 'OrçaConfort Tapeçaria', '', '', '', 200.0, 10.0)
            """.trimIndent(),
        )

        // 2. Catálogo inicial de materiais (Tabela de Preços)
        val materiaisIniciais = listOf(
            // Tecidos (largura padrão 1,40 m)
            SeedMaterial(CategoriaMaterial.TECIDO, "Linho", "m", 45.00, "Largura padrão 1,40m"),
            SeedMaterial(CategoriaMaterial.TECIDO, "Suede liso", "m", 32.00, "Largura padrão 1,40m"),
            SeedMaterial(CategoriaMaterial.TECIDO, "Veludo", "m", 58.00, "Largura padrão 1,40m"),

            // Espumas (lâmina 1,90 m x 3,00 m)
            SeedMaterial(CategoriaMaterial.ESPUMA, "Espuma 1cm", "m", 6.00, "Lâmina 1,90m × 3,00m"),
            SeedMaterial(CategoriaMaterial.ESPUMA, "Espuma 2cm", "m", 5.00, "Lâmina 1,90m × 3,00m"),
            SeedMaterial(CategoriaMaterial.ESPUMA, "Espuma 3cm", "m", 5.00, "Lâmina 1,90m × 3,00m"),
            SeedMaterial(CategoriaMaterial.ESPUMA, "Espuma 5cm", "m", 12.00, "Lâmina 1,90m × 3,00m"),
            SeedMaterial(CategoriaMaterial.ESPUMA, "Espuma 10cm", "m", 22.00, "Lâmina 1,90m × 3,00m"),

            // Fibra siliconada
            SeedMaterial(CategoriaMaterial.FIBRA, "Fibra siliconada", "kg", 28.00, null),

            // Percinta elástica
            SeedMaterial(CategoriaMaterial.PERCINTA, "Percinta elástica", "m", 6.00, null),

            // Madeira estrutural (Tábua 3,00 m x 0,15 m — RN03 / Seção 2.5)
            SeedMaterial(CategoriaMaterial.MADEIRA, "Tábuas de eucalipto", "un.", 18.00, "Tábua 3,00m × 0,15m"),

            // Chapas / Compensado (Folha bloco 2,20 m x 1,60 m — RN03)
            SeedMaterial(CategoriaMaterial.FOLHA, "Chapa compensado", "folha", 65.00, "Folha 2,20m × 1,60m"),

            // Pés e Molas
            SeedMaterial(CategoriaMaterial.PE, "Pé de madeira", "un.", 15.00, null),
            SeedMaterial(CategoriaMaterial.MOLA, "Mola espiral", "un.", 12.00, null),
        )

        val sql = """
            INSERT INTO materials_catalog
                (category, name, unit, unitPrice, lastUpdated, specification, isActive)
            VALUES (?, ?, ?, ?, ?, ?, 1)
        """.trimIndent()

        materiaisIniciais.forEach { item ->
            db.execSQL(
                sql,
                arrayOf<Any?>(
                    item.categoria.name,
                    item.nome,
                    item.unidade,
                    item.precoUnitario,
                    agora,
                    item.especificacao,
                ),
            )
        }
    }

    private data class SeedMaterial(
        val categoria: CategoriaMaterial,
        val nome: String,
        val unidade: String,
        val precoUnitario: Double,
        val especificacao: String?,
    )
}

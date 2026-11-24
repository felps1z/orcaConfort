package com.orcaconfort.app.data.local.converter

import androidx.room.TypeConverter
import com.orcaconfort.app.domain.model.CategoriaMaterial
import com.orcaconfort.app.domain.model.ModeloSofa
import com.orcaconfort.app.domain.model.StatusOrcamento
import com.orcaconfort.app.domain.model.TipoServico

/**
 * TypeConverters do Room para persistir enums de domínio como `String` no SQLite.
 *
 * Vantagem para manutenção: novos valores adicionados nos enums não exigem
 * alteração estrutural (ALTER TABLE) no banco de dados.
 */
class OrcaConfortTypeConverters {

    @TypeConverter
    fun fromCategoriaMaterial(valor: CategoriaMaterial): String = valor.name

    @TypeConverter
    fun toCategoriaMaterial(valor: String): CategoriaMaterial =
        CategoriaMaterial.fromNomeSeguro(valor)

    @TypeConverter
    fun fromTipoServico(valor: TipoServico): String = valor.name

    @TypeConverter
    fun toTipoServico(valor: String): TipoServico =
        TipoServico.fromNomeSeguro(valor)

    @TypeConverter
    fun fromModeloSofa(valor: ModeloSofa): String = valor.name

    @TypeConverter
    fun toModeloSofa(valor: String): ModeloSofa =
        ModeloSofa.fromNomeSeguro(valor)

    @TypeConverter
    fun fromStatusOrcamento(valor: StatusOrcamento): String = valor.name

    @TypeConverter
    fun toStatusOrcamento(valor: String): StatusOrcamento =
        StatusOrcamento.fromNomeSeguro(valor)
}

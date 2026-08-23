/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2026 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.t8rin.imagetoolbox.core.data.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import com.t8rin.imagetoolbox.core.domain.image.editing.EditValue

/**
 * Stable JSON representation for Reference Lab operation parameters.
 *
 * The explicit type tag prevents Moshi/JSON's generic number handling from
 * turning recipe integers into doubles and makes future schema migrations
 * deterministic.
 */
internal class EditValueJsonAdapter {

    @ToJson
    fun toJson(value: EditValue): Map<String, Any?> = when (value) {
        is EditValue.Decimal -> mapOf(
            TYPE to DECIMAL,
            VALUE to value.value
        )

        is EditValue.Integer -> mapOf(
            TYPE to INTEGER,
            VALUE to value.value
        )

        is EditValue.Flag -> mapOf(
            TYPE to FLAG,
            VALUE to value.value
        )

        is EditValue.Text -> mapOf(
            TYPE to TEXT,
            VALUE to value.value
        )

        is EditValue.ListValue -> mapOf(
            TYPE to LIST,
            VALUE to value.value.map(::toJson)
        )

        is EditValue.ObjectValue -> mapOf(
            TYPE to OBJECT,
            VALUE to value.value.mapValues { (_, child) -> toJson(child) }
        )

        EditValue.Null -> mapOf(TYPE to NULL)
    }

    @FromJson
    fun fromJson(json: Map<String, Any?>): EditValue {
        val type = json[TYPE] as? String
            ?: throw IllegalArgumentException("EditValue JSON is missing '$TYPE'")

        return when (type) {
            DECIMAL -> EditValue.Decimal(
                (json[VALUE] as? Number)?.toDouble()
                    ?: throw IllegalArgumentException("Decimal EditValue is missing numeric '$VALUE'")
            )

            INTEGER -> EditValue.Integer(
                (json[VALUE] as? Number)?.toLong()
                    ?: throw IllegalArgumentException("Integer EditValue is missing numeric '$VALUE'")
            )

            FLAG -> EditValue.Flag(
                json[VALUE] as? Boolean
                    ?: throw IllegalArgumentException("Flag EditValue is missing boolean '$VALUE'")
            )

            TEXT -> EditValue.Text(
                json[VALUE] as? String
                    ?: throw IllegalArgumentException("Text EditValue is missing string '$VALUE'")
            )

            LIST -> EditValue.ListValue(
                (json[VALUE] as? List<*>)
                    ?.map { child ->
                        @Suppress("UNCHECKED_CAST")
                        fromJson(
                            child as? Map<String, Any?>
                                ?: throw IllegalArgumentException("List EditValue contains invalid child")
                        )
                    }
                    ?: throw IllegalArgumentException("List EditValue is missing list '$VALUE'")
            )

            OBJECT -> EditValue.ObjectValue(
                (json[VALUE] as? Map<*, *>)
                    ?.map { (key, child) ->
                        val stringKey = key as? String
                            ?: throw IllegalArgumentException("Object EditValue contains non-string key")
                        @Suppress("UNCHECKED_CAST")
                        stringKey to fromJson(
                            child as? Map<String, Any?>
                                ?: throw IllegalArgumentException("Object EditValue contains invalid child")
                        )
                    }
                    ?.toMap()
                    ?: throw IllegalArgumentException("Object EditValue is missing object '$VALUE'")
            )

            NULL -> EditValue.Null
            else -> throw IllegalArgumentException("Unknown EditValue type '$type'")
        }
    }

    private companion object {
        const val TYPE = "type"
        const val VALUE = "value"

        const val DECIMAL = "decimal"
        const val INTEGER = "integer"
        const val FLAG = "flag"
        const val TEXT = "text"
        const val LIST = "list"
        const val OBJECT = "object"
        const val NULL = "null"
    }
}

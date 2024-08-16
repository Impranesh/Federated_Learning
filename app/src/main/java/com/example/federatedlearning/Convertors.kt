package com.example.federatedlearning

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromFloatList(values: List<Float>): String {
        return values.joinToString(separator = ",")
    }

    @TypeConverter
    fun toFloatList(data: String): List<Float> {
        return data.split(",").map { it.toFloat() }
    }
}

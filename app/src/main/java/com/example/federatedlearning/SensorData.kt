package com.example.federatedlearning

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SensorData(
    val sensorName: String,
    @PrimaryKey
    val timestamp: String,
    val values: List<Float>
)


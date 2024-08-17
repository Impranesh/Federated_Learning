package com.example.federatedlearning

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SensorData(
    @PrimaryKey (autoGenerate = true) val id:Long =0,
    val sensorName: String,
    val timestamp: String,
    val values: List<Float>
)


package com.example.federatedlearning

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [SensorData::class], version = 1 )
@TypeConverters(Converters::class)
abstract class SensorDataDatabase : RoomDatabase(){
    abstract fun sensorDAO() : SensorDataDAO
}


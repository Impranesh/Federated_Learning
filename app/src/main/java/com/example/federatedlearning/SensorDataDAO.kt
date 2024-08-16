package com.example.federatedlearning

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SensorDataDAO {

    @Insert
    fun save(data: SensorData)

    @Query("SELECT * FROM SensorData WHERE sensorName=:name")
    fun getSensorData(name: String) : List<SensorData>
    @Query("DELETE FROM SensorData WHERE sensorName = :name")
    fun deleteSensorDataByName(name: String)

    @Query("SELECT * FROM SensorData WHERE sensorName = 'accelerometer'")
    fun getAllAccelerometerData(): List<SensorData>

    @Query("SELECT * FROM SensorData WHERE sensorName = 'gyroscope'")
    fun getAllGyroscopeData(): List<SensorData>

    @Query("SELECT * FROM SensorData WHERE sensorName = :name AND timestamp = :timestamp")
    fun getSensorDataByTimestamp(name: String, timestamp: String): SensorData?

}
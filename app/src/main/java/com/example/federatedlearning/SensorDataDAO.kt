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

    //  Fetch a batch of accelerometer data
    @Query("SELECT * FROM SensorData WHERE sensorName = 'accelerometer' LIMIT :limit")
    fun getAccelerometerDataBatch(limit: Int): List<SensorData>

    // Fetch a batch of gyroscope data
    @Query("SELECT * FROM SensorData WHERE sensorName = 'gyroscope' LIMIT :limit")
    fun getGyroscopeDataBatch(limit: Int): List<SensorData>
    @Query("SELECT * FROM SensorData WHERE sensorName = :sensorName LIMIT :limit")
    fun getSensorDataBatch(sensorName: String, limit: Int): List<SensorData>


    // Delete a batch of sensor data based on their IDs
    @Query("DELETE FROM SensorData WHERE id IN (:ids)")
    fun deleteSensorDataBatch(ids: List<Long>)
}
package com.example.federatedlearning

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.room.Dao
import androidx.room.Room
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import kotlinx.coroutines.*
import java.io.*
import java.util.*


class CsvDataStorage(private val context: Context) {

    private val accelerometerDir: File = File(context.filesDir, "accelerometer")
    private val gyroscopeDir: File = File(context.filesDir, "gyroscope")
    private val dataQueue = LinkedBlockingQueue<SensorData>()
    private val handler = Handler(Looper.getMainLooper())

    val db = Room.databaseBuilder(
        context,
        SensorDataDatabase::class.java, "sensor-data"
    ).build()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    init {
        // Create directories if they don't exist
        if (!accelerometerDir.exists()) accelerometerDir.mkdir()
        if (!gyroscopeDir.exists()) gyroscopeDir.mkdir()
        startDataWriter()
    }

    private fun startDataWriter() {
        scope.launch {
            val dao = db.sensorDAO()
//            val buffer = mutableListOf<SensorData>()
            while (isActive) {
                try {
                    val data = dataQueue.poll(1000, java.util.concurrent.TimeUnit.MILLISECONDS)
                    if (data != null) dao.save(data)
                } catch (e: Exception) {
                    handleError(e)
                }
            }
        }
    }

    fun queueSensorData(sensorData: SensorData) {
        dataQueue.offer(sensorData) // Non-blocking call to add data to the queue
    }

    private fun handleError(e: Exception) {
        e.printStackTrace()
        handler.post {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

package com.example.federatedlearning

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.room.Room
import com.opencsv.CSVWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit


class CsvDataStorage(private val context: Context) {

    private val accelerometerDir: File = File(context.filesDir, "accelerometer")
    private val gyroscopeDir: File = File(context.filesDir, "gyroscope")
    private val dataQueue = LinkedBlockingQueue<SensorData>()
    private val handler = Handler(Looper.getMainLooper())
    private val bufferSize=8*1024
    private val batchSize=1000

    //RoomDatabase BUILDER

    private val db = Room.databaseBuilder(
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
                    val data = withContext(Dispatchers.IO) {
                        dataQueue.poll(1000, TimeUnit.MILLISECONDS)
                    }
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

     fun scheduleDataExport() {
        scope.launch {
            while (isActive) {
                delay(10_000) // 10 seconds delay
                exportDataToCSV()
            }
        }
         handler.post {
             Toast.makeText(context, "DataExported Successfully. ", Toast.LENGTH_SHORT).show()
         }
    }
    private fun exportDataToCSV() {
        scope.launch {
            val dao = db.sensorDAO()
            var hasMoreData = true
            while (hasMoreData) {
                // Fetch a batch of accelerometer data
                val accelerometerData = dao.getAccelerometerDataBatch(batchSize)
                if (accelerometerData.isNotEmpty()) {
                    val exportedIds =
                        exportToCsv(accelerometerData, accelerometerDir, "accelerometer_data.csv")
                    dao.deleteSensorDataBatch(exportedIds)
                }
                // Fetch a batch of gyroscope data
                val gyroscopeData = dao.getGyroscopeDataBatch(batchSize)
                if (gyroscopeData.isNotEmpty()) {
                    val exportedIds = exportToCsv(gyroscopeData, gyroscopeDir, "gyroscope_data.csv")
                    dao.deleteSensorDataBatch(exportedIds)
                }

                // Check if there is more data to process
                hasMoreData = accelerometerData.isNotEmpty() || gyroscopeData.isNotEmpty()
            }
        }
    }
    private fun exportToCsv(dataList: List<SensorData>, targetDir: File, fileName: String): List<Long> {
        val exportedIds = mutableListOf<Long>()
        if (dataList.isNotEmpty()) {
            val csvFile = File(targetDir, fileName)
            try {
                BufferedWriter(FileWriter(csvFile, true), bufferSize).use { fileWriter ->
                    CSVWriter(fileWriter).use { csvWriter ->
                        dataList.forEach { sensorData ->
                            csvWriter.writeNext(arrayOf(sensorData.timestamp, sensorData.values.joinToString(",")))
                            exportedIds.add(sensorData.id) // Track the exported ID
                        }
                    }
                }
            } catch (e: IOException) {
                handleError(e)
            }
        }
        return exportedIds // Return the list of IDs that were exported
    }


    private fun handleError(e: Exception) {
        e.printStackTrace()
        handler.post {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

package com.example.federatedlearning

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.federatedlearning.databinding.ActivityCsvDataBinding
import com.opencsv.CSVReader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CsvDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCsvDataBinding
    private val chunkSize=10000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_csv_data)
        binding = ActivityCsvDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.progressBar.visibility = View.GONE

        // Start loading data asynchronously
        loadCsvData()
    }
    private fun loadCsvData() {
        binding.progressBar.visibility=View.VISIBLE


        CoroutineScope(Dispatchers.IO).launch {
            try {
                val accelerometerData = readCsvFileInChunks(
                    File(filesDir, "accelerometer/accelerometer_data.csv"),
                    "accelerometer"
                )
                val gyroscopeData =
                    readCsvFileInChunks(File(filesDir, "gyroscope/gyroscope_data.csv"), "gyro")

                // Merge the data from both sensors
                val rawData = accelerometerData + gyroscopeData

                // Sort the data by timestamp in descending order
                val sortedDataList = rawData.sortedByDescending { parseTimestamp(it.timestamp) }

                // Convert data to display format
                val displayDataList = sortedDataList.map {
                    arrayOf(it.sensorName, it.timestamp, it.values.joinToString(","))
                }

                withContext(Dispatchers.Main) {
                    // Update UI on the main thread
                    val recyclerView: RecyclerView = binding.recyclerViewCsvData
                    recyclerView.layoutManager = LinearLayoutManager(this@CsvDataActivity)
                    recyclerView.adapter = CsvDataAdapter(displayDataList)
                        binding.progressBar.visibility = View.GONE  //Hide progressBar once data is loaded
                    binding.recyclerViewCsvData.visibility=View.VISIBLE
                }
            }catch (e:Exception){
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE // Hide progress bar in case of error
                }
            }
        }
    }
    private fun readCsvFileInChunks(file: File, sensorName: String): List<SensorData> {
        val sensorDataList = mutableListOf<SensorData>()

        if (file.exists()) {
            try {
                BufferedReader(FileReader(file)).use { reader ->
                    val csvReader = CSVReader(reader)
                    var nextLine: Array<String>?
                    var chunk = mutableListOf<SensorData>()

                    while (csvReader.readNext().also { nextLine = it } != null) {
                        // Create SensorData from each row
                        val sensorData = SensorData(
                            sensorName = sensorName,
                            timestamp = nextLine!![0],
                            values = nextLine!![1].split(",").map { value -> value.toFloatOrNull() ?: 0f }
                        )

                        chunk.add(sensorData)

                        // Process chunk if it reaches the defined size
                        if (chunk.size >= chunkSize) {
                            sensorDataList.addAll(chunk)
                            chunk.clear()
                        }
                    }

                    // Add remaining data
                    if (chunk.isNotEmpty()) {
                        sensorDataList.addAll(chunk)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }else{
            Log.e("CsvDataActivity", "File does not exist: ${file.absolutePath}")
        }

        return sensorDataList
    }

    // Helper function to parse the timestamp
    private fun parseTimestamp(timestamp: String): Date {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(timestamp) ?: Date()
    }
}


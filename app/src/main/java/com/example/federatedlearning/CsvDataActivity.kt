package com.example.federatedlearning

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.federatedlearning.databinding.ActivityCsvDataBinding
import com.opencsv.CSVReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CsvDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCsvDataBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCsvDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Read the data
        val accelerometerData = readCsvFile(File(filesDir, "accelerometer/accelerometer_data.csv"),"accelerometer")
        val gyroscopeData = readCsvFile(File(filesDir, "gyroscope/gyroscope_data.csv"),"gyro")

        //Merge the data from both sensors
        val rawData = accelerometerData + gyroscopeData

        // Convert raw CSV data to SensorData list

        // Sort the data by timestamp in descending order
        val sortedDataList = rawData.sortedByDescending { parseTimestamp(it.timestamp) }

        // Optionally convert back to Array<String> if needed for display
        val displayDataList = sortedDataList.map {
            arrayOf(it.sensorName, it.timestamp, it.values.joinToString(","))
        }

//        Log.d("CsvDataActivity", "Data size: ${displayDataList.size}")
//        displayDataList.forEach { Log.d("CsvDataActivity", "Data: ${it.joinToString()}") }


        val recyclerView: RecyclerView = binding.recyclerViewCsvData
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = CsvDataAdapter(displayDataList)
    }

    private fun readCsvFile(csvFile: File,sensorName: String): List<SensorData> {
//        val csvDataList = mutableListOf<Array<String>>()
        val sensorDataList = mutableListOf<SensorData>()

        if (csvFile.exists()) {
            try {
                FileReader(csvFile).use { fileReader ->
                    CSVReader(fileReader).use { csvReader ->
                        var nextLine: Array<String>?
                        while (csvReader.readNext().also { nextLine = it } != null) {
//                            csvDataList.add(nextLine!!)
                            //Create an object named SensorDataList
                            sensorDataList.add(
                                SensorData(
                                    sensorName = sensorName,
                                    timestamp = nextLine!![0],
                                    values = nextLine!![1].split(",").map { value -> value.toFloat() }
                                )
                            )
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return sensorDataList
    }

    // Helper function to parse the timestamp
    private fun parseTimestamp(timestamp: String): Date {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(timestamp) ?: Date()
    }
}


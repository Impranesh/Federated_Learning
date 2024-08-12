//// CsvDataActivity.kt
//package com.example.federatedlearning
//
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.federatedlearning.databinding.ActivityCsvDataBinding
//import com.opencsv.CSVReader
//import java.io.File
//import java.io.FileReader
//import java.io.IOException
//
//class CsvDataActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityCsvDataBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding = ActivityCsvDataBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        val csvDataList = readCsvFile()
//
//        val recyclerView: RecyclerView = binding.recyclerViewCsvData
//        recyclerView.layoutManager = LinearLayoutManager(this)
//        recyclerView.adapter = CsvDataAdapter(csvDataList)
//    }
//
//    private fun readCsvFile(): List<Array<String>> {
//        val csvFile = File(filesDir, "light_sensor_data.csv")
//        val csvDataList = mutableListOf<Array<String>>()
//
//        if (csvFile.exists()) {
//            try {
//                val fileReader = FileReader(csvFile)
//                val csvReader = CSVReader(fileReader)
//                var nextLine: Array<String>?
//                while (csvReader.readNext().also { nextLine = it } != null) {
//                    csvDataList.add(nextLine!!)
//                }
//                csvReader.close()
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }
//
//        return csvDataList
//    }
//}


package com.example.federatedlearning

import android.os.Bundle
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

        val rawData = readCsvFile()

        // Convert raw CSV data to SensorData list
        val sensorDataList = rawData.map {
            SensorData(
                sensorName = it[0],
                timestamp = it[1],
                values = it[2].split(",").map { value -> value.toFloat() }
            )
        }

        // Sort the data by timestamp in descending order
        val sortedDataList = sensorDataList.sortedByDescending { parseTimestamp(it.timestamp) }

        // Optionally convert back to Array<String> if needed
        val displayDataList = sortedDataList.map {
            arrayOf(it.sensorName, it.timestamp, it.values.joinToString(","))
        }

        val recyclerView: RecyclerView = binding.recyclerViewCsvData
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = CsvDataAdapter(displayDataList)
    }

    private fun readCsvFile(): List<Array<String>> {
        val csvFile = File(filesDir, "sensor_data.csv")
        val csvDataList = mutableListOf<Array<String>>()

        if (csvFile.exists()) {
            try {
                val fileReader = FileReader(csvFile)
                val csvReader = CSVReader(fileReader)
                var nextLine: Array<String>?
                while (csvReader.readNext().also { nextLine = it } != null) {
                    val sensorName=nextLine!![0];


                    csvDataList.add(nextLine!!)
                }
                csvReader.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return csvDataList
    }

    // Helper function to parse the timestamp
    private fun parseTimestamp(timestamp: String): Date {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(timestamp) ?: Date()
    }
}


package com.example.federatedlearning

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CsvDataStorage(private val context: Context) {

    private val accelerometerDir: File = File(context.filesDir, "accelerometer")
    private val gyroscopeDir: File = File(context.filesDir, "gyroscope")


    init {
        // Create directories if they don't exist
        if (!accelerometerDir.exists()) accelerometerDir.mkdir()
        if (!gyroscopeDir.exists()) gyroscopeDir.mkdir()
    }
    fun saveSensorData(sensorData: SensorData) {
        val (targetDir, fileName) = when (sensorData.sensorName) {
            "accelerometer" -> accelerometerDir to "accelerometer_data.csv"
            "gyroscope" -> gyroscopeDir to "gyroscope_data.csv"
            else -> return // Unsupported sensor type
        }

        val csvFile = File(targetDir, fileName)
        val existingData = readCsvFile(csvFile)

        // Find the most recent entry in the CSV file
        val mostRecentEntry = existingData.maxByOrNull { parseTimestamp(it[0]) }

        // Check if the new data is different from the most recent entry
        val isDifferent = mostRecentEntry == null || !valuesEqual(mostRecentEntry[1].split(",").map { it.toFloat() }, sensorData.values)

        if (isDifferent) {
            try {
                // Append new data to the CSV file
                FileWriter(csvFile, true).use { fileWriter ->
                    CSVWriter(fileWriter).use { csvWriter ->
                        csvWriter.writeNext(arrayOf(sensorData.timestamp, sensorData.values.joinToString(",")))
                    }
                }
                // Log the stored data
//                Log.d("CsvDataStorage", "Data saved: ${sensorData.sensorName}, ${sensorData.timestamp}, ${sensorData.values.joinToString(",")}")
//
//                // Show a Toast to confirm saving
//                Toast.makeText(context, "Data saved for ${sensorData.sensorName}", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                // Handle the error with a Toast message
                Toast.makeText(context, "Failed to save data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Log that the data was not different
//            Log.d("CsvDataStorage", "Data not saved as it is identical to the most recent entry.")
//            Toast.makeText(context, "Data not saved, as it is identical to the most recent entry for ${sensorData.sensorName}", Toast.LENGTH_SHORT).show()
        }
    }


    // Read CSV file and return list of data
    private fun readCsvFile(csvFile: File): List<Array<String>> {
        val csvDataList = mutableListOf<Array<String>>()
        if (csvFile.exists()) {
            try {
                FileReader(csvFile).use { fileReader ->
                    CSVReader(fileReader).use { csvReader ->
                        var nextLine: Array<String>?
                        while (csvReader.readNext().also { nextLine = it } != null) {
                            csvDataList.add(nextLine!!)
                        }
                    }
                }
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

    // Helper function to compare values
    private fun valuesEqual(existingValues: List<Float>, newValues: List<Float>): Boolean {
        if (existingValues.size != newValues.size) return false
        return existingValues.zip(newValues).all { (e, n) -> e == n }
    }
}

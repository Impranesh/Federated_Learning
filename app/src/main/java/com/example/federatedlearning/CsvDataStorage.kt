package com.example.federatedlearning

import android.content.Context
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

    private val csvFile: File = File(context.filesDir, "sensor_data.csv")

    // Save sensor data to CSV only if it's different from the most recent data
    fun saveSensorData(sensorData: SensorData) {

        //ReadCsvFile to get CSV file saved data

        val existingData = readCsvFile()

        // Find the most recent entry for the same sensor
        val mostRecentEntry = existingData.filter { it[0] == sensorData.sensorName }
            .maxByOrNull { parseTimestamp(it[1]) }

        // Check if the new data is different from the most recent entry
        val isDifferent = mostRecentEntry == null || !valuesEqual(mostRecentEntry[2].split(",").map { it.toFloat() }, sensorData.values)

        if (isDifferent) {
            try {
                FileWriter(csvFile, true).use { fileWriter ->
                    CSVWriter(fileWriter).use { csvWriter ->
                        csvWriter.writeNext(arrayOf(sensorData.sensorName, sensorData.timestamp, sensorData.values.joinToString(",")))
                    }
                }
                // Optionally show toast message after successfully saving data
                // Toast.makeText(context, "Data saved successfully", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                // Optionally show toast message if an error occurs
                // Toast.makeText(context, "Error saving data", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Optionally show toast message if data is already present
            // Toast.makeText(context, "Data already exists for this sensor and timestamp", Toast.LENGTH_SHORT).show()
        }
    }


    private fun saveCsvFile(dataList: List<Array<String>>) {
        try {
            FileWriter(csvFile, false).use { fileWriter ->
                CSVWriter(fileWriter).use { csvWriter ->
                    dataList.forEach { data ->
                        csvWriter.writeNext(data)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Error deleting data", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteSensorData(query: (SensorData) -> Boolean) {
        // Read the existing data
        val existingData = readCsvFile()

        // Convert raw CSV data to SensorData list
        val sensorDataList = existingData.map {
            SensorData(
                sensorName = it[0],
                timestamp = it[1],
                values = it[2].split(",").map { value -> value.toFloat() }
            )
        }

        // Filter out the data that matches the query
        val filteredDataList = sensorDataList.filterNot(query)

        // Convert back to Array<String> for saving to CSV
        val saveDataList = filteredDataList.map {
            arrayOf(it.sensorName, it.timestamp, it.values.joinToString(","))
        }

        // Save the filtered data back to the CSV file
        saveCsvFile(saveDataList)

        // Optionally show a toast message after deletion
        Toast.makeText(context, "Data deleted successfully", Toast.LENGTH_SHORT).show()
    }

    // Read CSV file and return list of data
    private fun readCsvFile(): List<Array<String>> {
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

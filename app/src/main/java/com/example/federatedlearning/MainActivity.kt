package com.example.federatedlearning

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.federatedlearning.databinding.ActivityMainBinding
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var gyroSensor: MeasurableSensor
    private lateinit var accelerometerSensor: MeasurableSensor
    private lateinit var csvDataStorage: CsvDataStorage
    private  var isListening: Boolean=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        csvDataStorage = CsvDataStorage(this)

        //lightSensor = LightSensor(this)
        gyroSensor = GyroSensor(this)
        accelerometerSensor = AccelerometerSensor(this)

        binding.buttonCSV.setOnClickListener {
            if(isListening){
                //lightSensor.stopListening()
                gyroSensor.stopListening()
                accelerometerSensor.stopListening()
                Toast.makeText(this, "Sensor listening stopped.",Toast.LENGTH_SHORT).show()
                isListening=false
                binding.buttonCSV.text="Start Data Collection"
            }else{
                //lightSensor.startListening()
                gyroSensor.startListening()
                accelerometerSensor.startListening()
                Toast.makeText(this, "Sensor listening started.", Toast.LENGTH_SHORT).show()
                isListening = true
                binding.buttonCSV.text = "Stop Data Collection"

                accelerometerSensor.onSensorValuesChanged = { values ->
                    if(values.size>=3) {
                        val x = values[0]
                        val y = values[1]
                        val z = values[2]
                        binding.accelerometer.text = "x:${x} y:${y} z:${z}"

                        val accelerometerSensorData = SensorData(
                            sensorName = "accelerometer",
                            timestamp = System.currentTimeMillis().toString(),
                            values = listOf(x, y, z)
                        )
                        saveSensorDataSafely(accelerometerSensorData)
                    }
                }

                gyroSensor.onSensorValuesChanged = { values ->
                    if(values.size>=3) {
                        val x = values[0]
                        val y = values[1]
                        val z = values[2]
                        binding.gyrometer.text = "x:${x} y:${y} z:${z}"
                        val gyroSensorData = SensorData(
                            sensorName = "gyroscope",
                            timestamp = System.currentTimeMillis().toString(),
                            values = listOf(x, y, z)
                        )
                        saveSensorDataSafely(gyroSensorData)
                    }
                }

            }
        }

        binding.buttonShowCsv.setOnClickListener {
            val intent = Intent(this, CsvDataActivity::class.java)
            startActivity(intent)
        }

    }
    private fun saveSensorDataSafely(sensorData: SensorData) {
        // Run data saving on a background thread
        Thread {
            try {
                csvDataStorage.queueSensorData(sensorData)
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Failed to save data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}
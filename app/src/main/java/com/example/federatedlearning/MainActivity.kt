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
    //private lateinit var lightSensor: MeasurableSensor
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
                    val x = values[0]
                    val y = values[1]
                    val z = values[2]
                    binding.textView.text = x.toString()
                    val accelerometerSensorData = SensorData(
                        sensorName = "accelerometer Sensor",
                        timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                        values = listOf(x,y,z)
                    )
                    //csvDataStorage.saveSensorData(accelerometerSensorData)
                }

                gyroSensor.onSensorValuesChanged = { values ->
                    val x = values[0]
                    val y = values[1]
                    val z = values[2]
                    //binding.textView.text = lightLevel.toString()
                    val gyroSensorData = SensorData(
                        sensorName = "Gyro Sensor",
                        timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                        values = listOf(x,y,z)
                    )
                    csvDataStorage.saveSensorData(gyroSensorData)
                }

            }
        }

        binding.buttonShowCsv.setOnClickListener {
            val intent = Intent(this, CsvDataActivity::class.java)
            startActivity(intent)
        }

    }
}

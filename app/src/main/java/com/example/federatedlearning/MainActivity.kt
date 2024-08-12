package com.example.federatedlearning

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.federatedlearning.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var lightSensor: MeasurableSensor
    private lateinit var csvDataStorage: CsvDataStorage
    private  var isListening: Boolean=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        csvDataStorage = CsvDataStorage(this)

        lightSensor = LightSensor(this)

        binding.buttonCSV.setOnClickListener {
            if(isListening){
                lightSensor.stopListening()
                Toast.makeText(this, "Sensor listening stopped.",Toast.LENGTH_SHORT).show()
                isListening=false
                binding.buttonCSV.text="Start Data Collection"
            }else{
                lightSensor.startListening()
                Toast.makeText(this, "Sensor listening started.", Toast.LENGTH_SHORT).show()
                isListening = true
                binding.buttonCSV.text = "Stop Data Collection"

                lightSensor.onSensorValuesChanged = { values ->
                    val lightLevel = values[0]
                    binding.textView.text = lightLevel.toString()


                    // for any sensor create object of data class SensorData

                    val sensorData = SensorData(
                        sensorName = "Light Sensor",
                        timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                        values = listOf(lightLevel)
                    )
                    csvDataStorage.saveSensorData(sensorData)


                }


            }
        }

        binding.buttonShowCsv.setOnClickListener {
            val intent = Intent(this, CsvDataActivity::class.java)
            startActivity(intent)
        }
    }
}

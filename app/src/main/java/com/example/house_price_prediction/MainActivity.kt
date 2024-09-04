package com.example.house_price_prediction

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MainActivity : AppCompatActivity() {

    lateinit var sv: ScrollView
    lateinit var interpreter: Interpreter

    var mean: FloatArray = floatArrayOf(
        -119.564154f,
        35.630318f,
        28.664505f,
        2622.235776f,
        535.281659f,
        1416.087055f,
        496.758167f,
        3.869337f,
        0.441454f,
        0.319405f,
        0.000306f,
        0.109874f,
        0.128961f
    )
    var std: FloatArray = floatArrayOf(
        2.002618f,
        2.138574f,
        12.556764f,
        2169.548287f,
        418.469078f,
        1103.842065f,
        379.109535f,
        1.902228f,
        0.496576f,
        0.466261f,
        0.017487f,
        0.312742f,
        0.335167f
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        try {
            interpreter = Interpreter(loadModelFile())
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        sv = findViewById<View>(R.id.sv) as ScrollView
        val logED = findViewById<EditText>(R.id.editText)
        val latED = findViewById<EditText>(R.id.editText2)
        val ageED = findViewById<EditText>(R.id.editText3)
        val roomsED = findViewById<EditText>(R.id.editText4)
        val bedroomsED = findViewById<EditText>(R.id.editText5)
        val populationED = findViewById<EditText>(R.id.editText6)
        val householdED = findViewById<EditText>(R.id.editText15)
        val incomeED = findViewById<EditText>(R.id.editText16)
        val ocean = findViewById<Spinner>(R.id.spinner)
        val arrayAdapter = ArrayAdapter(
            applicationContext, android.R.layout.simple_spinner_dropdown_item,
            arrayOf(
                "<1H OCEAN",
                "INLAND",
                "ISLAND",
                "NEAR BAY",
                "NEAR OCEAN"
            )
        )
        ocean.adapter = arrayAdapter
        val result = findViewById<TextView>(R.id.textView2)

        val btn = findViewById<Button>(R.id.button2)
        btn.setOnClickListener {
            sv.scrollTo(sv.bottom, 0)
            var logVal = logED.text.toString().toFloat()
            var latVal = latED.text.toString().toFloat()
            var ageVal = ageED.text.toString().toFloat()
            var roomsVal = roomsED.text.toString().toFloat()
            var bedroomsVal = bedroomsED.text.toString().toFloat()
            var populationVal = populationED.text.toString().toFloat()
            var householdVal = householdED.text.toString().toFloat()
            var incomeVal = incomeED.text.toString().toFloat()
            var oceanA = 0f
            var oceanB = 0f
            var oceanC = 0f
            var oceanD = 0f
            var oceanE = 0f
            when (ocean.selectedItemPosition) {
                0 -> {
                    oceanA = 1f
                    oceanB = 0f
                    oceanC = 0f
                    oceanD = 0f
                    oceanE = 0f
                }

                1 -> {
                    oceanA = 0f
                    oceanB = 1f
                    oceanC = 0f
                    oceanD = 0f
                    oceanE = 0f
                }

                2 -> {
                    oceanA = 0f
                    oceanB = 0f
                    oceanC = 1f
                    oceanD = 0f
                    oceanE = 0f
                }

                3 -> {
                    oceanA = 0f
                    oceanB = 0f
                    oceanC = 0f
                    oceanD = 1f
                    oceanE = 0f
                }

                4 -> {
                    oceanA = 0f
                    oceanB = 0f
                    oceanC = 0f
                    oceanD = 0f
                    oceanE = 1f
                }
            }
            logVal = (logVal - mean[0]) / std[0]
            latVal = (latVal - mean[1]) / std[1]
            ageVal = (ageVal - mean[2]) / std[2]
            roomsVal = (roomsVal - mean[3]) / std[3]
            bedroomsVal = (bedroomsVal - mean[4]) / std[4]
            populationVal = (populationVal - mean[5]) / std[5]
            householdVal = (householdVal - mean[6]) / std[6]
            incomeVal = (incomeVal - mean[7]) / std[7]
            oceanA = (oceanA - mean[8]) / std[8]
            oceanB = (oceanB - mean[9]) / std[9]
            oceanC = (oceanC - mean[10]) / std[10]
            oceanD = (oceanD - mean[11]) / std[11]
            oceanE = (oceanE - mean[12]) / std[12]

            val inputs = Array(1) { FloatArray(13) }
            inputs[0][0] = logVal
            inputs[0][1] = latVal
            inputs[0][2] = ageVal
            inputs[0][3] = roomsVal
            inputs[0][4] = bedroomsVal
            inputs[0][5] = populationVal
            inputs[0][6] = householdVal
            inputs[0][7] = incomeVal
            inputs[0][8] = oceanA
            inputs[0][9] = oceanB
            inputs[0][10] = oceanC
            inputs[0][11] = oceanD
            inputs[0][12] = oceanE

            val res: Float = doInference(inputs)
            result.text = "$"+String.format("%.2f", res)
        }
    }

    //TODO pass input to model and get output
    fun doInference(input: Array<FloatArray>): Float {
        val output = Array(1) { FloatArray(1) }

        interpreter!!.run(input, output)

        return output[0][0]
    }

    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer {
        val assetFileDescriptor = this.assets.openFd("house_prediction.tflite")
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val length = assetFileDescriptor.length
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, length)
    }

}
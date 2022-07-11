package com.example.second_app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import com.example.second_app.databinding.ActivitySetTemperatureBinding
import java.lang.Exception

class SetTemperatureActivity: Activity() {
    private var _binding: ActivitySetTemperatureBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        _binding = ActivitySetTemperatureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val windowSize = getScreenSize(this)
        window.attributes.width = (windowSize.width * 0.8).toInt()
        window.attributes.height = (windowSize.height * 0.6).toInt()

        // 이 액티비티로 올 때는 시작 온도를 받는다.
        val currentTemperature = intent?.getDoubleExtra("init_temperature", 0.0)!!
        binding.editTextSetInitTemp.setText(currentTemperature.toString(), TextView.BufferType.EDITABLE)

        binding.btnSetInitTemp.setOnClickListener {
            val intent = Intent(this, CreateLevelActivity::class.java)
            try {
                val double1 = binding.editTextSetInitTemp.text.toString().toDouble()
                val string1 = String.format("%.1f", double1)
                val double2 = string1.toDouble()

                if (double2 < 0.0 || double2 > 50.0) {
                    Toast.makeText(this, "0~50 사이의 값을 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
                else {
                    intent.putExtra("init_temperature", double2)
                    setResult(RESULT_OK, intent)
                    if (!isFinishing) finish()
                }

            } catch (e: Exception) {
                Toast.makeText(this, "적절한 온도를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }

        }
    }

    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        super.onBackPressed()
    }
}
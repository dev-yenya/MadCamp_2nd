package com.example.second_app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import com.example.second_app.databinding.ActivitySetTimeLimitBinding
import java.lang.Exception

class SetTimeLimitActivity: Activity() {
    private var _binding: ActivitySetTimeLimitBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        _binding = ActivitySetTimeLimitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val windowSize = getScreenSize(this)
        window.attributes.width = (windowSize.width * 0.8).toInt()
        window.attributes.height = (windowSize.height * 0.6).toInt()

        val stringArray = resources.getStringArray(R.array.seconds)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, stringArray)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSecond.adapter = adapter

        // 이 액티비티로 올 때는 제한시간을 받는다.
        val currentTimeLimit = intent?.getIntExtra("time_limit", 30)!!
        binding.spinnerSecond.setSelection((currentTimeLimit / 10) - 1)

        binding.btnSetTimeLimit.setOnClickListener {
            val second = binding.spinnerSecond.selectedItem.toString().toInt()
            val intent = Intent()
            intent.putExtra("time_limit", second)
            setResult(RESULT_OK, intent)
            if (!isFinishing) finish()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_OUTSIDE) {
            return false
        }
        return true
    }

    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        super.onBackPressed()
    }
}
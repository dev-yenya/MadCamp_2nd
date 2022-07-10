package com.example.second_app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.example.second_app.databinding.ActivityLevelCompleteBinding

class LevelCompleteActivity: Activity() {
    private var _binding: ActivityLevelCompleteBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        _binding = ActivityLevelCompleteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val windowSize = getScreenSize(this)
        window.attributes.width = (windowSize.width * 0.8).toInt()
        window.attributes.height = (windowSize.height * 0.6).toInt()

        val temperatureScore = intent?.getDoubleExtra("temperature_score", 0.0)
        binding.textLevelCompleteStat1.text = String.format("%.1f°C", temperatureScore)

        binding.btnLevelCompleteOk.setOnClickListener {
            setResult(RESULT_OK)
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
        return
    }
}
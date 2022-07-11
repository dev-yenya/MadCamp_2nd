package com.example.second_app

import android.app.Activity
import android.os.Bundle
import android.view.MotionEvent
import android.view.Window
import com.example.second_app.databinding.ActivityLevelFailBinding

class LevelFailActivity: Activity() {
    private var _binding: ActivityLevelFailBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        _binding = ActivityLevelFailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val windowSize = getScreenSize(this)
        window.attributes.width = (windowSize.width * 0.8).toInt()
        window.attributes.height = (windowSize.height * 0.6).toInt()

        binding.btnLevelFailOk.setOnClickListener {
            setResult(RESULT_FIRST_USER + 1)
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
package com.example.second_app

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.Window
import com.example.second_app.databinding.ActivityBackWarningBinding

class BackWarningActivity: Activity() {
    private var _binding: ActivityBackWarningBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        _binding = ActivityBackWarningBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val windowSize = getScreenSize(this)
        window.attributes.width = (windowSize.width * 0.8).toInt()
        window.attributes.height = (windowSize.height * 0.3).toInt()

        binding.btnBackConfirm.setOnClickListener {
            setResult(RESULT_OK)
            if (!isFinishing) finish()
        }

        binding.btnBackResume.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        super.onBackPressed()
    }
}
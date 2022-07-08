package com.example.second_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.second_app.databinding.ActivityViewOnlineLevelBinding

class ViewOnlineLevelActivity: AppCompatActivity() {
    private var _binding: ActivityViewOnlineLevelBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityViewOnlineLevelBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
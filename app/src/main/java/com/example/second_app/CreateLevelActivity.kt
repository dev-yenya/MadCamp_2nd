package com.example.second_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.second_app.databinding.ActivityCreateLevelBinding

class CreateLevelActivity: AppCompatActivity() {
    private var _binding: ActivityCreateLevelBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCreateLevelBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}
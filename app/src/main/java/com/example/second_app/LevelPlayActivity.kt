package com.example.second_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.second_app.databinding.ActivityStoreEnterBinding

class LevelPlayActivity: AppCompatActivity() {
    private var _binding: ActivityStoreEnterBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityStoreEnterBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
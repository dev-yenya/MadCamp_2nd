package com.example.second_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.second_app.databinding.ActivityLevelPlayBinding

class LevelPlayActivity: AppCompatActivity() {
    private var _binding: ActivityLevelPlayBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityLevelPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val levelData = intent.extras!!.getSerializable("level_data") as LevelInformation
        binding.textLevelPlayA.text = levelData.levelname


    }
}
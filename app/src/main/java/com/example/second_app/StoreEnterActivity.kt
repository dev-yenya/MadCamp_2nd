package com.example.second_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.second_app.databinding.ActivityStoreEnterBinding

class StoreEnterActivity: AppCompatActivity() {
    private var _binding: ActivityStoreEnterBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityStoreEnterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imgbtnStoreEnterBack.setOnClickListener {
            if (!isFinishing) finish()
        }

        binding.btnStoreEnterMake.setOnClickListener {
            val intent = Intent(this, CreateLevelActivity::class.java)
            startActivity(intent)
        }

        binding.btnStoreEnterOnline.setOnClickListener {
            val intent = Intent(this, OnlineLevelsActivity::class.java)
            startActivity(intent)
        }

        binding.btnStoreEnterSaved.setOnClickListener {
            val intent = Intent(this, SavedLevelsActivity::class.java)
            startActivity(intent)
        }
    }
}
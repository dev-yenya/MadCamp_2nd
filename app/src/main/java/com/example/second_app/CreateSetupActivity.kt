package com.example.second_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.second_app.databinding.ActivityCreateSetupBinding

class CreateSetupActivity: AppCompatActivity() {
    private var _binding: ActivityCreateSetupBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityCreateSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCreateSetupCreate.setOnClickListener {
            submitSize()
        }
    }

    private fun submitSize() {
        val intent = Intent(this, CreateLevelActivity::class.java)

        val boardSize = binding.editTextCreateSetupSize.text.toString().toInt()
        if (boardSize > 12 || boardSize < 4) {
            Toast.makeText(this, "4~12 사이의 값만 가능", Toast.LENGTH_SHORT).show()
        }
        else {
            intent.putExtra("board_size", boardSize)
            startActivity(intent)
        }
    }
}
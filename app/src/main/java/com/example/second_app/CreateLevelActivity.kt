package com.example.second_app

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.second_app.databinding.ActivityCreateLevelBinding
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class CreateLevelActivity: AppCompatActivity(), CoroutineScope {
    private var _binding: ActivityCreateLevelBinding? = null
    private val binding get() = _binding!!
    private val httpRequest = HttpRequest()
    private val gson = Gson()

    private val job = Job()
    override val coroutineContext: CoroutineContext get() = Dispatchers.IO + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCreateLevelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.text = intent.getIntExtra("board_size", 9999).toString()

        binding.btnBack.setOnClickListener {
            Toast.makeText(this, "테스트 중", Toast.LENGTH_SHORT).show()
            if (!isFinishing) finish()
        }
    }

    override fun onBackPressed() {
        return
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
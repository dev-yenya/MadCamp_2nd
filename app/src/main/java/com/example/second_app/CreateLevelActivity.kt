package com.example.second_app

import android.os.Bundle
import android.util.Log
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

        binding.testBtn.setOnClickListener {
            val postBody = gson.toJson(UserInformation("lasadsa", 2040, "hahahha"))
            Log.d("ASDF", postBody)
            httpRequest.request("POST", "/users", postBody, CoroutineScope(coroutineContext))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
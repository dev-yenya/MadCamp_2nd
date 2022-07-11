package com.example.second_app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.Window
import android.widget.Toast
import com.example.second_app.databinding.ActivityUploadConfirmBinding
import kotlinx.coroutines.CoroutineScope

class UploadConfirmActivity: Activity() {
    private var _binding: ActivityUploadConfirmBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        _binding = ActivityUploadConfirmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val windowSize = getScreenSize(this)
        window.attributes.width = (windowSize.width * 0.8).toInt()
        window.attributes.height = (windowSize.height * 0.6).toInt()

        binding.btnUploadLevel.setOnClickListener {
            if (uploadOk()) {
                val intent = Intent(this, CreateLevelActivity::class.java)
                intent.putExtra("level_name", binding.editTextLevelName.text.toString())
                Log.d("NAME?", binding.editTextLevelName.text.toString())
                setResult(RESULT_OK, intent)
                if (!isFinishing) finish()
            }
        }
    }

    private fun uploadOk(): Boolean {
        val levelName = binding.editTextLevelName.text
        if (levelName.isNotEmpty() || levelName.length < 32) {
            return true
        }
        Toast.makeText(this, "레벨 이름은 1 ~ 16 글자만 가능합니다.", Toast.LENGTH_SHORT).show()
        return false
    }

    override fun onBackPressed() {
        setResult(RESULT_CANCELED)

        super.onBackPressed()
    }
}
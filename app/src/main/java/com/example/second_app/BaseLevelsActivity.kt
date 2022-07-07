package com.example.second_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.second_app.databinding.ActivityStoreEnterBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class BaseLevelsActivity: AppCompatActivity() {
    private var _binding: ActivityStoreEnterBinding? = null
    private val binding get() = _binding!!
    private val baseLevelList: MutableList<LevelInformation> = mutableListOf()
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityStoreEnterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: RecyclerView 를 활용해서 레벨의 목록을 표시해보자.

        // 내부 저장소의 파일을 불러와 레벨 정보를 읽어오자.
        readBaseLevels()
        assert(baseLevelList.size == 3)

        // TODO: 각 버튼이 레벨을 플레이하는 Activity 로 이동하도록 만들자.
    }

    private fun readBaseLevels() {
        var jsonString = ""
        try {
            val jsonFile = assets.open("base_levels.json")
            val inputStream = InputStreamReader(jsonFile)
            val bufferedReader = BufferedReader(inputStream)
            val stringBuilder = StringBuilder()
            var receiveString: String?

            while (bufferedReader.readLine().also { receiveString = it } != null) {
                stringBuilder.append(receiveString)
            }
            jsonString = stringBuilder.toString()

            val itemType = object : TypeToken<MutableList<String>>() {}.type
            val data: MutableList<String> = gson.fromJson(jsonString, itemType)
            for (d in data) {
                baseLevelList.add(gson.fromJson(d, LevelInformation::class.java))
            }
        }
        catch (e: IOException) {
            e.printStackTrace()
        }
        catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}
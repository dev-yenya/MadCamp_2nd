package com.example.second_app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.second_app.databinding.ActivityBaseLevelsBinding
import com.example.second_app.databinding.LevelListItemBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class BaseLevelsActivity: AppCompatActivity() {
    private var _binding: ActivityBaseLevelsBinding? = null
    private val binding get() = _binding!!
    private lateinit var baseLevelList: MutableList<LevelInformation>
    private val gson = Gson()
    private lateinit var recyclerViewAdapter: BaseLevelsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityBaseLevelsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 내부 저장소의 파일을 불러와 레벨 정보를 읽어오자.
        readBaseLevels()

        recyclerViewAdapter = BaseLevelsAdapter()
        recyclerViewAdapter.dataList = baseLevelList
        val recyclerView = binding.recyclerViewBaseLevels
        recyclerView.adapter = recyclerViewAdapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

    }

    private fun readBaseLevels() {
        val jsonString: String
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

            val itemType = object : TypeToken<MutableList<LevelInformation>>() {}.type
            baseLevelList = gson.fromJson(jsonString, itemType)
        }
        catch (e: IOException) {
            e.printStackTrace()
        }
        catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}

class BaseLevelsAdapter : RecyclerView.Adapter<BaseLevelsAdapter.MyViewHolder>(){
    var dataList = mutableListOf<LevelInformation>()
    private var _bb : LevelListItemBinding? = null
    private val binding get() = _bb!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        _bb = LevelListItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        val binding = binding
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
    inner class MyViewHolder(private val binding: LevelListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(levelMetadata: LevelInformation) {
            binding.btnLevelListItem.text = levelMetadata.levelname
            val context = binding.btnLevelListItem.context
            binding.btnLevelListItem.setOnClickListener {
                val intent = Intent(context, LevelPlayActivity::class.java)
                intent.putExtra("level_metadata", levelMetadata)
                intent.putExtra("is_base_level", true)
                context.startActivity(intent)
            }
        }
    }
}
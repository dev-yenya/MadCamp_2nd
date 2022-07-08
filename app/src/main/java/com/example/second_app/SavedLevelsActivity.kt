package com.example.second_app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.second_app.databinding.ActivitySavedLevelsBinding
import com.example.second_app.databinding.LevelListItemBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.lang.NullPointerException

class SavedLevelsActivity: AppCompatActivity() {
    private var _binding: ActivitySavedLevelsBinding? = null
    private val binding get() = _binding!!
    private var savedLevelList = mutableListOf<LevelInformation>()
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySavedLevelsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        readSavedLevels()

        val recyclerView = binding.recyclerViewSavedLevels
        val adapter = SavedLevelsAdapter()
        adapter.dataList = savedLevelList

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    private fun readSavedLevels() {
        var jsonString = ""
        try {
            val jsonFile = File(filesDir, "saved_levels.json")
            jsonFile.createNewFile()
            val inputStream = openFileInput("saved_levels.json")

            if (inputStream != null) {
                val inputStreamReader = InputStreamReader(inputStream)
                val bufferedReader = BufferedReader(inputStreamReader)
                var receiveString: String?
                val stringBuilder = StringBuilder()
                while (bufferedReader.readLine().also { receiveString = it } != null) {
                    stringBuilder.append(receiveString)
                }
                jsonString = stringBuilder.toString()
                inputStream.close()
            }

            val itemType = object : TypeToken<MutableList<String>>() {}.type
            try {
                val data: MutableList<String> = gson.fromJson(jsonString, itemType)
                for (d in data) {
                    savedLevelList.add(gson.fromJson(d, LevelInformation::class.java))
                }
                if (savedLevelList.isNotEmpty()) {
                    binding.recyclerViewSavedLevels.visibility = View.VISIBLE
                    binding.savedNoLevelsImage.visibility = View.INVISIBLE
                    binding.savedNoLevelsText.visibility = View.INVISIBLE
                }
                else {
                    binding.recyclerViewSavedLevels.visibility = View.INVISIBLE
                    binding.savedNoLevelsImage.visibility = View.VISIBLE
                    binding.savedNoLevelsText.visibility = View.VISIBLE
                }
            }
            catch (_: NullPointerException) {

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

class SavedLevelsAdapter : RecyclerView.Adapter<SavedLevelsAdapter.MyViewHolder>(){
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
        fun bind(levelData: LevelInformation) {
            binding.btnLevelListItem.text = levelData.levelname
            val context = binding.btnLevelListItem.context
            binding.btnLevelListItem.setOnClickListener {
                val intent = Intent(context, LevelPlayActivity::class.java)
                intent.putExtra("level_data", levelData)
                context.startActivity(intent)
            }
        }
    }
}
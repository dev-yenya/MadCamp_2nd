package com.example.second_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.google.gson.JsonArray
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import java.io.*
import java.lang.NullPointerException
import java.util.logging.Level

class SavedLevelsActivity: AppCompatActivity() {
    private var _binding: ActivitySavedLevelsBinding? = null
    private val binding get() = _binding!!
    private var savedLevelList = mutableListOf<LevelInformation>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySavedLevelsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        savedLevelList = readSavedLevels(this)
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

        val recyclerView = binding.recyclerViewSavedLevels
        val adapter = SavedLevelsAdapter()
        adapter.dataList = savedLevelList

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
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
        fun bind(levelMetadata: LevelInformation) {
            binding.btnLevelListItem.text = levelMetadata.levelname
            val context = binding.btnLevelListItem.context
            binding.btnLevelListItem.setOnClickListener {
                val intent = Intent(context, LevelPlayActivity::class.java)
                intent.putExtra("level_metadata", levelMetadata)
                intent.putExtra("is_base_level", false)
                context.startActivity(intent)
            }
        }
    }
}

fun readSavedLevels(context: Context): MutableList<LevelInformation> {
    Log.d("READ_SAVED", "reading saved levels..")
    val gson = Gson()
    var jsonString = ""
    val resultList = mutableListOf<LevelInformation>()
    try {
        val jsonFile = File(context.filesDir, "saved_levels.json")
        jsonFile.createNewFile()
        val inputStream = context.openFileInput("saved_levels.json")

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
                resultList.add(gson.fromJson(d, LevelInformation::class.java))
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
    return resultList
}

// saved_levels.json 파일에 값을 저장한다.
fun writeSavedLevels(context: Context, listToWrite: MutableList<LevelInformation>) {
    Log.d("WRITE_SAVED", "writing saved levels..")
    val gson = Gson()
    val jsonFile = File(context.filesDir, "saved_levels.json")
    jsonFile.delete()
    jsonFile.createNewFile()
    try {
        val fileWriter = FileWriter(jsonFile)
        val jsonTotalObject = JsonArray()

        for (data in listToWrite) {
            val jsonEntry = gson.toJson(data)
            jsonTotalObject.add(jsonEntry)
        }
        fileWriter.append(jsonTotalObject.toString())
        fileWriter.flush()
        fileWriter.close()
    }
    catch (e: JSONException) {
        e.printStackTrace()
    }
}
package com.example.second_app

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.second_app.databinding.ActivityLevelPlayBinding
import com.example.second_app.databinding.TileItemBinding
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader


class LevelPlayActivity: AppCompatActivity() {
    private var _binding: ActivityLevelPlayBinding? = null
    private val binding get() = _binding!!
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityLevelPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val levelMetadata = intent.extras!!.getSerializable("level_metadata") as LevelInformation
        val isBaseLevel = intent.extras!!.getBoolean("is_base_level")

        binding.textLevelPlayTitle.text = levelMetadata.levelname

        val levelData = readLevelData(levelMetadata.id, isBaseLevel)

        // 보드 설정.
        val board = binding.recyclerViewLevelPlayBoard
        val boardSize = levelMetadata.boardsize

        val adapter = BoardAdapter()
        adapter.dataList = levelData.tiles
        Log.d("COUNT", "${adapter.dataList.size}")
        board.adapter = adapter
        board.setHasFixedSize(true)

        board.layoutManager = GridLayoutManager(this, boardSize)
    }

    // 이 시점에서 levelid.json 파일은 반드시 존재해야 한다.
    private fun readLevelData(id: Int, isBaseLevel: Boolean): LevelData {
        val jsonString: String
        if (isBaseLevel) {
            val jsonFile = assets.open("base_levels_data/$id.json")
            val inputStream = InputStreamReader(jsonFile)
            val bufferedReader = BufferedReader(inputStream)
            val stringBuilder = StringBuilder()
            var receiveString: String?

            while (bufferedReader.readLine().also { receiveString = it } != null) {
                stringBuilder.append(receiveString)
            }
            jsonString = stringBuilder.toString()
            inputStream.close()
        }
        else {
            // 반드시 값이 존재해야 하므로 assert
            val inputStream = openFileInput("$id.json")!!
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
        return gson.fromJson(jsonString, LevelData::class.java)
    }
}

class BoardAdapter : RecyclerView.Adapter<BoardAdapter.MyViewHolder>(){
    var dataList = mutableListOf<Tile>()
    private var _binding : TileItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        _binding = TileItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        val binding = binding


        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
    inner class MyViewHolder(private val binding: TileItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tileData: Tile) {
            val imageId = when (tileData.type) {
                "land" -> R.drawable.tile_land
                "water" -> R.drawable.tile_water
                else -> throw Error("${tileData.type} is not allowed.")
            }
            binding.imgTileItem.setImageResource(imageId)
        }
    }
}
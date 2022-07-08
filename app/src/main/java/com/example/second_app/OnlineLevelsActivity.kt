package com.example.second_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.second_app.databinding.ActivityOnlineLevelsBinding
import com.example.second_app.databinding.LevelListItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext

object NavigationDirection {
    const val LEFT = 9000
    const val RIGHT = 9001
}

class OnlineLevelsActivity : AppCompatActivity(), CoroutineScope {
    private var _binding: ActivityOnlineLevelsBinding? = null
    private val binding get() = _binding!!

    // 서버로부터 받아온 정보를 캐시에 저장해두자.
    private val pagesCacheLimit = 10
    // 캐시된 자료는 여기에 저장된다.
    private var levelsCached: MutableMap<Int, MutableList<LevelInformation>> = mutableMapOf()

    private val httpRequest = HttpRequest()
    private var currentPage = 0
    private var currentDataList = mutableListOf<LevelInformation>()

    private val adapter = OnlineLevelsAdapter()
    private lateinit var recyclerView: RecyclerView

    // 비동기적인 요청 전송을 위한 코루틴용 코드들.
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext get() = Dispatchers.IO + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityOnlineLevelsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        job = Job()

        recyclerView = binding.recyclerViewOnlineLevels
        queryPage(currentPage, NavigationDirection.RIGHT)

        adapter.dataList = currentDataList
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    // GET /level_list/pageNumber 를 전송한다.
    // 이 경우 (pageNumber * 20) ~ ((pageNumber + 1) * 20) - 1
    // 에 해당하는 레벨들을 받아온다.
    private fun queryPage(pageNumber: Int, navigationDirection: Int) {
        val pageData = levelsCached[pageNumber]

        if (pageData == null) {
            // 캐시에 자료가 없다.
            val urlStr = "/level_list/${pageNumber}"
            val result = httpRequest.request("GET", urlStr, CoroutineScope(coroutineContext))

            if (result == null) {
                // 리퀘스트 실패..
                binding.recyclerViewOnlineLevels.visibility = View.INVISIBLE
                binding.onlineLoadFailedImage.visibility = View.VISIBLE
                binding.onlineLoadFailedText.visibility = View.VISIBLE
                currentDataList = mutableListOf()
            }
            else {
                // 리퀘스트 성공!
                binding.recyclerViewOnlineLevels.visibility = View.VISIBLE
                binding.onlineLoadFailedImage.visibility = View.INVISIBLE
                binding.onlineLoadFailedText.visibility = View.INVISIBLE
                currentDataList = result.toMutableList()

                if (levelsCached.size == pagesCacheLimit) {
                    when (navigationDirection) {
                        NavigationDirection.LEFT -> {
                            // 왼쪽으로 가고 있었으므로, 오른쪽 끝의 캐시를 지운다.
                            levelsCached.remove(getMaxIndex())
                        }
                        NavigationDirection.RIGHT -> {
                            // 그 반대
                            levelsCached.remove(getMinIndex())
                        }
                    }
                }
                levelsCached[pageNumber] = currentDataList
            }

        }
        else {
            // 캐시에 자료가 있다.
            currentDataList = pageData
        }
    }

    private fun getMaxIndex(): Int {
        return levelsCached.maxByOrNull { it.key }?.key ?:-1
    }

    private fun getMinIndex(): Int {
        return levelsCached.minByOrNull { it.key }?.key ?:-1
    }
}

class OnlineLevelsAdapter : RecyclerView.Adapter<OnlineLevelsAdapter.MyViewHolder>(){
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
            Log.d("TAG", "TTT")
            binding.btnLevelListItem.setOnClickListener {
                val intent = Intent(context, ViewOnlineLevelActivity::class.java)
                intent.putExtra("level_data", levelData)
                context.startActivity(intent)
            }
        }
    }
}
package com.example.second_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.second_app.databinding.ActivityUserRatingBinding
import com.example.second_app.databinding.UserRatingListItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class UserRatingActivity : AppCompatActivity(), CoroutineScope {
    private var mBinding: ActivityUserRatingBinding? = null
    private val binding get() = mBinding!!

    private val adapter = UsersAdapter()
    private lateinit var recyclerView: RecyclerView

    private var currentDataList = mutableListOf<UserInformation>()

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext get() = Dispatchers.IO + job
    private val httpRequest = HttpRequest()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityUserRatingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        job = Job()

        recyclerView = binding.recyclerUserRating
        queryUser()

        adapter.dataList =  currentDataList
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        binding.imgbtnUserRatingBack.setOnClickListener{
            if (!isFinishing) finish()
        }

    }

    override fun onDestroy(){
        super.onDestroy()

    }

    // GET /users 를 전송한다.
    private fun queryUser(){
        val urlStr = "/rating_list"
        val result = httpRequest.requestGeneral<ArrayList<UserInformation>>("GET", urlStr,"", CoroutineScope(coroutineContext))
        if(result!=null){
            binding.recyclerUserRating.visibility = View.VISIBLE
            currentDataList = result.toMutableList()
        }
    }
}

class UsersAdapter : RecyclerView.Adapter<UsersAdapter.MyViewHolder>(){
    var dataList = mutableListOf<UserInformation>()
    var rank = 1
    private var _bb : UserRatingListItemBinding ?= null
    private val binding get() = _bb!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersAdapter.MyViewHolder {
        _bb = UserRatingListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val binding = binding
        return MyViewHolder(binding)
    }
    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: UsersAdapter.MyViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    inner class MyViewHolder(private val binding: UserRatingListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(userMetadata : UserInformation){
            binding.tvUserRatingUsername.text = userMetadata.username
            binding.tvRatingListRating.text = userMetadata.rating.toString()
            binding.tvRatingListRank.text = (rank++).toString()
        }
    }
}
package com.example.second_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.second_app.databinding.ActivityViewOnlineLevelBinding
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.json.JSONException
import java.io.File
import java.io.FileWriter
import java.io.IOException
import kotlin.coroutines.CoroutineContext

class ViewOnlineLevelActivity : AppCompatActivity(), CoroutineScope {
    private var _binding: ActivityViewOnlineLevelBinding? = null
    private val binding get() = _binding!!
    private val httpRequest = HttpRequest()
    private val gson = Gson()
    private var scoreString = ""
    private val job = Job()
    override val coroutineContext: CoroutineContext get() = Dispatchers.IO + job

    private lateinit var levelLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityViewOnlineLevelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val levelMetadata = intent.extras!!.getSerializable("level_metadata") as LevelInformation
        val id = levelMetadata.id

        // 레벨을 클리어한 경우 이 람다식이 실행된다.
        levelLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_FIRST_USER) {
                val score = it.data?.getDoubleExtra("temperature_score", 0.0)!!

                // MEMO: LevelPlayActivity에서는 하이스코어를 intent에 담아서 넘겨준다.


                // TODO: 하이스코어 업데이트.
                // (뷰 업데이트 코드)
                scoreString = String.format("%.1f", score)
                Log.d("SCORE", "$scoreString")
                Log.d("LEVEL", "complete.")
                binding.tvBestScoreOnlineLevel.text = scoreString+"℃"
            }
        }

        binding.textViewOnlineLevelTitle.text = levelMetadata.levelname
        binding.tvRatingViewOnlineLevel.text = levelMetadata.rating.toString()
        binding.tvBoardSizeOnlineLevel.text = levelMetadata.boardsize.toString()
        binding.tvTimelimitOnlineLevel.text = (levelMetadata.rating/10).toString()

        Log.e("levelMetaData : ", levelMetadata.rating.toString()+levelMetadata.rating.toString())

        checkUserHasLevel(levelMetadata)

        binding.btnViewOnlineLevelPlay.setOnClickListener{
            val intent = Intent(this, LevelPlayActivity::class.java)
            intent.putExtra("level_metadata", levelMetadata)
            intent.putExtra("is_base_level", false)
            intent.putExtra("test_mode", false)

            levelLauncher.launch(intent)
        }

        binding.imgbtnViewOnlineLevelBack.setOnClickListener {
            if (!isFinishing) finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    // 사용자의 내부 저장소에 레벨이 있는지 확인한다.
    // 있다면 아무것도 하지 않는다.
    // 없다면 파일을 만들고 다운로드 받는다.
    private fun checkUserHasLevel(metadata: LevelInformation) {
        try {
            val name = "${metadata.id}.json"
            if (!baseContext.getFileStreamPath(name).exists()) {
                // 레벨 파일이 없어 exception이 발생했다.
                // 파일을 생성하고, 요청을 보내고, 서버가 보낸 값을 파일에 저장하자.
                val file = File(filesDir, name)
                file.createNewFile()
                val levelData = httpRequest.requestGeneral<LevelData>("GET", "/levels/${metadata.id}", "", CoroutineScope(coroutineContext))

                if (levelData != null) {
                    // 레벨 다운 성공!
                    val levelJsonString = gson.toJson(levelData)
                    try {
                        val fileWriter = FileWriter(file)
                        fileWriter.append(levelJsonString)
                        fileWriter.flush()
                        fileWriter.close()
                        Log.d("OK", "파일 생성 성공: $name")

                        // 파일에 써준다.
                        val currentLevels = readSavedLevels(this)
                        currentLevels.add(metadata)
                        writeSavedLevels(this, currentLevels)
                    }
                    catch (e: JSONException) {
                        // 파일 쓰기 실패.. 여기서 뭘 하면 좋을까?
                        e.printStackTrace()
                    }
                }
                else {
                    // 레벨을 다운받는 것에 실패했으므로, 파일을 삭제한다.
                    // 버튼을 비활성화하고 토스트를 띄워주자.
                    file.delete()
                    binding.btnViewOnlineLevelPlay.isEnabled = false
                    Toast.makeText(this, "레벨 다운로드 실패", Toast.LENGTH_SHORT).show()
                }
            }

        }
        catch (e: IOException) {
            Log.d("IDCHECK", e.message!!)
            e.printStackTrace()
        }
        catch (e: JSONException) {
            e.printStackTrace()
        }

    }
}
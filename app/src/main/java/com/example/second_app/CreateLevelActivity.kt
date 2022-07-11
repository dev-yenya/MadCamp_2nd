package com.example.second_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.second_app.databinding.ActivityCreateLevelBinding
import com.example.second_app.databinding.TileButtonItemBinding
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.json.JSONException
import java.io.File
import java.io.FileWriter
import java.io.IOException
import kotlin.coroutines.CoroutineContext

val typeToName: Map<String, String> = mapOf(
    "land" to "땅",
    "water" to "물",
    "ice" to "얼음",
    // -------------
    "goal" to "골인 지점",
    "life" to "친환경",
    "fire" to "온난화",
)

class CreateLevelActivity: AppCompatActivity(), CoroutineScope {
    private var _binding: ActivityCreateLevelBinding? = null
    private val binding get() = _binding!!
    private val gson = Gson()


    // 각종 런처
    private lateinit var playTestLauncher: ActivityResultLauncher<Intent>
    private lateinit var uploadLauncher: ActivityResultLauncher<Intent>
    private lateinit var backLauncher: ActivityResultLauncher<Intent>
    private lateinit var setTemperatureLauncher: ActivityResultLauncher<Intent>
    private lateinit var setTimeLimitLauncher: ActivityResultLauncher<Intent>

    // 보드 세팅용 코드
    private var boardSize = 0
    private var recyclerWidth = 0
    private lateinit var boardAdapter: BoardCreateAdapter

    //레벨 세팅용 코드
    private var rating = 0
    private var username = ""

    // 타일/아이템 선택 창을 위한 코드
    private lateinit var tileAdapter: TileAdapter
    private lateinit var itemAdapter: ItemAdapter

    // HTTP 요청을 위한 코루틴 코드
    private val job = Job()
    override val coroutineContext: CoroutineContext get() = Dispatchers.IO + job

    // TODO: 코드가 너무 복잡하다.. 시간이 되면 코드들을 나눠보자.

    // 현재 선택중인 행동
    var currentAction = "nothing"

    // 아이템들을 담아놓는 맵
    private val itemMap = mutableMapOf<Item, Int>()

    // 시작점
    private var startPoint = Point(0, 0)

    // 시작 온도
    private var initTemperature = 20.0

    // 제한 시간
    private var timeLimit = 30

    // 레벨 데이터 오브젝트
    private lateinit var levelData: LevelData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCreateLevelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        boardSize = intent.getIntExtra(("board_size"), 9999)

        // 업로드 버튼 비활성화.
        binding.btnUpload.isEnabled = false
        binding.btnUpload.setTextColor(ContextCompat.getColor(this, R.color.gray))

        // 플레이 테스트가 성공할 경우 업로드 버튼을 활성화한다.
        playTestLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_FIRST_USER) {
                Toast.makeText(this, "레벨 테스트 성공!", Toast.LENGTH_SHORT).show()
                binding.btnUpload.isEnabled = true
                binding.btnUpload.setTextColor(ContextCompat.getColor(this, R.color.green))
            }
            else {
                // 만약 플레이테스트에 실패한다면, 0.json 파일을 삭제한다.
                val file = File(filesDir, "0.json")
                file.delete()
            }
        }

        // 업로드에 성공할 경우 액티비티를 닫는다.
        uploadLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val levelName = it.data?.getStringExtra("level_name")
                if (levelName != null && uploadLevel(levelName)) {
                    Toast.makeText(this, "레벨 업로드 완료!", Toast.LENGTH_SHORT).show()
                    if (!isFinishing) finish()
                }

            }
        }

        backLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                Toast.makeText(this, "레벨 만들기 취소", Toast.LENGTH_SHORT).show()
                if (!isFinishing) finish()
            }
        }

        setTemperatureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val newTemp = it.data?.getDoubleExtra("init_temperature", 20.0)!!
                Toast.makeText(this, "시작 온도: $newTemp", Toast.LENGTH_SHORT).show()
                initTemperature = newTemp
                binding.btnInitTemp.text = String.format(getString(R.string.create_level_init_temp), initTemperature.toString())
            }
        }

        setTimeLimitLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val newTimeLimit = it.data?.getIntExtra("time_limit", 30)!!
                Toast.makeText(this, "시간 제한: $newTimeLimit", Toast.LENGTH_SHORT).show()
                timeLimit = newTimeLimit
                binding.btnTimeLimit.text = String.format(getString(R.string.create_level_time_limit), timeLimit.toString())
            }
        }

        val screenSize = getScreenSize(this)
        recyclerWidth = screenSize.width / boardSize

        boardAdapter = BoardCreateAdapter(recyclerWidth, this)

        // 보드의 기본 형태는 땅만 있는 형태
        val tileList = mutableListOf<Tile>()
        for (i in 0 until (boardSize * boardSize)) {
            val tile = Tile("land", Point(i % boardSize, i / boardSize))
            tileList.add(tile)
        }

        boardAdapter.tileList = tileList

        val boardView = binding.recyclerBoard
        boardView.adapter = boardAdapter
        boardView.layoutManager = GridLayoutManager(this, boardSize)
        boardView.setHasFixedSize(true)

        itemAdapter = ItemAdapter(screenSize.height / 12, this)
        binding.recyclerItems.adapter = itemAdapter
        binding.recyclerItems.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        tileAdapter = TileAdapter(screenSize.height / 12, this)
        binding.recyclerTiles.adapter = tileAdapter
        binding.recyclerTiles.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // 시작 온도 설정. 우선 20.0도로 보이게 한다.
        binding.btnInitTemp.text = String.format(getString(R.string.create_level_init_temp), "20.0")
        binding.btnTimeLimit.text = String.format(getString(R.string.create_level_time_limit), "30")

        // 시작점 설정.
        setStartPoint(startPoint)

        // 그만두기 버튼을 누를 경우.
        binding.btnBack.setOnClickListener {
            val intent = Intent(this, BackWarningActivity::class.java)
            backLauncher.launch(intent)
        }

        // 테스트 버튼 기능.
        binding.btnPlayTest.setOnClickListener {
            playTestLevel()
        }

        // 업로드 버튼 기능.
        binding.btnUpload.setOnClickListener {
            val intent = Intent(this, UploadConfirmActivity::class.java)
            uploadLauncher.launch(intent)
        }

        // 시작 온도 버튼 기능.
        binding.btnInitTemp.setOnClickListener {
            val intent = Intent(this, SetTemperatureActivity::class.java)
            intent.putExtra("init_temperature", initTemperature)
            setTemperatureLauncher.launch(intent)
        }

        // 시간 제한 버튼 기능.
        binding.btnTimeLimit.setOnClickListener {
            val intent = Intent(this, SetTimeLimitActivity::class.java)
            intent.putExtra("time_limit", timeLimit)
            setTimeLimitLauncher.launch(intent)
        }

        // 삭제 버튼 기능.
        binding.imgbtnDelete.setOnClickListener {
            currentAction = "delete"
            showCurrentAction()
        }

        // 시작점 배치 기능.
        binding.imgbtnStart.setOnClickListener {
            currentAction = "start"
            showCurrentAction()
        }
    }

    // 레벨을 테스트하는 함수.
    private fun playTestLevel() {
        val tiles = boardAdapter.tileList
        // 1. 레벨이 적절한지 확인하는 단계.
        // 1-1. 시작점이 적절히 배치되었는가?
        //  이상한 타일 위에 있으면 안된다.
        val startIndex = startPoint.x + startPoint.y * boardSize
        when (tiles[startIndex].type) {
            "water" -> {
                Toast.makeText(this, "${typeToName[tiles[startIndex].type]} 타일에서 시작할 수 없습니다.", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // 1-2. 도착점이 배치되었는가?
        var goalExists = false
        for ((item, _) in itemMap) {
            if (item.type == "goal") {
                goalExists = true
                break
            }
        }
        if (!goalExists) {
            Toast.makeText(this, "골인 지점을 배치해야 해요!", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. 실제로 테스트하는 단계.
        // 2-1. 레벨 파일 0.json을 생성하고, 사용자의 내부 저장소에 저장한다.
        try {
            val file = File(filesDir, "0.json")
            file.delete()
            file.createNewFile()

            levelData = LevelData(
                id = 0,
                tiles = ArrayList(boardAdapter.tileList),
                startpoint = startPoint,
                inittemp = initTemperature,
                items = ArrayList(itemMap.keys),
                timelimit = timeLimit
            )
            val levelJsonString = gson.toJson(levelData)
            val fileWriter = FileWriter(file)
            fileWriter.append(levelJsonString)
            fileWriter.flush()
            fileWriter.close()
            Log.d("OK", "임시 파일 생성 성공")

        } catch (e: IOException) {
            Log.d("SAVE_FAILED", "파일 생성 과정에서 오류 발생")
            return
        }

        // 2-2. 데이터를 준비하고 레벨을 플레이 할 수 있도록 한다.
        val intent = Intent(this, LevelPlayActivity::class.java)
        val temporaryLevelMetaData = LevelInformation(0, "테스트 레벨", boardSize, rating, username)
        intent.putExtra("level_metadata", temporaryLevelMetaData)
        intent.putExtra("is_base_level", false)
        intent.putExtra("test_mode", true)
        playTestLauncher.launch(intent)
    }

    // 레벨을 업로드하는 함수.
    private fun uploadLevel(levelName: String): Boolean {
        // HTTP 연결
        // 업로드 확인 등등..
        val httpRequest = HttpRequest()
        val levelPayload = LevelPayload(
            LevelInformation(0, levelName, boardSize, rating, username),
            levelData
        )
        val result = httpRequest.request("POST", "/levels", gson.toJson(levelPayload), CoroutineScope(coroutineContext))
        return if (result == null) {
            Toast.makeText(this, "레벨을 올리지 못했습니다.", Toast.LENGTH_SHORT).show()
            false
        } else {
            Toast.makeText(this, "레벨 업로드 성공!", Toast.LENGTH_SHORT).show()
            true
        }
    }

    // 백버튼 무효화
    override fun onBackPressed() {
        return
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    // 현재 누른 타일/아이템이 무엇인지 TextView로 보여준다.
    fun showCurrentAction() {
        val currentThing = binding.textCurrentSelected

        when (currentAction) {
            "land", "water", "ice" -> {
                currentThing.text = String.format(getString(R.string.create_level_selected_thing), "타일", typeToName[currentAction])
            }
            "goal", "life", "fire" -> {
                currentThing.text = String.format(getString(R.string.create_level_selected_thing), "아이템", typeToName[currentAction])
            }
            "start" -> {
                currentThing.text = getString(R.string.create_level_start_position)
            }
            "delete" -> {
                currentThing.text = getString(R.string.create_level_delete_item)
            }
            "nothing" -> {
                currentThing.text = getString(R.string.create_level_not_selected)
            }
            else -> throw Error("lul")
        }
    }

    // 타일을 눌렀을 때 이벤트를 처리한다.
    fun onClickTile(tile: Tile) {
        // 현재 어떤 타일/아이템을 선택했느냐에 따라 작업이 바뀐다.
        when (currentAction) {
            "land", "water", "ice" -> setTile(tile, currentAction)
            "life", "fire" -> placeItem(tile, currentAction)
            "goal" -> placeLimitedItem(tile, currentAction)
            "start" -> setStartPoint(tile.point)
            "delete" -> deleteItem(tile)
            "nothing" -> Unit
            else -> throw Error("아 ㅋㅋ")
        }
    }

    // 타일을 선택중인 경우, 클릭한 타일을 그 타일로 바꾼다.
    private fun setTile(tile: Tile, newType: String) {
        val newIndex = tile.point.x + tile.point.y * boardSize
        boardAdapter.tileList[newIndex] = Tile(newType, tile.point)
        boardAdapter.notifyItemChanged(newIndex)
    }

    // 아이템을 선택중인 경우, 해당 타일에 아이템을 배치한다.
    // 이미 해당 타일에 아이템이 있다면 없애고 넣는다.
    private fun placeItem(tile: Tile, itemType: String) {
        // 뷰 추가
        val parentLayout = binding.root
        val boardLayout = binding.recyclerBoard
        val item = Item(itemType, tile.point)

        val cSet = ConstraintSet()
        val childView = ImageView(this)
        childView.id = View.generateViewId()
        parentLayout.addView(childView, 6)

        val childLayoutParams = childView.layoutParams
        childLayoutParams.width = recyclerWidth
        childLayoutParams.height = recyclerWidth
        childView.layoutParams = childLayoutParams
        childView.setImageResource(item.getImageId())
        childView.scaleType = ImageView.ScaleType.CENTER_CROP
        childView.bringToFront()

        cSet.clone(parentLayout)
        val (posX, posY) = item.point
        cSet.connect(
            childView.id,
            ConstraintSet.START,
            boardLayout.id,
            ConstraintSet.START,
            posX * recyclerWidth
        )
        cSet.connect(
            childView.id,
            ConstraintSet.TOP,
            boardLayout.id,
            ConstraintSet.TOP,
            posY * recyclerWidth
        )
        cSet.applyTo(parentLayout)

        // 이미 이 타일에 있는 아이템 삭제
        for ((theItem, itemId) in itemMap) {
            if (theItem.point == tile.point) {
                parentLayout.removeView(findViewById(itemId))
                itemMap.remove(theItem)
                break
            }
        }

        itemMap[item] = childView.id
    }

    // 제한된 아이템을 선택중인 경우. (예시: goal)
    // 이미 그 아이템이 배치되어 있는지 확인하고, 배치가 되어있으면 삭제한다.
    private fun placeLimitedItem(tile: Tile, itemType: String) {
        for ((theItem, itemId) in itemMap) {
            if (theItem.type == itemType) {
                val parentLayout = binding.root
                parentLayout.removeView(findViewById(itemId))
                itemMap.remove(theItem)
                break
            }
        }
        placeItem(tile, itemType)
    }

    // 시작점을 배치한다.
    private fun setStartPoint(point: Point) {
        startPoint = point

        val layoutParams = binding.imgMainCharacter.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.width = recyclerWidth
        layoutParams.height = recyclerWidth
        layoutParams.marginStart = point.x * recyclerWidth
        layoutParams.topMargin = point.y * recyclerWidth
        binding.imgMainCharacter.layoutParams = layoutParams

        // 시작점이 배치될 위치의 아이템을 삭제.
        deleteItem(Tile("??", point))
    }

    // 삭제를 선택중인 경우, 해당 타일에 이미 아이템이 있다면 그것을 없앤다.
    private fun deleteItem(tile: Tile) {
        for ((item, viewId) in itemMap) {
            if (item.point == tile.point) {
                val parentLayout = binding.root
                parentLayout.removeView(findViewById((viewId)))
                itemMap.remove(item)
                break
            }
        }
    }
}

class BoardCreateAdapter(
    private val recyclerWidth: Int,
    private val activity: CreateLevelActivity
    ) : RecyclerView.Adapter<BoardCreateAdapter.MyViewHolder>(){
    var tileList = mutableListOf<Tile>()
    private var _binding : TileButtonItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        _binding = TileButtonItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        val binding = binding

        return MyViewHolder(binding, recyclerWidth, activity)
    }

    override fun getItemCount(): Int = tileList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(tileList[position])
    }
    inner class MyViewHolder(
        private var binding: TileButtonItemBinding,
        private val width: Int,
        private val activity: CreateLevelActivity
        ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tileData: Tile) {
            val imageId = when (tileData.type) {
                "land" -> R.drawable.tile_land
                "water" -> R.drawable.tile_water
                "ice" -> R.drawable.tile_ice
                else -> throw Error("${tileData.type} is not allowed.")
            }
            val button = binding.imgbtnTileItem
            val layoutParams = button.layoutParams
            layoutParams.width = width
            layoutParams.height = width
            button.layoutParams = layoutParams
            button.setImageResource(imageId)
            button.setOnClickListener {
                activity.onClickTile(tileData)
            }
        }
    }
}

class TileAdapter(
    private val recyclerWidth: Int,
    private var activity: CreateLevelActivity,
    ) : RecyclerView.Adapter<TileAdapter.MyViewHolder>(){
    private val tileList = mutableListOf(
        Tile("land", Point(0, 0)),
        Tile("water", Point(0, 0)),
        Tile("ice", Point(0, 0))
    )
    private var _binding : TileButtonItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        _binding = TileButtonItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        val binding = binding

        return MyViewHolder(binding, recyclerWidth, activity)
    }

    override fun getItemCount(): Int = tileList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(tileList[position])
    }
    inner class MyViewHolder(
        private var binding: TileButtonItemBinding,
        private val width: Int,
        private var activity: CreateLevelActivity
        ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tileData: Tile) {
            val imageId = when (tileData.type) {
                "land" -> R.drawable.tile_land
                "water" -> R.drawable.tile_water
                "ice" -> R.drawable.tile_ice
                else -> throw Error("${tileData.type} is not allowed.")
            }
            val button = binding.imgbtnTileItem
            val layoutParams = button.layoutParams
            layoutParams.width = width
            layoutParams.height = width
            button.layoutParams = layoutParams
            button.setImageResource(imageId)
            button.setOnClickListener {
                activity.currentAction = tileData.type
                activity.showCurrentAction()
            }
        }
    }
}

class ItemAdapter(
    private val recyclerWidth: Int,
    private var activity: CreateLevelActivity,
    ) : RecyclerView.Adapter<ItemAdapter.MyViewHolder>(){
    private val itemList = mutableListOf(
        Item("goal", Point(0, 0)),
        Item("life", Point(0, 0)),
        Item("fire", Point(0, 0))
    )
    private var _binding : TileButtonItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        _binding = TileButtonItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        val binding = binding

        return MyViewHolder(binding, recyclerWidth, activity)
    }

    override fun getItemCount(): Int = itemList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(itemList[position])
    }
    inner class MyViewHolder(
        private var binding: TileButtonItemBinding,
        private val width: Int,
        private var activity: CreateLevelActivity,
        ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(itemData: Item) {
            val imageId = when (itemData.type) {
                "goal" -> R.drawable.board_goal
                "life" -> R.drawable.item_life
                "fire" -> R.drawable.item_fire
                else -> throw Error("${itemData.type} is not allowed.")
            }
            val button = binding.imgbtnTileItem
            val layoutParams = button.layoutParams
            layoutParams.width = width
            layoutParams.height = width
            button.layoutParams = layoutParams
            button.setImageResource(imageId)
            button.setOnClickListener {
                activity.currentAction = itemData.type
                activity.showCurrentAction()
            }
        }
    }
}


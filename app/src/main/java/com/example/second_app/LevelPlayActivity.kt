package com.example.second_app

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.second_app.databinding.ActivityLevelPlayBinding
import com.example.second_app.databinding.TileItemBinding
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader

object ButtonDirection {
    const val LEFT = 7500
    const val RIGHT = 7501
    const val UP = 7502
    const val DOWN = 7503
}

class LevelPlayActivity: AppCompatActivity() {
    private var _binding: ActivityLevelPlayBinding? = null
    private val binding get() = _binding!!
    private val gson = Gson()
    private lateinit var activityLauncher: ActivityResultLauncher<Intent>
    private lateinit var boardAdapter: BoardAdapter
    private var arrowButtonsEnabled = true
    private var extraButtonsEnabled = true
    private var recyclerWidth = 0
    private var boardSize = 0

    // 게임 중의 변수 값
    private var temperature: Double = 0.0
    private var mainCharacterMoveType = "walking"
    private lateinit var items: MutableList<Item>
    private val itemMap: MutableMap<Item, Int> = mutableMapOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityLevelPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 팝업 액티비티를 실행하여 "temperature_score"를 key로 갖는 점수를 받는다.
        // 그 후 레벨 선택 화면으로 돌아간다.
        activityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            {
                // MEMO: 레벨을 클리어할 경우 RESULT_FIRST_USER를 리턴한다.
                // 클리어하지 못하면 +1을 하는걸로..?
                val intent = Intent()
                intent.putExtra("temperature_score", temperature)
                setResult(RESULT_FIRST_USER, intent)
                if (!isFinishing) finish()
            }


        val levelMetadata = intent.extras!!.getSerializable("level_metadata") as LevelInformation
        val isBaseLevel = intent.extras!!.getBoolean("is_base_level")

        binding.textLevelPlayTitle.text = levelMetadata.levelname

        val levelData = readLevelData(levelMetadata.id, isBaseLevel)

        Log.d("LEVEL_PLAY", "ID=${levelMetadata.id}")

        val initLevelData = levelData

        items = levelData.items.toMutableList()

        // 보드 설정.
        val board = binding.recyclerViewLevelPlayBoard
        boardSize = levelMetadata.boardsize

        // 보드 설정을 위해 스크린 크기를 구하자.
        val screenWidth = getScreenSize(this).width
        recyclerWidth = (screenWidth * 0.9 / boardSize).toInt()

        boardAdapter = BoardAdapter(recyclerWidth)
        boardAdapter.tileList = levelData.tiles
        board.adapter = boardAdapter
        board.setHasFixedSize(true)

        board.layoutManager = GridLayoutManager(this, boardSize)

        // 메인 캐릭터의 이미지와 크기 설정.
        val layoutParams = binding.mainCharacter.layoutParams
        layoutParams.width = recyclerWidth
        layoutParams.height = recyclerWidth
        binding.mainCharacter.layoutParams = layoutParams

        val constraintLayoutParams = binding.mainCharacter.layoutParams as ConstraintLayout.LayoutParams
        constraintLayoutParams.marginStart = levelData.startpoint.x * recyclerWidth
        constraintLayoutParams.topMargin = levelData.startpoint.y * recyclerWidth
        binding.mainCharacter.layoutParams = constraintLayoutParams

        // 아이템의 위치 설정
        val parentLayout = binding.innerConstraint

        for ((i, item) in items.withIndex()) {
            val cSet = ConstraintSet()
            val childView = ImageView(this)
            childView.id = View.generateViewId()
            parentLayout.addView(childView, 3)

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
                parentLayout.id,
                ConstraintSet.START,
                posX * recyclerWidth
            )
            cSet.connect(
                childView.id,
                ConstraintSet.TOP,
                parentLayout.id,
                ConstraintSet.TOP,
                posY * recyclerWidth
            )
            cSet.applyTo(parentLayout)

            itemMap[item] = childView.id
        }
        // 그 외 설정.
        temperature = levelData.inittemp
        setTemperature()

        binding.imgbtnLevelPlayUp.setOnClickListener {
            moveCharacter(ButtonDirection.UP)
        }
        binding.imgbtnLevelPlayDown.setOnClickListener {
            moveCharacter(ButtonDirection.DOWN)
        }
        binding.imgbtnLevelPlayLeft.setOnClickListener {
            moveCharacter(ButtonDirection.LEFT)
        }
        binding.imgbtnLevelPlayRight.setOnClickListener {
            moveCharacter(ButtonDirection.RIGHT)
        }
        binding.btnLevelPlayExtra.setOnClickListener {
            handleButtonAvailability(isArrowButton = false)
            dropTemperature(0.5)
            handleButtonAvailability(isArrowButton = false)
        }
    }

    // 이 시점에서 levelid.json 파일은 반드시 존재해야 한다.
    private fun readLevelData(id: Int, isBaseLevel: Boolean): LevelData {
        val jsonString: String
        if (isBaseLevel) {
            // 기본 레벨인 경우
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
            // 사용자가 만든 레벨인 경우
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

    private fun handleButtonAvailability(isArrowButton: Boolean) {
        if (isArrowButton) {
            arrowButtonsEnabled = !arrowButtonsEnabled
            binding.imgbtnLevelPlayDown.isEnabled = arrowButtonsEnabled
            binding.imgbtnLevelPlayLeft.isEnabled = arrowButtonsEnabled
            binding.imgbtnLevelPlayUp.isEnabled = arrowButtonsEnabled
            binding.imgbtnLevelPlayDown.isEnabled = arrowButtonsEnabled
        }
        else {
            extraButtonsEnabled = !extraButtonsEnabled
            binding.imgbtnLevelPlayDown.isEnabled = extraButtonsEnabled
            binding.imgbtnLevelPlayUp.isEnabled = extraButtonsEnabled
            binding.imgbtnLevelPlayLeft.isEnabled = extraButtonsEnabled
            binding.imgbtnLevelPlayRight.isEnabled = extraButtonsEnabled
        }
    }

    // 캐릭터를 direction이 가리키는 방향으로 움직인다.
    // TODO: 애니메이션을 추가해보자.
    private fun moveCharacter(direction: Int) {
        handleButtonAvailability(isArrowButton = true)

        val constraintLayoutParams = binding.mainCharacter.layoutParams as ConstraintLayout.LayoutParams
        val posX = constraintLayoutParams.marginStart / recyclerWidth
        val posY = constraintLayoutParams.topMargin / recyclerWidth
        val tiles = boardAdapter.tileList
        var resultX = posX
        var resultY = posY

        when (direction) {
            ButtonDirection.LEFT -> {
                if (posX == 0) {
//                    Toast.makeText(this, "이미 왼쪽 끝이야!", Toast.LENGTH_SHORT).show()
                }
                else {
                    val targetTile = tiles[posX + boardSize * posY - 1]
                    if (checkMovable(targetTile.type)) {
                        constraintLayoutParams.marginStart -= recyclerWidth
                        raiseTemperature(0.1)
                        resultX -= 1
                    }
                    else {
//                        Toast.makeText(this, "물 위로는 갈 수 없어!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            ButtonDirection.RIGHT -> {
                if (posX == boardSize - 1) {
//                    Toast.makeText(this, "이미 오른쪽 끝이야!", Toast.LENGTH_SHORT).show()
                }
                else {
                    val targetTile = tiles[posX + boardSize * posY + 1]
                    if (checkMovable(targetTile.type)) {
                        constraintLayoutParams.marginStart += recyclerWidth
                        raiseTemperature(0.1)
                        resultX += 1
                    }
                    else {
//                        Toast.makeText(this, "물 위로는 갈 수 없어!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            ButtonDirection.UP -> {
                if (posY == 0) {
//                    Toast.makeText(this, "이미 위쪽 끝이야!", Toast.LENGTH_SHORT).show()
                }
                else {
                    val targetTile = tiles[posX + boardSize * (posY - 1)]
                    if (checkMovable(targetTile.type)) {
                        constraintLayoutParams.topMargin -= recyclerWidth
                        raiseTemperature(0.1)
                        resultY -= 1
                    }
                    else {
//                        Toast.makeText(this, "물 위로는 갈 수 없어!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            ButtonDirection.DOWN -> {
                if (posY == boardSize - 1) {
//                    Toast.makeText(this, "이미 아래쪽 끝이야!", Toast.LENGTH_SHORT).show()
                }
                else {
                    val targetTile = tiles[posX + boardSize * (posY + 1)]
                    if (checkMovable(targetTile.type)) {
                        constraintLayoutParams.topMargin += recyclerWidth
                        raiseTemperature(0.1)
                        resultY += 1
                    }
                    else {
//                        Toast.makeText(this, "물 위로는 갈 수 없어!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else -> throw Error("Direction $direction not allowed.")
        }

        binding.mainCharacter.layoutParams = constraintLayoutParams
        checkItemEvent(resultX, resultY)
        handleButtonAvailability(isArrowButton = true)
    }

    // 대상 타일로 움직일 수 있는지 확인한다.
    private fun checkMovable(tileType: String): Boolean {
        return when (mainCharacterMoveType) {
            "walking" -> when (tileType) {
                "water" -> false
                else -> true
            }
            else -> false
        }
    }

    // posX, posY 타일로 이동했을 때, 아이템이 있는지 확인한다.
    private fun checkItemEvent(posX: Int, posY: Int) {
        for (item in items) {
            if (posX == item.point.x && posY == item.point.y) {
                when (item.type) {
                    "goal" -> {
                        val intent = Intent(this, LevelCompleteActivity::class.java)
                        intent.putExtra("temperature_score", temperature)
                        activityLauncher.launch(intent)
                    }
                    "life" -> {
                        dropTemperature(1.0)
                        val viewId = itemMap[item]!!
                        val parentLayout = binding.innerConstraint
                        parentLayout.removeView(findViewById(viewId))
                        itemMap.remove(item)
                        items.remove(item)
                    }
                    "fire" -> {
                        raiseTemperature(2.0)
                        val viewId = itemMap[item]!!
                        val parentLayout = binding.innerConstraint
                        parentLayout.removeView(findViewById(viewId))
                        itemMap.remove(item)
                        items.remove(item)
                    }
                    else -> throw Error("Item type ${item.type} is not allowed")
                }
                break
            }
        }
    }

    // 온도 표시 및 수정 함수들.
    private fun setTemperature() {
        binding.progressBarLevelPlayTemperature.max=100
        val str = String.format(resources.getString(R.string.level_play_temperature_display), displayTemperature())
        when {
            temperature > 30.0 -> binding.textLevelPlayTemperature.setTextColor(ContextCompat.getColor(this, R.color.red))
            temperature > 25.0 -> binding.textLevelPlayTemperature.setTextColor(ContextCompat.getColor(this, R.color.green))
            else -> binding.textLevelPlayTemperature.setTextColor(ContextCompat.getColor(this, R.color.blue))
        }
        binding.textLevelPlayTemperature.text = str
        binding.progressBarLevelPlayTemperature.progress = (temperature*10-200).toInt()

    }

    private fun raiseTemperature(temp: Double) {
        assert(temp > 0.0)
        temperature += temp
        setTemperature()
    }

    private fun dropTemperature(temp: Double) {
        assert(temp > 0.0)
        temperature -= temp
        setTemperature()
    }

    private fun displayTemperature(): String {
        return String.format("%.1f", temperature)
    }
}

class BoardAdapter(private val recyclerWidth: Int) : RecyclerView.Adapter<BoardAdapter.MyViewHolder>(){
    var tileList = mutableListOf<Tile>()
    private var _binding : TileItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        _binding = TileItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        val binding = binding

        return MyViewHolder(binding, recyclerWidth)
    }

    override fun getItemCount(): Int = tileList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(tileList[position])
    }
    inner class MyViewHolder(private var binding: TileItemBinding, private val width: Int) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tileData: Tile) {
            val imageId = when (tileData.type) {
                "land" -> R.drawable.tile_land
                "water" -> R.drawable.tile_water
                "ice" -> R.drawable.tile_ice
                else -> throw Error("${tileData.type} is not allowed.")
            }
            val layoutParams = binding.imgTileItem.layoutParams
            layoutParams.width = width
            layoutParams.height = width
            binding.imgTileItem.layoutParams = layoutParams
            binding.imgTileItem.setImageResource(imageId)
        }
    }
}

fun getScreenSize(context: Context): Size {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        val metrics: WindowMetrics = context.getSystemService(WindowManager::class.java).currentWindowMetrics
        return Size(metrics.bounds.width(), metrics.bounds.height())
    }
    else {
        @Suppress("DEPRECATION")
        val display = context.getSystemService(WindowManager::class.java).defaultDisplay
        val metrics = if (display != null) {
            DisplayMetrics().also {
                @Suppress("DEPRECATION")
                display.getRealMetrics(it)
            }
        } else {
            Resources.getSystem().displayMetrics
        }
        return Size(metrics.widthPixels, metrics.heightPixels)
    }
}
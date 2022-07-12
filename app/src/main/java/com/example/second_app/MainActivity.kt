package com.example.second_app

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.example.second_app.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.kakao.sdk.talk.TalkApiClient
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, CoroutineScope {
    lateinit var navigationView: NavigationView
    lateinit var drawerLayout: DrawerLayout
    private var mBinding : ActivityMainBinding?= null
    private val binding get() = mBinding!!
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext get() = Dispatchers.IO + job
    lateinit var sharedManager: SharedManager
    override fun onResume() {
        super.onResume()
        // put your code here...
        val header = navigationView.getHeaderView(0)
        val userRating = header.findViewById<TextView>(R.id.tv_main_rating)
        userRating.text = "내 경험치 : " + sharedManager.getUserInfo().rating.toString()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedManager = SharedManager(this) // sharedManager 초기화
        val toolbar: Toolbar = findViewById(R.id.toolbar) // toolBar를 통해 App Bar 생성
        setSupportActionBar(toolbar) // 툴바 적용
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.appbar_menu) // 홈버튼 이미지 변경
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 네비게이션 드로어 생성
        drawerLayout = findViewById(R.id.drawer_layout)

        // 네비게이션 드로어 내에있는 화면의 이벤트를 처리하기 위해 생성
        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this) //navigation 리스너

//        val keyHash = Utility.getKeyHash(this)
//        Log.d("Hash", keyHash)

        binding.btnMainStore.setOnClickListener {
            val intent = Intent(this, StoreEnterActivity::class.java)
            startActivity(intent)
        }

        binding.btnMainBaseLevel.setOnClickListener {
            val intent = Intent(this, BaseLevelsActivity::class.java)
            startActivity(intent)
        }

        binding.btnMainUserRating.setOnClickListener {
            val intent = Intent(this, UserRatingActivity::class.java)
            startActivity(intent)
        }

        val header = navigationView.getHeaderView(0)
        val profileImg = header.findViewById<ImageView>(R.id.iv_main_user)
        val userName = header.findViewById<TextView>(R.id.tv_main_user_name)
        val userRating = header.findViewById<TextView>(R.id.tv_main_rating)

        // 카카오톡 프로필 가져오기
        TalkApiClient.instance.profile { profile, error ->
            if (error != null) {
                Log.e(TAG, "카카오톡 프로필 가져오기 실패", error)
            } else if (profile != null) {
                Log.i(
                    TAG, "카카오톡 프로필 가져오기 성공" +
                            "\n닉네임: ${profile.nickname}" +
                            "\n프로필사진: ${profile.thumbnailUrl}"
                )

                userName.text = profile.nickname
                userRating.text = "내 경험치 : " + sharedManager.getUserInfo().rating.toString()
                Glide.with(this).load(profile.thumbnailUrl).circleCrop().into(profileImg)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 클릭한 툴바 메뉴 아이템 id 마다 다르게 실행하도록 설정
        when (item!!.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    // 드로어 내 아이템 클릭 이벤트 처리하는 함수 : logout
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_logout -> {
                Toast.makeText(this,"Logout", Toast.LENGTH_SHORT).show()
                UserApiClient.instance.logout { error ->
                    if (error != null) {
                        Log.e(TAG, "로그아웃 실패. SDK에서 토큰 삭제됨", error)
                    }
                    else {
                        Log.i(TAG, "로그아웃 성공. SDK에서 토큰 삭제됨")
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        startActivity(intent)
                    }
                }
            }
        }
        return true
    }
    override fun onBackPressed(){
        finishAffinity();
        System.runFinalization();
        System.exit(0);
    }
}
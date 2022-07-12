package com.example.second_app

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkInfo
import android.net.NetworkRequest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast

class SplashActivity : AppCompatActivity() {

    private lateinit var cm2 : ConnectivityManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Toast.makeText(this, "인터넷 연결을 확인하고 있습니다.", Toast.LENGTH_SHORT)

        cm2 = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val builder = NetworkRequest.Builder()
        cm2.registerNetworkCallback(builder.build(),networkCallBack)

        // NetworkInfo부분
        if(isConnectInternet() != "null"){
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                val intent = Intent(baseContext, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }, 2000) // 2초
        }
        else{
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                Toast.makeText(this@SplashActivity, "네트워크 연결 실패", Toast.LENGTH_LONG).show()
                finish()
            }, 3000) // 3초
        }
    }


    private val networkCallBack = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            // 네트워크가 연결될 때 호출됩니다.
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                Toast.makeText(this@SplashActivity, "네트워크 연결 성공$network", Toast.LENGTH_SHORT).show()
            }, 1000)
        }

        override fun onLost(network: Network) {
            // 네트워크가 끊길 때 호출됩니다.
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                Toast.makeText(this@SplashActivity, "네트워크 연결 실패", Toast.LENGTH_LONG).show()
                finish()
            }, 1000)
        }
    }


    override fun onDestroy() { // 콜백 해제
        super.onDestroy()
        cm2 = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm2.unregisterNetworkCallback(networkCallBack)
    }

    private fun isConnectInternet(): String { // 인터넷 연결 체크 함수
        val cm: ConnectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = cm.activeNetworkInfo
        return networkInfo.toString()
    }
}


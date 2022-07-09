package com.example.second_app

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.second_app.databinding.ActivityLoginBinding
import com.google.gson.Gson
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.common.model.KakaoSdkError
import com.kakao.sdk.talk.TalkApiClient
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class LoginActivity : AppCompatActivity(), CoroutineScope {
    private var mBinding : ActivityLoginBinding?= null
    private val binding get() = mBinding!!

    private val httpRequest = HttpRequest()
    private val gson = Gson()

    private val job = Job()
    override val coroutineContext: CoroutineContext get() = Dispatchers.IO + job


    val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Log.e(TAG, "카카오계정으로 로그인 실패", error)
        } else if (token != null) {
            Log.i(TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val intent = Intent(this, MainActivity::class.java)
        //로그인
        binding.btnLoginLogin.setOnClickListener{
            UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
                if (error != null) {
                    Log.e(TAG, "로그인 실패", error)
                }
                else if (token != null) {
                    Log.i(TAG, "로그인 성공 ${token.accessToken}")
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

                    TalkApiClient.instance.profile { profile, error ->
                        if (error != null) {
                            Log.e(TAG, "카카오톡 프로필 가져오기 실패", error)
                        }
                        else if (profile != null) {
                            val postBody = gson.toJson(UserInformation(token.accessToken, 0, profile.nickname?:""))
                            httpRequest.request("POST", "/users", postBody, CoroutineScope(coroutineContext))
                        }
                    }

                    startActivity(intent)
                    finish()
                }
            }
        }
        //로그아웃
        binding.btnLoginLogout.setOnClickListener{
            UserApiClient.instance.logout { error ->
                if (error != null) {
                    Log.e(TAG, "로그아웃 실패. SDK에서 토큰 삭제됨", error)
                }
                else {
                    Log.i(TAG, "로그아웃 성공. SDK에서 토큰 삭제됨")
                }
            }
        }
        //개발 : 로그인 skip 버튼
        binding.btnLoginDev.setOnClickListener{
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
            finish()
        }
        //자동 로그인
        if (AuthApiClient.instance.hasToken()) {
            UserApiClient.instance.accessTokenInfo { _, error ->
                if (error != null) {
                    if (error is KakaoSdkError && error.isInvalidTokenError()) {
                        throw(error)
                    }
                    else {
                    }
                }
                else {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    finish()
                }
            }
        }
        else {
        }
    }
    override fun onDestroy() {
        // onDestroy 에서 binding class 인스턴스 참조를 정리해주어야 한다.
        mBinding = null
        super.onDestroy()
        job.cancel()
    }
}
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
        Log.e("size: ",getScreenSize(this).toString())
        //로그인
        binding.btnLoginLogin.setOnClickListener{
            UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
                if (error != null) {
                    Log.e(TAG, "로그인 실패", error)
                }
                else if (token != null) {
                    Log.i(TAG, "로그인 성공 ${token.accessToken}")
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    // 사용자 정보 요청 (기본)
                    UserApiClient.instance.me { user, error ->
                        if (error != null) {
                            Log.e(TAG, "사용자 정보 요청 실패", error)
                        }
                        else if (user != null) {
                            Log.i(TAG, "사용자 정보 요청 성공" +
                                    "\n회원번호: ${user.id}" +
                                    "\n이메일: ${user.kakaoAccount?.email}" +
                                    "\n닉네임: ${user.kakaoAccount?.profile?.nickname}" +
                                    "\n프로필사진: ${user.kakaoAccount?.profile?.thumbnailImageUrl}")

                            // 유저 정보 요청 방식을 수정해서 이 작업을 할 수 있게 만들자.
                            // 1. GET /users/id 를 보내 유저 정보가 있는지 확인한다.
                            // 2. 유저 정보가 있다면 그것을 Preference에 등록한다.
                            // 3. 유저 정보가 없다면 POST 요청을 보내고, Preference에 등록한다.
                            val userInfo = httpRequest.requestGeneral<UserInformation>("GET", "/users/${user.id}", "", CoroutineScope(coroutineContext))
                            val realUserInfo = if (userInfo == null) {
                                val rInfo = UserInformation(user.id.toString(), 0, user.kakaoAccount?.profile?.nickname.toString())
                                val postBody = gson.toJson(rInfo)
                                httpRequest.request("POST", "/users", postBody, CoroutineScope(coroutineContext))
                                rInfo
                            }
                            else {
                                userInfo
                            }

                            // MEMO: 이 시점부터 sharedManger를 불러오면 글로벌 변수에 접근할 수 있다.
                            val sharedManager = SharedManager(this)
                            sharedManager.saveUserInfo(realUserInfo)
                        }
                    }
                    startActivity(intent)
                    finish()
                }
            }
        }
        /*
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
        }*/
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
                    UserApiClient.instance.me { user, error ->
                        if (error != null) {
                            Log.e(TAG, "사용자 정보 요청 실패", error)
                        }
                        else if (user != null) {
                            Log.i(TAG, "사용자 정보 요청 성공" +
                                    "\n회원번호: ${user.id}" +
                                    "\n이메일: ${user.kakaoAccount?.email}" +
                                    "\n닉네임: ${user.kakaoAccount?.profile?.nickname}" +
                                    "\n프로필사진: ${user.kakaoAccount?.profile?.thumbnailImageUrl}")

                            // 유저 정보 요청 방식을 수정해서 이 작업을 할 수 있게 만들자.
                            // 1. GET /users/id 를 보내 유저 정보가 있는지 확인한다.
                            // 2. 유저 정보가 있다면 그것을 Preference에 등록한다.
                            // 3. 유저 정보가 없다면 POST 요청을 보내고, Preference에 등록한다.
                            val userInfo = httpRequest.requestGeneral<UserInformation>("GET", "/users/${user.id}", "", CoroutineScope(coroutineContext))
                            val realUserInfo = if (userInfo == null) {
                                val rInfo = UserInformation(user.id.toString(), 0, user.kakaoAccount?.profile?.nickname.toString())
                                val postBody = gson.toJson(rInfo)
                                httpRequest.request("POST", "/users", postBody, CoroutineScope(coroutineContext))
                                rInfo
                            }
                            else {
                                userInfo
                            }

                            // MEMO: 이 시점부터 sharedManger를 불러오면 글로벌 변수에 접근할 수 있다.
                            val sharedManager = SharedManager(this)
                            sharedManager.saveUserInfo(realUserInfo)
                        }
                    }

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
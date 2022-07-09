package com.example.second_app

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.Serializable
import java.net.HttpURLConnection
import java.net.URL
import java.util.logging.Level
import kotlin.String

// 서버 IP 주소.
const val ipAddress = "http://192.249.18.201"

class HttpRequest {
    val gson = Gson()

    fun request(method: String, urlStr: String, scope: CoroutineScope): String? = runBlocking {
        withContext(scope.coroutineContext) {
            val stringBuilder = StringBuilder()
            try {
                val url = URL(ipAddress + urlStr)
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 1000
                connection.requestMethod = method
                connection.doInput = true

                val resCode = connection.responseCode
                if (resCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = InputStreamReader(connection.inputStream)
                    val reader = BufferedReader(inputStream)
                    var line: String?
                    while (true) {
                        line = reader.readLine()
                        if (line == null) break
                        stringBuilder.append(line + "\n")
                    }
                    inputStream.close()
                    reader.close()
                    connection.disconnect()

                    val response = stringBuilder.toString()
                    Log.d("OK", response)
                    response
                } else {
                    Log.d("CODE", "request code: $resCode")
                    null
                }
            } catch (e: IOException) {
                Log.d("IO_ERR", "request error: ${e.message!!}")
                null
            }
        }
    }

    fun requestLevelList(urlStr: String, scope: CoroutineScope): MutableList<LevelInformation>? {
        val response = request("GET", urlStr, scope)
        val itemType = object: TypeToken<MutableList<LevelInformation>>() {}.type
        return if (response == null) {
            null
        } else {
            gson.fromJson(response, itemType)
        }
    }

    fun requestLevel(method: String, urlStr: String, scope: CoroutineScope): LevelData? {
        val response = request(method, urlStr, scope)
        val itemType = object: TypeToken<LevelData>() {}.type
        return if (response == null) {
            null
        } else {
            gson.fromJson(response, itemType)
        }
    }

    inline fun <reified T: Serializable> requestGeneral(method: String, urlStr: String, scope: CoroutineScope): T? {
        val response = request(method, urlStr, scope)
        val itemType = object: TypeToken<T>() {}.type
        return if (response == null) {
            null
        } else {
            gson.fromJson(response, itemType)
        }
    }
}
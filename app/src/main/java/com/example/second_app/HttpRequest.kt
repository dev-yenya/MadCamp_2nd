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

const val ipAddress = "http://172.10.5.165"

class HttpRequest {
    private val gson = Gson()

    fun requestLevelList(urlStr: String, scope: CoroutineScope): MutableList<LevelInformation>? = runBlocking {
        withContext(scope.coroutineContext) {
            val stringBuilder = StringBuilder()
            try {
                val url = URL(ipAddress + urlStr)
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 1000
                connection.requestMethod = "GET"
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

                    val itemType = object : TypeToken<MutableList<LevelInformation>>() {}.type
                    val returnList: MutableList<LevelInformation> = gson.fromJson(response, itemType)
                    returnList
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

    fun requestLevel(method: String, urlStr: String, scope: CoroutineScope): LevelData? = runBlocking {
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

                    gson.fromJson(response, LevelData::class.java)
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
}
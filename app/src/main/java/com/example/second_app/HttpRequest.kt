package com.example.second_app

import android.util.Log
import com.google.gson.Gson
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
import kotlin.String

class HttpRequest {
    val gson = Gson()

    inline fun <reified T: Serializable> request(method: String, urlStr: String, scope: CoroutineScope): T? = runBlocking {
        withContext(scope.coroutineContext) {
            val stringBuilder = StringBuilder()
            try {
                val url = URL(urlStr)
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 10000
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
                    gson.fromJson(response, T::class.java)
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
package com.xhan.tracker.internal

import android.util.Log
import com.xhan.tracker.model.Event
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * HttpURLConnection 기반의 실제 HTTP 네트워크 클라이언트.
 * 외부 라이브러리 없이 Android 기본 API만 사용하여 이벤트를 서버로 전송한다.
 *
 * @param baseUrl 서버 기본 URL (예: "https://api.apptracker.com")
 * @param isDebug true이면 요청/응답 로그 출력
 */
internal class HttpNetworkClient(
    private val baseUrl: String,
    private val isDebug: Boolean = false
) : NetworkClient {

    override fun send(events: List<Event>): Boolean {
        val url = URL("$baseUrl/events")
        val json = buildJson(events)

        if (isDebug) {
            Log.d("HttpNetworkClient", "POST $url")
            Log.d("HttpNetworkClient", "Body: $json")
        }

        val connection = url.openConnection() as HttpURLConnection
        return try {
            // 타임아웃 설정
            connection.connectTimeout = 5_000  // 5초
            connection.readTimeout = 10_000    // 10초
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.doOutput = true

            // JSON 본문 전송
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(json)
                writer.flush()
            }

            val responseCode = connection.responseCode

            if (isDebug) {
                Log.d("HttpNetworkClient", "Response: $responseCode")
            }

            // 200~299 이면 성공
            responseCode in 200..299
        } catch (e: Exception) {
            if (isDebug) {
                Log.e("HttpNetworkClient", "전송 실패: ${e.message}", e)
            }
            false
        } finally {
            connection.disconnect()
        }
    }

    /**
     * 이벤트 목록을 JSON 문자열로 직렬화한다.
     * Android 기본 내장 org.json 라이브러리를 사용한다.
     */
    private fun buildJson(events: List<Event>): String {
        val jsonArray = JSONArray()
        for (event in events) {
            val eventObj = JSONObject().apply {
                put("name", event.name)
                put("properties", JSONObject(event.properties))
                put("timestamp", event.timestamp)
            }
            jsonArray.put(eventObj)
        }
        return JSONObject().apply {
            put("events", jsonArray)
        }.toString()
    }
}

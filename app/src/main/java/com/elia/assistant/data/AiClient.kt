package com.elia.assistant.data
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AiClient {
    fun ask(endpoint:String, history:List<ChatMessage>, memories:List<String>):String {
        if(endpoint.isBlank()) return "I’m ready, Miss Presh, but my secure AI connection has not been configured yet. Open Settings and add your backend URL."
        val c=(URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod="POST"; connectTimeout=15000; readTimeout=30000; doOutput=true
            setRequestProperty("Content-Type","application/json")
        }
        val messages=JSONArray().apply {
            put(JSONObject().apply { put("role","system"); put("content","You are ELIA, a sophisticated personal AI assistant. Always address the user as Miss Presh. Be calm, intelligent, warm, elegant, confident and subtly witty. Never call yourself ChatGPT.") })
            history.takeLast(30).forEach { m -> put(JSONObject().apply { put("role",if(m.role=="user")"user" else "assistant");put("content",m.text) }) }
        }
        val body=JSONObject().apply { put("messages",messages);put("memories",JSONArray(memories)) }.toString()
        c.outputStream.use { it.write(body.toByteArray()) }
        val stream=if(c.responseCode in 200..299)c.inputStream else c.errorStream
        val response=stream.bufferedReader().use{it.readText()}
        if(c.responseCode !in 200..299) throw IllegalStateException("Backend error ${c.responseCode}")
        return JSONObject(response).optString("reply","I received an empty response, Miss Presh.")
    }
}

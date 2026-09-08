package com.elia.assistant.data
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class LocalStore(context: Context) {
    private val p = context.getSharedPreferences("elia_store", Context.MODE_PRIVATE)
    fun saveMessages(list: List<ChatMessage>) {
        val a=JSONArray(); list.forEach { a.put(JSONObject().apply { put("id",it.id);put("role",it.role);put("text",it.text);put("timestamp",it.timestamp) }) }
        p.edit().putString("messages",a.toString()).apply()
    }
    fun loadMessages(): MutableList<ChatMessage> {
        val a=JSONArray(p.getString("messages","[]") ?: "[]")
        return MutableList(a.length()) { i -> a.getJSONObject(i).let { ChatMessage(it.getLong("id"),it.getString("role"),it.getString("text"),it.getLong("timestamp")) } }
    }
    fun saveMemories(list: List<String>) { val a=JSONArray(); list.forEach(a::put); p.edit().putString("memories",a.toString()).apply() }
    fun loadMemories(): MutableList<String> { val a=JSONArray(p.getString("memories","[]") ?: "[]"); return MutableList(a.length()){i->a.getString(i)} }
    fun backendUrl()=p.getString("backend_url","")?:""
    fun saveBackendUrl(v:String)=p.edit().putString("backend_url",v).apply()
    fun voiceEnabled()=p.getBoolean("voice_enabled",true)
    fun setVoiceEnabled(v:Boolean)=p.edit().putBoolean("voice_enabled",v).apply()
    fun clearAll()=p.edit().clear().apply()
}

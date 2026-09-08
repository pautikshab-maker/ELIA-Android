package com.elia.assistant.ui
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elia.assistant.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EliaViewModel(app:Application):AndroidViewModel(app){
    private val store=LocalStore(app); private val client=AiClient()
    private val _messages=MutableStateFlow(store.loadMessages().toList()); val messages=_messages.asStateFlow()
    private val _memories=MutableStateFlow(store.loadMemories().toList()); val memories=_memories.asStateFlow()
    private val _thinking=MutableStateFlow(false); val thinking=_thinking.asStateFlow()
    private val _url=MutableStateFlow(store.backendUrl()); val backendUrl=_url.asStateFlow()
    private val _voice=MutableStateFlow(store.voiceEnabled()); val voiceEnabled=_voice.asStateFlow()
    fun send(text:String){
        val s=text.trim(); if(s.isEmpty()||_thinking.value)return
        val now=_messages.value.toMutableList(); now+=ChatMessage(role="user",text=s); _messages.value=now; store.saveMessages(now); remember(s)
        viewModelScope.launch(Dispatchers.IO){ _thinking.value=true
            val reply=runCatching{client.ask(_url.value,_messages.value,_memories.value)}.getOrElse{"I’m sorry, Miss Presh. I couldn’t reach my AI service. Please check the backend URL and your connection."}
            val updated=_messages.value.toMutableList(); updated+=ChatMessage(role="assistant",text=reply); _messages.value=updated; store.saveMessages(updated); _thinking.value=false
        }
    }
    private fun remember(s:String){ val prefix="remember that"; if(s.lowercase().startsWith(prefix)){val m=s.substring(prefix.length).trim().trimEnd('.', '!', '?'); if(m.isNotBlank()&&!_memories.value.contains(m)){val x=_memories.value+m;_memories.value=x;store.saveMemories(x)}}}
    fun deleteMemory(m:String){val x=_memories.value.filterNot{it==m};_memories.value=x;store.saveMemories(x)}
    fun clearMemories(){_memories.value=emptyList();store.saveMemories(emptyList())}
    fun clearConversations(){_messages.value=emptyList();store.saveMessages(emptyList())}
    fun saveBackendUrl(s:String){_url.value=s.trim();store.saveBackendUrl(s.trim())}
    fun setVoiceEnabled(v:Boolean){_voice.value=v;store.setVoiceEnabled(v)}
    fun clearAll(){store.clearAll();_messages.value=emptyList();_memories.value=emptyList();_url.value="";_voice.value=true}
}

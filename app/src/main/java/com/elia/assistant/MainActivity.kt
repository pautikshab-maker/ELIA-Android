package com.elia.assistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elia.assistant.data.ChatMessage
import com.elia.assistant.ui.EliaViewModel
import java.util.Locale

class MainActivity:ComponentActivity(){
    private val vm by viewModels<EliaViewModel>(); private var tts:TextToSpeech?=null
    override fun onCreate(b:Bundle?){super.onCreate(b);tts=TextToSpeech(this){if(it==TextToSpeech.SUCCESS)tts?.language=Locale.UK};setContent{App()}}
    override fun onDestroy(){tts?.stop();tts?.shutdown();super.onDestroy()}
    private fun speak(s:String){if(vm.voiceEnabled.value)tts?.speak(s,TextToSpeech.QUEUE_FLUSH,null,"elia")}
    @Composable fun App(){
        var page by remember{mutableStateOf("chat")}
        MaterialTheme(colorScheme=darkColorScheme(background=Color(0xFF090A10),surface=Color(0xFF12141D),primary=Color(0xFF67D5FF),secondary=Color(0xFF9AA7FF))){
            Surface(Modifier.fillMaxSize()){when(page){
                "chat"->Chat({page="settings"},{page="memories"}); "settings"->Settings{page="chat"}; else->Memories{page="chat"}
            }}
        }
    }
    @Composable fun Chat(settings:()->Unit,memories:()->Unit){
        val msgs by vm.messages.collectAsState(); val thinking by vm.thinking.collectAsState(); val voice by vm.voiceEnabled.collectAsState()
        var input by remember{mutableStateOf("")}; var listening by remember{mutableStateOf(false)}
        val pulse by rememberInfiniteTransition(label="orb").animateFloat(0.96f,1.04f,infiniteRepeatable(tween(1400),RepeatMode.Reverse),label="pulse")
        val askPermission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){if(it)startListening({listening=false}, {s->vm.send(s);listening=false}, {listening=false})}
        Column(Modifier.fillMaxSize().background(Color(0xFF090A10)).padding(18.dp)){
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
                Column{Text("ELIA",fontSize=30.sp,fontWeight=FontWeight.Bold);Text(if(listening)"Listening…"else if(thinking)"Thinking…"else"Ready",color=MaterialTheme.colorScheme.primary)}
                Row{IconButton(onClick=memories){Icon(Icons.Default.Psychology,"Memories")};IconButton(onClick=settings){Icon(Icons.Default.Settings,"Settings")}}
            }
            Box(Modifier.fillMaxWidth().padding(vertical=14.dp),contentAlignment=Alignment.Center){
                Box(Modifier.size(150.dp).scale(if(listening||thinking)pulse+0.04f else pulse).background(Brush.radialGradient(listOf(Color(0xFF67D5FF),Color(0xFF243E7A),Color.Transparent)),CircleShape))
                Text("ELIA",fontSize=24.sp,fontWeight=FontWeight.SemiBold)
            }
            LazyColumn(Modifier.weight(1f).fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(10.dp)){
                if(msgs.isEmpty())item{Text("Good evening, Miss Presh.\n\nI’m ELIA, your personal AI assistant.\n\nHow may I assist you?",fontSize=18.sp,lineHeight=28.sp)}
                items(msgs,key={it.id}){m->Bubble(m)}
                if(thinking)item{Text("ELIA is thinking…",color=MaterialTheme.colorScheme.primary)}
            }
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
                OutlinedTextField(input,{input=it},Modifier.weight(1f),placeholder={Text("Message ELIA…")},shape=RoundedCornerShape(22.dp),maxLines=4)
                IconButton(onClick={if(input.isNotBlank()){vm.send(input);input=""}}){Icon(Icons.Default.Send,"Send")}
                IconButton(onClick={
                    if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED){listening=true;startListening({listening=false},{s->vm.send(s);listening=false},{listening=false})}
                    else askPermission.launch(Manifest.permission.RECORD_AUDIO)
                }){Icon(if(listening)Icons.Default.MicOff else Icons.Default.Mic,"Microphone")}
            }
        }
    }
    private fun startListening(end:()->Unit,result:(String)->Unit,error:()->Unit){
        if(!SpeechRecognizer.isRecognitionAvailable(this)){error();return}
        val r=SpeechRecognizer.createSpeechRecognizer(this)
        r.setRecognitionListener(object:RecognitionListener{
            override fun onReadyForSpeech(p:Bundle?){}
            override fun onBeginningOfSpeech(){}
            override fun onRmsChanged(v:Float){}
            override fun onBufferReceived(b:ByteArray?){}
            override fun onEndOfSpeech(){end()}
            override fun onError(e:Int){error();r.destroy()}
            override fun onResults(b:Bundle?){b?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let(result);r.destroy()}
            override fun onPartialResults(b:Bundle?){}
            override fun onEvent(t:Int,b:Bundle?){}
        })
        r.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply{putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)})
    }
    @Composable fun Bubble(m:ChatMessage){
        val user=m.role=="user";Row(Modifier.fillMaxWidth(),horizontalArrangement=if(user)Arrangement.End else Arrangement.Start){
            Surface(shape=RoundedCornerShape(18.dp),color=if(user)Color(0xFF1D3557)else Color(0xFF151824),modifier=Modifier.widthIn(max=330.dp).clickable(enabled=!user){speak(m.text)}){
                Text(m.text,Modifier.padding(14.dp),fontSize=16.sp)
            }
        }
    }
    @Composable fun Settings(back:()->Unit){
        var url by remember{mutableStateOf(vm.backendUrl.value)};val voice by vm.voiceEnabled.collectAsState()
        Column(Modifier.fillMaxSize().padding(18.dp)){
            Row(verticalAlignment=Alignment.CenterVertically){IconButton(onClick=back){Icon(Icons.Default.ArrowBack,"Back")};Text("Settings",fontSize=26.sp,fontWeight=FontWeight.Bold)}
            Spacer(Modifier.height(20.dp));Text("Secure AI backend",fontWeight=FontWeight.Bold)
            OutlinedTextField(url,{url=it},Modifier.fillMaxWidth(),label={Text("Backend URL")},placeholder={Text("https://your-server.example/chat")})
            Text("Private AI keys must stay on your backend, never inside ELIA.",fontSize=13.sp)
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Spoken responses");Switch(voice,vm::setVoiceEnabled)}
            Button(onClick={vm::clearConversations}){Text("Clear conversation history")}
            OutlinedButton(onClick=vm::clearAll){Text("Clear all ELIA data")}
            Button(onClick={vm.run{ {saveBackendUrl(url);back()} }}){Text("Save & return")}
            Spacer(Modifier.height(20.dp));Text("ELIA — Personal AI Assistant");Text("Version 1.0.0")
        }
    }
    @Composable fun Memories(back:()->Unit){
        val mem by vm.memories.collectAsState()
        Column(Modifier.fillMaxSize().padding(18.dp)){
            Row(verticalAlignment=Alignment.CenterVertically){IconButton(onClick=back){Icon(Icons.Default.ArrowBack,"Back")};Text("Memories",fontSize=26.sp,fontWeight=FontWeight.Bold)}
            Text("Only information you explicitly ask ELIA to remember is stored.",fontSize=13.sp)
            LazyColumn(Modifier.weight(1f)){items(mem){m->Row(Modifier.fillMaxWidth().padding(vertical=8.dp),verticalAlignment=Alignment.CenterVertically){Text(m,Modifier.weight(1f));IconButton(onClick={vm.deleteMemory(m)}){Icon(Icons.Default.Delete,"Delete")}}}}
            OutlinedButton(onClick=vm::clearMemories,Modifier.fillMaxWidth()){Text("Clear all memories")}
        }
    }
}

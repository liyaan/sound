package com.liyaan.sound

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.EngineInfo
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.util.Locale


object TtsToMp3Helper {
    private var tts: TextToSpeech? = null
    private var outputPath: String? = null

    fun initTextToSpeech(context: Context?,
                         outputFilePath: String,content: String,
                         listener:UtteranceProgressListener) {
        this.outputPath = outputFilePath
//        val file = outputPath?.let { File(it) }
//        if (file != null) {
//            if (file.parentFile != null && !file.parentFile!!.exists()) {
//                file.parentFile!!.mkdirs()
//            }
//        }

        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.i("aaaaaaaaaaa","aaaaaaaaaaaaaaaaaaaaaaa $outputFilePath")
                tts!!.setLanguage(Locale.CHINA) // 设置语言
                // 初始化成功后可立即调用合成方法
                convertTextToMp3(outputFilePath,content)
                tts!!.setOnUtteranceProgressListener(listener)
            } else {
                Log.i("aaaaaaaaaaa","aaaaaaaaaaaaaaaaaaaaaaa 初始化失败处理")
                val installIntent = Intent()
                installIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA)
                (context as AppCompatActivity).startActivityForResult(installIntent, 0)
            }
        }
    }

    private fun convertTextToMp3(path:String,content: String) {
        val file = File(path)
        val result = tts!!.synthesizeToFile(content, null, file, "unique_id")
        Log.i("aaaaaaaaaaa","aaaaaaaaaaaaaaaaaaaaaaa result = $content")
        if (result == TextToSpeech.SUCCESS) {
            Log.i("aaaaaaaaaaa","aaaaaaaaaaaaaaaaaaaaaaa result = $result")
            // 注意：synthesizeToFile 是异步的，文件可能不会立即完成写入
            // 如果需要确切知道何时完成，建议使用 UtteranceProgressListener
        } else {
            // 处理错误
        }
    }

    fun shutdown() {
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
    }
}
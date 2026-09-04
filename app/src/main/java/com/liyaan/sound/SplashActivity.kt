package com.liyaan.sound

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.liyaan.sound.databinding.ActivitySplashBinding
import java.util.Locale


class SplashActivity:AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private var tts: TextToSpeech? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.submitContent.setOnClickListener {
            val path = "${getExternalFilesDir(null) }/record_audio.mp3"
            tts = TextToSpeech(this@SplashActivity){
                if (it ==TextToSpeech.SUCCESS){
                    tts!!.setLanguage(Locale.CHINESE)
                }else{
                    Toast.makeText(this@SplashActivity,"不支持TTS $it",Toast.LENGTH_SHORT).show()
                }
            }

        }

    }

}
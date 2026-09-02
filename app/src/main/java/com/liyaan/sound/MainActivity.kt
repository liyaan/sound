package com.liyaan.sound

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.liyaan.sound.databinding.ActivityMainBinding
import org.fmod.FMOD
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val MODE_NORMAL = 0 // 正常
    private val MODE_LUOLI = 1 // 萝莉
    private val MODE_DASHU = 2 // 大叔
    private val MODE_JINGSONG = 3 // 惊悚
    private val MODE_GAOGUAI = 4 // 搞怪
    private val MODE_KONGLING = 5 // 空灵
    // 播放的路径
    private val PATH = "file:///android_asset/123456.mp3"

    private var mRecorder: MediaRecorder? = null
    private var isRecording = false
    private var mFilePath: String? = null

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FMOD.init(this)
        initSystem()
        mFilePath =  "${getExternalFilesDir(null) }/record_audio.mp3"
        // Example of a call to a native method
        binding.sampleText.text = stringFromJNI()
        binding.record.setOnClickListener { clickFox(binding.record.id) }
        binding.luoli.setOnClickListener { clickFox(binding.luoli.id) }
        binding.dashu.setOnClickListener { clickFox(binding.dashu.id) }
        binding.jingsong.setOnClickListener { clickFox(binding.jingsong.id) }
        binding.gaoguai.setOnClickListener { clickFox(binding.gaoguai.id) }
        binding.kongling.setOnClickListener { clickFox(binding.kongling.id) }

        binding.startSound.setOnClickListener { clickFox(binding.kongling.id) }
        binding.selectSound.setOnClickListener { clickFox(binding.kongling.id) }

//        binding.record.setOnTouchListener{view,event->
//            when(event?.actionMasked){
//                MotionEvent.ACTION_DOWN->{
//                    // 1. 检查权限
//                    if (ContextCompat.checkSelfPermission(this@MainActivity,
//                            android.Manifest.permission.RECORD_AUDIO)
//                        != PackageManager.PERMISSION_GRANTED) {
//                        ActivityCompat.requestPermissions(this,
//                            arrayOf(android.Manifest.permission.RECORD_AUDIO), 200);
//                        true
//                    }else{
//                        startRecording(); // 开始录音
//                        true
//                    }
//
//                }
//                MotionEvent.ACTION_UP->{
//                    if (isRecording) stopRecording() // 停止录音
//                    true
//                }
//                MotionEvent.ACTION_CANCEL ->{
//                    if (isRecording) {
//                        stopRecording() // 停止录音
//                    }
//                    true
//                }
//
//
//            }
//
//            false
//        }

    }
    private fun clickFox(id:Int){
        if (TextUtils.isEmpty(PATH)){
            playerEnd("请选择音频或者录音")
            return
        }
        stopSound()
        when(id){
            R.id.record -> {
                Thread{
                    voiceChangeNative(MODE_NORMAL, PATH)
                }.start()
            }
            R.id.luoli -> {
                Thread{
                    voiceChangeNative(MODE_LUOLI, PATH)
                }.start()
            }
            R.id.dashu -> {
                Thread{
                    voiceChangeNative(MODE_DASHU, PATH)
                }.start()
            }
            R.id.jingsong -> {
                Thread{
                    voiceChangeNative(MODE_JINGSONG, PATH)
                }.start()
            }
            R.id.gaoguai -> {
                Thread{
                    voiceChangeNative(MODE_GAOGUAI, PATH)
                }.start()
            }
            R.id.kongling -> {
                Thread{
                    voiceChangeNative(MODE_KONGLING, PATH)
                }.start()
            }

        }
    }

    private external fun stringFromJNI(): String
    private external fun initSystem()
    private external fun releaseSystem()
    private external fun stopSound()
    private external fun voiceChangeNative(mode: Int, path: String)



    // 给native调用的   方法签名： (Ljava/lang/String;)V
    fun playerEnd(nativeMessageContent: String) {
        runOnUiThread{
            Toast.makeText(this, "" + nativeMessageContent, Toast.LENGTH_SHORT).show()
        }
    }


    @RequiresApi(Build.VERSION_CODES.S)
    private fun startRecording() {
        Log.i("AAAAAAAAA",mFilePath!!)
        mRecorder = MediaRecorder(this)
        mRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mRecorder?.setOutputFile(mFilePath)
        mRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        try {
            mRecorder?.prepare();
            mRecorder?.start()
            isRecording = true
            playerEnd("送开停止录音")
        } catch (e: IOException) {
            e.printStackTrace()
            isRecording = false
        }
    }

    private fun stopRecording() {
        mRecorder?.apply {
            stop()
            release()
            mRecorder = null
            isRecording = false
        }

    }

    override fun onPause() {
        super.onPause()
        stopSound()
    }
    override fun onDestroy() {
        super.onDestroy()
        mRecorder?.apply {
            release()
            mRecorder = null
        }
        releaseSystem()
        FMOD.close(); // 做实验 来验证
    }
    companion object {
        // Used to load the 'sound' library on application startup.
        init {
            System.loadLibrary("sound")
        }
    }
}
// TODO 后续增加

// 在 JNI 中暴露给 Java 的方法
//extern "C" JNIEXPORT void JNICALL
//Java_com_example_app_AudioManager_suspendAudio(JNIEnv *env, jobject thiz) {
//    if (gSystem) {
//            gSystem->mixerSuspend(); // 释放音频硬件，停止混音线程
//    }
//}
//
//extern "C" JNIEXPORT void JNICALL
//Java_com_example_app_AudioManager_resumeAudio(JNIEnv *env, jobject thiz) {
//    if (gSystem) {
//            gSystem->mixerResume(); // 重新获取音频硬件
//    }
//}
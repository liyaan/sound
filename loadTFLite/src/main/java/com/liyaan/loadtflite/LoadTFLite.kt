package com.liyaan.loadtflite

import android.app.Application
import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.nio.channels.FileChannel

object LoadTFLite {
    private var mInterpreter: Interpreter?=null
    private val mapData: HashMap<String, Interpreter?> = HashMap()
    fun initTFLite(fileName: String,context: Context):Interpreter?{
        mapData.get(fileName)?.let {
            return it
        }?: run {

            // 配置解释器选项
            val options = Interpreter.Options()
            options.setNumThreads(4) // 设置线程数

            // 【可选】启用 GPU 加速
            // val gpuDelegate = GpuDelegate()
            // options.addDelegate(gpuDelegate)
            mInterpreter =  Interpreter(
                context.assets.openFd(fileName).let {
                    FileInputStream(it.fileDescriptor).channel.map(
                        FileChannel.MapMode.READ_ONLY,
                        it.startOffset,
                        it.declaredLength
                    )
                }
            )
            mapData.put(fileName,mInterpreter)
        }

        return mInterpreter
    }


    fun initFileFTLite(file: File): Interpreter?{
        mapData.get(file.absolutePath)?.let {
            return it
        }?: run {
            mInterpreter = Interpreter(file)
            mapData.put(file.absolutePath,mInterpreter)
        }

        return mInterpreter
    }

    /**
     * 释放资源
     */
    fun unInit(){
        mapData.forEach { string, interpreter ->
            interpreter?.close()
        }
        mapData.clear()
    }
    fun releaseName(path: String){
        mapData.get(path)?.close()
        mapData.remove(path)
    }
}
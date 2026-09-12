package com.liyaan.tflite

import android.content.Context
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

object FileUtil {

     fun loadMappedFile(context: Context, fileName: String): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(fileName)
        val inputStream = assetFileDescriptor.createInputStream()
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
}
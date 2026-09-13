package com.liyaan.mnist

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.get
import androidx.core.graphics.scale
import com.liyaan.loadtflite.LoadTFLite
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

object MinstInit {
    private var interpreter: Interpreter? = null

    // 模型输入参数
    private val INPUT_WIDTH = 28
    private val INPUT_HEIGHT = 28
    private val PIXEL_SIZE = 1 // 单通道灰度图

    // 输出类别数 (0-9)
    private val NUM_CLASSES = 10

    // 输入和输出 Buffer，复用避免频繁 GC
    private var inputBuffer: ByteBuffer? = null
    private var outputBuffer = arrayOf(FloatArray(NUM_CLASSES))

    fun initMins(context: Context) {
        try {
            interpreter = LoadTFLite.initTFLite("mnist.tflite",context)

            // 预分配输入缓冲区：1 * 28 * 28 * 1 * 4字节 (float32)
            inputBuffer = ByteBuffer.allocateDirect(
                1 * INPUT_WIDTH * INPUT_HEIGHT * PIXEL_SIZE * 4
            )
            inputBuffer?.order(ByteOrder.nativeOrder())

        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("模型加载失败: ${e.message}")
        }
    }

    /**
     * 识别手写数字
     * @param bitmap 输入的 Bitmap 图像
     * @return 识别结果对象（数字 + 置信度）
     */
    fun recognize(bitmap: Bitmap): RecognitionResult {
        if (interpreter == null || inputBuffer == null) {
            return RecognitionResult(-1, 0f)
        }

        // 1. 图像预处理：缩放 + 灰度化 + 归一化
        preprocessBitmap(bitmap, inputBuffer!!)

        // 2. 重置输出缓冲区
        outputBuffer[0].fill(0f)

        // 3. 运行推理
        interpreter?.run(inputBuffer, outputBuffer)

        // 4. 解析结果
        return parseResult(outputBuffer[0])
    }

    /**
     * 图像预处理：将任意 Bitmap 转换为模型所需的 28x28 灰度浮点数据
     */
    private fun preprocessBitmap(bitmap: Bitmap, buffer: ByteBuffer) {
        buffer.rewind()

        // 将 Bitmap 缩放到 28x28
        val scaledBitmap = bitmap.scale(INPUT_WIDTH, INPUT_HEIGHT)

        // 遍历每一个像素
        for (y in 0 until INPUT_HEIGHT) {
            for (x in 0 until INPUT_WIDTH) {
                val pixel = scaledBitmap[x, y]

                // 转换为灰度值 (加权平均法)
                // R: 0.299, G: 0.587, B: 0.114
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF

                // 计算灰度值 (0-255)
                val gray = (0.299 * r + 0.587 * g + 0.114 * b).toFloat()

                // 【重要】归一化到 0.0 - 1.0
                // MNIST 数据集是黑底白字，如果你的输入是白底黑字，需要反转：
                // val normalized = 1.0f - (gray / 255.0f)
                val normalized = gray / 255.0f

                buffer.putFloat(normalized)
            }
        }

        scaledBitmap.recycle()
    }

    /**
     * 解析推理输出，找出置信度最高的数字
     */
    private fun parseResult(probabilities: FloatArray): RecognitionResult {
        var maxIndex = 0
        var maxConfidence = probabilities[0]

        for (i in 1 until NUM_CLASSES) {
            if (probabilities[i] > maxConfidence) {
                maxConfidence = probabilities[i]
                maxIndex = i
            }
        }

        return RecognitionResult(
            digit = maxIndex,
            confidence = maxConfidence
        )
    }

    /**
     * 释放资源
     */
    fun close() {
        LoadTFLite.releaseName("mnist.tflite")
        interpreter = null
    }

    // 结果数据类
    data class RecognitionResult(
        val digit: Int,
        val confidence: Float
    )
}
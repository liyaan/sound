package com.liyaan.tflite

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawView
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context,attrs,defStyleAttr) {

    private val drawPaint = Paint().apply {
        color = Color.WHITE    // MNIST 是白字黑底，所以笔画用白色
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 10f      // 笔画粗细，根据需要调整
    }

    private val path = Path()
    private lateinit var canvasBitmap: Bitmap
    private lateinit var drawCanvas: Canvas

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // 创建黑底画布
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565)
        drawCanvas = Canvas(canvasBitmap)
        drawCanvas.drawColor(Color.BLACK) // 黑色背景
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(canvasBitmap, 0f, 0f, null)
        canvas.drawPath(path, drawPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> path.moveTo(x, y)
            MotionEvent.ACTION_MOVE -> path.lineTo(x, y)
            MotionEvent.ACTION_UP -> {
                drawCanvas.drawPath(path, drawPaint)
                path.reset()
                // 识别完成后可以在这里调用识别回调
            }
        }
        invalidate()
        return true
    }

    // 清空画板
    fun clear() {
        drawCanvas.drawColor(Color.BLACK)
        path.reset()
        invalidate()
    }

    // 获取当前画板的 Bitmap
    fun getBitmap(): Bitmap {
        return canvasBitmap
    }
}

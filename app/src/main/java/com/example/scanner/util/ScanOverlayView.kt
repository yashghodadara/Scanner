package com.example.scanner.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.example.scanner.R

class ScanOverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var boxSize = dpToPx(250f)
    private val topMargin = dpToPx(200f)  // fixed 100 dp margin from top

    private val overlayPaint = Paint().apply {
        color = Color.parseColor("#80000000")
    }

    private val clearPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.ScanOverlayView)
            boxSize = typedArray.getDimension(R.styleable.ScanOverlayView_boxSize, dpToPx(250f))
            typedArray.recycle()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Dark background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)

        // Center horizontally ONLY
        val left = (width - boxSize) / 2f
        val top = topMargin
        val right = left + boxSize
        val bottom = top + boxSize

        // Transparent cut-out
        canvas.drawRoundRect(RectF(left, top, right, bottom), 30f, 30f, clearPaint)
    }

    private fun dpToPx(dp: Float): Float =
        dp * resources.displayMetrics.density
}

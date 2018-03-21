package com.bobekos.bobek.simplebarcodescanner

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.google.android.gms.vision.barcode.Barcode


class Test : View {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var mBarcode: Barcode? = null

    private val mPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 4.0f
    }

    init {
        setWillNotDraw(false)
    }

    fun update(barcode: Barcode) {
        mBarcode = barcode
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val widthScaleFactor = canvas?.width?.toFloat()?.div(Math.min(640, 480))
        val heightScaleFactor = canvas?.height?.toFloat()?.div(Math.max(640, 480))

        val rect = Rect(mBarcode?.boundingBox)
        rect.left = translateX(rect.left.toFloat(), widthScaleFactor ?: 0f).toInt()
        rect.top = translateY(rect.top.toFloat(), heightScaleFactor ?: 0f).toInt()
        rect.right = translateX(rect.right.toFloat(), widthScaleFactor ?: 0f).toInt()
        rect.bottom = translateY(rect.bottom.toFloat(), heightScaleFactor ?: 0f).toInt()

        if (mBarcode != null) {
            canvas?.drawRect(rect, mPaint)
        }
    }

    private fun translateX(x: Float, width: Float): Float {
        return x * width
    }

    private fun translateY(y: Float, height: Float): Float {
        return y * height
    }
}
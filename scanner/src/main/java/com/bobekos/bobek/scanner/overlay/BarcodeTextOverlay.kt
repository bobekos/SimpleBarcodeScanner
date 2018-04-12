package com.bobekos.bobek.scanner.overlay

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View


class BarcodeTextOverlay : View, BarcodeOverlay {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private lateinit var rect: Rect

    private var value: String = ""
    private val paint by lazy {
        Paint().apply {
            color = Color.WHITE
            textSize = 40f
            typeface = Typeface.DEFAULT_BOLD
        }
    }

    init {
        setWillNotDraw(false)
    }

    override fun onUpdate(posRect: Rect, barcodeValue: String) {
        rect = posRect
        value = barcodeValue

        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (::rect.isInitialized) {
            canvas?.drawText(value, rect.left.toFloat(), rect.bottom.toFloat(), paint)
        }
    }
}
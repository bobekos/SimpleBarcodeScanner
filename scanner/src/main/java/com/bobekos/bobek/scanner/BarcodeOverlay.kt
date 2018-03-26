package com.bobekos.bobek.scanner

import android.graphics.Rect


interface BarcodeOverlay {

    fun onUpdate(posRect: Rect)
}
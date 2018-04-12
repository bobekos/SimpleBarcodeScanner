package com.bobekos.bobek.scanner.overlay

import android.graphics.Rect


interface BarcodeOverlay {

    fun onUpdate(posRect: Rect = Rect(), barcodeValue: String = "")
}
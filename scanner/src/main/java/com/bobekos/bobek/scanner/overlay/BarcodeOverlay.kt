package com.bobekos.bobek.scanner.overlay

import android.graphics.Rect


interface BarcodeOverlay {

    /**
     * Is called when barcode detection fired. Make sure to redraw you custom view
     * on each time. When no barcode is found the method calls with empty parameters.
     * @param posRect The position of the barcode on the screen
     * @param barcodeValue The 'displayValue' parameter of the google barcode class
     */
    fun onUpdate(posRect: Rect = Rect(), barcodeValue: String = "")
}
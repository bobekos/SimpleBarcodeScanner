package com.bobekos.bobek.scanner

import com.google.android.gms.vision.barcode.Barcode


internal data class BarcodeScannerConfig(
        var isAutoFocus: Boolean = true,
        var drawOverLay: Boolean = false,
        var previewSize: Size = Size(640, 480),
        var barcodeFormat: Int = Barcode.ALL_FORMATS
)
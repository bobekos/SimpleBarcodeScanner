package com.bobekos.bobek.scanner

import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.barcode.Barcode


internal data class BarcodeScannerConfig(
        var isAutoFocus: Boolean = true,
        var drawOverLay: Boolean = false,
        var facing: Int = CameraSource.CAMERA_FACING_BACK,
        var previewSize: Size = Size(640, 480),
        var barcodeFormat: Int = Barcode.ALL_FORMATS
)
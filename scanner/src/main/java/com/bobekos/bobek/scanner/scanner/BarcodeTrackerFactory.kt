package com.bobekos.bobek.scanner.scanner

import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.barcode.Barcode


internal class BarcodeTrackerFactory(private val tracker: Tracker<Barcode>) : MultiProcessor.Factory<Barcode> {

    override fun create(p0: Barcode?): Tracker<Barcode> {
        return tracker
    }
}
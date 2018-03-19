package com.bobekos.bobek.scanner

import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.barcode.Barcode


class BarcodeTrackerFactory(private val callback: (barcode: Barcode?) -> Unit) : MultiProcessor.Factory<Barcode> {

    override fun create(p0: Barcode?): Tracker<Barcode> {
        return BarcodeTracker(callback)
    }

    class BarcodeTracker(private val callback: (barcode: Barcode?) -> Unit) : Tracker<Barcode>() {

        override fun onNewItem(id: Int, barcode: Barcode?) {
            callback(barcode)
        }

        override fun onUpdate(p0: Detector.Detections<Barcode>?, p1: Barcode?) {
        }

        override fun onMissing(p0: Detector.Detections<Barcode>?) {
        }

        override fun onDone() {
        }

    }
}
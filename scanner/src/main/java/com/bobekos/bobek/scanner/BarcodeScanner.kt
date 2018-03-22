package com.bobekos.bobek.scanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.support.v4.app.ActivityCompat
import android.view.SurfaceHolder
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import io.reactivex.Observable
import io.reactivex.ObservableEmitter


class BarcodeScanner(private val context: Context?, private val holder: SurfaceHolder) {

    private val barcodeDetector by lazy {
        BarcodeDetector.Builder(context).build()
    }

    @SuppressLint("MissingPermission")
    fun getObservable(size: Size): Observable<Barcode> {
        return Observable.create<Barcode> { emitter ->

            if (context == null) {
                emitter.onError(NullPointerException("Context is null"))
            } else {
                if (checkPermission()) {
                    getCameraSource(size).start(holder)
                } else {
                    emitter.onError(SecurityException("Permission Denial: Camera"))
                }

                val tracker = BarcodeTracker(emitter)
                val processor = MultiProcessor.Builder(BarcodeTrackerFactory(tracker)).build()

                barcodeDetector.setProcessor(processor)
            }
        }
    }

    inner class BarcodeTracker(private val emitter: ObservableEmitter<Barcode>) : Tracker<Barcode>() {

        override fun onNewItem(id: Int, barcode: Barcode?) {
            if (barcode != null) {
                if (!emitter.isDisposed) {
                    emitter.onNext(barcode)
                }
                BarcodeView.overlaySubject.onNext(barcode.boundingBox)
            }
        }

        override fun onUpdate(detection: Detector.Detections<Barcode>?, barcode: Barcode?) {
            if (barcode != null) {
                BarcodeView.overlaySubject.onNext(barcode.boundingBox)
            }
        }

        override fun onMissing(p0: Detector.Detections<Barcode>?) {

        }

        override fun onDone() {
            BarcodeView.overlaySubject.onNext(Rect())
        }
    }

    private fun getCameraSource(size: Size): CameraSource {
        return CameraSource.Builder(context, barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(size.width, size.height)
                .setRequestedFps(15.0f)
                .setAutoFocusEnabled(true)
                .build()
    }

    private fun checkPermission(): Boolean {
        return context != null &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }
}
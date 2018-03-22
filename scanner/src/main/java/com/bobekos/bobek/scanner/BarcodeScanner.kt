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
import org.reactivestreams.Subscriber


internal class BarcodeScanner(
        private val context: Context?,
        private val holder: SurfaceHolder,
        private val config: BarcodeScannerConfig) {

    private val barcodeDetector by lazy {
        BarcodeDetector.Builder(context)
                .setBarcodeFormats(config.barcodeFormat)
                .build()
    }

    @SuppressLint("MissingPermission")
    fun getObservable(): Observable<Barcode> {
        return Observable.fromPublisher<Barcode> {
            if (context == null) {
                it.onError(NullPointerException("Context is null"))
            } else {
                if (checkPermission()) {
                    getCameraSource(config.previewSize, config.isAutoFocus).start(holder)

                    val tracker = BarcodeTracker(it)
                    val processor = MultiProcessor.Builder(BarcodeTrackerFactory(tracker)).build()

                    barcodeDetector.setProcessor(processor)
                } else {
                    it.onError(SecurityException("Permission Denial: Camera"))
                }
            }
        }
    }

    inner class BarcodeTracker(private val subscriber: Subscriber<in Barcode>) : Tracker<Barcode>() {

        override fun onNewItem(id: Int, barcode: Barcode?) {
            if (barcode != null) {
                subscriber.onNext(barcode)

                if (config.drawOverLay) {
                    BarcodeView.overlaySubject.onNext(barcode.boundingBox)
                }
            }
        }

        override fun onUpdate(detection: Detector.Detections<Barcode>?, barcode: Barcode?) {
            if (barcode != null && config.drawOverLay) {
                BarcodeView.overlaySubject.onNext(barcode.boundingBox)
            }
        }

        override fun onMissing(p0: Detector.Detections<Barcode>?) {

        }

        override fun onDone() {
            if (config.drawOverLay) {
                BarcodeView.overlaySubject.onNext(Rect())
            }
        }
    }

    private fun getCameraSource(size: Size, isAutoFocus: Boolean): CameraSource {
        return CameraSource.Builder(context, barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(size.width, size.height)
                .setRequestedFps(15.0f)
                .setAutoFocusEnabled(isAutoFocus)
                .build()
    }

    private fun checkPermission(): Boolean {
        return context != null &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }
}
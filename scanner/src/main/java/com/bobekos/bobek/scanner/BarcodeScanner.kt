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


internal class BarcodeScanner(
        private val context: Context?,
        private val holder: SurfaceHolder,
        private val config: BarcodeScannerConfig) {

    private val barcodeDetector by lazy {
        BarcodeDetector.Builder(context)
                .setBarcodeFormats(config.barcodeFormat)
                .build()
    }

    private val cameraSource by lazy {
        createCameraSource()
    }

    @SuppressLint("MissingPermission")
    fun getObservable(): Observable<Barcode> {
        return Observable.create { emitter ->
            if (context == null && !emitter.isDisposed) {
                emitter.onError(NullPointerException("Context is null"))
            } else {
                if (checkPermission()) {
                    cameraSource.start(holder)

                    val tracker = BarcodeTracker(emitter)
                    val processor = MultiProcessor.Builder(BarcodeTrackerFactory(tracker)).build()
                    barcodeDetector.setProcessor(processor)
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(SecurityException("Permission Denial: Camera"))
                    }
                }

                emitter.setCancellable {
                    cameraSource.release()
                }
            }
        }
    }

    inner class BarcodeTracker(private val subscriber: ObservableEmitter<Barcode>) : Tracker<Barcode>() {

        override fun onNewItem(id: Int, barcode: Barcode?) {
            if (barcode != null) {
                if (config.drawOverLay) {
                    BarcodeView.overlaySubject.onNext(barcode.boundingBox)
                }

                if (!subscriber.isDisposed) {
                    subscriber.onNext(barcode)
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

    private fun createCameraSource(): CameraSource {
        return CameraSource.Builder(context, barcodeDetector)
                .setFacing(config.facing)
                .setRequestedPreviewSize(config.previewSize.width, config.previewSize.height)
                .setAutoFocusEnabled(config.isAutoFocus)
                .build()
    }

    private fun checkPermission(): Boolean {
        return context != null &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }
}
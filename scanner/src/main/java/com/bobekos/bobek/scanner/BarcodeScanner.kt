package com.bobekos.bobek.scanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.view.SurfaceHolder
import com.bobekos.bobek.scanner.BarcodeTrackerFactory
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import io.reactivex.Observable


class BarcodeScanner(private val context: Context?, private val holder: SurfaceHolder) {

    private val barcodeDetector by lazy {
        BarcodeDetector.Builder(context).build()
    }

    @SuppressLint("MissingPermission")
    fun getObservable(): Observable<Barcode> {
        return Observable.create<Barcode> { emitter ->

            if (context == null) {
                emitter.onError(NullPointerException("Context is null"))
            } else {
                if (checkPermission()) {
                    getCameraSource().start(holder)
                } else {
                    emitter.onError(SecurityException("Permission Denial: Camera"))
                }

                val processor = MultiProcessor.Builder(BarcodeTrackerFactory({
                    if (holder.surface == null || !holder.surface.isValid) {
                        emitter.onComplete()
                    } else if (it != null) {
                        emitter.onNext(it)
                    }
                })).build()

                barcodeDetector.setProcessor(processor)
            }
        }
    }

    private fun getCameraSource(): CameraSource {
        return CameraSource.Builder(context, barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(640, 480)
                .setRequestedFps(15.0f)
                .setAutoFocusEnabled(true)
                .build()
    }

    private fun checkPermission(): Boolean {
        return context != null &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }
}
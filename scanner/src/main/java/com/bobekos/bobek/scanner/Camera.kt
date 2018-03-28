package com.bobekos.bobek.scanner

import android.content.Context
import android.hardware.Camera
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.barcode.BarcodeDetector


internal class Camera(ctx: Context?, detector: BarcodeDetector, config: BarcodeScannerConfig) {

    private val cameraSource: CameraSource = CameraSource.Builder(ctx, detector)
            .setFacing(config.facing)
            .setRequestedPreviewSize(config.previewSize.width, config.previewSize.height)
            .setAutoFocusEnabled(config.isAutoFocus)
            .build()

    private fun get(): Camera? {
        val declaredFields = CameraSource::class.java.declaredFields

        declaredFields.forEach {
            if (it.type == Camera::class.java) {
                it.isAccessible = true
                try {
                    val camera = it.get(cameraSource)

                    if (camera != null) {
                        return camera as Camera
                    }
                } catch (e: IllegalAccessException) {
                    return null
                }
            }
        }

        return null
    }

    fun setParametersFromConfig() {
        val camera = get()
        val parameters = camera?.parameters
        parameters?.flashMode = "on"
        camera?.parameters = parameters
    }

    fun getCameraSource(): CameraSource {
        return cameraSource
    }

}
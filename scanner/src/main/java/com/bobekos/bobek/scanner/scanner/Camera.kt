package com.bobekos.bobek.scanner.scanner

import android.content.Context
import android.hardware.Camera
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.barcode.BarcodeDetector


internal class Camera(ctx: Context?, detector: BarcodeDetector, private val config: BarcodeScannerConfig) {

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
        setFlash(parameters)
        camera?.parameters = parameters
    }

    private fun setFlash(parameters: Camera.Parameters?) {
        parameters?.flashMode = if (config.useFlash) {
            Camera.Parameters.FLASH_MODE_TORCH
        } else {
            Camera.Parameters.FLASH_MODE_OFF
        }
    }

    fun getCameraSource(): CameraSource {
        return cameraSource
    }

}
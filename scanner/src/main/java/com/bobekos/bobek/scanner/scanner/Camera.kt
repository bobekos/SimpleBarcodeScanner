package com.bobekos.bobek.scanner.scanner

import android.content.Context
import android.hardware.Camera
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.barcode.BarcodeDetector


internal class Camera(private val ctx: Context?, private val config: BarcodeScannerConfig) {

    companion object {
        fun getCameraIdByFacing(facing: Int): Int {
            val cameraInfo = Camera.CameraInfo()
            for (i in 0..Camera.getNumberOfCameras()) {
                Camera.getCameraInfo(i, cameraInfo)
                if (cameraInfo.facing == facing) {
                    return i
                }
            }

            return -1
        }

        fun getValidPreviewSize(cameraId: Int, defaultSize: Size): Size {
            val camera = Camera.open(cameraId)
            val supportedPreviewSize = camera.parameters.supportedPreviewSizes

            var result = defaultSize
            var minDiff = Int.MAX_VALUE

            supportedPreviewSize.forEach {
                val diff = Math.abs(it.width - defaultSize.width) +
                        Math.abs(it.height - defaultSize.height)
                if (diff < minDiff) {
                    result = Size(it.width, it.height)
                    minDiff = diff
                }
            }

            camera.release()

            return result
        }

        fun isFacingFront(facing: Int): Boolean {
            return facing == CameraSource.CAMERA_FACING_FRONT
        }
    }

    private var cameraSource: CameraSource? = null

    fun init(detector: BarcodeDetector) = apply {
        cameraSource = CameraSource.Builder(ctx, detector)
                .setFacing(config.facing)
                .setRequestedPreviewSize(config.previewSize.width, config.previewSize.height)
                .setAutoFocusEnabled(config.isAutoFocus)
                .build()
    }

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

    fun getCameraSource(): CameraSource? {
        return cameraSource
    }

}
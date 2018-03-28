package com.bobekos.bobek.scanner

import android.content.Context
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.hardware.Camera
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.barcode.Barcode
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject


class BarcodeView : FrameLayout {

    private var overlayDisposable: Disposable? = null

    private var drawOverlay: BarcodeOverlay? = null

    private val config by lazy {
        BarcodeScannerConfig()
    }

    companion object {
        val overlaySubject: PublishSubject<Rect> = PublishSubject.create<Rect>()
    }

    private val xScaleFactor by lazy {
        width.toFloat().div(Math.min(config.previewSize.width, config.previewSize.height))
    }

    private val yScaleFactor by lazy {
        height.toFloat().div(Math.max(config.previewSize.width, config.previewSize.height))
    }

    private val cameraView by lazy {
        SurfaceView(context)
    }

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        addView(cameraView)
    }

    //region public
    fun getObservable(): Observable<Barcode> {
        return getSurfaceObservable()
                .flatMap { BarcodeScanner(context, it, config).getObservable() }
                .subscribeOn(Schedulers.io())
    }

    fun setPreviewSize(width: Int, height: Int): BarcodeView {
        config.previewSize = Size(width, height)

        return this
    }

    fun setAutoFocus(enabled: Boolean): BarcodeView {
        config.isAutoFocus = enabled

        return this
    }

    fun setBarcodeFormats(vararg formats: Int): BarcodeView {
        config.barcodeFormat = formats.sum()

        return this
    }

    fun setFacing(facing: Int): BarcodeView {
        config.facing = facing

        return this
    }

    fun drawOverlay(overlay: BarcodeOverlay? = BarcodeRectOverlay(context)): BarcodeView {
        drawOverlay = overlay
        config.drawOverLay = true

        return this
    }
    //endregion

    //region private
    private fun getSurfaceObservable(): Observable<SurfaceHolder> {
        return Observable.create<SurfaceHolder> { emitter ->
            cameraView.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {

                }

                override fun surfaceDestroyed(p0: SurfaceHolder?) {
                    overlayDisposable?.dispose()

                    if (!emitter.isDisposed) {
                        emitter.onComplete()
                    }
                }

                override fun surfaceCreated(holder: SurfaceHolder?) {
                    loadCameraSettings()

                    if (drawOverlay != null) {
                        startOverlay()
                    }

                    if (holder != null && !emitter.isDisposed) {
                        emitter.onNext(holder)
                    }
                }
            })
        }
    }


    private fun startOverlay() {
        removeView(drawOverlay as View)
        addView(drawOverlay as View, FrameLayout.LayoutParams(width, height))

        overlayDisposable = overlaySubject
                .observeOn(AndroidSchedulers.mainThread())
                .filter { drawOverlay != null }
                .subscribe(
                        { rect ->
                            drawOverlay?.let { overlay ->
                                overlay.onUpdate(calculateOverlayView(rect))
                                if (isFacingFront()) {
                                    (overlay as View).scaleX = -1f
                                }
                            }
                        },
                        {
                            drawOverlay?.onUpdate(Rect())
                        })
    }

    private fun loadCameraSettings() {
        val cameraId = getCameraIdByFacing()
        if (cameraId == -1) {
            throw NullPointerException("Could not find camera for selected facing")
        }

        val camera = Camera.open(cameraId)
        config.previewSize = getValidPreviewSize(camera)

        camera.release()
    }

    private fun getCameraIdByFacing(): Int {
        val cameraInfo = Camera.CameraInfo()
        for (i in 0..Camera.getNumberOfCameras()) {
            Camera.getCameraInfo(i, cameraInfo)
            if (cameraInfo.facing == config.facing) {
                return i
            }
        }

        return -1
    }

    private fun isFacingFront(): Boolean {
        return config.facing == CameraSource.CAMERA_FACING_FRONT
    }

    private fun getValidPreviewSize(camera: Camera): Size {
        val supportedPreviewSize = camera.parameters.supportedPreviewSizes

        var result = config.previewSize
        var minDiff = Int.MAX_VALUE

        supportedPreviewSize.forEach {
            val diff = Math.abs(it.width - width) +
                    Math.abs(it.height - height)
            if (diff < minDiff) {
                result = Size(it.width, it.height)
                minDiff = diff
            }
        }

        return result
    }

    private fun calculateOverlayView(barcodeRect: Rect): Rect {
        val rect = Rect(barcodeRect)

        return rect.also {
            it.left = translateX(rect.left)
            it.top = translateY(rect.top)
            it.right = translateX(rect.right)
            it.bottom = translateY(rect.bottom)
        }
    }

    private fun translateX(x: Int): Int {
        return (x * xScaleFactor).toInt()
    }

    private fun translateY(y: Int): Int {
        return (y * yScaleFactor).toInt()
    }
    //endregion
}
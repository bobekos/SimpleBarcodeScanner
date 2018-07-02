package com.bobekos.bobek.scanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Rect
import android.support.v4.app.ActivityCompat
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.*
import android.widget.FrameLayout
import com.bobekos.bobek.scanner.overlay.BarcodeOverlay
import com.bobekos.bobek.scanner.overlay.BarcodeRectOverlay
import com.bobekos.bobek.scanner.overlay.Optional
import com.bobekos.bobek.scanner.scanner.BarcodeScanner
import com.bobekos.bobek.scanner.scanner.BarcodeScannerConfig
import com.bobekos.bobek.scanner.scanner.Camera
import com.bobekos.bobek.scanner.scanner.Size
import com.google.android.gms.vision.barcode.Barcode
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject


class BarcodeView : FrameLayout {

    companion object {
        internal val overlaySubject: PublishSubject<Optional<Barcode>> = PublishSubject.create<Optional<Barcode>>()
    }

    private var drawOverlay: BarcodeOverlay? = null

    private var overlayDisposable: Disposable? = null

    private val config by lazy {
        BarcodeScannerConfig(previewSize = getDisplayMetrics())
    }

    private val cameraView = SurfaceView(context)

    private val xScaleFactor by lazy {
        cameraView.width.toFloat().div(Math.min(config.previewSize.width, config.previewSize.height))
    }

    private val yScaleFactor by lazy {
        cameraView.height.toFloat().div(Math.max(config.previewSize.width, config.previewSize.height))
    }

    private val barcodeScanner by lazy {
        BarcodeScanner(context, cameraView.holder, config)
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
        setBackgroundColor(Color.BLACK)
        addView(cameraView, getPreviewParams())
    }

    //region public
    fun getObservable(): Observable<Barcode> {
        return getSurfaceObservable()
                .flatMap { barcodeScanner.getObservable(it) }
    }

    /**
     * Set the preview size for the camera source.
     * The given size is calculated to the closet value from camera available sizes.
     * @param width default value is 640
     * @param height default value is 480
     */
    fun setPreviewSize(width: Int, height: Int) = apply {
        config.previewSize = Size(width, height)
    }

    /**
     * Enable autofocus.
     * @param enabled Default value is true
     */
    fun setAutoFocus(enabled: Boolean) = apply {
        config.isAutoFocus = enabled
    }

    /**
     * Set which barcode format should be detected.
     * @param formats Default value is Barcode.ALL_FORMATS
     * @see Barcode
     */
    fun setBarcodeFormats(vararg formats: Int) = apply {
        config.barcodeFormat = formats.sum()
    }

    /**
     * Set camera facing.
     * @param facing Default value is CameraSource.CAMERA_FACING_BACK
     * @see com.google.android.gms.vision.CameraSource
     */
    fun setFacing(facing: Int) = apply {
        config.facing = facing
    }

    /**
     * Draw a overlay view over the detected barcode.
     * For custom view visit the github documentation.
     * @param overlay Default overlay is a white rect
     * @see <a href="https://github.com/bobekos/SimpleBarcodeScanner#custom-overlay">Custom overlay</a>
     */
    fun drawOverlay(overlay: BarcodeOverlay? = BarcodeRectOverlay(context)) = apply {
        drawOverlay = overlay
        config.drawOverLay = true
    }

    /**
     * Enable camera flash.
     * Also changeable after the subscription
     * @param enabled Default value is false
     */
    fun setFlash(enabled: Boolean) = apply {
        config.useFlash = enabled

        BarcodeScanner.updateSubject.onNext(true)
    }

    /**
     * Play beep sound at detection
     * Also changeable after the subscription
     * @param play Default value is true
     */
    fun setBeepSound(play: Boolean = true) = apply {
        config.playBeep = play
    }

    /**
     * Vibrate at detection
     * Also changeable after the subscription
     * @param duration Default value is 500ms (0 = disable)
     */
    fun setVibration(duration: Long = 500) = apply {
        config.vibrateDuration = duration
    }
    //endregion

    //region private
    private fun getSurfaceObservable(): Observable<Boolean> {
        return Observable.create<Boolean> { emitter ->
            cameraView.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {

                }

                override fun surfaceDestroyed(holder: SurfaceHolder?) {
                    overlayDisposable?.dispose()

                    if (!emitter.isDisposed) {
                        emitter.onNext(false)
                    }
                }

                override fun surfaceCreated(holder: SurfaceHolder?) {
                    holder?.let {
                        onSurfaceReady(emitter)
                    }
                }
            })

            if (cameraView.holder.surface.isValid && !emitter.isDisposed) {
                onSurfaceReady(emitter)
            }
        }.subscribeOn(Schedulers.io())
    }

    private fun onSurfaceReady(emitter: ObservableEmitter<Boolean>) {
        if (!emitter.isDisposed) {
            if (!checkPermission()) {
                emitter.onError(SecurityException("Permission Denial: Camera"))
            } else {
                try {
                    setCameraSettings()
                } catch (e: Exception) {
                    emitter.onError(e)
                }
            }

            setLayoutBasedOnPreviewSize()
        }

        if (drawOverlay != null) {
            drawOverlayOnSurface()

            if (overlayDisposable == null || overlayDisposable!!.isDisposed) {
                startOverlay()
            }
        }

        if (!emitter.isDisposed) {
            emitter.onNext(true)
        }
    }

    private fun setLayoutBasedOnPreviewSize() {
        var previewWidth = config.previewSize.width
        var previewHeight = config.previewSize.height

        // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
        if (isPortraitMode()) {
            val tmp = previewWidth

            previewWidth = previewHeight
            previewHeight = tmp
        }

        val parentWidth = right - left
        val parentHeight = bottom - top

        val surfaceWidthRatio = parentWidth.toFloat() / previewWidth.toFloat()
        val surfaceHeightRatio = parentHeight.toFloat() / previewHeight.toFloat()

        val surfaceWidth: Int
        val surfaceHeight: Int

        if (surfaceWidthRatio > surfaceHeightRatio) {
            surfaceWidth = parentWidth
            surfaceHeight = (previewHeight * surfaceWidthRatio).toInt()
        } else {
            surfaceWidth = (previewWidth * surfaceHeightRatio).toInt()
            surfaceHeight = parentHeight
        }

        cameraView.layoutParams = getPreviewParams(surfaceWidth, surfaceHeight)
    }

    private fun startOverlay() {
        overlayDisposable = overlaySubject
                .observeOn(AndroidSchedulers.mainThread())
                .filter { drawOverlay != null }
                .subscribe(
                        { result ->
                            drawOverlay?.let { overlay ->
                                when (result) {
                                    is Optional.Some -> {
                                        overlay.onUpdate(
                                                calculateOverlayView(result.element.boundingBox),
                                                result.element.displayValue)
                                    }
                                    is Optional.None -> {
                                        overlay.onUpdate()
                                    }
                                }

                                if (Camera.isFacingFront(config.facing) && overlay is View) {
                                    (overlay as View).scaleX = -1f
                                }
                            }
                        },
                        {
                            drawOverlay?.onUpdate()
                        })
    }

    private fun drawOverlayOnSurface() {
        cameraView.post {
            removeView(drawOverlay as View)
            addView(drawOverlay as View, getPreviewParams(cameraView.width, cameraView.height))
        }
    }

    private fun setCameraSettings() {
        val cameraId = Camera.getCameraIdByFacing(config.facing)
        if (cameraId != -1) {
            try {
                config.previewSize = Camera.getValidPreviewSize(cameraId, config.previewSize)
            } catch (e: RuntimeException) {
                throw e
            }
        } else {
            throw NullPointerException("No camera found for selected facing")
        }
    }

    private fun checkPermission(): Boolean {
        return context != null &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
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

    private fun isPortraitMode(): Boolean {
        return context.resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE
    }

    private fun getPreviewParams(
            w: Int = LayoutParams.MATCH_PARENT,
            h: Int = LayoutParams.MATCH_PARENT,
            gravity: Int = Gravity.CENTER): LayoutParams {

        return FrameLayout.LayoutParams(w, h).apply {
            this.gravity = gravity
        }
    }

    private fun getDisplayMetrics(): Size {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val result = DisplayMetrics()
        wm.defaultDisplay.getMetrics(result)

        return Size(result.widthPixels, result.heightPixels)
    }
    //endregion
}
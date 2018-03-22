package com.bobekos.bobek.scanner

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
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

    fun getObservable(): Observable<Barcode> {
        return Observable.fromPublisher<SurfaceHolder> {
            cameraView.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {

                }

                override fun surfaceDestroyed(p0: SurfaceHolder?) {
                    overlayDisposable?.dispose()
                    it.onComplete()
                }

                override fun surfaceCreated(holder: SurfaceHolder?) {
                    if (drawOverlay != null) {
                        startOverlay()
                    }

                    it.onNext(holder)
                }
            })
        }.flatMap {
            BarcodeScanner(context, it, config).getObservable()
        }.subscribeOn(Schedulers.io())
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

    fun drawOverlay(overlay: BarcodeOverlay? = BarcodeRectOverlay(context)): BarcodeView {
        drawOverlay = overlay
        config.drawOverLay = true

        return this
    }

    private fun startOverlay() {
        addView(drawOverlay as View, FrameLayout.LayoutParams(width, height))

        overlayDisposable = overlaySubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (drawOverlay != null) {
                        drawOverlay?.onUpdate(calculateOverlayView(it))
                    }
                }
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
}
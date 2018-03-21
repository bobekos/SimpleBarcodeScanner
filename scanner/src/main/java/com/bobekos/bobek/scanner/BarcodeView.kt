package com.bobekos.bobek.scanner

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.google.android.gms.vision.barcode.Barcode
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


class BarcodeView : SurfaceView {

    private var isSurfaceAvailable = false

    private var isAutoFocus = true

    private var scannerDisposable: Disposable? = null

    private var listener: BarcodeListener? = null

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
        setWillNotDraw(true)

        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {

            }

            override fun surfaceDestroyed(p0: SurfaceHolder?) {
                isSurfaceAvailable = false

                scannerDisposable?.dispose()
            }

            override fun surfaceCreated(p0: SurfaceHolder?) {
                isSurfaceAvailable = true

                start()
            }
        })
    }

    fun setAutoFocus(enabled: Boolean): BarcodeView {
        isAutoFocus = enabled

        return this
    }

    fun addBarcodeListener(l: BarcodeListener) {
        listener = l
    }

    //TODO return whole observable
    private fun start() {
        val qrScanner = BarcodeScanner(context, holder)

        scannerDisposable = qrScanner.getObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            listener?.onBarcodeDetected(it)
                        },
                        {
                            listener?.onError(it)
                        }
                )
    }

    interface BarcodeListener {
        fun onBarcodeDetected(barcode: Barcode) {

        }

        fun onError(throwable: Throwable) {

        }
    }
}
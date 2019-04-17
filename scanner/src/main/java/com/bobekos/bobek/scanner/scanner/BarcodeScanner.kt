package com.bobekos.bobek.scanner.scanner

import android.annotation.SuppressLint
import android.content.Context
import android.view.SurfaceHolder
import com.bobekos.bobek.scanner.BarcodeView
import com.bobekos.bobek.scanner.overlay.Optional
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit


internal class BarcodeScanner(
        private val context: Context?,
        private val holder: SurfaceHolder,
        private val config: BarcodeScannerConfig) {

    private val barcodeDetector by lazy {
        BarcodeDetector.Builder(context)
                .setBarcodeFormats(config.barcodeFormat)
                .build()
    }

    private val camera by lazy {
        Camera(context, config)
    }

    companion object {
        val updateSubject: PublishSubject<Boolean> = PublishSubject.create<Boolean>()
    }

    private var updateDisposable: Disposable? = null

    @SuppressLint("MissingPermission")
    fun getObservable(holderAvailable: Boolean): Observable<Barcode> {
        return Observable.create<Barcode> { emitter ->
            if (!holderAvailable) {
                emitter.onComplete()
            } else {
                if (context == null && !emitter.isDisposed) {
                    emitter.onError(NullPointerException("Context is null"))
                } else if (!barcodeDetector.isOperational) {
                    emitter.onError(DetectorNotReadyException())
                } else if (!emitter.isDisposed) {
                    camera.init(barcodeDetector).getCameraSource()?.start(holder)
                    camera.setParametersFromConfig()

                    val tracker = BarcodeTracker(emitter)
                    val processor = MultiProcessor.Builder(BarcodeTrackerFactory(tracker)).build()
                    barcodeDetector.setProcessor(processor)

                    updateDisposable = updateSubject.subscribe({
                        if (!emitter.isDisposed) {
                            camera.setParametersFromConfig()
                        }
                    }, {})

                    emitter.setCancellable {
                        processor.release()
                        updateDisposable?.dispose()

                        if (!config.holdCameraOnDispose) {
                            releaseDetection()
                        }
                    }
                }
            }
        }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext {
                    if (config.playBeep) {
                        DetectionHelper.playBeepSound()
                    }

                    if (config.vibrateDuration > 0) {
                        DetectionHelper.vibrate(context, config.vibrateDuration)
                    }
                }.retryWhen { exc ->
                    exc.flatMap {
                        if (it is DetectorNotReadyException && !config.isManualOperationalCheck) {
                            Observable.timer(3, TimeUnit.SECONDS)
                        } else {
                            Observable.error(it)
                        }
                    }

                }
    }

    fun releaseDetection() {
        camera.releaseCameraSource()
        barcodeDetector.release()
    }

    inner class BarcodeTracker(private val subscriber: ObservableEmitter<Barcode>) : Tracker<Barcode>() {

        @SuppressLint("MissingPermission")
        override fun onNewItem(id: Int, barcode: Barcode?) {
            if (barcode != null) {
                if (!subscriber.isDisposed) {
                    if (config.drawOverLay) {
                        BarcodeView.overlaySubject.onNext(Optional.Some(barcode))
                    }

                    subscriber.onNext(barcode)
                }
            }
        }

        override fun onUpdate(detection: Detector.Detections<Barcode>?, barcode: Barcode?) {
            if (barcode != null && config.drawOverLay && !subscriber.isDisposed) {
                BarcodeView.overlaySubject.onNext(Optional.Some(barcode))
            }
        }

        override fun onMissing(p0: Detector.Detections<Barcode>?) {
            if (config.drawOverLay && !subscriber.isDisposed) {
                BarcodeView.overlaySubject.onNext(Optional.None)
            }
        }

        override fun onDone() {
            if (config.drawOverLay) {
                BarcodeView.overlaySubject.onNext(Optional.None)
            }
        }
    }
}
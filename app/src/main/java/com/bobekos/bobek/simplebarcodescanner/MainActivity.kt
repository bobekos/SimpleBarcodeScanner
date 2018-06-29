package com.bobekos.bobek.simplebarcodescanner

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.android.gms.vision.CameraSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var disposable: Disposable? = null

    private var isFlashOn = false
    private var isBeepOn = false
    private var isVibrateOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setBtnText()

        btnFlash.setOnClickListener {
            isFlashOn = !isFlashOn
            barcodeView.setFlash(isFlashOn)
            setBtnText()
        }
        btnVibrate.setOnClickListener {
            isVibrateOn = !isVibrateOn
            barcodeView.setVibration(if (isVibrateOn) 500 else 0)
            setBtnText()
        }
        btnBeep.setOnClickListener {
            isBeepOn = !isBeepOn
            barcodeView.setBeepSound(isBeepOn)
            setBtnText()
        }

    }

    private fun setBtnText() {
        btnFlash.text = String.format("Turn the flashlight %s", if (isFlashOn) "off" else "on")
        btnBeep.text = String.format("Turn the beep sound %s", if (isBeepOn) "off" else "on")
        btnVibrate.text = String.format("Turn the vibration %s", if (isVibrateOn) "off" else "on")
    }

    override fun onStart() {
        super.onStart()

        disposable = barcodeView
                .drawOverlay()
                .setBeepSound(isBeepOn)
                .setVibration(if (isVibrateOn) 500 else 0)
                .setAutoFocus(true)
                .setFlash(isFlashOn)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setPreviewSize(640, 480)
                .getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            Toast.makeText(this@MainActivity, it.displayValue, Toast.LENGTH_SHORT).show()
                        },
                        {
                            Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_LONG).show()
                        })
    }

    override fun onStop() {
        super.onStop()

        disposable?.dispose()
    }
}
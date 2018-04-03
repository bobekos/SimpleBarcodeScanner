package com.bobekos.bobek.simplebarcodescanner

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.barcode.Barcode
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()

        disposable = barcodeView
                .setBarcodeFormats(Barcode.QR_CODE)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setFlash(false)
                .setAutoFocus(true)
                .setPreviewSize(640, 480)
                .drawOverlay()
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
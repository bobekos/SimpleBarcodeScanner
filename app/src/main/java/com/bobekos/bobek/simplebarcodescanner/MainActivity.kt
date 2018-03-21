package com.bobekos.bobek.simplebarcodescanner

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import com.bobekos.bobek.scanner.BarcodeView
import com.google.android.gms.vision.barcode.Barcode
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val overlay by lazy {
        Test(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        root.post {
            framelayout.addView(overlay)
        }

        barcodeView.addBarcodeListener(object: BarcodeView.BarcodeListener {
            override fun onBarcodeDetected(barcode: Barcode) {
                overlay.update(barcode)
            }

            override fun onError(throwable: Throwable) {

            }
        })
    }
}

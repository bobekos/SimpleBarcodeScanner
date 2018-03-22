package com.bobekos.bobek.simplebarcodescanner

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.android.gms.vision.barcode.Barcode
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        barcodeView
                .setBarcodeFormats(Barcode.QR_CODE)
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
}

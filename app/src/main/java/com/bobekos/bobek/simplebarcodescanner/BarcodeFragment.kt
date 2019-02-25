package com.bobekos.bobek.simplebarcodescanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_barcode.*


class BarcodeFragment : Fragment() {

    private var disposable: Disposable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_barcode, container, false)
    }

    override fun onStart() {
        super.onStart()

        disposable = barcodeView
                .drawOverlay()
                .getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            Toast.makeText(requireContext(), it.displayValue, Toast.LENGTH_SHORT).show()
                        },
                        {
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                        })
    }

    override fun onStop() {
        disposable?.dispose()

        super.onStop()
    }

}
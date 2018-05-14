# SimpleBarcodeScanner

Barcode Scanner by Google Mobile Vision Api with RxJava

## Getting Started

### Setting up the dependency

The first step is to include SimpleBarcodeScanner into your project, as a Gradle dependency:

```
implementation 'com.github.bobekos:SimpleBarcodeScanner:1.0.12'
```

## Usage

### Include following code in your layout:

```xml
<com.bobekos.bobek.scanner.BarcodeView
        android:id="@+id/barcodeView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
```

For all supported attributes see the list below (not supported yet)

### Include followin code in your activity or fragment

```kotlin
class MainActivity : AppCompatActivity() {

    private var mDisposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()

        //make sure to request camera permission before the subscription

        mDisposable = barcodeView
                .getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { barcode ->
                            //handle barcode object
                        },
                        { throwable ->
                            //handle exceptions like no available camera for selected facing
                        })
    }

    override fun onStop() {
        super.onStop()

        mDisposable?.dispose()
    }
}
```

## Features

### Available Options

```
.setBarcodeFormats(Barcode.QR_CODE)
```

Which barcode format should be detected. Default value is all formats.

```
.setFacing(CameraSource.CAMERA_FACING_BACK)
```

Set the camera facing. Default value is back facing.

```
.setFlash(false)
```

Turn on the flash. Default value is false.

```
.setAutoFocus(true)
```

Enable autofocus. Default value is true.

```
.setPreviewSize(640, 480)
```

Set preview size for the camera source. The given preview size is calculated to the closet value from camera available sizes.

```
.drawOverlay()
```

Draw a overlay view over the detected barcode. Default overlay is a white rect.

### Advanced Options

#### Custom overlay

There a already two implemented overlay views (BarcodeRectOverlay and BarcodeTextOverlay). 
To create your own overlay, you only need to implement the "BarcodeOverlay" interface to your custom view. 
The method "onUpdate" passes the position of the barcode on the screen and its value. An own view looks like this, for example: 

```kotlin
class RedRectOverlay : View, BarcodeOverlay {

    //constructors

    private lateinit var rect: Rect

    private val paint by lazy {
        Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, context?.resources?.displayMetrics)
        }
    }

    init {
        setWillNotDraw(false)
    }

    override fun onUpdate(posRect: Rect, barcodeValue: String) {
        rect = posRect
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (::rect.isInitialized) {
            canvas?.drawRect(rect, paint)
        }
    }
}

// Activity or Fragment
barcodeView
	.drawOverlay(RedRectOverlay(this))
```

If the barcode detection failed or finished the "onUpdate" method passed empty values for the position and barcode value.

#### Rx

You have full control of the observable which is returned from the BarcodeView.
Nevertheless, I have prepared a few examples to show what is possible.

```
//filter the detected results

.getObservable()
.filter { barcode ->
    barcode.displayValue == "12345"
}
.observeOn...
```

```
//skip results until the raw value changed

.getObservable()
.distinctUntilChanged { barcode1, barcode2 ->
	barcode1.rawValue == barcode2.rawValue
}
.observeOn...
```

```
//get only first item

.getObservable()
.firstOrError()
.observeOn...
```

```
// combine your api/database/etc. observables directly

.getObservable()
.firstOrError()
.flatMap { barcode ->
    ApiService.getDateByBarcodeId(barcode.displayValue)
}
.flatMap ...
```

and many more...

**On which thread does the detection run?**

The detection runs on an background thread. Don't forget to set the correct scheduler to the "observeOn" method of the observable
if you want to have the result on the main android thread for example.

**It is necessary to dispose the subscription on his own?**

Short answer 'yes'. Although the observable called "onComplete" when the surface is destroyed. 
But to avoid memory leaks you should always dispose your subscription.

## Resources and Credits

* [google android-vision sample](https://github.com/googlesamples/android-vision/tree/master/visionSamples/barcode-reader)
* [rxJava](https://github.com/ReactiveX/RxJava)
* [rxAndroid](https://github.com/ReactiveX/RxAndroid)

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

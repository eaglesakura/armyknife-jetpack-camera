package com.eaglesakura.armyknife.experimental.android.extensions

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata

/**
 * ImageAnalysis.setAnalyzer with time interval
 *
 * `analyze` => `intervalMilliSeconds or more time, skip analyze` => `analyze`
 *
 * e.g.)
 * analysis.setIntervalAnalyzer(TimeUnit.SECOND.toMillis(1)) { image, rotationDegrees ->
 *      // detect this image.
 *      ...
 * }
 *
 * @see ImageAnalysis.setAnalyzer
 */
fun ImageAnalysis.setIntervalAnalyzer(intervalMilliSeconds: Long, analyzer: ImageAnalysis.Analyzer?) {
    if (analyzer == null) {
        setAnalyzer(analyzer)
    } else {
        setAnalyzer(object : ImageAnalysis.Analyzer {
            private var lastAnalyzeTime: Long = 0

            override fun analyze(image: ImageProxy?, rotationDegrees: Int) {
                if ((System.currentTimeMillis() - lastAnalyzeTime) < intervalMilliSeconds) {
                    // skip this analyze
                    return
                }

                try {
                    analyzer.analyze(image, rotationDegrees)
                } finally {
                    lastAnalyzeTime = System.currentTimeMillis()
                }
            }
        })
    }
}

/**
 * Analyze image by async task(Firebase ML or such else.)
 */
fun ImageAnalysis.setVisionAnalyzer(taskFactory: (image: ImageProxy, rotationDegrees: Int) -> Task<*>?) {
    analyzer = object : ImageAnalysis.Analyzer {
        /**
         * Current task
         */
        private var task: Task<*>? = null

        private val canStartNextTask: Boolean
            get() {
                return task == null ||
                        task?.isComplete == true ||
                        task?.isCanceled == true
            }

        override fun analyze(image: ImageProxy?, rotationDegrees: Int) {
            if (canStartNextTask) {
                task = taskFactory(image ?: return, rotationDegrees)
            }
        }
    }
}

/**
 * Analyze image by Firebase ML Kit.
 *
 * you may add meta-data into AndroidManifest.xml
 *
 * e.g.) AndroidManifest.xml
 * <meta-data
 *      android:name="com.google.firebase.ml.vision.DEPENDENCIES"
 *      android:value="ocr" />
 *
 * e.g.) example code.
 * analysis.setVisionBarcodeDetectorAnalyzer { barcodeList ->
 *      barcodeList.forEach { barcode ->
 *          // do something.
 *      }
 * }
 *
 * @see FirebaseVision.getInstance
 * @see FirebaseVisionImage
 * @see FirebaseVisionImageMetadata
 */
fun ImageAnalysis.setVisionBarcodeDetectorAnalyzer(analyzer: (barcode: List<FirebaseVisionBarcode>) -> Unit) {
    setVisionAnalyzer { image, rotationDegrees ->
        val vision = try {
            FirebaseVision.getInstance()!!
        } catch (e: ClassNotFoundException) {
            return@setVisionAnalyzer null
        }

        vision.visionBarcodeDetector.detectInImage(
            FirebaseVisionImage.fromMediaImage(
                image.image ?: return@setVisionAnalyzer null,
                when (rotationDegrees) {
                    90 -> FirebaseVisionImageMetadata.ROTATION_90
                    180 -> FirebaseVisionImageMetadata.ROTATION_180
                    270 -> FirebaseVisionImageMetadata.ROTATION_270
                    else -> FirebaseVisionImageMetadata.ROTATION_0
                }
            )
        ).also { task ->
            task.addOnCompleteListener {
                if (task.isSuccessful) {
                    analyzer(task.result ?: emptyList())
                }
            }
        }
    }
}
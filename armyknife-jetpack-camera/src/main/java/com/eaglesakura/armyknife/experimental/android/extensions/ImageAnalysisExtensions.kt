package com.eaglesakura.armyknife.experimental.android.extensions

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

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
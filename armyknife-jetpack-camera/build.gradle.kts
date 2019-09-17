apply(from = "../dsl/android-library.gradle")
apply(from = "../dsl/ktlint.gradle")
apply(from = "../dsl/bintray.gradle")

dependencies {
    "api"("com.eaglesakura.armyknife.armyknife-runtime:armyknife-runtime:1.3.3")
    "api"("com.google.android.gms:play-services-vision:18.0.0")
    "api"("com.google.firebase:firebase-ml-vision:23.0.0")

    "api"("androidx.core:core:1.1.0")
    "api"("androidx.core:core-ktx:1.1.0")

    // TODO: check for updates(current version is alpha).
    "api"("androidx.camera:camera-core:1.0.0-alpha05")
    "api"("androidx.camera:camera-camera2:1.0.0-alpha05")
}
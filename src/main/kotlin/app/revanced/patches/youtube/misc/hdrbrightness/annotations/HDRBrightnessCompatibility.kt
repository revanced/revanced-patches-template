package app.revanced.patches.youtube.misc.hdrbrightness.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [Package(
        "com.google.android.youtube", arrayOf("17.33.42", "17.34.35", "17.34.36", "17.36.37", "17.37.35", "17.38.35", "17.39.35", "17.40.41")
    )]
)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class HDRBrightnessCompatibility

package app.revanced.patches.youtube.layout.playerbuttonoverlay.annotations
import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [
        Package("com.google.android.youtube", arrayOf("17.34.36","17.36.37", "17.36.39", "17.38.36", "17.39.35" ))
    ]
)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class PlayerButtonOverlayCompatibility

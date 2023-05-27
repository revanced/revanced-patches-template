package app.revanced.patches.youtube.interaction.downloads.annotation

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("com.google.android.youtube", arrayOf("18.19.35", "18.19.36"))])
@Target(AnnotationTarget.CLASS)
internal annotation class DownloadsCompatibility


package app.revanced.patches.music.layout.upgradebutton.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("com.google.android.apps.youtube.music")])
@Target(AnnotationTarget.CLASS)
internal annotation class RemoveUpgradeButtonCompatibility

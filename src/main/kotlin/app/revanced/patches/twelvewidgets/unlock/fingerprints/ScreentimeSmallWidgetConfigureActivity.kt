package app.revanced.patches.twelvewidgets.unlock.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object ScreentimeSmallWidgetConfigureActivity : MethodFingerprint(
    "L",
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("/ScreentimeSmallWidgetConfigureActivity;") && methodDef.name == "getAddButton"
    }
)

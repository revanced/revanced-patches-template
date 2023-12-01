package app.revanced.patches.youtube.misc.microg.fingerprints


import app.revanced.patcher.fingerprint.MethodFingerprint

internal object PrimeFingerprint : MethodFingerprint(
    strings = listOf("com.google.android.GoogleCamera", "com.android.vending")
)
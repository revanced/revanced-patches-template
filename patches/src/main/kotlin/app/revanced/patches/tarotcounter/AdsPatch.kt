package app.revanced.patches.example

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.fingerprint
import app.revanced.util.returnEarlyString
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.AccessFlags

@Suppress("unused")
val examplePatch = bytecodePatch(
    name = "Remove ads",
) {
    compatibleWith("net.aasuited.tarotscore"("3.8.2"));

    execute {
        bannerAdUnitFingerprint.method.returnEarlyString();
        openAdUnitFingerprint.method.returnEarlyString();
    }
}

package app.revanced.patches.nyx.misc.pro.patch

import app.revanced.patcher.BytecodeContext
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.nyx.misc.pro.annotations.UnlockProCompatibility
import app.revanced.patches.nyx.misc.pro.fingerprints.CheckProFingerprint

@Patch
@Name("Unlock pro")
@Description("Unlocks all pro features.")
@UnlockProCompatibility
@Version("0.0.1")
class UnlockProPatch : BytecodePatch(
    listOf(
        CheckProFingerprint
    )
) {
    override suspend fun execute(context: BytecodeContext) {
        val method = CheckProFingerprint.result!!.mutableMethod
        method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )

    }
}

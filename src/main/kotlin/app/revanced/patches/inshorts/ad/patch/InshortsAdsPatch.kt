package app.revanced.patches.inshorts.ad.patch

import app.revanced.extensions.error
import app.revanced.patcher.BytecodeContext
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.inshorts.ad.annotations.HideAdsCompatibility
import app.revanced.patches.inshorts.ad.fingerprints.InshortsAdsFingerprint

@Patch
@Name("hide-ads")
@Description("Removes ads from Inshorts.")
@HideAdsCompatibility
@Version("0.0.1")
class HideAdsPatch : BytecodePatch(
    listOf(InshortsAdsFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        InshortsAdsFingerprint.result?.let { result ->
            result.apply {
                mutableMethod.addInstruction(
                    0,
                    """
                        return-void
                    """
                )
            }
        } ?: return InshortsAdsFingerprint.error()

    }
}

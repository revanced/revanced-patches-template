package app.revanced.patches.youtube.layout.hide.endscreencards.bytecode.patch

import app.revanced.patcher.BytecodeContext
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.layout.hide.endscreencards.annotations.HideEndscreenCardsCompatibility
import app.revanced.patches.youtube.layout.hide.endscreencards.bytecode.fingerprints.LayoutCircleFingerprint
import app.revanced.patches.youtube.layout.hide.endscreencards.bytecode.fingerprints.LayoutIconFingerprint
import app.revanced.patches.youtube.layout.hide.endscreencards.bytecode.fingerprints.LayoutVideoFingerprint
import app.revanced.patches.youtube.layout.hide.endscreencards.resource.patch.HideEndscreenCardsResourcePatch
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import org.jf.dexlib2.iface.instruction.formats.Instruction21c

@Patch
@DependsOn([IntegrationsPatch::class, HideEndscreenCardsResourcePatch::class])
@Name("hide-endscreen-cards")
@Description("Hides the suggested video cards at the end of a video in fullscreen.")
@HideEndscreenCardsCompatibility
@Version("0.0.1")
class HideEndscreenCardsPatch : BytecodePatch(
    listOf(
        LayoutCircleFingerprint,
        LayoutIconFingerprint,
        LayoutVideoFingerprint,
    )
) {
    override fun execute(context: BytecodeContext) {
        fun MethodFingerprint.injectHideCall() {
            val layoutResult = result!!
            val layoutMethod = layoutResult.mutableMethod

            val checkCastIndex = layoutResult.scanResult.patternScanResult!!.endIndex
            val viewRegister = (layoutMethod.instruction(checkCastIndex) as Instruction21c).registerA

            layoutMethod.addInstruction(
                checkCastIndex + 1,
                "invoke-static { v$viewRegister }, Lapp/revanced/integrations/patches/HideEndscreenCardsPatch;->hideEndscreen(Landroid/view/View;)V"
            )
        }
        
        listOf(LayoutCircleFingerprint, LayoutIconFingerprint, LayoutVideoFingerprint).forEach(MethodFingerprint::injectHideCall)

    }
}

package app.revanced.patches.youtube.misc.playercontrols.bytecode.patch

import app.revanced.patcher.BytecodeContext
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprintResult
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.youtube.misc.playercontrols.annotation.PlayerControlsCompatibility
import app.revanced.patches.youtube.misc.playercontrols.fingerprints.BottomControlsInflateFingerprint
import app.revanced.patches.youtube.misc.playercontrols.fingerprints.PlayerControlsVisibilityFingerprint
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction

@Name("player-controls-bytecode-patch")
@Description("Manages the code for the player controls of the YouTube player.")
@DependsOn([ResourceMappingPatch::class])
@PlayerControlsCompatibility
@Version("0.0.1")
class PlayerControlsBytecodePatch : BytecodePatch(
    listOf(PlayerControlsVisibilityFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        showPlayerControlsFingerprintResult = PlayerControlsVisibilityFingerprint.result!!

        bottomUiContainerResourceId = ResourceMappingPatch.resourceIdOf("id", "bottom_ui_container_stub")

        // TODO: another solution is required, this is hacky
        listOf(BottomControlsInflateFingerprint).resolve(context, context.classes)
        inflateFingerprintResult = BottomControlsInflateFingerprint.result!!

    }

    internal companion object {
        var bottomUiContainerResourceId: Long = 0

        lateinit var showPlayerControlsFingerprintResult: MethodFingerprintResult

        private var inflateFingerprintResult: MethodFingerprintResult? = null
            set(fingerprint) {
                field = fingerprint!!.also {
                    moveToRegisterInstructionIndex = it.scanResult.patternScanResult!!.endIndex
                    viewRegister =
                        (it.mutableMethod.implementation!!.instructions[moveToRegisterInstructionIndex] as OneRegisterInstruction).registerA
                }
            }

        private var moveToRegisterInstructionIndex: Int = 0
        private var viewRegister: Int = 0

        /**
         * Injects the code to change the visibility of controls.
         * @param descriptor The descriptor of the method which should be called.
         */
        fun injectVisibilityCheckCall(descriptor: String) {
            showPlayerControlsFingerprintResult.mutableMethod.addInstruction(
                0,
                """
                    invoke-static {p1}, $descriptor
                """
            )
        }

        /**
         * Injects the code to initialize the controls.
         * @param descriptor The descriptor of the method which should be calleed.
         */
        fun initializeControl(descriptor: String) {
            inflateFingerprintResult!!.mutableMethod.addInstruction(
                moveToRegisterInstructionIndex + 1,
                "invoke-static {v$viewRegister}, $descriptor"
            )
        }
    }
}
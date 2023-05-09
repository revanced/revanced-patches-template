package app.revanced.patches.messenger.inputfield.patch

import app.revanced.extensions.error
import app.revanced.patcher.annotation.*
import app.revanced.patcher.BytecodeContext
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.messenger.inputfield.fingerprints.SwitchMessangeInputEmojiButtonFingerprint
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction

@Patch
@Name("disable-switching-emoji-to-sticker-in-message-input-field")
@Description("Disables switching from emoji to sticker search mode in message input field")
@Compatibility([Package("com.facebook.orca")])
@Version("0.0.1")
class DisableSwitchingEmojiToStickerInMessageInputField : BytecodePatch(listOf(SwitchMessangeInputEmojiButtonFingerprint)) {
    override fun execute(context: BytecodeContext) {
        SwitchMessangeInputEmojiButtonFingerprint.result?.let {
            val setStringIndex = it.scanResult.patternScanResult!!.startIndex + 2

            it.mutableMethod.apply {
                val targetRegister = instruction<OneRegisterInstruction>(setStringIndex).registerA

                replaceInstruction(
                    setStringIndex,
                    "const-string v$targetRegister, \"expression\""
                )
            }
        } ?: SwitchMessangeInputEmojiButtonFingerprint.error()
    }
}

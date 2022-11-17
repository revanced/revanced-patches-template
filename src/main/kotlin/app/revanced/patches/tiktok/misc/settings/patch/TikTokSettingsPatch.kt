package app.revanced.patches.tiktok.misc.settings.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.tiktok.misc.integrations.patch.TikTokIntegrationsPatch
import app.revanced.patches.tiktok.misc.settings.annotations.TikTokSettingsCompatibility
import app.revanced.patches.tiktok.misc.settings.fingerprints.AboutOnClickMethodFingerprint
import app.revanced.patches.tiktok.misc.settings.fingerprints.AdPersonalizationActivityOnCreateFingerprint
import app.revanced.patches.tiktok.misc.settings.fingerprints.SettingsOnViewCreatedFingerprint
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.instruction.formats.Instruction21c
import org.jf.dexlib2.iface.instruction.formats.Instruction35c
import org.jf.dexlib2.iface.reference.MethodReference
import org.jf.dexlib2.iface.reference.StringReference
import org.jf.dexlib2.iface.reference.TypeReference

@Patch
@DependsOn([TikTokIntegrationsPatch::class])
@Name("tiktok-settings")
@Description("Add settings menu to TikTok.")
@TikTokSettingsCompatibility
@Version("0.0.1")
class TikTokSettingsPatch : BytecodePatch(
    listOf(
        AdPersonalizationActivityOnCreateFingerprint,
        SettingsOnViewCreatedFingerprint,
        AboutOnClickMethodFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        //Change onClick of About to start revanced settings
        val aboutOnClickMethod = AboutOnClickMethodFingerprint.result!!.mutableMethod
        aboutOnClickMethod.addInstructions(
            0,
            """
                    invoke-static {}, Lapp/revanced/tiktok/settingsmenu/SettingsMenu;->startSettingsActivity()V
                    return-void
                """
        )

        // Replace string `Copyright Policy` to 'Revanced Settings` in TikTok settings.
        with(SettingsOnViewCreatedFingerprint.result!!.mutableMethod) {
            val instructions = implementation!!.instructions

            for ((index, instruction) in instructions.withIndex()) {
                if (instruction.opcode != Opcode.CONST_STRING) continue

                val string = ((instruction as ReferenceInstruction).reference as StringReference).string
                if (string != "copyright_policy") continue

                val targetIndex = index - 6

                val resultInstruction = instructions[targetIndex].also {
                    if (it.opcode != Opcode.MOVE_RESULT_OBJECT)
                        return PatchResultError("Hardcode offset changed.")
                }


                val overrideRegister = (resultInstruction as OneRegisterInstruction).registerA

                replaceInstruction(
                    targetIndex, "const-string v$overrideRegister, \"Revanced Settings\""
                )

                // Change onClick to start settings activity.
                val clickInstruction = instruction(index - 1).also {
                    if (it.opcode != Opcode.INVOKE_DIRECT)
                        return PatchResultError("Hardcode offset changed.")
                }

                with(((clickInstruction as ReferenceInstruction).reference as MethodReference).definingClass) {
                    context.findClass(this)!!
                        .mutableClass
                        .methods.first { it.name == "onClick" }.addInstructions(
                            0,
                            """
                                    invoke-static {}, Lapp/revanced/tiktok/settingsmenu/SettingsMenu;->startSettingsActivity()V
                                    return-void
                                """
                        )
                }

                break
            }

            //Replace string `About` to 'Revanced Settings` in TikTok settings.
            for ((index, instruction) in instructions.withIndex()) {
                if (instruction.opcode != Opcode.NEW_INSTANCE) continue

                val onClickClass = ((instruction as Instruction21c).reference as TypeReference).type
                if (onClickClass != aboutOnClickMethod.definingClass) continue

                val targetIndex = index - 4

                val resultInstruction = instructions[targetIndex].also {
                    if (it.opcode != Opcode.MOVE_RESULT_OBJECT)
                        return PatchResultError("Hardcode offset changed.")
                }

                val overrideRegister = (resultInstruction as OneRegisterInstruction).registerA
                replaceInstruction(
                    targetIndex,
                    """
                        const-string v$overrideRegister, "Revanced Settings"
                    """
                )
                break
            }

        }


        //Implement revanced settings screen in `AdPersonalizationActivity`
        with(AdPersonalizationActivityOnCreateFingerprint.result!!.mutableMethod) {
            for ((index, instruction) in implementation!!.instructions.withIndex()) {
                if (instruction.opcode != Opcode.INVOKE_SUPER) continue

                val thisRegister = (instruction as Instruction35c).registerC

                addInstructions(
                    index + 1,
                    """
                        invoke-static {v$thisRegister}, Lapp/revanced/tiktok/settingsmenu/SettingsMenu;->initializeSettings(Lcom/bytedance/ies/ugc/aweme/commercialize/compliance/personalization/AdPersonalizationActivity;)V
                        return-void
                    """
                )
                break
            }
        }

        return PatchResultSuccess()
    }
}
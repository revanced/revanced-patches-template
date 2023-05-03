package app.revanced.patches.spotify.premium_navbar_tab.patch

import app.revanced.patcher.BytecodeContext
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.removeInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.spotify.premium_navbar_tab.annotations.PremiumNavbarTabCompatibility
import app.revanced.patches.spotify.premium_navbar_tab.fingerprints.AddPremiumNavbarTabFingerprint
import app.revanced.patches.spotify.premium_navbar_tab.fingerprints.AddPremiumNavbarTabParentFingerprint
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.WideLiteralInstruction

@Patch
@Name("hide-premium-navbar")
@Description("Removes the premium tab from the navbar.")
@PremiumNavbarTabCompatibility
@DependsOn([ResourceMappingPatch::class])
@Version("0.0.1")
class PremiumNavbarTabPatch : BytecodePatch(
    listOf(
        AddPremiumNavbarTabParentFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        val parentResult = AddPremiumNavbarTabParentFingerprint.result!!
        AddPremiumNavbarTabFingerprint.resolve(context, parentResult.classDef)

        val result = AddPremiumNavbarTabFingerprint.result!!

        val method = result.mutableMethod
        val methodInstructions = method.implementation!!.instructions
        val lastInstructionIdx = methodInstructions.size - 1

        val premiumTabId = ResourceMappingPatch.resourceIdOf("id", "premium_tab")

        var removeAmount = 2
        // 2nd const remove method
        for ((i, instruction) in methodInstructions.asReversed().withIndex()) {
            if (instruction.opcode.ordinal != Opcode.CONST.ordinal) continue
            if ((instruction as WideLiteralInstruction).wideLiteral != premiumTabId) continue

            val findThreshold = 10
            val constIndex = lastInstructionIdx - i
            val invokeInstruction = methodInstructions.subList(constIndex, constIndex + findThreshold).first {
                it.opcode.ordinal == Opcode.INVOKE_VIRTUAL_RANGE.ordinal
            }
            method.removeInstruction(methodInstructions.indexOf(invokeInstruction))

            if (--removeAmount == 0) break
        }

    }
}
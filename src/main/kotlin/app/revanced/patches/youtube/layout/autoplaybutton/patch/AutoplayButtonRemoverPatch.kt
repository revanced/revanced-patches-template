package app.revanced.patches.youtube.layout.autoplaybutton.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.removeInstructions
import app.revanced.patcher.extensions.replaceInstructions
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Dependencies
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patches.youtube.layout.autoplaybutton.annotations.AutoplayButtonCompatibility
import app.revanced.patches.youtube.layout.autoplaybutton.fingerprints.LayoutConstructorFingerprint
import app.revanced.patches.youtube.layout.autoplaybutton.fingerprints.AutonavInformerFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.mapping.patch.ResourceIdMappingProviderResourcePatch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.instruction.WideLiteralInstruction
import org.jf.dexlib2.iface.reference.MethodReference

@Patch
@Dependencies(dependencies = [ResourceIdMappingProviderResourcePatch::class])
@Name("disable-autoplay-button")
@Description("Disable the autoplay button.")
@AutoplayButtonCompatibility
@Version("0.0.1")
class AutoplayButtonRemoverPatch : BytecodePatch(
    listOf(
        LayoutConstructorFingerprint, AutonavInformerFingerprint
        )
) {
    override fun execute(data: BytecodeData): PatchResult {
        val layoutGenMethod = LayoutConstructorFingerprint.result!!.mutableMethod

        val autonavToggle = ResourceIdMappingProviderResourcePatch.resourceMappings.first { it.type == "id" && it.name == "autonav_toggle" }

        val autonavPreviewStub = ResourceIdMappingProviderResourcePatch.resourceMappings.first { it.type == "id" && it.name == "autonav_preview_stub" }

        val autonavToggleConstIndex =
            layoutGenMethod.implementation!!.instructions.indexOfFirst { (it as? WideLiteralInstruction)?.wideLiteral == autonavToggle.id }

        val autonavPreviewStubConstIndex =
            layoutGenMethod.implementation!!.instructions.indexOfFirst { (it as? WideLiteralInstruction)?.wideLiteral == autonavPreviewStub.id }

        layoutGenMethod.removeInstructions(autonavToggleConstIndex, 5)
        layoutGenMethod.removeInstructions(autonavPreviewStubConstIndex, 5)

        

        val informerClassDef = data.classes.first { it.type.contains("WillAutonavInformer") }
        val autonavInformerMethod = AutonavInformerFingerprint.result!!.mutableMethod
        println("${autonavInformerMethod.definingClass}: ${autonavInformerMethod.name}")

        autonavInformerMethod.replaceInstructions(0, 
        """
        const/4 v0, 0x0
        return v0
        """)

        return PatchResultSuccess()
    }
}

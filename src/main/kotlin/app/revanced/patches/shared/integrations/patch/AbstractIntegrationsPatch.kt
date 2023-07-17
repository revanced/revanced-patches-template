package app.revanced.patches.shared.integrations.patch

import app.revanced.patcher.BytecodeContext
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patches.shared.integrations.patch.AbstractIntegrationsPatch.IntegrationsFingerprint.RegisterResolver
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.Method

@Description("Applies mandatory patches to implement the ReVanced integrations into the application.")
@Version("0.0.1")
abstract class AbstractIntegrationsPatch(
    private val integrationsDescriptor: String,
    private val hooks: Iterable<IntegrationsFingerprint>
) : BytecodePatch(hooks) {
    /**
     * [MethodFingerprint] for integrations.
     *
     * @param contextRegisterResolver A [RegisterResolver] to get the register.
     * @see MethodFingerprint
     */
    abstract class IntegrationsFingerprint(
        returnType: String? = null,
        accessFlags: Int? = null,
        parameters: Iterable<String>? = null,
        opcodes: Iterable<Opcode?>? = null,
        strings: Iterable<String>? = null,
        customFingerprint: ((methodDef: Method, classDef: ClassDef) -> Boolean)? = null,
        private val contextRegisterResolver: (Method) -> Int = object : RegisterResolver {}
    ) : MethodFingerprint(
        returnType,
        accessFlags,
        parameters,
        opcodes,
        strings,
        customFingerprint
    ) {
        fun invoke(integrationsDescriptor: String) {
            result?.mutableMethod?.let { method ->
                val contextRegister = contextRegisterResolver(method)

                method.addInstruction(
                    0,
                    "sput-object v$contextRegister, " +
                            "$integrationsDescriptor->context:Landroid/content/Context;"
                )
            } ?: throw PatchException("Could not find hook target fingerprint.")
        }

        interface RegisterResolver : (Method) -> Int {
            override operator fun invoke(method: Method) = method.implementation!!.registerCount - 1
        }
    }

    override suspend fun execute(context: BytecodeContext) {
        if (context.classes.find { it.type == integrationsDescriptor } == null) throw MISSING_INTEGRATIONS

        for (hook in hooks) hook.invoke(integrationsDescriptor)

    }

    private companion object {
        val MISSING_INTEGRATIONS = PatchException(
            "Integrations have not been merged yet. " +
                    "This patch can not succeed without merging the integrations."
        )
    }
}
package app.revanced.util.fingerprint

import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.Method
import org.jf.dexlib2.iface.instruction.Instruction

object FingerprintUtils {
    // TODO: Use CustomFingerprint typealias once https://github.com/revanced/revanced-patcher/pull/189 is merged
    inline fun <reified T : Instruction> any(
        opcode: Opcode,
        crossinline predicate: (T) -> Boolean
    ): ((methodDef: Method, classDef: ClassDef) -> Boolean) {
        return { methodDef, _ ->
            methodDef.implementation?.instructions?.any anyInstruction@{
                if (it.opcode != opcode) return@anyInstruction false
                if (it !is T) return@anyInstruction false

                predicate(it)
            } ?: false
        }
    }
}

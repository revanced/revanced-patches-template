package app.revanced.patches.reddit

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.fingerprint
import app.revanced.util.returnEarlyString
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.AccessFlags

@Suppress("unused")
val proxyPatch = bytecodePatch(
    name = "Reroute API calls",
) {
    compatibleWith("com.reddit.frontpage"("2025.03.1"));

    extendWith("extensions/extension.rve")

    execute {
        println(httpClientNewCallFingerprint.method);


        httpClientNewCallFingerprint.method.addInstructions(0, """
            invoke-static {p1}, Lapp/revanced/extension/LoggerPatch;->printUrl(Lokhttp3/Request;)V
        """)
    }
}

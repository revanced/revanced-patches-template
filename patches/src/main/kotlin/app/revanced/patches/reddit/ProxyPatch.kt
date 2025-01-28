package app.revanced.patches.reddit

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.stringOption
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

    val proxyHost = stringOption(key = "proxyHost", required = true);
    val proxyCookies = stringOption(key = "proxyCookies");

    execute {
        httpClientNewCallFingerprint.method.addInstructions(0, """
            const-string v0, "${proxyHost.value}"
            const-string v1, "${proxyCookies.value ?: ""}"
            invoke-static {p1, v0, v1}, Lapp/revanced/extension/LoggerPatch;->printAndModifyUrl(Lokhttp3/Request;Ljava/lang/String;Ljava/lang/String;)Lokhttp3/Request;

            move-result-object p1
        """)
    }
}

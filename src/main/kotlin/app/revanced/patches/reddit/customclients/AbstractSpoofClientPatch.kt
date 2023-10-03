package app.revanced.patches.reddit.customclients

import android.os.Environment
import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprintResult
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.options.PatchOptionException
import app.revanced.patcher.patch.options.types.StringPatchOption.Companion.stringPatchOption
import java.io.File
import java.util.*

abstract class AbstractSpoofClientPatch(
    private val redirectUri: String,
    private val clientIdFingerprints: List<MethodFingerprint>,
    private val userAgentFingerprints: List<MethodFingerprint>? = null,
    private val miscellaneousFingerprints: List<MethodFingerprint>? = null
) : BytecodePatch(buildSet {
    addAll(clientIdFingerprints)
    userAgentFingerprints?.let(::addAll)
    miscellaneousFingerprints?.let(::addAll)
}) {
    var clientId by stringPatchOption(
        "client-id",
        null,
        "OAuth client ID",
        "The Reddit OAuth client ID. " +
                "You can get your client ID from https://www.reddit.com/prefs/apps. " +
                "The application type has to be \"Installed app\" " +
                "and the redirect URI has to be set to \"$redirectUri\".",
        true
    )

    override fun execute(context: BytecodeContext) {
        val requiredOptions = options.values.filter { it.required }

        val isAndroidButRequiredOptionsUnset = try {
            Class.forName("android.os.Environment")

            requiredOptions.any { it.value == null }
        } catch (_: ClassNotFoundException) {
            false
        }

        if (isAndroidButRequiredOptionsUnset) {
            val properties = Properties()

            val propertiesFile = File(
                Environment.getExternalStorageDirectory(),
                "revanced_client_spoof_${redirectUri.hashCode()}.properties"
            )
            if (propertiesFile.exists()) {
                properties.load(propertiesFile.inputStream())

                // Set options from properties file.
                properties.forEach { (name, value) ->
                    try {
                        options[name.toString()] = value.toString().trim()
                    } catch (_: PatchOptionException.PatchOptionNotFoundException) {
                        // Ignore unknown options.
                    }
                }
            } else {
                options.keys.forEach { properties.setProperty(it, "") }

                properties.store(
                    propertiesFile.outputStream(),
                    "Options for the ReVanced \"Client Spoof\" patch. Required options: " +
                            requiredOptions.joinToString { it.key }
                )
            }

            requiredOptions.filter { it.value == null }.let { requiredUnsetOptions ->
                if (requiredUnsetOptions.isEmpty()) return@let

                val error = """
                    In order to use this patch, you need to provide the following options:
                    ${requiredUnsetOptions.joinToString("\n") { "${it.key}: ${it.description}" }}

                    A properties file has been created at ${propertiesFile.absolutePath}.
                    Please fill in the required options before using this patch.
                """.trimIndent()

                throw PatchException(error)
            }
        }

        fun List<MethodFingerprint>?.executePatch(
            patch: List<MethodFingerprintResult>.(BytecodeContext) -> Unit
        ) = this?.map { it.result ?: throw it.exception }?.patch(context)

        clientIdFingerprints.executePatch { patchClientId(context) }
        userAgentFingerprints.executePatch { patchUserAgent(context) }
        miscellaneousFingerprints.executePatch { patchMiscellaneous(context) }
    }

    /**
     * Patch the client ID.
     * The fingerprints are guaranteed to be in the same order as in [clientIdFingerprints].
     *
     * @param context The current [BytecodeContext].
     *
     */
    abstract fun List<MethodFingerprintResult>.patchClientId(context: BytecodeContext)

    /**
     * Patch the user agent.
     * The fingerprints are guaranteed to be in the same order as in [userAgentFingerprints].
     *
     * @param context The current [BytecodeContext].
     */
    // Not every client needs to patch the user agent.
    open fun List<MethodFingerprintResult>.patchUserAgent(context: BytecodeContext) {}

    /**
     * Patch miscellaneous things such as protection measures.
     * The fingerprints are guaranteed to be in the same order as in [miscellaneousFingerprints].
     *
     * @param context The current [BytecodeContext].
     */
    // Not every client needs to patch miscellaneous things.
    open fun List<MethodFingerprintResult>.patchMiscellaneous(context: BytecodeContext) {}
}
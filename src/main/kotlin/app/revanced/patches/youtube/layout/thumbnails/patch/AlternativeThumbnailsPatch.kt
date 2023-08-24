package app.revanced.patches.youtube.layout.thumbnails.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.shared.settings.preference.impl.*
import app.revanced.patches.youtube.layout.thumbnails.annotations.AlternativeThumbnailsCompatibility
import app.revanced.patches.youtube.layout.thumbnails.fingerprints.*
import app.revanced.patches.youtube.misc.integrations.patch.YouTubeIntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.YouTubeSettingsPatch

@Patch
@DependsOn([YouTubeIntegrationsPatch::class, YouTubeSettingsPatch::class])
@Name("Alternative thumbnails")
@AlternativeThumbnailsCompatibility
@Description("Adds options to replace video thumbnails with still image captures of the video.")
class AlternativeThumbnailsPatch : BytecodePatch(
    listOf(MessageDigestImageUrlParentFingerprint, CronetURLRequestCallbackOnResponseStartedFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        YouTubeSettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            PreferenceScreen(
                "revanced_alt_thumbnails_preference_screen",
                "revanced_alt_thumbnails_preference_screen_title",
                listOf(
                    SwitchPreference(
                        "revanced_alt_thumbnail",
                        "revanced_alt_thumbnail_title",
                        "revanced_alt_thumbnail_summary_on",
                        "revanced_alt_thumbnail_summary_off",
                    ),
                    ListPreference(
                        "revanced_alt_thumbnail_type",
                        "revanced_alt_thumbnail_type_title",
                        ArrayResource(
                            "revanced_alt_thumbnail_type_entries",
                            listOf(
                                "revanced_alt_thumbnail_type_entry_1",
                                "revanced_alt_thumbnail_type_entry_2",
                                "revanced_alt_thumbnail_type_entry_3",
                            )
                        ),
                        ArrayResource(
                            "revanced_alt_thumbnail_type_entry_values",
                            listOf(
                                "1",
                                "2",
                                "3",
                            ),
                            literalValues = true
                        )
                    ),
                    SwitchPreference(
                        "revanced_alt_thumbnail_fast_quality",
                        "revanced_alt_thumbnail_fast_quality_title",
                        "revanced_alt_thumbnail_fast_quality_summary_on",
                        "revanced_alt_thumbnail_fast_quality_summary_off",
                    ),
                    NonInteractivePreference(
                        "revanced_alt_thumbnail_about_title",
                        "revanced_alt_thumbnail_about_summary",
                    )
                ),
                "revanced_alt_thumbnails_preference_screen_summary",
            )
        )

        MessageDigestImageUrlParentFingerprint.result
            ?: throw MessageDigestImageUrlParentFingerprint.toErrorResult()
        MessageDigestImageUrlFingerprint.resolve(context, MessageDigestImageUrlParentFingerprint.result!!.classDef)
        MessageDigestImageUrlFingerprint.result?.apply {
            loadImageUrlMethod = mutableMethod
        } ?: throw MessageDigestImageUrlFingerprint.toErrorResult()
        addImageUrlHook(INTEGRATIONS_CLASS_DESCRIPTOR, true)


        CronetURLRequestCallbackOnResponseStartedFingerprint.result
            ?: throw CronetURLRequestCallbackOnResponseStartedFingerprint.toErrorResult()
        CronetURLRequestCallbackOnSucceededFingerprint.resolve(
            context,
            CronetURLRequestCallbackOnResponseStartedFingerprint.result!!.classDef
        )
        CronetURLRequestCallbackOnSucceededFingerprint.result?.apply {
            loadImageSuccessCallbackMethod = mutableMethod
        } ?: throw CronetURLRequestCallbackOnSucceededFingerprint.toErrorResult()
        addImageUrlSuccessCallbackHook(INTEGRATIONS_CLASS_DESCRIPTOR)


        CronetURLRequestCallbackOnFailureFingerprint.resolve(
            context,
            CronetURLRequestCallbackOnResponseStartedFingerprint.result!!.classDef
        )
        CronetURLRequestCallbackOnFailureFingerprint.result?.apply {
            loadImageErrorCallbackMethod = mutableMethod
        } ?: throw CronetURLRequestCallbackOnFailureFingerprint.toErrorResult()
    }

    internal companion object {
        private const val INTEGRATIONS_CLASS_DESCRIPTOR =
            "Lapp/revanced/integrations/patches/AlternativeThumbnailsPatch;"

        private lateinit var loadImageUrlMethod: MutableMethod
        private var loadImageUrlIndex = 0

        private lateinit var loadImageSuccessCallbackMethod: MutableMethod
        private var loadImageSuccessCallbackIndex = 0

        private lateinit var loadImageErrorCallbackMethod: MutableMethod
        private var loadImageErrorCallbackIndex = 0

        /**
         * @param highPriority If the hook should be called before all other hooks.
         */
        fun addImageUrlHook(targetMethodClass: String, highPriority: Boolean) {
            loadImageUrlMethod.addInstructions(
                if (highPriority) 0 else loadImageUrlIndex, """
                    invoke-static { p1 }, $targetMethodClass->overrideImageURL(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object p1
                """
            )
            loadImageUrlIndex += 2
        }

        /**
         * If a connection completed, which includes normal 200 responses but also includes
         * status 404 and other error like http responses.
         */
        fun addImageUrlSuccessCallbackHook(targetMethodClass: String) {
            loadImageSuccessCallbackMethod.addInstruction(
                loadImageSuccessCallbackIndex++,
                "invoke-static { p2 }, $targetMethodClass->handleCronetSuccess(Lorg/chromium/net/UrlResponseInfo;)V"
            )
        }

        /**
         * If a connection outright failed to complete any connection.
         */
        fun addImageUrlErrorCallbackHook(targetMethodClass: String) {
            loadImageErrorCallbackMethod.addInstruction(
                loadImageErrorCallbackIndex++,
                "invoke-static { p2, p3 }, $targetMethodClass->handleCronetFailure(Lorg/chromium/net/UrlResponseInfo;Ljava/io/IOException;)V"
            )
        }

    }
}

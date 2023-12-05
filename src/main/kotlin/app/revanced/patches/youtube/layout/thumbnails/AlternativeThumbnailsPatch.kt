package app.revanced.patches.youtube.layout.thumbnails

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.shared.settings.preference.impl.*
import app.revanced.patches.youtube.layout.thumbnails.fingerprints.*
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.exception

@Patch(
    name = "Alternative thumbnails",
    description = "Adds options to replace video thumbnails with still image captures of the video.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.32.39",
                "18.37.36",
                "18.38.44",
                "18.43.45",
                "18.44.41",
                "18.45.41",
                "18.45.43"
            ]
        )
    ]
)
@Suppress("unused")
object AlternativeThumbnailsPatch : BytecodePatch(
    setOf(MessageDigestImageUrlParentFingerprint, CronetURLRequestCallbackOnResponseStartedFingerprint)
) {
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
    private fun addImageUrlHook(targetMethodClass: String, highPriority: Boolean) {
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
    private fun addImageUrlSuccessCallbackHook(targetMethodClass: String) {
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

    override fun execute(context: BytecodeContext) {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            PreferenceScreen(
                "revanced_alt_thumbnails_preference_screen",
                StringResource("revanced_alt_thumbnails_preference_screen_title", "Alternative thumbnails"),
                listOf(
                    ListPreference(
                        "revanced_alt_thumbnail_mode",
                        StringResource("revanced_alt_thumbnail_mode_title", "Thumbnail mode"),
                        ArrayResource(
                            "revanced_alt_thumbnail_mode_entries",
                            listOf(
                                StringResource("revanced_alt_thumbnail_mode_entry_1", "Original thumbnails"),
                                StringResource("revanced_alt_thumbnail_mode_entry_2", "YouTube video stills"),
                                StringResource(
                                    "revanced_alt_thumbnail_mode_entry_3",
                                    "DeArrow, fallback to original thumbnails"
                                ),
                                StringResource(
                                    "revanced_alt_thumbnail_mode_entry_4",
                                    "DeArrow, fallback to YouTube video stills"
                                )
                            )
                        ),
                        ArrayResource(
                            "revanced_alt_thumbnail_mode_entry_values",
                            listOf(
                                StringResource("revanced_alt_thumbnail_mode_entry_value_1", "1"),
                                StringResource("revanced_alt_thumbnail_mode_entry_value_2", "2"),
                                StringResource("revanced_alt_thumbnail_mode_entry_value_3", "3"),
                                StringResource("revanced_alt_thumbnail_mode_entry_value_4", "4"),
                            )
                        )
                    ),
                    ListPreference(
                        "revanced_alt_thumbnail_type",
                        StringResource("revanced_alt_thumbnail_type_title", "Video time to take the still from"),
                        ArrayResource(
                            "revanced_alt_thumbnail_type_entries",
                            listOf(
                                StringResource("revanced_alt_thumbnail_type_entry_1", "Beginning of video"),
                                StringResource("revanced_alt_thumbnail_type_entry_2", "Middle of video"),
                                StringResource("revanced_alt_thumbnail_type_entry_3", "End of video"),
                            )
                        ),
                        ArrayResource(
                            "revanced_alt_thumbnail_type_entry_values",
                            listOf(
                                StringResource("revanced_alt_thumbnail_type_entry_value_1", "1"),
                                StringResource("revanced_alt_thumbnail_type_entry_value_2", "2"),
                                StringResource("revanced_alt_thumbnail_type_entry_value_3", "3"),
                            )
                        )
                    ),
                    SwitchPreference(
                        "revanced_alt_thumbnail_fast_quality",
                        StringResource("revanced_alt_thumbnail_fast_quality_title", "Use fast alternative thumbnails"),
                        StringResource(
                            "revanced_alt_thumbnail_fast_quality_summary_on",
                            "Using medium quality stills. " +
                                    "Thumbnails will load faster, but live streams, unreleased, " +
                                    "or very old videos may show blank thumbnails"
                        ),
                        StringResource("revanced_alt_thumbnail_fast_quality_summary_off", "Using high quality stills")
                    ),
                    TextPreference(
                        "revanced_alt_thumbnail_dearrow_api_url",
                        StringResource(
                            "revanced_alt_thumbnail_dearrow_api_url_title",
                            "DeArrow Thumbnail Cache Endpoint"
                        ),
                        StringResource(
                            "revanced_alt_thumbnail_dearrow_api_url_summary",
                            "The URL of the DeArrow thumbnail cache endpoint. " +
                                    "This should not be changed unless you know what you are doing."
                        ),
                    ),
                    NonInteractivePreference(
                        StringResource("revanced_alt_thumbnail_about_stills_title", "About video stills"),
                        StringResource(
                            "revanced_alt_thumbnail_about_stills_summary",
                            "Video stills are still images from the beginning/middle/end of each video. " +
                                    "No external API is used, as these images are built into YouTube"
                        )
                    ),
                    NonInteractivePreference(
                        StringResource("revanced_alt_thumbnail_about_dearrow_title", "About DeArrow"),
                        StringResource(
                            "revanced_alt_thumbnail_about_dearrow_summary",
                            "DeArrow provides crowd-sourced thumbnails for YouTube videos. " +
                                    "These thumbnails are often more relevant than the ones provided by YouTube. " +
                                    "Using DeArrow will send requests to DeArrows servers. No other data is sent."
                        )
                    )
                ),
                StringResource("revanced_alt_thumbnails_preference_screen_summary", "Video thumbnail settings")
            )
        )

        MessageDigestImageUrlParentFingerprint.result
            ?: throw MessageDigestImageUrlParentFingerprint.exception
        MessageDigestImageUrlFingerprint.resolve(context, MessageDigestImageUrlParentFingerprint.result!!.classDef)
        MessageDigestImageUrlFingerprint.result?.apply {
            loadImageUrlMethod = mutableMethod
        } ?: throw MessageDigestImageUrlFingerprint.exception
        addImageUrlHook(INTEGRATIONS_CLASS_DESCRIPTOR, true)


        CronetURLRequestCallbackOnResponseStartedFingerprint.result
            ?: throw CronetURLRequestCallbackOnResponseStartedFingerprint.exception
        CronetURLRequestCallbackOnSucceededFingerprint.resolve(
            context,
            CronetURLRequestCallbackOnResponseStartedFingerprint.result!!.classDef
        )
        CronetURLRequestCallbackOnSucceededFingerprint.result?.apply {
            loadImageSuccessCallbackMethod = mutableMethod
        } ?: throw CronetURLRequestCallbackOnSucceededFingerprint.exception
        addImageUrlSuccessCallbackHook(INTEGRATIONS_CLASS_DESCRIPTOR)


        CronetURLRequestCallbackOnFailureFingerprint.resolve(
            context,
            CronetURLRequestCallbackOnResponseStartedFingerprint.result!!.classDef
        )
        CronetURLRequestCallbackOnFailureFingerprint.result?.apply {
            loadImageErrorCallbackMethod = mutableMethod
        } ?: throw CronetURLRequestCallbackOnFailureFingerprint.exception
        addImageUrlErrorCallbackHook(INTEGRATIONS_CLASS_DESCRIPTOR)
    }
}

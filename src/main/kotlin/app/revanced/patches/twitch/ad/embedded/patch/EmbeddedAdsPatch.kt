package app.revanced.patches.twitch.ad.embedded.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.MethodFingerprintExtensions.name
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.shared.settings.preference.impl.ArrayResource
import app.revanced.patches.shared.settings.preference.impl.ListPreference
import app.revanced.patches.twitch.ad.embedded.annotations.EmbeddedAdsCompatibility
import app.revanced.patches.twitch.ad.embedded.fingerprints.CreateUsherClientFingerprint
import app.revanced.patches.twitch.ad.video.patch.VideoAdsPatch
import app.revanced.patches.twitch.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.twitch.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.twitch.misc.settings.resource.patch.SettingsResourcePatch

@Patch
@DependsOn([VideoAdsPatch::class, IntegrationsPatch::class, SettingsPatch::class])
@Name("Block embedded ads")
@Description("Blocks embedded stream ads using services like TTV.lol or PurpleAdBlocker.")
@EmbeddedAdsCompatibility
class EmbeddedAdsPatch : BytecodePatch(
    listOf(CreateUsherClientFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        val result = CreateUsherClientFingerprint.result ?: throw PatchException("${CreateUsherClientFingerprint.name} not found")

        // Inject OkHttp3 application interceptor
        result.mutableMethod.addInstructions(
            3,
            """
                invoke-static  {}, Lapp/revanced/twitch/patches/EmbeddedAdsPatch;->createRequestInterceptor()Lapp/revanced/twitch/api/RequestInterceptor;
                move-result-object v2
                invoke-virtual {v0, v2}, Lokhttp3/OkHttpClient${"$"}Builder;->addInterceptor(Lokhttp3/Interceptor;)Lokhttp3/OkHttpClient${"$"}Builder;
            """
        )

        SettingsPatch.PreferenceScreen.ADS.SURESTREAM.addPreferences(
            ListPreference(
                "revanced_block_embedded_ads",
                "revanced_block_embedded_ads",
                ArrayResource(
                    "revanced_hls_proxies",
                    listOf(
                        "revanced_proxy_disabled",
                        "revanced_proxy_luminous",
                        "revanced_proxy_purpleadblock",
                    )
                ),
                ArrayResource(
                    "revanced_hls_proxies_values",
                    listOf(
                        "disabled",
                        "luminous",
                        "purpleadblock",
                    ),
                    literalValues = true
                ),
                default = "luminous"
            )
        )

        SettingsResourcePatch.mergePatchStrings("EmbeddedAds")
    }
}

package app.revanced.patches.youtube.layout.hide.time

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.hide.time.fingerprints.TimeCounterFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.strings.StringsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    name = "Hide timestamp",
    description = "Hides timestamp in video player.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", [
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
object HideTimestampPatch : BytecodePatch(
    setOf(TimeCounterFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        StringsPatch.includePatchStrings("HideTimestamp")
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_timestamp",
                "revanced_hide_timestamp_title",
                "revanced_hide_timestamp_summary_on",
                "revanced_hide_timestamp_summary_off"
            )
        )

        TimeCounterFingerprint.result?.apply {
            mutableMethod.addInstructionsWithLabels(
                0,
                """
                invoke-static { }, Lapp/revanced/integrations/patches/HideTimestampPatch;->hideTimestamp()Z
                move-result v0
                if-eqz v0, :hide_time
                return-void
                :hide_time
                nop
            """
            )
        } ?: throw TimeCounterFingerprint.exception
    }
}

package app.revanced.patches.youtube.layout.panels.popup.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.panels.popup.annotations.PlayerPopupPanelsCompatibility
import app.revanced.patches.youtube.layout.panels.popup.fingerprints.EngagementPanelControllerFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.YouTubeIntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.YouTubeSettingsPatch

@Patch
@DependsOn([YouTubeIntegrationsPatch::class, YouTubeSettingsPatch::class])
@Name("Disable player popup panels")
@Description("Disables panels from appearing automatically when going into fullscreen (playlist or live chat).")
@PlayerPopupPanelsCompatibility
class PlayerPopupPanelsPatch : BytecodePatch(
    listOf(
        EngagementPanelControllerFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        YouTubeSettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_player_popup_panels",
                "revanced_hide_player_popup_panels_title",
                "revanced_hide_player_popup_panels_summary_on",
                "revanced_hide_player_popup_panels_summary_off"
            )
        )

        val engagementPanelControllerMethod = EngagementPanelControllerFingerprint
            .result?.mutableMethod ?: throw EngagementPanelControllerFingerprint.exception

        engagementPanelControllerMethod.addInstructionsWithLabels(
            0,
            """
                invoke-static { }, Lapp/revanced/integrations/patches/DisablePlayerPopupPanelsPatch;->disablePlayerPopupPanels()Z
                move-result v0
                if-eqz v0, :player_popup_panels
                if-eqz p4, :player_popup_panels
                const/4 v0, 0x0
                return-object v0
                :player_popup_panels
                nop
            """
        )
    }
}

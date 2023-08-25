package app.revanced.patches.youtube.layout.hide.suggestionsshelf.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.hide.suggestionsshelf.annotations.SuggestionsShelfCompatibility
import app.revanced.patches.youtube.layout.utils.navbarindexhook.patch.NavBarIndexHookPatch
import app.revanced.patches.youtube.misc.litho.filter.patch.LithoFilterPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch

@Patch
@DependsOn([LithoFilterPatch::class, NavBarIndexHookPatch::class])
@Name("Hide Suggestions shelf")
@Description("Hides suggestions shelf on the homepage tab.")
@SuggestionsShelfCompatibility
class SuggestionsShelfPatch : BytecodePatch(
    listOf()
) {
    override fun execute(context: BytecodeContext) {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_suggestions_shelf",
                StringResource("revanced_hide_suggestions_shelf_title", "Hide Suggestions shelves"),
                StringResource("revanced_hide_suggestions_shelf_on", "Suggestions shelves are hidden"),
                StringResource("revanced_hide_suggestions_shelf_off", "Suggestions shelves are shown")
            )
        )

        LithoFilterPatch.addFilter(FILTER_CLASS_DESCRIPTOR)
    }

    private companion object {
        const val FILTER_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/patches/components/SuggestionsShelfFilter;"
    }
}

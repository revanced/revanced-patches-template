package app.revanced.patches.shared.predictivebackgesture.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.shared.predictivebackgesture.annotations.PredictiveBackCompatibility

@Patch
@Name("predictive-back-gesture")
@Description("Enables the predictive back gesture introduced in Android 13.")
@PredictiveBackCompatibility
@Version("0.0.1")
class PredictiveBackGesturePatch : ResourcePatch {
    override fun execute(context: ResourceContext): PatchResult {
        context.xmlEditor["AndroidManifest.xml"].use { editor ->
            val document = editor.file
            val application = document.getElementsByTagName("application").item(0)

            if(application.attributes.getNamedItem("android:enableOnBackInvokedCallback") == null) {
                val attr = document.createAttribute("android:enableOnBackInvokedCallback")
                attr.value = "true"
                application.attributes.setNamedItem(attr)
            }
        }

        return PatchResultSuccess()
    }
}

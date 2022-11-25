package app.revanced.patches.shared.settings.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.DomFileEditor
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.patch.*
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.shared.settings.BasePreference
import app.revanced.patches.shared.settings.IResource
import app.revanced.patches.shared.settings.addPreference
import app.revanced.patches.shared.settings.addResource
import app.revanced.patches.shared.settings.impl.ArrayResource
import app.revanced.patches.shared.settings.impl.StringResource
import app.revanced.patches.youtube.misc.manifest.patch.FixLocaleConfigErrorPatch
import app.revanced.util.resources.ResourceUtils
import app.revanced.util.resources.ResourceUtils.copyResources
import org.w3c.dom.Node

/**
 * Abstract settings resource patch
 *
 * @param revancedPreferencesName Name of the settings preference xml file
 * @param revancedPreferencesSourceDir Source directory to copy the preference template from
 */
@Description("Applies mandatory patches to implement ReVanced settings into the application.")
@Version("0.0.1")
@DependsOn([FixLocaleConfigErrorPatch::class])
abstract class AbstractSettingsResourcePatch(
    val revancedPreferencesName: String,
    val revancedPreferencesSourceDir: String,
) : ResourcePatch {
    override fun execute(context: ResourceContext): PatchResult {
        /* used for self-restart */
        context.xmlEditor["AndroidManifest.xml"].use { editor ->
            editor.file.getElementsByTagName("manifest").item(0).also {
                it.appendChild(it.ownerDocument.createElement("uses-permission").also { element ->
                    element.setAttribute("android:name", "android.permission.SCHEDULE_EXACT_ALARM")
                })
            }
        }

        /* copy ReVanced preference template from source dir */
        context.copyResources(
            revancedPreferencesSourceDir,
            ResourceUtils.ResourceGroup(
                "xml", "$revancedPreferencesName.xml"
            )
        )

        /* prepare xml editors */
        stringsEditor = context.xmlEditor["res/values/strings.xml"]
        arraysEditor = context.xmlEditor["res/values/arrays.xml"]
        revancedPreferencesEditor = context.xmlEditor["res/xml/$revancedPreferencesName.xml"]

        return PatchResultSuccess()
    }

    internal companion object {
        private var revancedPreferenceNode: Node? = null
        private var stringsNode: Node? = null
        private var arraysNode: Node? = null

        private var strings = mutableListOf<StringResource>()

        private var revancedPreferencesEditor: DomFileEditor? = null
            set(value) {
                field = value
                revancedPreferenceNode = value.getNode("PreferenceScreen")
            }
        private var stringsEditor: DomFileEditor? = null
            set(value) {
                field = value
                stringsNode = value.getNode("resources")
            }
        private var arraysEditor: DomFileEditor? = null
            set(value) {
                field = value
                arraysNode = value.getNode("resources")
            }

        /**
         * Add a new string to the resources.
         *
         * @param identifier The key of the string.
         * @param value The value of the string.
         * @throws IllegalArgumentException if the string already exists.
         */
        fun addString(identifier: String, value: String, formatted: Boolean) =
            StringResource(identifier, value, formatted).include()

        /**
         * Add an array to the resources.
         *
         * @param arrayResource The array resource to add.
         */
        fun addArray(arrayResource: ArrayResource) =
            arraysNode!!.addResource(arrayResource)

        /**
         * Add a preference to the settings.
         *
         * @param preference The preference to add.
         */
        fun addPreference(preference: BasePreference) =
            revancedPreferenceNode!!.addPreference(preference) { it.include() }

        /**
         * Add a new resource to the resources.
         *
         * @throws IllegalArgumentException if the resource already exists.
         */
        internal fun IResource.include() {
            when(this) {
                is StringResource -> {
                    if (strings.any { it.name == name }) return
                    strings.add(this)
                }
                is ArrayResource -> addArray(this)
                else -> throw NotImplementedError("Unsupported resource type")
            }
        }

        internal fun DomFileEditor?.getNode(tagName: String) = this!!.file.getElementsByTagName(tagName).item(0)
    }

    override fun close() {
        // merge all strings, skip duplicates
        strings.forEach {
            stringsNode!!.addResource(it)
        }

        revancedPreferencesEditor?.close()
        stringsEditor?.close()
        arraysEditor?.close()
    }
}
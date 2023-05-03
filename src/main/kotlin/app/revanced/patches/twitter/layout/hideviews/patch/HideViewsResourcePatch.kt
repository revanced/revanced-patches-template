package app.revanced.patches.twitter.layout.hideviews.patch

import app.revanced.patcher.ResourceContext
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.patch.*
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.twitter.layout.hideviews.annotations.HideViewsCompatibility
import app.revanced.util.resources.ResourceUtils.base
import org.w3c.dom.Element

@Patch
@DependsOn([HideViewsBytecodePatch::class])
@Name("hide-views-stats")
@Description("Hides the view stats under tweets.")
@HideViewsCompatibility
@Version("0.0.1")
class HideViewsResourcePatch : ResourcePatch {
    override fun execute(context: ResourceContext) {
        arrayOf(
            "res/layout/condensed_tweet_stats.xml",
            "res/layout/focal_tweet_stats.xml"
        ).forEach { file ->
            context.base.editXmlFile(file).use { editor ->
                val tags = editor.file.getElementsByTagName("com.twitter.ui.tweet.TweetStatView")
                List(tags.length) { tags.item(it) as Element }
                    .filter { it.getAttribute("android:id").contains("views_stat") }
                    .forEach { it.parentNode.removeChild(it) }
            }
        }
    }
}

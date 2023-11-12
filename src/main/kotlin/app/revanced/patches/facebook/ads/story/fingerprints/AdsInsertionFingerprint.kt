package app.revanced.patches.facebook.ads.story.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.dexbacked.value.DexBackedStringEncodedValue

object AdsInsertionFingerprint : MethodFingerprint(
    returnType = "V",
    parameters = listOf(),
    customFingerprint = { methodDef, classDef ->
        methodDef.name == "run" && classDef.fields.any {
            it.name == "__redex_internal_original_name"
                    && (it.initialValue as? DexBackedStringEncodedValue)?.value == "AdBucketDataSourceUtil\$attemptAdsInsertion\$1"
        }
    }
)

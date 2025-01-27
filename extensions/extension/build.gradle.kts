plugins {
    id("kotlin-android")
}

dependencies {
    compileOnly("com.squareup.okhttp3:okhttp:4.9.2")
}

extension {
    name = "extensions/extension.rve"
}

android {
    namespace = "app.revanced.extension"
}

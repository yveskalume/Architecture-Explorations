plugins {
    alias(libs.plugins.newsapp.android.sample.app)
}

android {
    namespace = "dev.yveskalume.newsapp"

    defaultConfig {
        applicationId = "dev.yveskalume.newsapp"
        versionCode = 1
        versionName = "1.0"
    }
}

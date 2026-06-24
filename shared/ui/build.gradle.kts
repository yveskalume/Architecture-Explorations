plugins {
    alias(libs.plugins.newsapp.android.compose.library)
}

android {
    namespace = "dev.yveskalume.newsapp.ui"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(projects.data)
}

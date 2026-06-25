plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.android.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.compose.gradle.plugin)
    implementation(libs.kotlin.serialization.gradle.plugin)
    implementation(libs.secrets.gradle.plugin.artifact)
}

gradlePlugin {
    plugins {
        register("androidSampleApp") {
            id = libs.plugins.newsapp.android.sample.app.get().pluginId
            implementationClass = "dev.yveskalume.newsapp.gradle.AndroidSampleAppConventionPlugin"
        }
        register("androidDataLibrary") {
            id = libs.plugins.newsapp.android.data.library.get().pluginId
            implementationClass = "dev.yveskalume.newsapp.gradle.AndroidDataLibraryConventionPlugin"
        }
        register("androidComposeLibrary") {
            id = libs.plugins.newsapp.android.compose.library.get().pluginId
            implementationClass = "dev.yveskalume.newsapp.gradle.AndroidComposeLibraryConventionPlugin"
        }
    }
}

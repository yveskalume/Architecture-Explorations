package dev.yveskalume.newsapp.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

internal const val COMPILE_SDK = 37
internal const val MIN_SDK = 26
internal const val TARGET_SDK = 37

internal fun Project.configureKotlin() {
    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            freeCompilerArgs.add("-XXLanguage:+ExplicitBackingFields")
        }
    }
}

internal val Project.versionCatalog: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.library(alias: String) = findLibrary(alias).get()

internal fun Project.addSampleAppDependencies() {
    val libs = versionCatalog
    dependencies {
        add("implementation", libs.library("shared-ui"))
        add("implementation", libs.library("shared-data"))
        add("implementation", libs.library("androidx-core-ktx"))
        add("implementation", libs.library("androidx-lifecycle-runtime-ktx"))
        add("implementation", libs.library("androidx-activity-compose"))
        add("implementation", platform(libs.library("androidx-compose-bom")))
        add("implementation", libs.library("androidx-compose-ui"))
        add("implementation", libs.library("androidx-compose-ui-graphics"))
        add("implementation", libs.library("androidx-compose-ui-tooling-preview"))
        add("implementation", libs.library("androidx-compose-material3"))
        add("implementation", libs.library("koin-android"))
        add("implementation", libs.library("koin-androidx-compose"))
        add("implementation", libs.library("kotlinx-serialization-json"))
        add("implementation", libs.library("navigation3-runtime"))
        add("implementation", libs.library("navigation3-ui"))
        addCommonTestDependencies(libs)
        add("androidTestImplementation", libs.library("androidx-junit"))
        add("androidTestImplementation", libs.library("androidx-espresso-core"))
        add("androidTestImplementation", platform(libs.library("androidx-compose-bom")))
        add("androidTestImplementation", libs.library("androidx-compose-ui-test-junit4"))
        add("debugImplementation", libs.library("androidx-compose-ui-tooling"))
        add("debugImplementation", libs.library("androidx-compose-ui-test-manifest"))
    }
}

internal fun Project.addDataLibraryDependencies() {
    val libs = versionCatalog
    dependencies {
        add("implementation", libs.library("androidx-core-ktx"))
        add("implementation", libs.library("ktor-client-core"))
        add("implementation", libs.library("ktor-client-okhttp"))
        add("implementation", libs.library("ktor-client-content-negotiation"))
        add("implementation", libs.library("ktor-serialization-kotlinx-json"))
        add("implementation", libs.library("ktor-client-logging"))
        add("debugImplementation", libs.library("chucker-library"))
        add("releaseImplementation", libs.library("chucker-library-no-op"))
        add("implementation", libs.library("koin-android"))
        add("implementation", libs.library("kotlinx-serialization-json"))
        addCommonTestDependencies(libs)
        add("androidTestImplementation", libs.library("androidx-junit"))
        add("androidTestImplementation", libs.library("androidx-espresso-core"))
    }
}

internal fun Project.addComposeLibraryDependencies() {
    val libs = versionCatalog
    dependencies {
        add("implementation", platform(libs.library("androidx-compose-bom")))
        add("implementation", libs.library("androidx-compose-ui"))
        add("implementation", libs.library("androidx-compose-ui-graphics"))
        add("implementation", libs.library("androidx-compose-ui-tooling-preview"))
        add("implementation", libs.library("androidx-compose-material3"))
        add("implementation", libs.library("coil-compose"))
        add("implementation", libs.library("coil-network-okhttp"))
        add("androidTestImplementation", libs.library("androidx-junit"))
        add("androidTestImplementation", platform(libs.library("androidx-compose-bom")))
        add("androidTestImplementation", libs.library("androidx-compose-ui-test-junit4"))
        add("debugImplementation", libs.library("androidx-compose-ui-tooling"))
        add("debugImplementation", libs.library("androidx-compose-ui-test-manifest"))
    }
}

internal fun org.gradle.api.artifacts.dsl.DependencyHandler.addCommonTestDependencies(libs: VersionCatalog) {
    add("testImplementation", libs.library("junit"))
    add("testImplementation", libs.library("kotlinx-coroutines-test"))
    add("testImplementation", libs.library("kotest-runner-junit5"))
    add("testImplementation", libs.library("kotest-assertions-core"))
    add("testRuntimeOnly", libs.library("junit-vintage-engine"))
}



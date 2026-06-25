package dev.yveskalume.newsapp.gradle

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidDataLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")
        pluginManager.apply("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")

        extensions.configure<LibraryExtension> {
            compileSdk {
                version = release(COMPILE_SDK)
            }

            defaultConfig {
                minSdk = MIN_SDK
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_21
                targetCompatibility = JavaVersion.VERSION_21
            }

            buildFeatures {
                buildConfig = true
            }

            testOptions {
                unitTests {
                    isReturnDefaultValues = true
                    all {
                        it.useJUnitPlatform()
                    }
                }
            }
        }

        configureKotlin()
        addDataLibraryDependencies()
    }
}

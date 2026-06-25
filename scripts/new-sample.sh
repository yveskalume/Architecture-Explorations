#!/usr/bin/env bash

set -euo pipefail

function usage() {
    echo "Usage: scripts/new-sample.sh <sample-name> <package-name>"
    echo
    echo "Example:"
    echo "  scripts/new-sample.sh redux dev.yveskalume.newsappp.redux"
    exit 1
}

if [ "$#" -ne 2 ]; then
    usage
fi

sample_name="$1"
package_name="$2"

if [[ ! "$sample_name" =~ ^[a-z][a-z0-9-]*$ ]]; then
    echo "Error: sample-name must use lowercase letters, numbers, and hyphens."
    exit 1
fi

if [[ ! "$package_name" =~ ^[a-zA-Z_][a-zA-Z0-9_]*(\.[a-zA-Z_][a-zA-Z0-9_]*)+$ ]]; then
    echo "Error: package-name must be a valid dotted package name."
    exit 1
fi

script_dir="$(cd "$(dirname "$0")" && pwd)"
repo_dir="$(cd "$script_dir/.." && pwd)"
sample_dir="$repo_dir/$sample_name"
wrapper_source="$repo_dir/mvvm"

if [ -e "$sample_dir" ]; then
    echo "Error: '$sample_name' already exists."
    exit 1
fi

if [ ! -f "$wrapper_source/gradlew" ]; then
    echo "Error: could not find Gradle wrapper source at '$wrapper_source'."
    exit 1
fi

package_path="${package_name//.//}"

mkdir -p "$sample_dir/app/src/main/java/$package_path"
mkdir -p "$sample_dir/app/src/main/res/values"
mkdir -p "$sample_dir/app/src/test/java/$package_path"
mkdir -p "$sample_dir/gradle"

cp "$wrapper_source/gradlew" "$sample_dir/gradlew"
cp "$wrapper_source/gradlew.bat" "$sample_dir/gradlew.bat"
cp -R "$wrapper_source/gradle/wrapper" "$sample_dir/gradle/wrapper"
chmod +x "$sample_dir/gradlew"

cat > "$sample_dir/settings.gradle.kts" <<'EOF'
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    includeBuild("../build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "NewsAppp"
includeBuild("../shared")
include(":app")
EOF

cat > "$sample_dir/build.gradle.kts" <<'EOF'
// Shared Gradle conventions are provided by the root build-logic included build.
EOF

cat > "$sample_dir/.gitignore" <<'EOF'
.gradle
.kotlin
/build
local.properties
EOF

cat > "$sample_dir/app/.gitignore" <<'EOF'
/build
EOF

cat > "$sample_dir/gradle.properties" <<'EOF'
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
EOF

cat > "$sample_dir/local.properties" <<EOF
$(grep '^sdk.dir=' "$wrapper_source/local.properties" 2>/dev/null || true)
NEWS_API_KEY=your_api_key_here
EOF

cat > "$sample_dir/app/build.gradle.kts" <<EOF
plugins {
    alias(libs.plugins.newsapp.android.sample.app)
}

android {
    namespace = "$package_name"

    defaultConfig {
        applicationId = "$package_name"
        versionCode = 1
        versionName = "1.0"
    }
}
EOF

cat > "$sample_dir/app/proguard-rules.pro" <<'EOF'
# Add app-specific R8 rules here when needed.
EOF

cat > "$sample_dir/app/src/main/AndroidManifest.xml" <<'EOF'
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:name=".NewsApplication"
        android:allowBackup="true"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.NewsAppp">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.NewsAppp">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
EOF

cat > "$sample_dir/app/src/main/res/values/strings.xml" <<EOF
<resources>
    <string name="app_name">$sample_name</string>
</resources>
EOF

cat > "$sample_dir/app/src/main/res/values/themes.xml" <<'EOF'
<resources>
    <style name="Theme.NewsAppp" parent="android:style/Theme.Material.Light.NoActionBar" />
</resources>
EOF

cat > "$sample_dir/app/src/main/java/$package_path/NewsApplication.kt" <<EOF
package $package_name

import android.app.Application

class NewsApplication : Application()
EOF

cat > "$sample_dir/app/src/main/java/$package_path/MainActivity.kt" <<EOF
package $package_name

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SampleApp()
        }
    }
}

@Composable
private fun SampleApp() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(contentAlignment = Alignment.Center) {
                Text("$sample_name")
            }
        }
    }
}

@Preview
@Composable
private fun SampleAppPreview() {
    SampleApp()
}
EOF

cat > "$sample_dir/app/src/test/java/$package_path/ExampleUnitTest.kt" <<EOF
package $package_name

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class ExampleUnitTest : ShouldSpec({
    should("create the sample") {
        true shouldBe true
    }
})
EOF

cat > "$sample_dir/README.md" <<EOF
# $sample_name

Architecture sample generated with \`scripts/new-sample.sh\`.

This project uses the shared Gradle conventions from \`../build-logic\` and shared app modules from \`../shared\`.
EOF

echo "Created sample project: $sample_dir"
echo "Open it with: ./studiow $sample_name"

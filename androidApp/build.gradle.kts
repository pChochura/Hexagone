import org.jetbrains.compose.internal.utils.getLocalProperty

plugins {
    alias(libs.plugins.androidVersionGit)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

androidGitVersion {
    format = "%tag%%-commit%"
    codeFormat = "MMNNPPBBB"
}

android {
    namespace = "com.pointlessgames.hexagone"
    compileSdk = libs.versions.sdk.compile.get().toInt()

    defaultConfig {
        applicationId = "com.pointlessgames.hexagone"
        minSdk = libs.versions.sdk.min.get().toInt()
        targetSdk = libs.versions.sdk.target.get().toInt()
        versionCode = androidGitVersion.code().takeIf { it > 0 } ?: 1
        versionName = androidGitVersion.name().takeIf { it.isNotEmpty() } ?: "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        val storeFile = getLocalProperty("storeFile")
        val storePassword = getLocalProperty("storePassword")
        val keyAlias = getLocalProperty("keyAlias")
        val keyPassword = getLocalProperty("keyPassword")
        if (storeFile != null && storePassword != null && keyAlias != null && keyPassword != null) {
            create("release") {
                this.storeFile = file(storeFile)
                this.storePassword = storePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }
    buildTypes {
        runCatching { signingConfigs.getByName("release") }.getOrNull()?.let { config ->
            getByName("release") {
                isMinifyEnabled = true
                isShrinkResources = true
                signingConfig = config
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro",
                )
            }
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(projects.shared)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.material)
    implementation(libs.koin.android)

    debugImplementation(libs.androidx.compose.ui.tooling)
}

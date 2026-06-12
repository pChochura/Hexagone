import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.codingfeline.buildkonfig.compiler.FieldSpec
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.buildKonfig)
}

buildkonfig {
    packageName = "com.pointlessgames.hexagone"

    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use {
            localProperties.load(it)
        }
    }

    val supabaseUrl = localProperties.getProperty("SUPABASE_URL") ?: ""
    val supabaseKey = localProperties.getProperty("SUPABASE_KEY") ?: ""
    val revenueCatAndroidKey = localProperties.getProperty("REVENUECAT_ANDROID_KEY") ?: ""
    val revenueCatIosKey = localProperties.getProperty("REVENUECAT_IOS_KEY") ?: ""

    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "SUPABASE_URL", supabaseUrl)
        buildConfigField(FieldSpec.Type.STRING, "SUPABASE_KEY", supabaseKey)
        buildConfigField(FieldSpec.Type.STRING, "REVENUECAT_ANDROID_KEY", revenueCatAndroidKey)
        buildConfigField(FieldSpec.Type.STRING, "REVENUECAT_IOS_KEY", revenueCatIosKey)
        buildConfigField(FieldSpec.Type.BOOLEAN, "IS_DEBUG", "true")
    }
}

repositories {
    google()
    mavenCentral()
    maven { url = uri("https://maven.revenuecat.com/repository/maven-public/") }
}

kotlin {
    android {
        namespace = "com.pointlessgames.hexagone.core"
        compileSdk = libs.versions.sdk.compile.get().toInt()
        minSdk = libs.versions.sdk.min.get().toInt()

        androidResources {
            enable = true
        }

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    val iosTargets = listOf(
        iosArm64(),
        iosSimulatorArm64(),
    )

    iosTargets.forEach {
        it.binaries.framework {
            baseName = "Shared"
            isStatic = true
            freeCompilerArgs += "-Xbinary=bundleId=com.pointlessgames.hexagone.shared"
        }
    }

    jvm()

    sourceSets {
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.androidx.compose.ui.tooling)
            implementation(libs.ktor.client.okhttp)
            val rcVersion = "3.0.6"
            api("com.revenuecat.purchases:purchases-kmp-core:$rcVersion")
            implementation("com.revenuecat.purchases:purchases-kmp-models:$rcVersion")
            implementation("com.revenuecat.purchases:purchases-kmp-result:$rcVersion")
        }
        commonMain.dependencies {
            implementation(libs.compose.mp.runtime)
            implementation(libs.compose.mp.foundation)
            implementation(libs.compose.mp.material3)
            implementation(libs.compose.mp.ui)
            implementation(libs.compose.mp.components.resources)
            implementation(libs.compose.mp.ui.tooling.preview)

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.immutable)
            implementation(libs.kotlinx.datetime)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.navigation)

            implementation(libs.androidx.datastore)
            implementation(libs.androidx.datastore.prefs)

            implementation(libs.androidx.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.viewmodel.navigation3)

            implementation(libs.gadulka)

            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.auth)
            implementation(libs.ktor.client.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            val rcVersion = "3.0.6"
            api("com.revenuecat.purchases:purchases-kmp-core:$rcVersion")
            implementation("com.revenuecat.purchases:purchases-kmp-models:$rcVersion")
            implementation("com.revenuecat.purchases:purchases-kmp-result:$rcVersion")
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.pointlessgames.hexagone.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.pointlessgames.hexagone"
            packageVersion = "1.0.0"
        }
    }
}

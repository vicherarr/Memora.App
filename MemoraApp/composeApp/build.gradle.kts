import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktorfit)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
                }
            }
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = false
            
            // Bundle ID for iOS framework
            binaryOptions["bundleId"] = "com.vicherarr.memora"
        }
    }
    
    // SQLDelight requires sqlite3 linking for all native targets
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        binaries.all {
            linkerOpts.add("-lsqlite3")
        }
        binaries.withType<org.jetbrains.kotlin.gradle.plugin.mpp.Framework> {
            // Fix for Xcode 16 SwiftUICore linking issue
            linkerOpts.add("-Wl,-weak_reference_mismatches,weak")
            
            // Additional fixes for iOS linking issues
            linkerOpts.add("-framework")
            linkerOpts.add("Foundation")
            linkerOpts.add("-framework") 
            linkerOpts.add("UIKit")
            linkerOpts.add("-framework")
            linkerOpts.add("CoreGraphics")
            
            // Fix for SwiftUICore framework linking
            linkerOpts.add("-Wl,-U,_objc_msgSend")
            linkerOpts.add("-Wl,-undefined,dynamic_lookup")
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            
            // Ktor Android HTTP Client
            implementation(libs.ktor.client.okhttp)
            
            // SQLDelight Android Driver
            implementation(libs.sqldelight.android.driver)
            
            // Koin Android
            implementation(libs.koin.android)
            
            // CameraX dependencies for multimedia
            implementation(libs.androidx.camera.core)
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.video)
            implementation(libs.androidx.camera.view)
            implementation(libs.androidx.camera.extensions)
            
            // Activity Result API for permissions
            implementation(libs.androidx.activity.result)
        }
        iosMain.dependencies {
            // Ktor iOS HTTP Client  
            implementation(libs.ktor.client.darwin)
            
            // SQLDelight iOS Driver
            implementation(libs.sqldelight.native.driver)
            
            // moko-permissions iOS specific dependencies
            implementation(libs.moko.permissions)
            implementation(libs.moko.permissions.camera)
            implementation(libs.moko.permissions.compose)
            
            // API dependencies for framework export
            api(libs.koin.core)
            api(libs.kotlinx.datetime)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            
            // Koin for Dependency Injection
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            
            // KtorFit for HTTP Client (includes Ktor automatically)
            implementation(libs.ktorfit.lib)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.auth)
            
            // SQLDelight for Database
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines.extensions)
            
            // DateTime for multiplatform
            implementation(libs.kotlinx.datetime)
            
            // Voyager Navigation for Compose Multiplatform
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenmodel)
            implementation(libs.voyager.bottom.sheet.navigator)
            implementation(libs.voyager.tab.navigator)
            implementation(libs.voyager.transitions)
            implementation(libs.voyager.koin)
            
            // moko-permissions for multiplatform permissions handling
            implementation(libs.moko.permissions)
            implementation(libs.moko.permissions.camera)
            implementation(libs.moko.permissions.compose)
            
            // Coil for image loading
            implementation(libs.coil)
            implementation(libs.coil.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.vicherarr.memora"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.vicherarr.memora"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

sqldelight {
    databases {
        create("MemoraDatabase") {
            packageName.set("com.vicherarr.memora.database")
        }
    }
}


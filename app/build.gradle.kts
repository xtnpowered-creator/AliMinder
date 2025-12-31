plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("com.google.protobuf")
}

android {
    namespace = "com.aliminder.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.aliminder.app"
        minSdk = 26  // Android 8.0 (Oreo)
        targetSdk = 34  // Android 14
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11" // Compatible with Kotlin 1.9.23
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/io.netty.versions.properties"
        }
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.24.0"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
                create("kotlin") {
                    option("lite")
                }
            }
        }
    }
}

dependencies {
    // OpenTelemetry BOM (required for MSAL compatibility)
    implementation(platform("io.opentelemetry:opentelemetry-bom:1.32.0"))
    
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.23")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Material Design Components (Required for Theme.Material3 in XML)
    implementation("com.google.android.material:material:1.11.0")

    // Jetpack Compose
    // Upgraded BOM to 2024.06.00 to include Material3 1.2.x+ and VerticalDivider
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.6")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    
    // Hilt Dependency Injection
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // Room Database + SQLCipher
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("net.zetetic:android-database-sqlcipher:4.5.4")
    implementation("androidx.sqlite:sqlite-ktx:2.4.0")
    
    // DataStore (Preferences)
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Media3/ExoPlayer (Audio Engine)
    implementation("androidx.media3:media3-exoplayer:1.2.0")
    implementation("androidx.media3:media3-ui:1.2.0")
    implementation("androidx.media3:media3-common:1.2.0")
    
    // OAuth (AppAuth)
    implementation("net.openid:appauth:0.11.1")
    
    // Protocol Buffers
    implementation("com.google.protobuf:protobuf-kotlin-lite:3.24.0")
    implementation("com.google.protobuf:protobuf-javalite:3.24.0")
    
    // QR Code Generation/Scanning (ZXing)
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    
    // ML Kit Barcode Scanning (Alternative/Complement to ZXing)
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    
    // Encryption
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Location Services (Geofencing)
    implementation("com.google.android.gms:play-services-location:21.0.1")
    
    // Biometric Authentication
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
    
    // Microsoft Graph API
    implementation("com.microsoft.graph:microsoft-graph:5.80.0")
    implementation("com.microsoft.identity.client:msal:4.9.0") {
        exclude(group = "io.opentelemetry", module = "opentelemetry-bom")
    }
    
    // Google Calendar API
    implementation("com.google.api-client:google-api-client-android:2.2.0")
    implementation("com.google.apis:google-api-services-calendar:v3-rev20230825-2.0.0")
    
    // HTTP Client (for ad-hoc migration server)
    implementation("io.ktor:ktor-server-core:2.3.7")
    implementation("io.ktor:ktor-server-netty:2.3.7")
    
    // Gson (JSON parsing)
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00")) // Updated here too
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
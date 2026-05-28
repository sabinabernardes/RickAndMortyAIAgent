plugins {
    id("org.jetbrains.kotlin.plugin.compose")
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.bina.features.home"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        targetSdk = 35
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(project(":core:designsystem"))
    implementation(project(":core:network"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.activity.compose)
    // Compose e Material 3
    implementation(libs.androidx.compose.bom)
    implementation(libs.androidx.ui.tooling)
    implementation(libs.compose.material3)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.ui.test.junit4.android)
    implementation(libs.androidx.paging.common.android)
    implementation(libs.androidx.paging.compose)
    implementation(libs.navigation.runtime.android)

    // Preview e testes opcionais
    debugImplementation(libs.androidx.ui.tooling)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation(libs.androidx.espresso.core.v350)

    // Retrofit
    implementation(libs.retrofit.v290)
    implementation(libs.retrofit.gson)

    // Coil
    implementation(libs.coil.compose)

    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    //Testes
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

}
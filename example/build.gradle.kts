plugins {
    id("com.android.application")
    id("kotlin-android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdkVersion(30)

    defaultConfig {
        applicationId = "jp.takuji31.compose.navigation.example"
        minSdkVersion(23)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        useIR = true
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Libs.composeVersion
        kotlinCompilerVersion = "1.4.21"
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("test").java.srcDirs("src/test/kotlin")
        getByName("androidTest").java.srcDirs("src/androidTest/kotlin")
    }
}

dependencies {
    implementation(project(":library"))

    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("com.google.android.material:material:1.2.1")
    implementation("androidx.compose.ui:ui:${Libs.composeVersion}")
    implementation("androidx.compose.material:material:${Libs.composeVersion}")
    implementation("androidx.compose.ui:ui-tooling:${Libs.composeVersion}")
    implementation(Libs.Hilt.core)
    implementation(Libs.Hilt.viewModel)
    kapt(Libs.Hilt.androidXCompiler)
    kapt(Libs.Hilt.compiler)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.0-rc01")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.3.0-rc01")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.0-rc01")
    testImplementation("junit:junit:4.13.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}

kapt {
    correctErrorTypes = true
}

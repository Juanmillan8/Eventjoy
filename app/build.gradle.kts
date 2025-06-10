import com.android.build.api.dsl.Packaging

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.eventjoy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.eventjoyAndroid"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testApplicationId = "com.example.eventjoy.tests"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        reportDir = project.buildDir.resolve("results/report").toString()
        resultsDir = project.buildDir.resolve("results").toString()
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
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }

    packagingOptions {
        resources {
            excludes += setOf(
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md"
            )
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.13.1")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.2.0")
    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))
    implementation("com.google.firebase:firebase-analytics:22.4.0")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("com.google.firebase:firebase-auth:23.2.0")
    implementation("com.google.android.gms:play-services-base:18.7.0")
    androidTestImplementation(libs.junit.jupiter)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.squareup.picasso:picasso:2.8")
    implementation("com.cloudinary:cloudinary-android:3.0.2")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")
}
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id ("org.jetbrains.kotlin.plugin.serialization")
}


android {
    namespace = "com.feng.netmaster"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.feng.netmaster"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }

}

dependencies {

    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.6.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.6.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("android.arch.lifecycle:viewmodel:1.1.1")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("com.sealwu:kscript-tools:1.0.22")

//    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.annotation:annotation:1.9.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //markdown
    val markwon_version="4.6.2"
    implementation ("io.noties.markwon:core:$markwon_version")
    implementation ("io.noties.markwon:html:$markwon_version")
    implementation ("io.noties.markwon:ext-tables:$markwon_version")
    implementation ("io.noties.markwon:recycler:$markwon_version")
    implementation ("io.noties.markwon:recycler-table:$markwon_version")
    implementation ("io.noties.markwon:image:$markwon_version")
    implementation ("io.noties.markwon:ext-strikethrough:$markwon_version")
    implementation ("io.noties.markwon:ext-tasklist:$markwon_version")
}

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 28
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("androidx.core:core-ktx:1.13.1") // General AndroidX core utilities (still useful)
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4") // If you use ConstraintLayout

    // Room components (Java compatible)
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    // For LiveData integration with Room in Java, we can use the rxjava2 artifact
    // or simply rely on LiveData's observation mechanism for Room's LiveData return types.
    // Given the ViewModel already handles LiveData, direct use of Room's LiveData is sufficient.
    // If you plan to explicitly use RxJava, add:
    // implementation("androidx.room:room-rxjava2:2.6.1")

    // Lifecycle components (ViewModel, LiveData)
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.1") // Latest stable
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.1") // Latest stable
    implementation("androidx.lifecycle:lifecycle-common-java8:2.8.1") // For Java 8 lambda support in Lifecycle components

    // Image loading library (Glide)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // For testing (optional, but good practice)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
}
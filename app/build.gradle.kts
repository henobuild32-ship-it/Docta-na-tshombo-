plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

import java.util.Properties

fun loadEnvProps(path: String): Properties {
  val props = Properties()
  val file = file(path)
  if (file.exists()) {
    file.forEachLine { raw ->
      val line = raw.trim()
      if (line.isNotEmpty() && !line.startsWith("#") && line.contains("=")) {
        val idx = line.indexOf('=')
        val key = line.substring(0, idx).trim()
        val value = line.substring(idx + 1).trim().trim('"', '\'')
        if (key.isNotEmpty()) props.setProperty(key, value)
      }
    }
  }
  return props
}

val envProps = loadEnvProps("$rootDir/.env")
val envDefaultProps = loadEnvProps("$rootDir/.env.example")

fun envOr(key: String, fallback: String): String {
  return envProps.getProperty(key) ?: envDefaultProps.getProperty(key) ?: fallback
}

fun esc(s: String): String = s.replace("\\", "\\\\").replace("\"", "\\\"")

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.doctanatshom.com"
    minSdk = 24
    targetSdk = 36
    versionCode = 3
    versionName = "1.2"
    buildConfigField("String", "ONESIGNAL_APP_ID", "\"${esc(envOr("ONESIGNAL_APP_ID", ""))}\"")
    buildConfigField("String", "SUPABASE_URL", "\"${esc(envOr("SUPABASE_URL", ""))}\"")
    buildConfigField("String", "SUPABASE_ANON_KEY", "\"${esc(envOr("SUPABASE_ANON_KEY", ""))}\"")
    buildConfigField("String", "SUPABASE_BUCKET_NAME", "\"${esc(envOr("SUPABASE_BUCKET_NAME", "docta-na-tshombo"))}\"")
    buildConfigField("String", "VERSION_CHECK_URL", "\"${esc(envOr("VERSION_CHECK_URL", "https://henobuild32-ship-it.github.io/Docta-na-tshombo-/version.json"))}\"")

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH")
        ?: envOr("KEYSTORE_PATH", "${rootDir}/my-upload-key.jks")
      val ksFile = file(keystorePath)
      storeFile = if (ksFile.isAbsolute) ksFile else file("${rootDir}/${keystorePath}")
      storePassword = System.getenv("STORE_PASSWORD") ?: envOr("STORE_PASSWORD", "")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD") ?: envOr("KEY_PASSWORD", "")
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      isDebuggable = true
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

dependencies {
  // Compose BOM
  implementation(platform(libs.androidx.compose.bom))

  // Supabase Storage - using BOM for version management
  implementation(platform(libs.supabase.bom))
  implementation(libs.supabase.storage)
  implementation(libs.supabase.auth)
  implementation(libs.supabase.postgrest)
  implementation(libs.supabase.realtime)
  implementation(libs.ktor.client.android)
  implementation(libs.onesignal)
  implementation(libs.kotlinx.serialization.json)

  // AndroidX Core
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.core.ktx)

  // Compose
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)

  // Lifecycle & Navigation
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)

  // Room (local cache for offline support)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)

  // Networking & Images
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  implementation(libs.retrofit)
  implementation(libs.logging.interceptor)

  // Coroutines
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)

  // Google Play Services
  implementation(libs.androidx.credentials)
  implementation(libs.androidx.credentials.play.services)
  implementation(libs.googleid)
  implementation(libs.play.services.location)

  // Testing
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}

# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep Room entities - Room requires all fields and constructors
# More specific than { *; } - only keeps what's needed for Room
-keep @androidx.room.Entity class * {
    <fields>;
    <init>(...);
}
-keep class com.argumentor.app.data.model.** {
    <fields>;
    <init>(...);
}

# Keep Fallacy entity - Added for CRUD operations
-keep class com.argumentor.app.data.model.Fallacy {
    <fields>;
    <init>(...);
}

# Keep FallacyDao - Added for database operations
-keep interface com.argumentor.app.data.local.dao.FallacyDao {
    public abstract <methods>;
}

# Keep DTO classes - Only keep fields and constructors needed for serialization
-keep class com.argumentor.app.data.dto.** {
    <fields>;
    <init>(...);
}

# Keep Gson models - Only essential parts for reflection
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**

# Keep Gson public API
-keep class com.google.gson.Gson {
    <init>(...);
    public <methods>;
}
-keep class com.google.gson.GsonBuilder {
    <init>(...);
    public <methods>;
}

# Keep Gson type adapters
-keep class * implements com.google.gson.TypeAdapter {
    <init>(...);
    public <methods>;
}
-keep class * implements com.google.gson.TypeAdapterFactory {
    <init>(...);
    public <methods>;
}
-keep class * implements com.google.gson.JsonSerializer {
    <init>(...);
    public <methods>;
}
-keep class * implements com.google.gson.JsonDeserializer {
    <init>(...);
    public <methods>;
}

# Keep Hilt - Only essential injection points
-keepclassmembers,allowobfuscation class * {
    @javax.inject.Inject <init>(...);
    @javax.inject.Inject <fields>;
}
-keep class dagger.hilt.android.** {
    public <methods>;
}
-keep class javax.inject.** {
    public <methods>;
}

# Keep Room DAOs - Interfaces need all methods for Room runtime
-keep interface com.argumentor.app.data.local.dao.** {
    public abstract <methods>;
}

# Kotlin coroutines - Essential classes for coroutine runtime
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.flow.**

# Preserve line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Jetpack Compose - Keep @Composable functions and runtime classes
-keep class androidx.compose.runtime.** { *; }
-keep @androidx.compose.runtime.Composable class * { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# R8 optimizations - Safe optimizations for Compose and Coroutines
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void check*(...);
    public static void throw*(...);
}

# Keep FallacyCatalog for localized resources
-keep class com.argumentor.app.data.constants.FallacyCatalog {
    public <methods>;
}
-keep class com.argumentor.app.data.constants.FallacyCatalog$Fallacy {
    <fields>;
    <init>(...);
}

# Keep FTS entities - Required for Room FTS4 full-text search
# FTS entities use reflection and must not be obfuscated
-keep @androidx.room.Fts4 class * {
    <fields>;
    <init>(...);
}
-keep class com.argumentor.app.data.local.FtsEntities$* {
    <fields>;
    <init>(...);
}
-keep class com.argumentor.app.data.local.*Fts {
    <fields>;
    <init>(...);
}

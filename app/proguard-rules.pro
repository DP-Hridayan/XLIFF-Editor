# Preserve attributes required for reflection and coroutines
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# AndroidX Room
-keepclassmembers class * {
    @androidx.room.* *;
}

# Google ML Kit Translate & Core
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Google Play Services & Tasks
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Firebase Components (used by ML Kit component discovery and DI)
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class * implements com.google.firebase.components.ComponentRegistrar { *; }

# Keep all native methods for translate JNI & TensorFlow Lite
-keepclasseswithmembernames class * {
    native <methods>;
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

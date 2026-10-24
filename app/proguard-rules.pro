# ProGuard rules for LinkFlow
-dontwarn okhttp3.**
-dontwarn okio.**
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* *;
}

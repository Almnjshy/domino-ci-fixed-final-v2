# Domain models - keep for serialization
-keep class com.agon.app.domain.model.** { *; }
-keep class com.agon.app.domain.repository.** { *; }

# Room entities
-keep class com.agon.app.data.local.entity.** { *; }
-keep @androidx.room.Entity class * { *; }

# Hilt
-keepclassmembers class * { @dagger.hilt.android.lifecycle.HiltViewModel *; }

# General
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn kotlin.**
-dontwarn kotlinx.**

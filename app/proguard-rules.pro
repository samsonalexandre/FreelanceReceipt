# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**

# ML Kit
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Koin
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# Credentials API
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keep class androidx.credentials.** { *; }

# Keep data classes used with Room
-keep class com.alexandresamson.freelancereceipt.data.local.entity.** { *; }

# Google Play Billing
-keep class com.android.billingclient.** { *; }
-keep class com.android.vending.billing.** { *; }
-dontwarn com.android.billingclient.**

# DataStore
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# Splash Screen
-keep class androidx.core.splashscreen.** { *; }

# Kotlin Coroutines
-dontwarn kotlinx.coroutines.**

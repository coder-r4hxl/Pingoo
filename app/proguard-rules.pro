# Keep Room Database entities and DAOs
-keep class androidx.room.RoomDatabase
-dontwarn androidx.room.**

# Keep OSMDroid Map rendering classes
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**

# Keep DataStore Preferences
-keep class androidx.datastore.** { *; }

# Add rules to whitelist your app from battery optimization and background process restrictions

# Keep your application classes - this is usually the entry point for your app
-keep class com.example.backgroundservice.** { *; }

# Keep any classes that are used for background tasks or services
-keep class com.example.backgroundservice.background.** { *; }

# Keep any classes that are used for services or broadcast receivers
-keep class com.example.backgroundservice.services.** { *; }
-keep class com.example.backgroundservice.receivers.** { *; }

# Keep any classes used for asynchronous operations or tasks
-keep class com.example.backgroundservice.tasks.** { *; }

# If your app uses any specific APIs or libraries related to battery optimization or background tasks,
# you might need to add rules to keep those classes and methods as well.

# Optionally, you can also keep any interfaces or classes used for battery-related functionalities
-keep class com.example.backgroundservice.battery.** { *; }

# Keep any native libraries or JNI methods used by your app
-keep class com.example.backgroundservice.jni.** { *; }

# Allow access to sensitive Android APIs, if necessary
-keep class android.** { *; }
-keep interface android.** { *; }

# Allow access to other necessary libraries or third-party dependencies
-keep class org.** { *; }
-keep interface org.** { *; }

# You might need to add additional rules based on the specific requirements and dependencies of your app.

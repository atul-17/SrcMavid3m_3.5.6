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

-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable


-verbose
-keepattributes *Annotation*
-keepattributes Signature

# Exposed to the main app
-keep class com.vodafone.idtmlib.exceptions.** { *; }
-keep class com.vodafone.idtmlib.observers.** { *; }
-keep class com.vodafone.idtmlib.AccessToken { *; }
-keep class com.vodafone.idtmlib.EnvironmentType { *; }
-keep class com.vodafone.idtmlib.IdtmLibInjector { *; }
-keep class com.vodafone.idtmlib.IdtmLib { *; }
# Not needed by the main app, but needed internally
-keep class com.vodafone.idtmlib.lib.network.models.** { *; }
-keep class com.vodafone.idtmlib.lib.rxjava.** { *; }
-keep class com.vodafone.idtmlib.lib.utils.** { *; }

##### THIRD PARTY LIBS #####

##
# Retrofit
##
-dontnote retrofit2.Platform
-dontwarn retrofit2.Platform$Java8
-keepattributes Signature
-keepattributes Exceptions
-dontwarn okio.**
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

##
# Bouncycastle (used by JWT)
##
-keep class javax.naming.** { *; }
-dontwarn javax.naming.**

##
# Play Services
##
-dontwarn sun.misc.Unsafe

##
# Guava
##
-dontwarn javax.lang.model.element.Modifier
-keep class javax.lang.model.element.Modifier
-dontwarn java.lang.ClassValue
-keep class java.lang.ClassValue { *; }
-dontwarn afu.org.checkerframework.**
-dontwarn org.checkerframework.**

-dontwarn com.vodafone.lib.seclibng.**
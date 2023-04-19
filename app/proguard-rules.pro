# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/sergey/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

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

-flattenpackagehierarchy
-dontusemixedcaseclassnames
-adaptclassstrings

## Sample Start
-keepclassmembers class ru.usedesk.sample.model.configuration.entity.** { *; }
## Sample End


## Gson Start
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { <fields>; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
## Gson End

## common-sdk Start
-keepclassmembers class ru.usedesk.common_sdk.api.entity.** { *; }
## common-sdk End

## chat-sdk Start
-keepclassmembers class ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration { *; }
-keepclassmembers class ru.usedesk.chat_sdk.entity.UsedeskFile { *; }
-keepclassmembers class ru.usedesk.chat_sdk.data.repository.api.entity.** { *; }
-keepclassmembers class ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.** { *; }
-keepclassmembers class ru.usedesk.chat_sdk.data.repository.messages.MessagesRepository$NotSentMessage { *; }
-keepclassmembers class ru.usedesk.chat_sdk.data.repository.form.entity.** { *; }
## chat-sdk End

## knowledgebase-sdk Start
-keepclassmembers class ru.usedesk.knowledgebase_sdk.data.repository.api.entity.** { *; }
## knowledgebase-sdk End

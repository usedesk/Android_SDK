# Usedesk Android SDK

## This manual in other languages

Also available in [Russian](README_RU.md)

## Demo app

The ["Example" folder](https://github.com/usedesk/Android_SDK/tree/master/app/src/main/java/ru/usedesk/sample) of this repository contains a demo project that serves as an example of embedding the Usedesk chat SDK into a client application. You can use it to get acquainted with the basic functionality of the application and test the SDK.

## Adding a library to the project

### Minimal Android version

At the moment, the minimum version of the OS that is supported by the SDK — **Android 5.0 (API 21)**

### SDK components

**[Chat SDK](https://github.com/usedesk/Android_SDK/tree/master/chat-sdk/src/main/java/ru/usedesk/chat_sdk/)** — chat library

**[Chat GUI](https://github.com/usedesk/Android_SDK/tree/master/chat-gui/src/main/java/ru/usedesk/chat_gui/)** — library for embedding ready-to-use GUI (includes **Chat SDK**)

**[KnowledgeBase SDK](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-sdk/src/main/java/ru/usedesk/knowledgebase_sdk/)** — library for working with the Knowledge Base

**[KnowledgeBase GUI](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-gui/src/main/java/ru/usedesk/knowledgebase_gui/)** — library for embedding ready-to-use GUI (includes **KnowledgeBase SDK**)

### Steps to add the SDK to a project

Add a line to `build.gradle` of your project:

```
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}
```

Add to the dependencies `build.gradle` of your module following lines:

```
//Chat SDK
implementation "com.github.usedesk.Android_SDK:chat-sdk:$usedeskSdkVersion"
//Chat GUI
implementation "com.github.usedesk.Android_SDK:chat-gui:$usedeskSdkVersion"
//Knowlage Base SDK
implementation "com.github.usedesk.Android_SDK:knowledgebase-sdk:$usedeskSdkVersion"
//Knowlage Base GUI
implementation "com.github.usedesk.Android_SDK:knowledgebase-gui:$usedeskSdkVersion"
```

Add to Manifest file:

```
<uses - permission android : name ="android.permission.INTERNET" / >

<!--To attach a photo from a camera to chat-- >
<uses - permission android : name ="android.permission.CAMERA" / >

<!--Only when using the foreground notification service -->
<uses - permission android : name ="android.permission.FOREGROUND_SERVICE" / >
```

## Initializing a Chat

### Parameters used in SDK configuration

Chat configuration is set in the file [**UsedeskChatConfiguration**](https://github.com/usedesk/Android_SDK/tree/master/chat-sdk/src/main/java/ru/usedesk/chat_sdk/entity/UsedeskChatConfiguration.kt):

Where * — required parameter

| **Parameter**              | **Type**                | **Description**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
|----------------------------|-------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **urlChat***               | String                  | **Server URL for SDK chats**<br/>By default: `pubsubsec.usedesk.ru`<br/>If you use server version of Usedesk on your own server, value may be different for you. Check with support for valid URL — support@usedesk.com                                                                                                                                                                                                                                                                                                                                                             |
| **urlChatApi***            | String                  | **URL to work with Usedesk API**<br/>By default: `secure.usedesk.ru/uapi`<br/>If you use server version of Usedesk on your own server, value may be different for you. Check with support for valid URL — support@usedesk.com                                                                                                                                                                                                                                                                                                                                                       |
| **companyId***             | String                  | **Company ID in Usedesk**<br/>[How to find a company ID](https://en.usedocs.com/article/6396)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| **channelId***             | String                  | **ID of the chat channel through which messages from the application will be placed at Usedesk**<br/>[How to create and set up a channel](https://en.usedocs.com/article/16616)                                                                                                                                                                                                                                                                                                                                                                                                     |
| **messagesPageSize**       | Int                     | **Number of loaded messages when starting the chat**<br/>When client open a chat, a specified number of messages are loaded. As client scrolls chat, 20 more messages are loaded                                                                                                                                                                                                                                                                                                                                                                                                    |
| **clientToken**            | String?                 | **A unique token that uniquely identifies the user and his conversation**<br/>The token is provided in the callback after the initialization of the chat and is linked to the mail-phone-user name.<br/>To identify different users on the same device, you must store and pass the received token to the initialization method<br/>By specifying `null` the library itself will use the saved token on the device used earlier with the same `clientEmail`, `clientPhoneNumber`, `clientName` fields in the configuration.<br/>By specifying `""` the saved token will not be used |
| **clientEmail**            | String?                 | **Client Email**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| **clientName**             | String?                 | **Client name**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| **clientNote**             | String?                 | **Text of note**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| **clientPhoneNumber**      | Long?                   | **Client Phone**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| **clientAdditionalId**     | String?                 | **Additional customer ID**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| **clientInitMessage**      | String?                 | **Automatic message**<br/>Sent immediately after initialization on behalf of the client                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| **clientAvatar**           | String?                 | **Customer avatar image**<br/>Path to the image file. <br/>If set, then sdk will send the avatar at a time                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| **additionalFields**       | Map<Long, String>       | **Collection of additional request fields**<br/>Where `key` is the field ID, `value` is the field value. <br/>Field values depend on the type, for checkboxes - `"true"` / `"false"`, for list boxes - text that exactly matches the text of the list value, for text - any text.                                                                                                                                                                                                                                                                                                   |
| **additionalNestedFields** | List<Map<Long, String>> | **List of collections of nested lists**<br/>Each list item is a collection of values of one nested list, where `key` is the field ID, `value` is the field value with text that exactly matches the text of the list value.                                                                                                                                                                                                                                                                                                                                                         |

### Local notification service configuration

The SDK can send notifications about new messages if the application is running and a connection to the chat server is established.

To enable the local notification service, you need to create 2 own classes:

1) Service inherited from [UsedeskForegroundNotificationsService](https://github.com/usedesk/Android_SDK/tree/master/chat-sdk/src/main/java/ru/usedesk/chat_sdk/service/notifications/view/UsedeskForegroundNotificationsService.kt), where you can override the following methods:

| **Method**                  | **Returning type** | **Event description**                           |
|-----------------------------|--------------------|-------------------------------------------------|
| **getContentPendingIntent** | PendingIntent?     | **Action on click on a notification**           |
| **getDeletePendingIntent**  | PendingIntent?     | **Action on removing a notification**           |
| **getClosePendingIntent**   | PendingIntent?     | **Action on closing a foreground notification** |
| **getChannelId**            | String             | **Notification channel number**                 |
| **getChannelTitle**         | String             | **Notification channel name**                   |
| **createNotification**      | Notification?      | **Creating a notification**                     |

2) Factory inherited from [UsedeskNotificationsServiceFactory](https://github.com/usedesk/Android_SDK/tree/master/chat-sdk/src/main/java/ru/usedesk/chat_sdk/service/notifications/UsedeskNotificationsServiceFactory.kt) to override the method:

| **Method**          | **Returning type** | **Event description** |
|---------------------|--------------------|-----------------------|
| **getServiceClass** | Class \<?>         | **Class of service**  |

After creating classes, you can use the factory in the SDK:

```kotlin
UsedeskChatSdk.setNotificationsServiceFactory(CustomNotificationsServiceFactory())
```

### Using with GUI

To run the SDK with a ready-made chat user interface (GUI), use [UsedeskChatScreen](https://github.com/usedesk/Android_SDK/tree/master/chat-gui/src/main/java/ru/usedesk/chat_gui/chat/UsedeskChatScreen.kt). For example, using the `newInstance` method:

```kotlin
supportFragmentManager.beginTransaction()
    .replace(
        R.id.container,
        UsedeskChatScreen.newInstance(chatConfiguration)
    ).commit()
```

To use with Jetpack Navigation, you can use the `createBundle` method, for example:

```kotlin
navController.navigate(
    R.id.action_configurationScreen_to_usedeskChatScreen,
    UsedeskChatScreen.createBundle(chatConfiguration)
)
```

The `newInstance` and `createBundle` methods take the following arguments:

| **Argument**                       | **Type**                 | **Description**                                                                                        |
|------------------------------------|--------------------------|--------------------------------------------------------------------------------------------------------|
| **chatConfiguration**              | UsedeskChatConfiguration | `UsedeskChatScreen` assumes the responsibility of calling the `UsedeskChatSdk.setConfiguration` method |
| **customAgentName**                | String?                  | If set, all agent names in the chat will be replaced by the value of the parameter                     |
| **rejectedFileExtensions**         | Collection\<String>?     | List of file extensions marked as dangerous (the `onFileClick` method of the parent is called anyway)  |
| **messagesDateFormat**             | String?                  | If set, changes the format of the message group date display                                           |
| **messageTimeFormat**              | String?                  | If set, changes the format of the message time display                                                 |
| **adaptiveTextMessageTimePadding** | Boolean                  | If `true` is set, shifts the text of messages relative to the time                                     |
| **groupAgentMessages**             | Boolean                  | If `true` is set, groups messages from the same agent                                                  |

For the fragment to work fully it is necessary to:

1) Pass the `onBackPressed` event by calling the same method on the fragment, which will return `true` if the event was handled, or `false` if not

Example:

```kotlin
override fun onBackPressed() {
    val fragment = getCurrentFragment()
    if (fragment is UsedeskFragment && fragment.onBackPressed()) {
        return
    }
}
```

2) Implement the [IUsedeskOnFileClickListener](https://github.com/usedesk/Android_SDK/tree/master/chat-gui/src/main/java/ru/usedesk/chat_gui/IUsedeskOnFileClickListener.kt) interface by overriding the `onFileClick` method

Example:

```kotlin
override fun onFileClick(usedeskFile: UsedeskFile) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.container, UsedeskShowFileScreen.newInstance(usedeskFile))
        .commit()
    //or
    navController.navigate(
        R.id.action_usedeskChatScreen_to_usedeskShowFileScreen,
        UsedeskShowFileScreen.createBundle(usedeskFile)
    )
}
```

3) Implement the [IUsedeskOnDownloadListener](https://github.com/usedesk/Android_SDK/tree/master/chat-gui/src/main/java/ru/usedesk/chat_gui/IUsedeskOnDownloadListener.kt) interface as a parent by overriding the `onDownload` method.

4) To bind a ViewModel lifecycle to a parent, you need to implement the [IUsedeskChatViewModelStoreOwner](https://github.com/usedesk/Android_SDK/tree/master/chat-gui/src/main/java/ru/usedesk/chat_gui/chat/IUsedeskChatViewModelStoreOwner.kt) interface

5) To receive a client token, implement the [IUsedeskOnClientTokenListener](https://github.com/usedesk/Android_SDK/tree/master/chat-gui/src/main/java/ru/usedesk/chat_gui/IUsedeskOnClientTokenListener.kt) interface as a parent

6) Implement the [IUsedeskOnChatInitedListener](https://github.com/usedesk/Android_SDK/tree/master/chat-gui/src/main/java/ru/usedesk/chat_gui/IUsedeskOnChatInitedListener.kt) interface as a parent to track when the chat is initialized

7) To correctly work with a camera photo attachment, you need to add the following lines to the `AndroidManifest.xml` file:

```
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.provider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/usedesk_provider_paths" />
</provider>
```

8) To be able to display video in full screen, the [IUsedeskOnFullscreenListener](https://github.com/usedesk/Android_SDK/tree/master/chat-gui/src/main/java/ru/usedesk/chat_gui/IUsedeskOnFullscreenListener.kt) interface must be implemented

### Using without a GUI

To work with chat without GUI you need to perform the following steps:

1) Set the configuration and initialize chat:

```kotlin
UsedeskChatSdk.setConfiguration(UsedeskChatConfiguration())
val usedeskChat = UsedeskChatSdk.init(requireContext())
//or
val usedeskChat = UsedeskChatSdk.init(requireContext(), UsedeskChatConfiguration())
```

2) Get an instance of `IUsedeskChat` after initialization. To do this, call:

```kotlin
val usedeskChat = UsedeskChatSdk.requireInstance()
```

3) Add an event listener:

```kotlin
val listener = object : IUsedeskActionListener {}
usedeskChat.addListener(listener)
```

In order to remove the listener, you need to call the corresponding method:

```kotlin
usedeskChat.removeListener(listener)
```

4) When you have finished working with the chat, to free the resources you need to call following method:

```kotlin
UsedeskChatSdk.release(false)
```

If you pass `false` to the method, the resources will only be released if all listeners have been deleted. If you pass the value `true`, the resources will be released immediately.

Attempting to retrieve an instance without initializing or after release will raise an exception.

5) Use interface to listen to chat events [IUsedeskActionListener](https://github.com/usedesk/Android_SDK/tree/master/chat-sdk/src/main/java/ru/usedesk/chat_sdk/entity/IUsedeskActionListener.kt):

| **Method**      | **Event description**                             |
|-----------------|---------------------------------------------------|
| **onModel**     | **Chat model, new, updated and deleted messages** |
| **onException** | **The exception that has risen**                  |

### Starting and stopping the local notification service

Starting the notification service:

```kotlin
UsedeskChatSdk.startService(context)
```

Stopping the notification service:

```kotlin
UsedeskChatSdk.stopService(context)
```

### Error Logging

To log server response processing errors, you can use the class `UsedeskLog`:

- `enable()` — enabling logging.
- `disable()` — disabling logging.
- `addLogListener(logListener: (String) -> Unit)` — adding the log listener.
- `removeLogListener(logListener: (String) -> Unit)` — removing the log listener.

## Initializing the Knowledge Base

### Configuration

The configuration of the Knowledge Base is set in the file [UsedeskKnowledgeBaseConfiguration](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-sdk/src/main/java/ru/usedesk/knowledgebase_sdk/entity/UsedeskKnowledgeBaseConfiguration.kt):

Where * — required parameter

| **Parameter**   | **Type** | **Description**                                                                                                                                                                                                                  |
|-----------------|----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **urlApi***     | String   | **URL to work with Usedesk API**<br/>By default: `https://secure.usedesk.ru`<br/>If you use server version of Usedesk on your own server, value may be different for you. Check with support for valid URL — support@usedesk.com |
| **accountId***  | String   | **Knowledge Base ID**<br/>[How to create a Knowledge Base](https://en.usedocs.com/article/7182)                                                                                                                                  |
| **token***      | String   | **Usedesk API Token**<br/>[How to get API Token](https://en.usedocs.com/article/10169)                                                                                                                                           |
| **clientEmail** | String?  | **Client email**                                                                                                                                                                                                                 |
| **clientName**  | String?  | **Client name**                                                                                                                                                                                                                  |

### Using with the GUI

Use [UsedeskKnowledgeBaseFragment](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-gui/src/main/java/ru/usedesk/knowledgebase_gui/screens/main/UsedeskKnowledgeBaseScreen.kt) to use a ready-made user interface, for example using the `newInstance` method:

```kotlin
supportFragmentManager().beginTransaction()
    .replace(
        R.id.container,
        UsedeskKnowledgeBaseFragment.newInstance(
            configuration = UsedeskKnowledgeBaseConfiguration(),
            withSupportButton = true,
            deepLink = DeepLink.Article(articleId = 123L, noBackStack = true)
        )
    ).commit()
```

You can use the `createBundle` method for use with Jetpack Navigation.

Example:

```kotlin
navController.navigate(
    R.id.action_configurationScreen_to_usedeskKnowledgeBaseScreen,
    UsedeskKnowledgeBaseScreen.createBundle(
        configuration = UsedeskKnowledgeBaseConfiguration(),
        withSupportButton = true,
        deepLink = DeepLink.Article(articleId = 123L, noBackStack = true)
    )
)
```

For the fragment to work properly it is necessary to:

1) Pass `onBackPressed` events by calling a similar method on the fragment, which will return `true` if the event was handled, or `false` if not.

Example:

```kotlin
override fun onBackPressed() {
    val fragment = getCurrentFragment()
    if (fragment is UsedeskFragment && fragment.onBackPressed()) {
        return
    }
}
```

2) Implement the [IUsedeskOnSupportClickListener](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-gui/src/main/java/ru/usedesk/knowledgebase_gui/screens/IUsedeskOnSupportClickListener.kt) interface as a parent, overriding the `onSupportClick()` method,

Example:

```kotlin
override fun onSupportClick() {
    supportFragmentManager().beginTransaction()
        .replace(R.id.container, UsedeskChatScreen().newInstance())
        .commit()
}
```

3) To process clicks on links in articles, you need to implement [IUsedeskOnWebUrlListener](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-gui/src/main/java/ru/usedesk/knowledgebase_gui/screens/IUsedeskOnWebUrlListener.kt) interface as a parent.

### Using without a GUI

To work with the Knowledge Base without GUI you need to perform the following steps:

1) Initialize the Knowledge Base:

```kotlin
val usedeskKnowledgeBase =
    UsedeskSdk.initKnowledgeBase(context, UsedeskKnowledgeBaseConfiguration())
//or
UsedeskKnowledgeBaseSdk.setConfiguration(UsedeskKnowledgeBaseConfiguration())
val usedeskKnowledgeBase = UsedeskSdk.initKnowledgeBase(context)
```

2) Get a class object at any place:

```kotlin
val usedeskKnowledgeBase = UsedeskKnowledgeBase.requireInstance()
```

3) Release the object:

```kotlin
UsedeskKnowledgeBaseSdk.release()
```

If you try to retrieve an instance without initializing the Knowledge Base, or after the object has been released, an exception will be raised.

## GUI localization

SDK supports the following languages:

- English (default),
- Russian,
- Spanish,
- Portuguese.

In addition, you can change an existing language or add a new one. To do this, copy the values from the file [strings_template.xml](https://github.com/usedesk/Android_SDK/blob/master/strings_template.xml "strings_template.xml"), which is in the root of the project, and add to all strings.xml files of your project. After that you can substitute your own values of string resources.

In the case of changing string resource references when customizing an application, changing string resources in this way may not have the desired result.

## Documentation

Methods to work with the SDK, element customization, and bugs are described in our documentation: [https://sdk.usedocs.com](https://sdk.usedocs.com/)
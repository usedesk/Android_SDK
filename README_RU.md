# Usedesk Android SDK

## Демонстрационное приложение

В  [директории Sample](https://github.com/usedesk/Android_SDK/tree/master/app/src/main/java/ru/usedesk/sample) этого репозитория находится демонстрационный проект, который служит примером встраивания SDK чатов Юздеска в клиентское приложение. С его помощью можно ознакомиться с базовой функциональностью приложения и проверить работу SDK.

## Добавление библиотеки в проект

### Минимальная версия Android

На данный момент минимальная версия ОС, которая поддерживается в SDK — **Android 5.0 (API 21)**

### Состав SDK

**[Chat SDK](https://github.com/usedesk/Android_SDK/tree/master/chat-sdk/src/main/java/ru/usedesk/chat_sdk/)** — библиотека для работы с чатом

**[Chat GUI](https://github.com/usedesk/Android_SDK/tree/master/chat-gui/src/main/java/ru/usedesk/chat_gui/)** — библиотека для встраивания готовых элементов интерфейса чата (включает в себя **Chat SDK**)

**[KnowledgeBase SDK](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-sdk/src/main/java/ru/usedesk/knowledgebase_sdk/)** — библиотека для работы с Базой Знаний

**[KnowledgeBase GUI](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-gui/src/main/java/ru/usedesk/knowledgebase_gui/)** — библиотека для встраивания готовых элементов интерфейса чата (включает в себя **KnowledgeBase SDK**)

### Шаги для добавления SDK в проект

Добавьте в `build.gradle` вашего проекта строку:

```
allprojects {
   repositories {
      ...
      maven { url "https://jitpack.io" }
   }
}
```

Добавьте в dependencies `build.gradle` вашего модуля строки:

```
//sdk чата
implementation "com.github.Usedesk.Android_SDK:chat-sdk:$usedeskSdkVersion"
//графический интерфейс чата
implementation "com.github.Usedesk.Android_SDK:chat-gui:$usedeskSdkVersion"
//sdk базы знаний
implementation "com.github.Usedesk.Android_SDK:knowledgebase-sdk:$usedeskSdkVersion"
//графический интерфейс базы знаний
implementation "com.github.Usedesk.Android_SDK:knowledgebase-gui:$usedeskSdkVersion"
```

Добавьте в файл Manifest:

```
<uses - permission android : name ="android.permission.INTERNET" / >

<!--Для прикрепления фото с камеры в чате-- >
<uses - permission android : name ="android.permission.CAMERA" / >

<!--Только при использовании foreground сервиса уведомлений -->
<uses - permission android : name ="android.permission.FOREGROUND_SERVICE" / >
```

## Инициализация Чата

### Параметры, используемые в конфигурации SDK

Конфигурация чата задается в файле [**UsedeskChatConfiguration**](https://github.com/usedesk/Android_SDK/tree/master/chat-sdk/src/main/java/ru/usedesk/chat_sdk/entity/UsedeskChatConfiguration.kt):

Где * — обязательный параметр

| **Параметр**               | **Тип**                 | **Описание**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
|----------------------------|-------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **urlChat***               | String                  | **Адрес сервера для работы с чатами SDK**<br/>Стандартное значение: `pubsubsec.usedesk.ru`<br/>Если вы используете коробочную версию Юздеска на собственном сервере, то этот параметр у вас может отличаться. Уточните актуальный адрес у поддержки — support@usedesk.ru                                                                                                                                                                                                                                                                                                                |
| **urlChatApi***            | String                  | **URL для работы с API**<br/>Стандартное значение: `secure.usedesk.ru/uapi`<br/>Если вы используете коробочную версию Юздеска на собственном сервере, то этот параметр у вас будет отличаться. Уточните актуальный адрес у поддержки — support@usedesk.ru                                                                                                                                                                                                                                                                                                                               |
| **companyId***             | String                  | **Идентификатор компании в Юздеске**<br/>[Как найти ID компании](https://docs.usedesk.ru/article/61)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| **channelId***             | String                  | **Идентификатор канала чата, через который в Юздеск будут поступать обращения из приложения**<br/>[Как создать и настроить канал](https://docs.usedesk.ru/article/858)                                                                                                                                                                                                                                                                                                                                                                                                                  |
| **messagesPageSize**       | Int                     | **Количество загружаемых сообщений при открытии чата**<br/>При открытии чата загружается указанное количество сообщений. По мере прокрутки чата подгружается по 20 сообщений                                                                                                                                                                                                                                                                                                                                                                                                            |
| **clientToken**            | String?                 | **Подпись, однозначно идентифицирующая пользователя и его чат**<br/>Токен выдается в коллбэке после инициализации чата и привязывается к связке почта-телефон-имя пользователя.<br/>Для идентификации различных пользователей на одном устройстве вы должны хранить и передавать полученный токен в метод инициализации<br/>Указав `null` библиотека самостоятельно воспользуется сохранённым токеном на устройстве, использованным ранее с такими же полями `clientEmail`, `clientPhoneNumber`, `clientName` в конфигурации.<br/>Указав `""` сохранённый токен использоваться не будет |
| **clientEmail**            | String?                 | **Почта клиента**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| **clientName**             | String?                 | **Имя клиента**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| **clientNote**             | String?                 | **Текст заметки**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| **clientPhoneNumber**      | Long?                   | **Телефон клиента**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| **clientAdditionalId**     | String?                 | **Дополнительный идентификатор клиента**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| **clientInitMessage**      | String?                 | **Автоматическое сообщение**<br/>Отправится сразу после инициализации от имени клиента                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| **clientAvatar**           | String?                 | **Изображение аватара клиента**<br/>Путь до файла с изображением. <br/>Если задан, то sdk отправит аватарку единовременно                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| **additionalFields**       | Map<Long, String>       | **Коллекция дополнительный полей запроса**<br/>Где `key` - ID поля, `value` - значение поля. <br/>Значения поля зависят от типа, для чекбоксов - `"true"` / `"false"`, для списков - текст, точно совпадающий с текстом значения списка, для текста - любой текст.                                                                                                                                                                                                                                                                                                                      |
| **additionalNestedFields** | List<Map<Long, String>> | **Список коллекций вложенных списков**<br/>Каждый элемент списка - это коллекия значений одного вложенного списка, где `key` - ID поля, `value` - значение поля с текстом, точно совпадающим с текстом значения списка.                                                                                                                                                                                                                                                                                                                                                                 |

### Конфигурация сервиса локальных уведомлений

SDK умеет отправлять уведомления, если приложение запущено и установлено соединение с сервером чата.

Для включения сервиса локальных уведомлений нужно создать 2 собственных класса:

1) Сервис, унаследованный от [UsedeskForegroundNotificationsService](https://github.com/usedesk/Android_SDK/tree/master/chat-sdk/src/main/java/ru/usedesk/chat_sdk/service/notifications/view/UsedeskForegroundNotificationsService.kt), в котором можно переопределить следующие методы:

| **Метод**                   | **Тип возвращаемого значения** | **Описание события**                             |
|-----------------------------|--------------------------------|--------------------------------------------------|
| **getContentPendingIntent** | PendingIntent?                 | **Действие при нажатии на уведомление**          |
| **getDeletePendingIntent**  | PendingIntent?                 | **Действие при удалении уведомления**            |
| **getClosePendingIntent**   | PendingIntent?                 | **Действие при закрытии foreground уведомления** |
| **getChannelId**            | String                         | **Номер канала уведомления**                     |
| **getChannelTitle**         | String                         | **Названия канала уведомления**                  |
| **createNotification**      | Notification?                  | **Создание уведомления**                         |

2) Фабрику, унаследованную от [UsedeskNotificationsServiceFactory](https://github.com/usedesk/Android_SDK/tree/master/chat-sdk/src/main/java/ru/usedesk/chat_sdk/service/notifications/UsedeskNotificationsServiceFactory.kt) для переопределения метода:

| **Метод**           | **Тип возвращаемого значения** | **Описание события** |
|---------------------|--------------------------------|----------------------|
| **getServiceClass** | Class \<?>                     | **Класс сервиса**    |

После создания классов можно использовать фабрику в SDK:

```kotlin
UsedeskChatSdk.setNotificationsServiceFactory(CustomNotificationsServiceFactory())
```

### Использование с GUI

Для запуска SDK с готовым пользовательским интерфейсом чата (GUI)
воспользуйтесь [UsedeskChatScreen](https://github.com/usedesk/Android_SDK/tree/master/chat-gui/src/main/java/ru/usedesk/chat_gui/chat/UsedeskChatScreen.kt), например при помощи метода `newInstance`:

```kotlin
supportFragmentManager.beginTransaction()
   .replace(
      R.id.container,
      UsedeskChatScreen.newInstance(chatConfiguration)
   ).commit()
```

Для использования с Jetpack Navigation можно воспользоваться методом `createBundle`, например:

```kotlin
navController.navigate(
   R.id.action_configurationScreen_to_usedeskChatScreen,
   UsedeskChatScreen.createBundle(chatConfiguration)
)
```

Методы `newInstance` и `createBundle` принимают следующие аргументы:

| **Аргумент**                       | **Тип**                  | **Описание**                                                                                              |
|------------------------------------|--------------------------|-----------------------------------------------------------------------------------------------------------|
| **chatConfiguration**              | UsedeskChatConfiguration | `UsedeskChatScreen` берёт на себя обязанность вызова метода `UsedeskChatSdk.setConfiguration`             |
| **agentName**                      | String?                  | Если задан, то все имена агентов в чате будут заменены на значение параметра                              |
| **rejectedFileExtensions**         | Collection\<String>?     | Список расширений файлов, помечаемых как опасные (метод `onFileClick` родителя вызывается в любом случае) |
| **messagesDateFormat**             | String?                  | Если задан, то меняет формат одображения даты группы сообщений                                            |
| **messageTimeFormat**              | String?                  | Если задан, то меняет формат одображения времени сообщений                                                |
| **adaptiveTextMessageTimePadding** | Boolean                  | При значении `true` сдвигает текст сообщений относительно времени                                         |
| **groupAgentMessages**             | Boolean                  | При значении `true` группирует сообщения от одного агента                                                 |

Для полноценной работы фрагмента необходимо:

1) Передавать события `onBackPressed`, вызывая аналогичный метод у фрагмента, который вернёт `true`, если событие было обработано, либо `false` если нет

Пример:

```kotlin
override fun onBackPressed() {
   val fragment = getCurrentFragment()
   if (fragment is UsedeskFragment && fragment.onBackPressed()) {
      return
   }
}
```

2) Реализовать интерфейс [IUsedeskOnFileClickListener](https://github.com/usedesk/Android_SDK/tree/master/chat-gui/src/main/java/ru/usedesk/chat_gui/IUsedeskOnFileClickListener.kt) родителем, переопределив метод `onFileClick`

Пример:

```kotlin
override fun onFileClick(usedeskFile: UsedeskFile) {
   supportFragmentManager.beginTransaction()
      .replace(R.id.container, UsedeskShowFileScreen.newInstance(usedeskFile))
      .commit()
   //или
   navController.navigate(
      R.id.action_usedeskChatScreen_to_usedeskShowFileScreen,
      UsedeskShowFileScreen.createBundle(usedeskFile)
   )
}
```

3) Реализовать интерфейс [IUsedeskOnDownloadListener](https://github.com/usedesk/Android_SDK/tree/master/chat-gui/src/main/java/ru/usedesk/chat_gui/IUsedeskOnDownloadListener.kt) родителем, переопределив метод `onDownload`.

4) Для привязки жизненного цикла ViewModel к родителю необходимо реализовать интерфейс [IUsedeskChatViewModelStoreOwner](https://github.com/usedesk/Android_SDK/tree/master/chat-gui/src/main/java/ru/usedesk/chat_gui/chat/IUsedeskChatViewModelStoreOwner.kt)

5) Для получения токена клиента реализовать интерфейс [IUsedeskOnClientTokenListener](https://github.com/usedesk/Android_SDK/tree/master/chat-gui/src/main/java/ru/usedesk/chat_gui/IUsedeskOnClientTokenListener.kt)
   родителем.

6) Для отслеживания момента инициализации чата реализовать интерфейс [IUsedeskOnChatInitedListener](https://github.com/usedesk/Android_SDK/tree/master/chat-gui/src/main/java/ru/usedesk/chat_gui/IUsedeskOnChatInitedListener.kt) родителем.

7) Для корректной работы прикрепления фото с камеры необходимо добавить в файл `AndroidManifest.xml` следующие строки:

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

8) Для возможности отображения видео во весь экран необходимо реализовать интерфейс [IUsedeskOnFullscreenListener](https://github.com/usedesk/Android_SDK/tree/master/chat-gui/src/main/java/ru/usedesk/chat_gui/IUsedeskOnFullscreenListener.kt)

### Использование без GUI

Для работы с чатом без GUI необходимо выполнить следующие шаги:

1) Задать конфигурацию и проинициализировать чат:

```kotlin
UsedeskChatSdk.setConfiguration(UsedeskChatConfiguration())
val usedeskChat = UsedeskChatSdk.init(requireContext())
//или
val usedeskChat = UsedeskChatSdk.init(requireContext(), UsedeskChatConfiguration())
```

2) Получить экземпляр `IUsedeskChat` после инициализации. Для этого нужно вызвать:

```kotlin
val usedeskChat = UsedeskChatSdk.requireInstance()
```

3) Добавить слушателя событий:

```kotlin
val listener = object : IUsedeskActionListener {}
usedeskChat.addListener(listener)
```

Для того, чтобы удалить слушателя, нужно вызвать соответствующий метод:

```kotlin
usedeskChat.removeListener(listener)
```

4) После окончания работы с чатом для освобождения ресурсов необходимо вызвать метод:

```kotlin
UsedeskChatSdk.release(false)
```

Если передать в метод значение `false`, то ресурсы будут освобождены только в том случае, если все слушатели были удалены. Если передать значение `true`, то ресурсы будут свобождены немедленно.

Попытка получить экземпляр без инициализации или после освобожения вызовет исключение.

5) Использовать интерфейс для прослушивания событий чата [IUsedeskActionListener](https://github.com/usedesk/Android_SDK/tree/master/chat-sdk/src/main/java/ru/usedesk/chat_sdk/entity/IUsedeskActionListener.kt):

| **Метод**       | **Описание события**                                      |
|-----------------|-----------------------------------------------------------|
| **onModel**     | **Модель чата, новые, обновлённые и удалённые сообщения** |
| **onException** | **Возникшее исключение**                                  |

### Запуск и остановка сервиса локальных уведомлений

Запуск сервиса уведомлений:

```kotlin
UsedeskChatSdk.startService(context)
```

Остановка сервиса уведомлений:

```kotlin
UsedeskChatSdk.stopService(context)
```

### Логирование ошибок

Для логирования ошибок обработки ответов сервера можно воспользоваться классом `UsedeskLog`:

- `enable()` — включение логирования.
- `disable()` — выключение логирования.
- `addLogListener(logListener: (String) -> Unit)` — добавление слушателя логов.
- `removeLogListener(logListener: (String) -> Unit)` — удаление слушателя логов.

## Инициализация Базы знаний

### Конфигурация

Конфигурация Базы знаний задается в файле [UsedeskKnowledgeBaseConfiguration](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-sdk/src/main/java/ru/usedesk/knowledgebase_sdk/entity/UsedeskKnowledgeBaseConfiguration.kt):

Где * — обязательный параметр

| **Параметр**    | **Тип** | **Описание**                                                                                                                                                                                                                                                 |
|-----------------|---------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **urlApi***     | String  | **URL для работы с API**<br/>Стандартное значение: `https://secure.usedesk.ru`<br/>Если вы используете коробочную версию Юздеска на собственном сервере, то этот параметр у вас будет отличаться. Уточните актуальный адрес у поддержки — support@usedesk.ru |
| **accountId***  | String  | **Идентификатор базы знаний**<br/>[Как создать Базу знаний](https://docs.usedesk.ru/article/1678)                                                                                                                                                            |
| **token***      | String  | **Ключ для доступа к API Юздеска**<br/>[Как получить API ключ](https://docs.usedesk.ru/article/10167)                                                                                                                                                        |
| **clientEmail** | String? | **Почта клиента**                                                                                                                                                                                                                                            |
| **clientName**  | String? | **Имя клиента**                                                                                                                                                                                                                                              |

### Использование с GUI

Для использования готового пользовательского интерфейса воспользуйтесь [UsedeskKnowledgeBaseScreen](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-gui/src/main/java/ru/usedesk/knowledgebase_gui/screen/UsedeskKnowledgeBaseScreen.kt), например при помощи метода `newInstance`:

```kotlin
supportFragmentManager().beginTransaction()
   .replace(
      R.id.container,
      UsedeskKnowledgeBaseScreen.newInstance(
         configuration = UsedeskKnowledgeBaseConfiguration(),
         withSupportButton = true,
         deepLink = DeepLink.Article(articleId = 123L, noBackStack = true)
      )
   ).commit()
```

Для использования с Jetpack Navigation можно воспользоваться методом `createBundle`.

Пример:

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

Для полноценной работы фрагмента необходимо:

1) Передавать события `onBackPressed`, вызывая аналогичный метод у фрагмента, который вернёт `true` если событие было обработано, либо `false` если нет.

Пример:

```kotlin
override fun onBackPressed() {
   val fragment = getCurrentFragment()
   if (fragment is UsedeskFragment && fragment.onBackPressed()) {
      return
   }
}
```

2) Реализовать интерфейс [IUsedeskOnSupportClickListener](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-gui/src/main/java/ru/usedesk/knowledgebase_gui/screen/IUsedeskOnSupportClickListener.kt) родителем, переопределив метод `onSupportClick()`,

Пример:

```kotlin
override fun onSupportClick() {
   supportFragmentManager().beginTransaction()
      .replace(R.id.container, UsedeskChatScreen().newInstance())
      .commit()
}
```

3) Для обработки кликов по ссылкам в статьях, необходимо реализовать интерфейс [IUsedeskOnWebUrlListener](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-gui/src/main/java/ru/usedesk/knowledgebase_gui/screen/IUsedeskOnWebUrlListener.kt).

### Использование без GUI

Для работы с Базой знаний без GUI необходимо выполнить следующие шаги:

1) Инициализировать Базу Знаний:

```kotlin
val usedeskKnowledgeBase: IUsedeskKnowledgeBase =
   UsedeskSdk.initKnowledgeBase(context, сonfiguration)
//или
UsedeskKnowledgeBaseSdk.setConfiguration(configuration)
val usedeskKnowledgeBase: IUsedeskKnowledgeBase = UsedeskSdk.initKnowledgeBase(context)
```

2) Получить объект класса в любом месте:

```kotlin
val usedeskKnowledgeBase = UsedeskKnowledgeBase.requireInstance()
```

3) Освободить объект:

```kotlin
UsedeskKnowledgeBaseSdk.release()
```

Если попытаться получить экземпляр без инициализации Базы знаний, или после того, как объект был освобожден, вызовется исключение.

## Локализация GUI

SDK поддерживает следующие языки:

- английский (по умолчанию),
- русский,
- испанский,
- португальский.

Помимо этого, можно изменить существующий язык или добавить новый. Для этого необходимо скопировать ресурсы из файлов, ссылающиеся на `@string/usedesk_string`, и добавить в strings.xml вашего проекта, подставив нужные значения:

- **common-gui** [strings.xml](https://github.com/usedesk/Android_SDK/blob/master/common-gui/src/main/res/values/strings.xml "strings.xml")
- **chat-gui** [strings.xml](https://github.com/usedesk/Android_SDK/blob/master/chat-gui/src/main/res/values/strings.xml "strings.xml")
- **knowledgebase-gui** [strings.xml](https://github.com/usedesk/Android_SDK/blob/master/knowledgebase-gui/src/main/res/values/strings.xml "strings.xml")

В случае изменения ссылок на строковые ресурсы при кастомизации приложения изменение строковых ресурсов таким способом может не привести к желаемому результату.

## Документация

Методы для работы с SDK, кастомизация элементов, а также ошибки описаны в нашей документации: [http://sdk.usedocs.ru](http://sdk.usedocs.ru/)
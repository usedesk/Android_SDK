# Android Usedesk SDK (v3.11.10)

### !Важно! В версии 3.10.5 изменены методы работы с библиотекой. О всех особенностях обновления со старых версий [читайте тут.](https://github.com/usedesk/Android_SDK/releases/tag/3.10.5)

- [Подключение к проекту](#preparation)
- [Локализация](#gui_localization)
- [Чат](#chat)
  - [Конфигурация](#chat_configuration)
  - [Использование с GUI](#chat_gui)
  - [Использование без GUI](#chat_sdk)
- [База знаний](#knowledge_base)
  - [Конфигурация](#knowledge_base_configuration)
  - [Использование с GUI](#knowledge_base_gui)
  - [Использование без GUI](#knowledge_base_sdk)
- [Локализация GUI](#gui_localization)
- [Предыдущие версии](#previous_versions)

<a name="preparation"></a>

## Подключение к проекту

Минимальная версия **Android 4.4 (API 19)**

**[Chat SDK](https://github.com/usedesk/Android_SDK/tree/master/chat-sdk/src/main/java/ru/usedesk/chat_sdk/)** - библиотека для работы с чатом.

**[Chat GUI](https://github.com/usedesk/Android_SDK/tree/master/chat-gui/src/main/java/ru/usedesk/chat_gui/)** - библиотека для встраивания готовых элементов интерфейса чата (включает в себя **Chat SDK**).

**[KnowledgeBase SDK](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-sdk/src/main/java/ru/usedesk/knowledgebase_sdk/)** - библиотека для работы с Базой Знаний.

**[KnowledgeBase GUI](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-gui/src/main/java/ru/usedesk/knowledgebase_gui/)** - библиотека для встраивания готовых элементов интерфейса чата (включает в себя **KnowledgeBase SDK**).

**[Usedesk Sample App](https://github.com/usedesk/Android_SDK/tree/master/app/src/main/java/ru/usedesk/sample)** - пример использования библиотеки.

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
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.CAMERA" />
```

<a name="chat"></a>
## Чат

<a name="chat_configuration"></a>
### Конфигурация

Для работы с чатом необходимо задать конфигурацию:

```
UsedeskChatSdk.setConfiguration(UsedeskChatConfiguration(...)
```

[**UsedeskChatConfiguration**](https://github.com/usedesk/Android_SDK/tree/master/chat-sdk/src/main/java/ru/usedesk/chat_sdk/entity/UsedeskChatConfiguration.kt) - конфигурация чата:

| Переменная | Тип | Описание |
|----------------|------|-------------|
| urlChat \* | String | Адрес сервера Чата. Стандартное значение `https://pubsubsec.usedesk.ru` |
| urlOfflineForm \* | String | Адрес для отправки формы обратной связи. Стандартное значение `https://secure.usedesk.ru/` |
| urlToSendFile \* | String | Адрес для отправки файлов. Стандартное значение `https://secure.usedesk.ru/uapi/v1/` |
| companyId \* | String | Идентификатор компании |
| channelId \* | String | Идентификатор канала |
| clientToken | String? | Токен, позволяющий однозначно идентифицировать клиента в системе. Указав `null` библиотека самостоятельно воспользуется сохранённым токеном на устройстве, использованным ранее с такими же полями `clientEmail`, `clientPhoneNumber`, `clientName ` в конфигурации. Указав `""` сохранённый токен использоваться не будет |
| clientEmail | String? | Почта клиента |
| clientName | String? | Имя клиента |
| clientNote | String? | Заметка о клиенте |
| clientPhoneNumber | Long? | Телефонный номер клиента |
| clientAdditionalId | String? | Дополнительный идентификатор клиента |
| clientInitMessage | String? | Сообщение, автоматически отправляемое от клиента при открытии чата |
| additionalFields | Map<Long, String> | Коллекция дополнительных полей, где key - Id поля, value - значение поля. Значения поля зависят от типа, для чекбоксов - `"true"` / `"false"`, для списков - текст, точно совпадающий с текстом значения списка, для текста - любой текст. |
| additionalNestedFields | List<Map<Long, String>> | Список коллекций вложенных списков, где каждый элемент списка - это коллекия значений одного вложенного списка, где key - Id поля, value - значение поля с текстом, точно совпадающим с текстом значения списка. |

\* - обязательный параметр

Для включения локальных уведомлений нужно создать 2 собственных класса:
- Сервис, унаследованный от [UsedeskSimpleNotificationsService](https://github.com/usedesk/Android_SDK/tree/master/chat-sdk/src/main/java/ru/usedesk/chat_sdk/service/notifications/view/UsedeskSimpleNotificationsService.kt) (обычный сервис) или [UsedeskForegroundNotificationsService](https://github.com/usedesk/Android_SDK/tree/master/chat-sdk/src/main/java/ru/usedesk/chat_sdk/service/notifications/view/UsedeskForegroundNotificationsService.kt) (foreground сервис). Где можно переопределить некоторые методы:

| Метод | Тип возвращаемого значения | Описание события |
|---------|------------------------------------|-----------------------|
| getContentPendingIntent | PendingIntent? | Действие при нажатии на уведомление |
| getDeletePendingIntent | PendingIntent? | Действие при удалении уведомления |
| getClosePendingIntent | PendingIntent? | Действие при закрытии foreground уведомления |
| getChannelId | String | Номер канала уведомления |
| getChannelTitle | String | Названия канала уведомления |
| createNotification | Notification? | Создание уведомления |

- Фабрику, унаследованную от [UsedeskNotificationsServiceFactory](https://github.com/usedesk/Android_SDK/tree/master/chat-sdk/src/main/java/ru/usedesk/chat_sdk/service/notifications/UsedeskNotificationsServiceFactory.kt) для переопределения метода:

| Метод | Тип возвращаемого значения | Описание события |
|---------|------------------------------------|-----------------------|
| getServiceClass | Class<?> | Класс сервиса |

После чего указать SDK использовать фабрику:

```
UsedeskChatSdk.setNotificationsServiceFactory(CustomNotificationsServiceFactory())
```

<a name="chat_gui"></a>

### Использование с GUI

Для использования готового пользовательского интерфейса чата
воспользуйтесь [UsedeskChatScreen](https://github.com/usedesk/Android_SDK/tree/master/chat-gui/src/main/java/ru/usedesk/chat_gui/chat/UsedeskChatScreen.kt)
, например при помощи метода `newInstance`:

```
supportFragmentManager.beginTransaction()
    .replace(
        R.id.container, 
        UsedeskChatScreen.newInstance(...)
    ).commit()
```

Для использования с Jetpack Navigation можно воспользоваться методом `createBundle`, например:

```
navController.navigate(
    R.id.action_configurationScreen_to_usedeskChatScreen,
    UsedeskChatScreen.createBundle(...)
)
```

Методы `newInstance` и `createBundle` принимают следующие аргументы:

| Аргумент                        | Тип                       |                                                              |
| ------------------------------- | ------------------------- | ------------------------------------------------------------ |
| customAgentName                 | String?                   | Если задан, то все имена агентов в чате будут заменены на значение параметра. |
| rejectedFileExtensions          | Collection<String>?       | Список расширений файлов, помечаемых как опасные (метод `onFileClick` родителя вызывается в любом случае). |
| chatConfiguration               | UsedeskChatConfiguration? | Если задан, то `UsedeskChatScreen` берёт на себя обязанность вызова метода `UsedeskChatSdk.setConfiguration`. |
| messagesDateFormat              | String?                   | Если задан, то меняет формат одображения даты группы сообщений |
| messageTimeFormat               | String?                   | Если задан, то меняет формат одображения времени сообщений |
| adaptiveTextMessageTimePadding  | Boolean                   | При значении `true` сдвигает текст сообщений относительно времени |
| groupAgentMessages              | Boolean                   | При значении `true` группирует сообщения от одного агента |

Для полноценной работы фрагмента необходимо:

- Передавать события `onBackPressed`, вызывая аналогичный метод у фрагмента, который вернёт `true`
  если событие было обработано, либо `false` если нет, например:

```
override fun onBackPressed() {
    val fragment = getCurrentFragment()
    if (fragment is UsedeskFragment && fragment.onBackPressed()) {
        return
    }
}
```

- Для привязки жизненного цикла ViewModel к родителю необходимо расширить
  интерфейс [IUsedeskChatViewModelStoreOwner](https://github.com/usedesk/Android_SDK/tree/master/chat-gui/src/main/java/ru/usedesk/chat_gui/chat/IUsedeskChatViewModelStoreOwner.kt)
  .

- Расширить
  интерфейс [IUsedeskOnFileClickListener](https://github.com/usedesk/Android_SDK/tree/master/chat-gui/src/main/java/ru/usedesk/chat_gui/IUsedeskOnFileClickListener.kt)
  родителем, переопределив метод `onFileClick`, например:

```
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

- Расширить
  интерфейс [IUsedeskOnDownloadListener](https://github.com/usedesk/Android_SDK/tree/master/chat-gui/src/main/java/ru/usedesk/chat_gui/IUsedeskOnDownloadListener.kt)
  родителем, переопределив метод `onDownload`.

<a name="chat_gui_files"></a>

- Для корректной работы прикрепления фото с камеры необходимо добавить в файл `AndroidManifest.xml`
  следующие строки:

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

- Для возможности отображения видео во весь экран необходимо расширить
  интерфейс [IUsedeskOnFullscreenListener](https://github.com/usedesk/Android_SDK/tree/master/chat-gui/src/main/java/ru/usedesk/chat_gui/IUsedeskOnFullscreenListener.kt)

<a name="chat_sdk"></a>

### Использование без GUI

После установки конфигурации нужно проинициализировать чат:

```
val usedeskChat = UsedeskChatSdk.init(requireContext())
```

После инициализации можно получить экземпляр `IUsedeskChat` вызвав:

```
val usedeskChat = UsedeskChatSdk.requireInstance()
```

Теперь можно добавлять слушателей событий:

```
val listener = object : IUsedeskActionListener{...}
usedeskChat.addListener(listener)
```

Для того, чтобы удалить слушателя, нужно вызвать соответствующий метод:

```
usedeskChat.removeListener(listener)
```

После окончания работы с чатом для освобождения ресурсов необходимо вызвать метод:

```
UsedeskChatSdk.release(false)
```

Если передать в метод значение `false`, то ресурсы будут освобождены только в том случае, если все слушатели были удалены. Если передать значение `true`, то ресурсы будут свобождены немедленно.
Попытка получить экземпляр без инициализации или после освобожения вызовет исключение.


[IUsedeskActionListener](https://github.com/usedesk/Android_SDK/tree/master/chat-sdk/src/main/java/ru/usedesk/chat_sdk/entity/IUsedeskActionListener.kt) - интерфейс для прослушивания событий чата:

| Метод | Параметры | Описание события |
|---------|---------------|-----------------------|
| onConnectionState | UsedeskConnectionState | Состояние подключения к серверу |
| onMessageReceived | UsedeskMessage | Каждое сообщение |
| onNewMessageReceived | UsedeskMessage | Каждое новое сообщение |
| onMessagesReceived | List&lt;UsedeskMessage&gt; | Список сообщений из чата при каждом изменении |
| onMessageUpdated | UsedeskMessage | Обновление полученного ранее сообщения |
| onFeedbackReceived | - | Отзыв доставлен |
| onOfflineFormExpected | UUsedeskOfflineFormSettings| Ожидается Форма Обратной Связи |
| onException | UsedeskException | Возникшее исключение |

[IUsedeskActionListenerRx](https://github.com/usedesk/Android_SDK/tree/master/chat-sdk/src/main/java/ru/usedesk/chat_sdk/entity/IUsedeskActionListenerRx.kt) - класс для прослушивания событий чата:

| Метод | Параметры | Описание события |
|---------|---------------|-----------------------|
| onConnectionStateObservable | Observable&lt;UsedeskConnectionState&gt; | Состояние подключения к серверу |
| onMessageObservable | Observable&lt;UsedeskMessage&gt; | Каждое сообщение |
| onNewMessageObservable | Observable&lt;UsedeskMessage&gt; | Каждое новое сообщение |
| onMessagesObservable | Observable&lt;List&lt;UsedeskMessage&gt;&gt; | Список сообщений из чата на момент подключения |
| onMessageUpdateObservable | Observable&lt;UsedeskMessage&gt; | Обновление полученного ранее сообщения |
| onFeedbackObservable | Observable&lt;UsedeskEvent&lt;Any?&gt;&gt; | Отзыв доставлен |
| onOfflineFormExpectedObservable | Observable&lt;UsedeskOfflineFormSettings&gt; | Ожидается Форма Обратной Связи |
| onExceptionObservable | Observable&lt;Exception&gt; | Возникшее исключение |

Запуск сервиса уведомлений:

```
UsedeskChatSdk.startService(context)
```

Остановка сервиса уведомлений:

```
UsedeskChatSdk.stopService(context)
```

Для логирования ошибок обработки ответов сервера можно воспользоваться классом `UsedeskLog`:

- `enable()` - включение логирования.
- `disable()` - выключение логирования.
- `addLogListener(logListener: (String) -> Unit)` - добавление слушателя логов.
- `removeLogListener(logListener: (String) -> Unit)` - удаление слушателя логов.

<a name="knowledge_base"></a>

## База знаний

<a name="knowledge_base_configuration"></a>

### Конфигурация

Для работы с SDK Базы Знаний необходимо задать конфигурацию:

```
UsedeskKnowledgeBaseSdk.setConfiguration(UsedeskKnowledgeBaseConfiguration(...))
```

[UsedeskKnowledgeBaseConfiguration](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-sdk/src/main/java/ru/usedesk/knowledgebase_sdk/entity/UsedeskKnowledgeBaseConfiguration.kt) - конфигурация Базы Знаний:

| Переменная | Тип | Описание |
|----------------|------|-------------|
| urlApi \*| String | Адрес api сервера. Стандартное значение `https://api.usedesk.ru/` |
| accountId \*| String | Идентификатор Базы Знаний в системе |
| token \*| String | Токен доступа к API |
| clientEmail | String? | Email клиента |
| clientName | String? | Имя клиента |

\* - обязательный параметр

<a name="knowledge_base_gui"></a>

### Использование с GUI

Для использования готового пользовательского интерфейса
воспользуйтесь [UsedeskKnowledgeBaseFragment](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-gui/src/main/java/ru/usedesk/knowledgebase_gui/screens/main/UsedeskKnowledgeBaseScreen.kt)
, например при помощи метода `newInstance`:

```
supportFragmentManager().beginTransaction()
    .replace(
        R.id.container, 
        UsedeskKnowledgeBaseFragment.newInstance(
            withSupportButton, 
            withArticleRating, 
            knowledgeBaseConfiguration
        )
    ).commit()
```

Для использования с Jetpack Navigation можно воспользоваться методом `createBundle`, например:

```
navController.navigate(
    R.id.action_configurationScreen_to_usedeskKnowledgeBaseScreen,
    UsedeskKnowledgeBaseScreen.createBundle(
        withSupportButton, 
        withArticleRating, 
        knowledgeBaseConfiguration
    )
)
```

Для полноценной работы фрагмента необходимо:

- Передавать события `onBackPressed`, вызывая аналогичный метод у фрагмента, который вернёт `true`
  если событие было обработано, либо `false` если нет, например:

```
override fun onBackPressed() {
    val fragment = getCurrentFragment()
    if (fragment is UsedeskFragment && fragment.onBackPressed()) {
        return
    }
}
```

- Реализовать интерфейс [IUsedeskOnSupportClickListener](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-gui/src/main/java/ru/usedesk/knowledgebase_gui/screens/IUsedeskOnSupportClickListener.kt) родителем, переопределив метод `onSupportClick()`, например:

```
override fun onSupportClick() {
    supportFragmentManager().beginTransaction()
        .replace(R.id.container, UsedeskChatScreen().newInstance())
        .commit()
}
```

<a name="knowledge_base_sdk"></a>
### Использование без GUI

После установки конфигурации необходимо инициализировать Базу Знаний:

```
val usedeskKnowledgeBase = UsedeskSdk.initKnowledgeBase(context)
```

После этого можно получить объект класса в любом месте:

```
val usedeskKnowledgeBase = UsedeskKnowledgeBase.requireInstance()
```

Освободить объект:

```
UsedeskKnowledgeBaseSdk.release()
```

Попытка получить экземпляр без инициализации или после освобожения вызовет исключение.

<a name="gui_localization"></a>

### Локализация GUI

SDK поддерживает следующие языки:

- английский (по умолчанию),
- русский,
- испанский,
- португальский.

Помимо этого можно изменить существующий язык или добавить новый. Для этого необходимо скопировать
значения из
файла [strings_template.xml](https://github.com/usedesk/Android_SDK/blob/master/strings_template.xml "strings_template.xml")
, который находится в корне проекта, и добавить во все файлы strings.xml вашего проекта. После чего
можно подставить свои значения строковых ресурсов.
**Важно!** В случае изменения ссылок на строковые ресурсы при кастомизации приложения изменение
строковых ресурсов таким способом может не привеcти к желаемому результату.

<a name="last_versions"></a>

### Предыдущие версии

- [v3.9.0](https://github.com/usedesk/Android_SDK/tree/3ee34eaeeaa0668e94cf8dadf78afad6901b52c4)

# Android Usedesk SDK (v3.7.2)
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
- [Список изменений](#change_list)

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
        maven { url 'https://github.com/usedesk/Android_SDK_Maven/raw/master/' }
    }
}
```

Добавьте в dependencies `build.gradle` вашего модуля строки, для использования только SDK библиотек:
```
implementation 'ru.usedesk:chat-sdk:{last_version}'
implementation 'ru.usedesk:knowledgebase-sdk:{last_version}'
```

Или для использования готовых элементов интерфейса:
```
implementation 'ru.usedesk:chat-gui:{last_version}'
implementation 'ru.usedesk:knowledgebase-gui:{last_version}'
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
| urlChat \* | String | Адрес сервера Чата |
| urlOfflineForm \* | String | Адрес для отправки формы обратной связи. Стандартное значение `https://secure.usedesk.ru/` |
| urlToSendFile \* | String | Адрес для отправки файлов. Стандартное значение `https://secure.usedesk.ru/uapi/v1/` |
| companyId \* | String | Идентификатор компании |
| channelId \* | String | Идентификатор канала (добавлен в **v3.1.6**) |
| clientSignature | String? | Сигнатура, позволяющая однозначно идентифицировать клиента в системе (удалён в **v3.5.1**)|
| clientToken | String? | Токен, позволяющий однозначно идентифицировать клиента в системе (добавлен в **v3.5.1**). Указав null библиотека самостоятельно воспользуется сохранённым токеном на устройстве, использованным ранее с такими же полями `clientEmail`, `clientPhoneNumber`, `clientName ` в конфигурации. Для первого входа указывается null, для последующих - полученный с сервера токен |
| clientEmail | String? | Почта клиента |
| clientName | String? | Имя клиента |
| clientNote | String? | Заметка о клиенте |
| clientPhoneNumber | Long? | Телефонный номер клиента |
| clientAdditionalId | Long? | Дополнительный идентификатор клиента |
| clientInitMessage | String? | Сообщение, автоматически отправляемое от клиента при открытии чата |

\* - обязательный параметр

Для кастомизации локальных уведомлений нужно создать 2 собственных класса:
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

После чего указать SDK использовать нестандартную фабрику:

```
UsedeskChatSdk.setNotificationsServiceFactory(CustomNotificationsServiceFactory())
```

<a name="chat_gui"></a>
### Использование с GUI

Для использования готового пользовательского интерфейса чата воспользуйтесь [UsedeskChatScreen](https://github.com/usedesk/Android_SDK/tree/master/chat-gui/src/main/java/ru/usedesk/chat_gui/chat/UsedeskChatScreen.kt), например:

```
supportFragmentManager.beginTransaction()
        .replace(R.id.container, UsedeskChatScreen.newInstance())
        .commit()
```

Метод `newInstance` может принять:
- Параметр типа `String` с именем агента. В случае если такой параметр задан, все имена агентов в чате будут заменены на значение параметра.
- Параметр типа `Collection<String>` со списком расширений файлов, помечаемых как опасные (метод `onFileClick` родителя вызывается в любом случае).
- Параметр типа `UsedeskChatConfiguration` с конфигурацией чата. В случае если такой параметр задан, `UsedeskChatScreen` берёт на себя обязанность вызова метода `UsedeskChatSdk.setConfiguration` даже после пересоздания.

Для полноценной работы фрагмента необходимо:
- Передавать события `onBackPressed`, вызывая аналогичный метод у фрагмента, который вернёт `true` если событие было обработано, либо `false` если нет, например:

```
override fun onBackPressed() {
    val fragment = getCurrentFragment()
    if (fragment is IUsedeskOnBackPressedListener && fragment.onBackPressed()) {
          return
    }
}
```

- Расширить интерфейс [IUsedeskOnFileClickListener](https://github.com/usedesk/Android_SDK/tree/master/chat-gui/src/main/java/ru/usedesk/chat_gui/IUsedeskOnFileClickListener.kt) родителем, переопределив метод `onFileClick(usedeskFile: UsedeskFile)`, например:

```
override fun onFileClick(usedeskFile: UsedeskFile ) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.container, UsedeskShowFileScreen.newInstance(usedeskFile))
        .commit()
}
```

- Вызывать метод `clear()` перед выходом из фрагмента, для освобождения ресурсов изображений и остановки загрузок, например:

```
fun onBackPressed() {
    val fragmentManager = activity.getSupportFragmentManager()
    if (fragmentManager.backStackEntryCount > 1) {
        val fragment = fragmentManager.fragments[0]
        if (fragment is UsedeskChatScreen) {
            fragment.clear()
        }
        fragmentManager.popBackStack()
    } else {
        activity.finish()
    }
}
```

<a name="chat_gui_files"></a>
Начиная с **v3.0.10** для корректной работы прикрепления фото с камеры необходимо добавить в файл `AndroidManifest.xml` следующие строки:

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
| onConnectedState | Boolean | Состояние подключения к серверу (true - подключено, false - разорвано) |
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
| onConnectedStateObservable | Observable&lt;Boolean&gt; | Состояние подключения к серверу (true - подключено, false - разорвано) |
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

Для использования готового пользовательского интерфейса воспользуйтесь [UsedeskKnowledgeBaseFragment](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-gui/src/main/java/ru/usedesk/knowledgebase_gui/screens/main/UsedeskKnowledgeBaseScreen.kt), например:

```
supportFragmentManager().beginTransaction()
        .replace(R.id.container, UsedeskKnowledgeBaseFragment.newInstance())
        .commit()
```

Для полноценной работы фрагмента необходимо:
- Передавать события `onBackPressed`, вызывая аналогичный метод у фрагмента, который вернёт `true` если событие было обработано, либо `false` если нет, например:
```
override fun onBackPressed() {
    val fragment = getCurrentFragment()
    if (fragment is IUsedeskOnBackPressedListener && fragment.onBackPressed()) {
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

Начиная с **v3.1.6** SDK поддерживает следующие языки:
- английский (по умолчанию),
- русский,
- испанский,
- португальский.

Помимо этого можно изменить существующий язык или добавить новый. Для этого необходимо скопировать значения из файла [strings_template.xml](https://github.com/usedesk/Android_SDK/blob/master/strings_template.xml "strings_template.xml"), который находится в корне проекта, и добавить во все файлы strings.xml вашего проекта. После чего можно подставить свои значения строковых ресурсов.
**Важно!** В случае изменения ссылок на строковые ресурсы при кастомизации приложения изменение строковых ресурсов таким способом может не привеcти к желаемому результату.

<a name="change_list"></a>
### Список изменений
- v2.1.4
  - Исправлено прикрепление изображений/файлов
  - Исправлена ошибка инициализации чата без сообщений
  - Исправлено отсутствие уведомлений
  - Добавлена валидация значений конфигурации чата
- v2.1.6
  - Исправлена ошибка сбрасывания истории чата
- v2.1.8
  - Исправлена ошибка при которой первое сообщение пользователя было неопознанным
- v2.1.11
  - Исправлено падение сервиса при отсутствии телефона или доп id клиента в конфигурации
  - Исправлено отображение текста и изображения в сообщении
  - Исправлено отображение текста кнопок в сообщении, если они должны отображаться
  - Исправлено падение в семпле при переполнении поля Client Additional ID
- v2.1.12
  - Исправлена ошибка, при которой не отображался прикреплённый к сообщению файл, отправленный из интерфейса тикета
- v2.1.13
  - Метод отправки файлов заменён на новый с ограничением в 150мб
- v2.1.17
  - Исправлена работа OkHttpClient, Glide на версиях API 19-21
  - Исправлено отображение текста в БЗ на версиях API 19-25
  - Изменены некоторые иконки чата
  - Изменена кастомизация фона сообщений
  - Изменены кнопки в сообщении оператора
  - Исправлена работа атбирутов кастомизации в теме
  - Исправлена обработка невалидных кнопок в сообщениях чата
- v3.0.0
  - Библиотека теперь полностью на Kotlin
  - Полностью переделан дизайн и его кастомизация
  - Переделаны слушатели событий, теперь их может быть несколько
  - Все внутренние классы библиотеки скрыты
  - Доработана работа с сервером, устранены возможные ошибки
  - Добавлены параметры адресов в конфигурацию
- v3.0.2
  - Изменена конфигурация чата, добавлен параметр clientSignature, остальные параметры клиента стали необязательными
- v3.0.5
  - Исправлена работа окна превью файла:
    - Исправлен сброс ссылки при ошибке загрузки изображения
    - Исправлена работа интерфейса после переворота устройства
  - Добавлен метод очистки загрузок изображений чата
  - Исправлено задвоение сообщений при разрыве соединения в чате
  - Прикрепление файлов в чате теперь дополняет ранее прикреплённые файлы, с камеры также можно прикрепить более 1го файла
- v3.0.7
  - Добавлена возможность отключения кнопки "Чат" в Базе Знаний
  - Добавлена возможность отключения оценки в статьях Базы знаний
  - Теперь вместе с оценкой чата отправляется id сообщения, в котором поставилась оценка
- v3.0.8
  - Добавлены статусы отправки сообщений
  - Исправлен краш при прерывании асинхронных операций (например отправка сообщения с изображением)
- v3.0.10
  - Исправлена ошибка прикрепления фото с камеры на версиях API 24 и выше
  - Исправлен таймаут отправки файлов
  - Отправляемые файлы сразу отображаются в чате
  - Максимальный размер отправляемых файлов ограничен в 100мб
  - **Важно!** В README.md [дополнен раздел](#chat_gui_files) "Чат -> Использование с GUI"
- v3.1.6
  - Переработана вкладка Формы Обратной Связи, добавлены аргументы и стили кастомизации для новых элементов и удалены старые
  - Изменена логика отображения внутренних вкладок в окне Чата без затрагивания кастомизации
  - Исправлена ошибка скрытия аватарки агента, теперь параметр visibility в стиле обрабатывается корректно
  - Добавлены Английский, Испанский и Португальский языки, а также шаблон строковых ресурсов
  - В README.md [добавлен раздел](#gui_localization) "Локализация"
  - Исправлена инициализации чата в случае отсутствия сообщений
  - **Важно!** В конфигурацию чата добавлен **обязательный** параметр `channelId`
- v3.1.8
  - Исправлена ошибка NullSafeMutableLiveData
  - Добавлена валидация на companyId и channelId в конфигурации Чата
- v3.1.9
  - Добавлен парсинг изображений из текста сообщений в Чате
  - Исправлено отображение расширений файлов в Чате
- v3.2.0
  - Добавлен параметр `rejectedFileExtensions` в метод `UsedeskChatScreen.newInstance()` для фильтрации опасных файлов
  - Подправлена вёрстка `usedesk_item_chat_message_file`, добавлены параметры в стиль `Usedesk.Chat.Message.File.Size.Text`
  - **Важно!** Обновлён файл `strings_template.xml`
- v3.2.1
  - Исправлена ошибка при вызове метода `connect()` после `disconnect()`
  - Добавлен [файл конфигурации ProGuard](https://github.com/usedesk/Android_SDK/blob/master/app/proguard-rules.pro "proguard-rules.pro") для примера обфускации библиотеки
- v3.3.1
  - **Важно!** Добавлены аргументы кастомизации для заднего фона вкладок
  - Исправлено сохранение состояний фрагментов при пересоздании активити
  - Добавлен параметр конфигурации в `UsedeskChatScreen`
- v3.4.1
  - **Важно!** Изменена кастомизация:
    - Некоторые атрибуты удалены, а их функционал перенесён в стили
    - От стиля `Usedesk.Chat.Screen` отделены некоторые атрибуты и перенесены в отдельные стили:
      - `Usedesk.Chat.Screen.Loading.Page`
      - `Usedesk.Chat.Screen.Messages.Page`
      - `Usedesk.Chat.Screen.Offline.Form.Page`
      - `Usedesk.Chat.Screen.Offline.Form.Selector.Page`
    - Добавлена кастомизация для всех недостающих элементов (Layout, Toolbar, View)
  - Исправлена ошибка сброса оценки в чате
  - Исправлена ошибка задваивания сообщений агента в чате после сворачивания
- v3.4.2
  - Исправлена обработка ответов от сервера по сокету
- v3.5.1
  - Обновлён клиент socket.io для работы с версиями 3.x/4.x
  - В [конфигурации чата](#chat_configuration) параметр `clientSignature` заменён на `clientToken` с
    новой логикой работы.
- v3.6.0
  - Добавлена фильтрация событий отображения ФОС по статусу тикета
  - Обновлены методы прикрепления файлов
- v3.7.2
  - Добавлены статусы сообщений (черновик, не отправлено, отправлено) с возможностью заменить
    дефолтное хранилище сообщений
  - Добавлена кнопка скролла вниз чата
  - Исправлен краш приложения на версии API 19
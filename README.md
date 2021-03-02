# Android Usedesk SDK (v3.0.5)
- [Подключение к проекту](#preparation)
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

Минимальная версия SDK - **19**

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
| urlChat | String | Адрес сервера Чата |
| urlOfflineForm | String | Адрес для отправки формы обратной связи |
| urlToSendFile | String | Адрес для отправки файлов |
| companyId | String | Идентификатор компании |
| clientSignature | String? | Сигнатура, позволяющая однозначно идентифицировать клиента в системе |
| clientEmail | String? | Почта клиента |
| clientName | String? | Имя клиента |
| clientPhoneNumber | Long? | Телефонный номер клиента |
| clientAdditionalId | Long? | Дополнительный идентификатор клиента |
| clientInitMessage | String? | Сообщение, автоматически отправляемое от клиента при открытии чата |

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
        .replace(R.id.container, UsedeskChatFragment.newInstance())
        .commit()
```

Метод `newInstance` может принять параметр типа `String` с именем агента. В случае если такой параметр задан, все имена агентов в чате будут заменены на значение параметра.

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
| onMessageReceived | UsedeskMessage | Новое сообщение |
| onMessagesReceived | List&lt;UsedeskMessage&gt; | Список сообщений из в чата на момент подключения |
| onMessageUpdated | UsedeskMessage | Обновление уже полученного сообщения |
| onFeedbackReceived | - | Отзыв доставлен |
| onOfflineFormExpected | UsedeskChatConfiguration | Ожидается оффлайн форма |
| onException | UsedeskException | Возникшее исключение |

[IUsedeskActionListenerRx](https://github.com/usedesk/Android_SDK/tree/master/chat-sdk/src/main/java/ru/usedesk/chat_sdk/entity/IUsedeskActionListenerRx.kt) - класс для прослушивания событий чата:

| Метод | Параметры | Описание события |
|---------|---------------|-----------------------|
| onConnectedStateObservable | Observable&lt;Boolean&gt; | Состояние подключения к серверу (true - подключено, false - разорвано) |
| onMessageObservable | Observable&lt;UsedeskMessage&gt; | Обновлённое состояние уже полученного сообщения |
| onNewMessageObservable | Observable&lt;UsedeskMessage&gt; | Отзыв доставлен |
| onMessagesObservable | Observable&lt;List&lt;UsedeskMessage&gt;&gt; | Ожидается оффлайн форма |
| onMessageUpdateObservable | Observable&lt;UsedeskMessage&gt; | Соединение разорвано |
| onOfflineFormExpectedObservable | Observable&lt;UsedeskChatConfiguration&gt; | Возникшее исключение |
| onFeedbackObservable | Observable&lt;UsedeskEvent&lt;Any?&gt;&gt; | Возникшее исключение |
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
| accountId | String | Идентификатор Базы Знаний в системе |
| token | String | Токен доступа к API |

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
  - Исправлено падение в семпле при переполнении поля Client Addinional ID
- v2.1.12
  - Исправлена ошибка, при которой не отображался прикреплённый к сообщению файл, отправленный из интерфейса тикета
- v2.1.13
  - Метод отправки файлов заменён на новый с ограничением в 150мб
- v3.0.0
  - Библиотека теперь полностью на Kotlin
  - Полностью переделан дизайн и его кастомизация
  - Переделаны слушатели событий, теперь их может быть несколько
  - Все внутренние классы быблиотеки скрыты
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
# Android Usedesk SDK (v2.0.2)
- [Требования к API](#requires)
- [Подключение к проекту](#implementation)
- [Основные этапы работы/взаимодействия с библиотекой](#base)
  - [1. Chat SDK](#base_chat_sdk)
    - [Локальные уведомления](#base_chat_sdk_notifications)
  - [2. Chat GUI](#base_chat_gui)
  - [3. Подключение к KnowledgeBase SDK](#base_knowledge_base_sdk)
  - [4. KnowledgeBase GUI](#base_knowledge_base_gui)
- [Кастомизация готовых пользовательских интерфейсов](#custom_view)

<a name="requires"></a>
## Требования к проекту

- compileSdkVersion = **28**
- buildToolsVersion = **"28.0.3"**
- minSdkVersion = **19**
- targetSdkVersion = **28**

При использовании методов-обёрток RxJava:

    implementation 'io.reactivex.rxjava2:rxjava:2.2.10'
    implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'

<a name="implementation"></a>
## Подключение к проекту

**[Chat SDK](https://github.com/usedesk/Android_SDK/tree/master/chat-sdk/)** - библиотека для работы с чатом.

**[Chat GUI](https://github.com/usedesk/Android_SDK/tree/master/chat-gui/)** - библиотека для встраивания готовых элементов интерфейса чата (включает в себя **Chat SDK**).

**[KnowledgeBase SDK](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-sdk/)** - библиотека для работы с Базой Знаний.

**[KnowledgeBase GUI](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-gui/)** - библиотека для встраивания готовых элементов интерфейса чата (включает в себя **KnowledgeBase SDK**).

**[Usedesk Sample App](https://github.com/usedesk/Android_SDK/tree/master/app/)** - пример использования библиотеки.

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

<a name="base"></a>
## Основные этапы работы/взаимодействия с библиотекой

<a name="base_chat_sdk"></a>
### 1. Chat SDK

Для работы с SDK чата необходимо сначала задать конфигурацию:

    UsedeskChatSdk.setConfiguration(UsedeskChatConfiguration usedeskChatConfiguration);

[**UsedeskChatConfiguration**](https://github.com/usedesk/Android_SDK/tree/master/chat-sdk/src/main/java/ru/usedesk/chat_sdk/external/entity/UsedeskChatConfiguration.java) - конфигурация чата:

| Переменная            | Тип                   | Описание                    |
|-----------------------|-----------------------|-----------------------------|
| companyId  | String  | Идентификатор компании |
| clientEmail  | String  | Почта клиента |
| apiUrl  | String  | Адрес API сервера |
| offlineFormUrl  | String  | Адрес для отправки оффлайн формы |
| clientName  | String  | Имя клиента |
| clientPhone  | Long  | Телефонный номер клиента |
| clientAdditionalId  | Long  | Дополнительный идентификатор клиента |

После этого нужно проинициализировать чат:

    IUsedeskChat usedeskChat = UsedeskChatSdk.init(Context appContext, IUsedeskActionListener actionListener);

[IUsedeskActionListener](https://github.com/usedesk/Android_SDK/tree/master/chat-sdk/src/main/java/ru/usedesk/chat_sdk/external/entity/IUsedeskActionListener.java) - класс для прослушивания событий чата:

| Метод | Параметры | Описание события |
|--------------------------|---------------|--------------------------------------------------|
| onConnected | - | Успешное подключение к серверу |
| onMessageReceived | UsedeskMessage | Новое сообщение |
| onMessagesReceived | List&lt;UsedeskMessage&gt; | Список сообщений из в чата на момент подключения |
| onServiceMessageReceived | UsedeskMessage | Сервисное сообщение |
| onOfflineFormExpected | UsedeskChatConfiguration | Ожидается оффлайн форма |
| onDisconnected | - | Соединение разорвано |
| onException | UsedeskException | Возникшее исключение |

После инициализации можно получить экземпляр `IUsedeskChat` вызвав:

    IUsedeskChat usedeskChat = UsedeskChatSdk.getInstance();

После окончания работы с чатом для освобождения ресурсов необходимо вызвать метод:

    UsedeskChatSdk.release();

[**IUsedeskChat**](https://github.com/usedesk/Android_SDK/tree/master/chat-sdk/src/main/java/ru/usedesk/chat_sdk/external/IUsedeskChat.java) - основной класс для взаимодействия с чатом.

| Метод   | Параметры     | Описание события                                         |
|--------------------------|------------------|------------------|
| connect  | -                        | Подключение к серверу |
| disconnect  | -                        | Отключение от сервера |
| send  | String | Отправить текстовое сообщение |
| send  | UsedeskFileInfo | Отправить файл |
| send  | List&lt;UsedeskFileInfo&gt; | Отправить список файлов  |
| send  | UsedeskFeedback | Отправить отзыв |
| send  | UsedeskOfflineForm | Отправить оффлайн-форму |
| send  | UsedeskMessageButton | Отправить готовый ответ на сообщение оператора |

 - [UsedeskFileInfo](https://github.com/usedesk/Android_SDK/tree/master/chat-sdk/src/main/java/ru/usedesk/chat_sdk/external/entity/UsedeskFileInfo.java):

| Переменная            | Тип                   | Описание                    |
|-----------------------|-----------------------|-----------------------------|
| uri  | Uri  | Ссылка на файл |
| type  | UsedeskFileInfo.Type  | MIME-тип файла |

- [UsedeskFeedback](https://github.com/usedesk/Android_SDK/tree/master/chat-sdk/src/main/java/ru/usedesk/chat_sdk/external/entity/UsedeskFeedback.java) - может иметь значение `LIKE` или `DISLIKE`.

- [UsedeskOfflineForm](https://github.com/usedesk/Android_SDK/tree/master/chat-sdk/src/main/java/ru/usedesk/chat_sdk/external/entity/UsedeskOfflineForm.java):

| Переменная            | Тип                   | Описание                    |
|-----------------------|-----------------------|-----------------------------|
| companyId  | String  | Идентификатор компании |
| name  | String  | Имя клиента |
| email  | String  | Email клиента |
| message  | String  | Сообщение клиента |

- [UsedeskMessageButton](https://github.com/usedesk/Android_SDK/tree/master/chat-sdk/src/main/java/ru/usedesk/chat_sdk/external/entity/UsedeskMessageButton.java) - готовый ответ клиента на сообщение оператора.

Каждый метод может выбросить [UsedeskException](https://github.com/usedesk/Android_SDK/tree/master/common-sdk/src/main/java/ru/usedesk/common_sdk/external/entity/exceptions/UsedeskException.java):

- [UsedeskSocketException](https://github.com/usedesk/Android_SDK/tree/master/common-sdk/src/main/java/ru/usedesk/common_sdk/external/entity/exceptions/UsedeskSocketException.java) - исключение, обработанное во время работы чата. Метод getError возвращает следующие значения:

| Тип ошибки                | Описание                         |
|---------------------------|----------------------------------|
| INTERNAL_SERVER_ERROR     | Внутренняя ошибка на сервере    |
| BAD_REQUEST_ERROR         | Неправильный запрос   |
| FORBIDDEN_ERROR           | Доступ запрещён    |
| IO_ERROR                  | Ошибка исполнения запроса        |
| JSON_ERROR                | Ошибка обработки JSON            |
| DISCONNECTED              | Соединение разорвано             |
| UNKNOWN_FROM_SERVER_ERROR | Необработанная ошибка от сервера |
| UNKNOWN_ERROR             | Необработанная ошибка            |

-  [UsedeskHttpException](https://github.com/usedesk/Android_SDK/tree/master/common-sdk/src/main/java/ru/usedesk/common_sdk/external/entity/exceptions/UsedeskHttpException.java) - Исключение, обработанное во время работы базы знаний. Метод getError возвращает следующие значения:

| Тип ошибки    | Описание                  |
|---------------|---------------------------|
| SERVER_ERROR  | Ошибка на сервере         |
| INVALID_TOKEN | Некорректный токен        |
| ACCESS_ERROR  | Ошибка доступа            |
| IO_ERROR      | Ошибка исполнения запроса |
| JSON_ERROR    | Ошибка обработки JSON     |
| UNKNOWN_ERROR | Необработанная ошибка     |

<a name="base_chat_sdk_notifications"></a>
#### Локальные уведомления
Для уведомления пользователей о новых сообщениях от оператора в чате можно использовать сервис локальных уведомлений. Для инициализации этого сервиса нужно создать 2 собственных класса:
- Унаследованный от [UsedeskSimpleNotificationsService](https://github.com/usedesk/Android_SDK/tree/master/chat-sdk/src/main/java/ru/usedesk/chat_sdk/external/service/notifications/view/UsedeskSimpleNotificationsService.java) (обычный сервис) или [UsedeskForegroundNotificationsService](https://github.com/usedesk/Android_SDK/tree/master/chat-sdk/src/main/java/ru/usedesk/chat_sdk/external/service/notifications/view/UsedeskForegroundNotificationsService.java) (foreground сервис). Где можно переопределить некоторые методы:

| Метод | Тип возвращаемого значения | Описание события |
|--------------------------|------------------|------------------|
| getContentPendingIntent | PendingIntent | Действие при нажатии на уведомление |
| getDeletePendingIntent | PendingIntent | Действие при удалении уведомления |
| getChannelId | String | Номер канала уведомления |
| getChannelTitle | String | Названия канала уведомления |
| createNotification | Notification | Создание уведомления |

- Унаследованный от [UsedeskNotificationsServiceFactory](https://github.com/usedesk/Android_SDK/tree/master/chat-sdk/src/main/java/ru/usedesk/chat_sdk/external/service/notifications/UsedeskNotificationsServiceFactory.java) для переопределения метода:

| Метод | Тип возвращаемого значения | Описание события |
|--------------------------|------------------|------------------|
| getServiceClass | Class<?> | Класс сервиса |

Для инициализации уведомлений необходимо вызвать следующий метод, где аргументом будет объект класса `UsedeskNotificationsServiceFactory`, например:

    UsedeskChatSdk.setNotificationsServiceFactory(new CustomNotificationsService.Factory());

Запуск сервиса:
```
UsedeskSdk.getUsedeskNotificationsServiceFactory()
                .startService(context);
```
Остановка сервиса:
```
UsedeskSdk.getUsedeskNotificationsServiceFactory()
                .stopService(context);
```

<a name="base_chat_gui"></a>
### 2. Chat GUI

Для использования готового пользовательского интерфейса чата воспользуйтесь [UsedeskChatFragment](https://github.com/usedesk/Android_SDK/tree/master/chat-gui/src/main/java/ru/usedesk/chat_gui/external/UsedeskChatFragment.java), например:

  ```
 getSupportFragmentManager().beginTransaction()
        .replace(R.id.container, UsedeskChatFragment.newInstance())
        .commit();
  ```

<a name="base_knowledge_base_sdk"></a>
### 3. KnowledgeBase SDK

Для работы с SDK Базы Знаний необходимо задать конфигурацию:

    UsedeskKnowledgeBaseSdk.setConfiguration(UsedeskKnowledgeBaseConfiguration knowledgeBaseConfiguration);

[UsedeskKnowledgeBaseConfiguration](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-sdk/src/main/java/ru/usedesk/knowledgebase_sdk/external/entity/UsedeskKnowledgeBaseConfiguration.java) - конфигурация Базы Знаний:

| Переменная | Тип    | Описание               |
|------------|--------|------------------------|
| accountId  | String | Идентификатор Базы Знаний в системе |
| token  | String | Токен доступа к API |

После установки конфигурации необходимо инициализировать Базу Знаний:

    UsedeskKnowledgeBase usedeskKnowledgeBase = UsedeskSdk.initKnowledgeBase(context);

После этого можно получить объект класса в любом месте:

    UsedeskKnowledgeBase usedeskKnowledgeBase = UsedeskKnowledgeBase.getUsedeskKnowledgeBase();

Освободить объект:

    UsedeskSdk.releaseUsedeskKnowledgeBase();

Попытка получить объект без инициализации или после освобожения вызовет исключение.

[IUsedeskKnowledgeBase](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-sdk/src/main/java/ru/usedesk/knowledgebase_sdk/external/UsedeskKnowledgeBaseSdk.java) - основной класс для работы с Базой Знаний:

| Метод   | Возвращаемый тип | Параметры | Описание события                                         |
|--------------------------|--------------|----|------------------|
| getSections  | List&lt;UsedeskSection&gt; | -| Получить список секций |
| getArticle  | UsedeskArticleBody | long articleId | Получить статью |
| getArticles  | List&lt;UsedeskArticleBody&gt; | String searchQuery | Получить список статей по строке поиска |
| getArticles  | List&lt;UsedeskArticleBody&gt; | UsedeskSearchQuery searchQuery | Получить список статей по фильтру |
| getCategories  | List&lt;UsedeskCategory&gt; | long sectionId | Получить список категорий  |
| getArticles  | List&lt;UsedeskArticleInfo&gt; | long categoryId | Получить заголовки статей |
| addViews  | - | long articleId | Добавить просмотр статье |

Каждый метод может выбросить [UsedeskException](https://github.com/usedesk/Android_SDK/tree/master/common-sdk/src/main/java/ru/usedesk/common_sdk/external/entity/exceptions/UsedeskException.java).

 - [UsedeskSection](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-sdk/src/main/java/ru/usedesk/knowledgebase_sdk/external/entity/UsedeskCategory.java) - секция Базы Знаний:
 
| Переменная | Тип        | Описание                     |
|------------|------------|------------------------------|
| id         | long       | Идентификатор секции         |
| title      | String     | Название секции              |
| image      | String     | Ссылка на изображение секции |
| categories | UsedeskCategory[] | Категории                    |

 - [UsedeskCategory](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-sdk/src/main/java/ru/usedesk/knowledgebase_sdk/external/entity/UsedeskCategory.java) - категория Базы Знаний:
 
| Переменная | Тип           | Описание                       |
|------------|---------------|--------------------------------|
| id         | long          | Идентификатор категории        |
| title      | String        | Название категории             |
| articles   | UsedeskArticleInfo[] | Список с информацией о статьях |

 - [UsedeskArticleInfo](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-sdk/src/main/java/ru/usedesk/knowledgebase_sdk/external/entity/UsedeskArticleInfo.java) - заголовок статьи Базы Знаний:
 
| Переменная | Тип    | Описание              |
|------------|--------|-----------------------|
| id         | long   | Идентификатор статьи  |
| title      | String | Название статьи       |
| views      | int    | Количество просмотров |

 - [ArticleBody](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-sdk/src/main/java/ru/usedesk/knowledgebase_sdk/external/entity/UsedeskArticleBody.java) - текст статьи Базы Знаний:
 
| Переменная | Тип    | Описание                             |
|------------|--------|--------------------------------------|
| id         | long   | Идентификатор статьи                 |
| title      | String | Название статьи                      |
| text       | String | Текст статьи                         |
| categoryId | long   | Идентификатор родительской категории |
| views      | int    | Количество просмотров                |

<a name="base_knowledge_base_gui"></a>
### 4. KnowledgeBase GUI

Для использования готового пользовательского интерфейса воспользуйтесь [UsedeskKnowledgeBaseFragment](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-gui/src/main/java/ru/usedesk/knowledgebase_gui/external/UsedeskKnowledgeBaseFragment.java), например:

  ```
 getSupportFragmentManager().beginTransaction()
        .replace(R.id.container, UsedeskKnowledgeBaseFragment.newInstance())
        .commit();
  ```

При этом родитель должен:
1) Обеспечить вызов метода `onBackPressed` при нажатиях на кнопку "назад", для внутренней навигации по страницам. Метод вернёт `false` в случае, если был вызван на корневой странице фрагмента. Пример:
```
@Override
public void onBackPressed() {
    Fragment fragment = getCurrentFragment();

    if (fragment instanceof IUsedeskOnBackPressedListener && !((IUsedeskOnBackPressedListener) fragment).onBackPressed()) {
            return;
        }
    } else {
        super.onBackPressed();
    }
}
```
2) Обеспечить вызов метода `onSearchQuery(String searchQuery)` для отображения страницы с результатом поискового запроса.
```
private void onQuery(String query) {
    Fragment fragment = getCurrentFragment();
    if (fragment instanceof IUsedeskOnSearchQueryListener) {
        ((IUsedeskOnSearchQueryListener) fragment).onSearchQuery(query);
    }
}
```
3) Реализовать интерфейс [IUsedeskOnSupportClickListener](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-gui/src/main/java/ru/usedesk/knowledgebase_gui/external/IUsedeskOnSupportClickListener.java) для реагирования на нажатие на кнопку "Чат с поддержкой", например:
```
@Override
public void onSupportClick() {
    switchFragment(ChatFragment.newInstance());//Переход к фрагменту чата
}
```

<a name="custom_view"></a>
## Кастомизация готовых пользовательских интерфейсов

Для кастомизации интерфейса можно переопределить тему [Usedesk.Theme.Chat](https://github.com/usedesk/Android_SDK/tree/master/chat-gui/src/main/res/values/styles.xml) и [Usedesk.Theme.KnowledgeBase](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-gui/src/main/res/values/styles.xml), после чего указать её для использования:

  ```
UsedeskViewCustomizer.getInstance()
    .replaceId(ru.usedesk.chat_gui.R.style.Usedesk_Theme_Chat, R.style.Usedesk_Theme_Chat_Custom);
  ```
  
Так же можно переопределить любой слой из [ресурсов Чата](https://github.com/usedesk/Android_SDK/tree/master/chat-gui/src/main/res/layout/)  и [ресурсов Базы Знаний](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-gui/src/main/res/layout/):

  ```
UsedeskViewCustomizer.getInstance()
    .replaceId(ru.usedesk.chat_gui.R.layout.usedesk_fragment_chat, R.layout.custom__fragment_chat);
  ```

Главным критерием последнего способа является полное соответсвие идентификаторов элементов и их тип стандартному слою.
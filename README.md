# Android Usedesk SDK (v2.0.0)
- [Требования к API](#requires)
- [Подключение к проекту](#implementation)
- [Основные этапы работы/взаимодействия с библиотекой](#base)
  - [Настройка UsedeskChat](#base_setup)
  - [Методы взаимодействия с UsedeskChat](#base_manage)
- [База знаний](#knowledge_base)
  - [Настройка UsedeskKnowledgeBase](#knowledge_base_setup)
  - [Методы взаимодействия с UsedeskKnowledgeBase](#knowledge_base_manage)
- [UI базы знаний и чата](#ui)
- [Локальные уведомления](#local_notifications)

<a name="requires"></a>
## Требования к API

- compileSdkVersion = **28**
- buildToolsVersion = **"28.0.3"**
- minSdkVersion = **19**
- targetSdkVersion = **28**

<a name="implementation"></a>
## Подключение к проекту

**[Chat SDK](https://github.com/usedesk/Android_SDK/tree/master/chat-sdk)** - библиотека для работы с чатом.

**[Chat GUI](https://github.com/usedesk/Android_SDK/tree/master/chat-gui)** - библиотека для встраивания готовых элементов интерфейса чата (включает в себя **Chat SDK**).

**[KnowledgeBase SDK](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-sdk)** - библиотека для работы с Базой Знаний.

**[KnowledgeBase GUI](https://github.com/usedesk/Android_SDK/tree/master/knowledgebase-gui)** - библиотека для встраивания готовых элементов интерфейса чата (включает в себя **KnowledgeBase SDK**).

**[Usedesk Sample App](https://github.com/usedesk/Android_SDK/tree/master/app)** - пример использования библиотеки.

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

[**UsedeskChatConfiguration**](https://bitbucket.org/usedesk_mobile/android_sdk/src/separation_to_modules/chat-sdk/src/main/java/ru/usedesk/chat_sdk/external/entity/UsedeskChatConfiguration.java) - класс конфигурации чата:

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

[IUsedeskActionListener](https://bitbucket.org/usedesk_mobile/android_sdk/src/separation_to_modules/chat-sdk/src/main/java/ru/usedesk/chat_sdk/external/entity/IUsedeskActionListener.java) - класс для прослушивания событий чата:

| Метод                    | Параметры     | Описание события                                 |
|--------------------------|---------------|--------------------------------------------------|
| onConnected              | -             | Успешное подключение к серверу                   |
| onMessageReceived        | Message       | Новое сообщение                                  |
| onMessagesReceived       | List&lt;Message&gt; | Список сообщений из в чата на момент подключения |
| onServiceMessageReceived | Message       | Сервисное сообщение                              |
| onOfflineFormExpected    | -             | Ожидается оффлайн форма                          |
| onDisconnected           | -             | Соединение разорвано                             |
| onException                  | UsedeskException     | Возникшее исключение         |

После инициализации можно получить экземпляр `IUsedeskChat` вызвав:

    IUsedeskChat usedeskChat = UsedeskChatSdk.getInstance();

После окончания работы с чатом для освобождения ресурсов необходимо вызвать метод:

    UsedeskChatSdk.release();

[**IUsedeskChat**](https://bitbucket.org/usedesk_mobile/android_sdk/src/separation_to_modules/chat-sdk/src/main/java/ru/usedesk/chat_sdk/external/IUsedeskChat.java) - основной класс для взаимодействия с чатом.

| Метод   | Параметры     | Описание события                                         |
|--------------------------|------------------|------------------|
| connect  | -                        | Подключение к серверу |
| disconnect  | -                        | Отключение от сервера |
| send  | String textMessage | Отправить текстовое сообщение |
| send  | UsedeskFileInfo usedeskFileInfo | Отправить файл |
| send  | List&lt;UsedeskFileInfo&gt; usedeskFileInfoList | Отправить список файлов  |
| send  | UsedeskFeedback feedback | Отправить отзыв |
| send  | UsedeskOfflineForm offlineForm | Отправить оффлайн-форму |
| send  | UsedeskMessageButton messageButton | Отправить готовый ответ на сообщение оператора |

[UsedeskFileInfo](https://bitbucket.org/usedesk_mobile/android_sdk/src/separation_to_modules/chat-sdk/src/main/java/ru/usedesk/chat_sdk/external/entity/UsedeskFileInfo.java):

| Переменная            | Тип                   | Описание                    |
|-----------------------|-----------------------|-----------------------------|
| uri  | Uri  | Ссылка на файл |
| type  | UsedeskFileInfo.Type  | MIME-тип файла |

[UsedeskFeedback](https://bitbucket.org/usedesk_mobile/android_sdk/src/separation_to_modules/chat-sdk/src/main/java/ru/usedesk/chat_sdk/external/entity/UsedeskFeedback.java): - может иметь значение `LIKE` или `DISLIKE`.

[UsedeskOfflineForm](https://bitbucket.org/usedesk_mobile/android_sdk/src/separation_to_modules/chat-sdk/src/main/java/ru/usedesk/chat_sdk/external/entity/UsedeskOfflineForm.java):

| Переменная            | Тип                   | Описание                    |
|-----------------------|-----------------------|-----------------------------|
| companyId  | String  | Идентификатор компании |
| name  | String  | Имя клиента |
| email  | String  | Email клиента |
| message  | String  | Сообщение клиента |

[UsedeskMessageButton](https://bitbucket.org/usedesk_mobile/android_sdk/src/separation_to_modules/chat-sdk/src/main/java/ru/usedesk/chat_sdk/external/entity/UsedeskMessageButton.java) - готовый ответ клиента на сообщение оператора.

Каждый метод может выбросить [UsedeskException](https://github.com/usedesk/Android_SDK/blob/master/usedesk_sdk/src/main/java/ru/usedesk/sdk/external/entity/exceptions/UsedeskException.java):

- [UsedeskSocketException](https://github.com/usedesk/Android_SDK/blob/master/usedesk_sdk/src/main/java/ru/usedesk/sdk/external/entity/exceptions/UsedeskSocketException.java) - исключение, обработанное во время работы чата. Метод getError возвращает следующие значения:

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

 -  [UsedeskHttpException](https://github.com/usedesk/Android_SDK/blob/master/usedesk_sdk/src/main/java/ru/usedesk/sdk/external/entity/exceptions/UsedeskHttpException.java) - Исключение, обработанное во время работы базы знаний. Метод getError возвращает следующие значения:

| Тип ошибки    | Описание                  |
|---------------|---------------------------|
| SERVER_ERROR  | Ошибка на сервере         |
| INVALID_TOKEN | Некорректный токен        |
| ACCESS_ERROR  | Ошибка доступа            |
| IO_ERROR      | Ошибка исполнения запроса |
| JSON_ERROR    | Ошибка обработки JSON     |
| UNKNOWN_ERROR | Необработанная ошибка     |

<a name="chat_gui"></a>
### 2. Chat GUI

Для использования готового интерфейса чата воспользуйтесь [UsedeskChatFragment](https://github.com/usedesk/Android_SDK/blob/master/usedesk_sdk/src/main/java/ru/usedesk/sdk/external/entity/exceptions/UsedeskHttpException.java), например:

  ```
 getSupportFragmentManager().beginTransaction()
        .replace(R.id.container, UsedeskChatFragment.newInstance())
        .commit();
  ```

Для кастомизации интерфейса можно переопределить тему [Usedesk.Theme.Chat](https://bitbucket.org/usedesk_mobile/android_sdk/src/separation_to_modules/chat-gui/src/main/res/values/styles.xml), после чего указать её для использования:

  ```
IUsedeskViewCustomizer usedeskViewCustomizer = UsedeskViewCustomizer.getInstance();

usedeskViewCustomizer.replaceId(ru.usedesk.chat_gui.R.style.Usedesk_Theme_Chat, R.style.Usedesk_Theme_Chat_Custom);
  ```
  
Так же можно переопределить любой слой из [ресурсов](https://bitbucket.org/usedesk_mobile/android_sdk/src/separation_to_modules/chat-gui/src/main/res/layout/):

  ```
IUsedeskViewCustomizer usedeskViewCustomizer = UsedeskViewCustomizer.getInstance();

usedeskViewCustomizer.replaceId(ru.usedesk.knowledgebase_gui.R.layout.usedesk_item_category, R.layout.custom_item_category);

usedeskViewCustomizer.replaceId(ru.usedesk.knowledgebase_gui.R.layout.usedesk_item_section, R.layout.custom_item_section);

usedeskViewCustomizer.replaceId(ru.usedesk.knowledgebase_gui.R.layout.usedesk_item_article_info, R.layout.custom_item_article_info);
  ```

Главным критерием последнего способа является полное соответсвие идентификаторов элементов и их тип стандартному слою.

<a name="knowledge_base_sdk"></a>
### 3. KnowledgeBase SDK

[UsedeskKnowledgeBase](https://github.com/usedesk/Android_SDK/blob/master/usedesk_sdk/src/main/java/ru/usedesk/sdk/external/UsedeskKnowledgeBase.java) - класс работы с базой знаний.

Перед началом работы необходимо задать конфигурацию, вызвав метод setKnowledgeBaseConfiguration со следующими параметрами:

| Переменная | Тип    | Описание               |
|------------|--------|------------------------|
| accountId  | String | Идентификатор Базы Знаний в системе |
| token  | String | Токен доступа к API |

Пример:

    UsedeskSdk.setKnowledgeBaseConfiguration(new KnowledgeBaseConfiguration(accountId, token)

После этого его неоходимо проинициализировать:

    UsedeskKnowledgeBase usedeskKnowledgeBase = UsedeskSdk.initKnowledgeBase(context);

После этого можно получить объект класса в любом месте:

    UsedeskKnowledgeBase usedeskKnowledgeBase = UsedeskKnowledgeBase.getUsedeskKnowledgeBase();

Освободить объект:

    UsedeskSdk.releaseUsedeskKnowledgeBase();

Попытка получить объект без инициализации или после освобожения вызовет исключение.

Для работы напрямую с API необходимо подключить библиотеку:

    implementation 'io.reactivex.rxjava2:rxjava:2.2.10'
    implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'

<a name="knowledge_base_manage"></a>

### 2. Методы взаимодействия с UsedeskKnowledgeBase

- Получить список секций:
  ```
  @NonNull
  public Single<List<Section>> getSectionsSingle();
  ```
  
-- [Section](https://github.com/usedesk/Android_SDK/blob/master/usedesk_sdk/src/main/java/ru/usedesk/sdk/external/entity/knowledgebase/Section.java) - класс секций Базы Знаний, со следующими полями:
 
| Переменная | Тип        | Описание                     |
|------------|------------|------------------------------|
| id         | long       | Идентификатор секции         |
| title      | String     | Название секции              |
| image      | String     | Ссылка на изображение секции |
| categories | Category[] | Категории                    |

-- [Category](https://github.com/usedesk/Android_SDK/blob/master/usedesk_sdk/src/main/java/ru/usedesk/sdk/external/entity/knowledgebase/Category.java) - класс категории Базы Знаний, со следующими полями:
 
| Переменная | Тип           | Описание                       |
|------------|---------------|--------------------------------|
| id         | long          | Идентификатор категории        |
| title      | String        | Название категории             |
| articles   | ArticleInfo[] | Список с информацией о статьях |

-- [ArticleInfo](https://github.com/usedesk/Android_SDK/blob/master/usedesk_sdk/src/main/java/ru/usedesk/sdk/external/entity/knowledgebase/ArticleInfo.java) - класс категории Базы Знаний, со следующими полями:
 
| Переменная | Тип    | Описание              |
|------------|--------|-----------------------|
| id         | long   | Идентификатор статьи  |
| title      | String | Название статьи       |
| views      | int    | Количество просмотров |

- Получить статью целиком по её идентификатору:
  ```
  @NonNull
  public Single<ArticleBody> getArticleSingle(long articleId);
  ```

-- [ArticleBody](https://github.com/usedesk/Android_SDK/blob/master/usedesk_sdk/src/main/java/ru/usedesk/sdk/external/entity/knowledgebase/ArticleBody.java) - класс категории Базы Знаний, со следующими полями:
 
| Переменная | Тип    | Описание                             |
|------------|--------|--------------------------------------|
| id         | long   | Идентификатор статьи                 |
| title      | String | Название статьи                      |
| text       | String | Текст статьи                         |
| categoryId | long   | Идентификатор родительской категории |
| views      | int    | Количество просмотров                |

- Получить список статей по поисковому запросу:
  ```
  @NonNull
  public Single<List<ArticleBody>> getArticlesSingle(@NonNull String searchQuery);
  ```

- Получить список статей по поисковому запросу, с возможностью кастомизации параметров:
  ```
  @NonNull
  public Single<List<ArticleBody>> getArticlesSingle(@NonNull SearchQuery searchQuery);
  ```

- Получить список категорий в секции по её идентификатору:
  ```
  @NonNull
  public Single<List<Category>> getCategoriesSingle(long sectionId);
  ```

- Получить список статей (не полная статья) в категории по её идентификатору:
  ```
  @NonNull
  public Single<List<ArticleInfo>> getArticlesSingle(long categoryId);
  ```

<a name="ui"></a>

## UI базы знаний и чата

В UsedeskSDK есть готовые фрагменты, реализующие функционал базы знаний `KnowledgeBaseFragment` и чата `ChatFragment`. Родитель, использующий этот фрагмент, должен:

- Реализовать интерфейс `IOnUsedeskSupportClickListener`, для отслеживания событий нажатия на кнопку и реализации перехода к чату. Пример из семпла:
```
@Override
    public void onSupportClick() {
        //Обновление состояния тулбара
        toolbarHelper.update(ToolbarHelper.State.HOME);
        //Переход к фрагменту чата
        switchFragment(ChatFragment.newInstance());
    }
```

- Предоставить события ввода поискового запроса в базу знаний (например из верхней панели). Пример из семпла:
```
private void onQuery(String query) {
    Fragment fragment = getCurrentFragment();
    if (fragment instanceof IUsedeskOnSearchQueryListener) {
        ((IUsedeskOnSearchQueryListener) fragment).onSearchQuery(query);
    }
}
```

- Предоставлять события `onBackPressed` для `KnowledgeBaseFragment`, т.к. он имеет свой стек фрагментов(вернёт `false` если стек пустой), и переключать фрагменты чата и базы знаний самостоятельно, пример из семпла:

```
@Override
public void onBackPressed() {
    Fragment fragment = getCurrentFragment();

    if (fragment instanceof KnowledgeBaseFragment) {
        if (!((KnowledgeBaseFragment) fragment).onBackPressed()) {
        //Если текущий фрагмент - база знаний, и возвраты в нём закончились, обработаем самостоятельно и перейдём на домашний фрагмент
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        }
    } else if (fragment instanceof ChatFragment || fragment instanceof InfoFragment) {
        //Если текущий фрагмент - чат или инфо(из семпла), переключим на базу знаний
        bottomNavigationView.setSelectedItemId(R.id.navigation_base);
    } else {
        //Выйдем из приложения
        super.onBackPressed();
    }
}
```

<a name="local_notifications"></a>

## Локальные уведомления

Для уведомления пользователей о новых сообщениях от оператора в чате можно использовать сервис локальных уведомлений. Для инициализации этого сервиса нужно создать 2 собственных класса:
- Унаследованный от `UsedeskSimpleNotificationsService` (обычный сервис) или `UsedeskForegroundNotificationsService`(foreground сервис). Где можно переопределить некоторые методы:
```
  @Nullable
  @Override
  protected PendingIntent getContentPendingIntent();//Действие при нажатии на уведомление
  @Nullable
  @Override
  protected PendingIntent getDeletePendingIntent();//Действие при удалении уведомления
  @NonNull
  protected String getChannelId();//Номер канала уведомления
  @NonNull
  protected String getChannelTitle();//Названия канала уведомления
  @NonNull
  protected Notification createNotification(@NonNull NotificationsModel model);//Создание уведомления
```
- Унаследованный от `UsedeskNotificationsServiceFactory` для переопределения метода
```
    @NonNull
    @Override
    protected Class<?> getServiceClass();
```

Для инициализации уведомлений необходимо вызвать следующий метод, где аргументов будет объект класса `UsedeskNotificationsServiceFactory`, пример из семпла:
```
UsedeskSdk.setUsedeskNotificationsServiceFactory(foregroundService
                ? new CustomForegroundNotificationsService.Factory()
                : new CustomSimpleNotificationsService.Factory());
```

В случае использования фрагментов из SDK, действий описанных выше буде достаточно, чтобы уведомления начали работать. В ином случае для запуска и остановки сервиса необходимо вызвать следующие методы:
```
UsedeskSdk.getUsedeskNotificationsServiceFactory()
                .startService(context, usedeskConfiguration);

UsedeskSdk.getUsedeskNotificationsServiceFactory()
                .stopService(context);
```

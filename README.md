# Android Usedesk SDK
- [Требования к API](#requires)
- [Подключение к проекту](#implementation)
 - [Импорт модуля в проект](#implementation_import)
 - [Через Maven репозиторий](#implementation_maven)
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

**[Usedesk SDK](https://github.com/usedesk/Android_SDK/tree/master/usedesk_sdk)** - библиотека Usedesk.

**[Usedesk Sample App](https://github.com/usedesk/Android_SDK/tree/master/app)** - пример использования библиотеки с использованием готовых фрагментов из sdk.


<a name="implementation_import"></a>

### Импорт модуля в проект
Перенесите модуль в проект и добавьте в `build.gradle` вашего модуля строку:
```
dependencies {
    ...
    implementation project(':usedesk_sdk')
    ...
}
```

<a name="implementation_maven"></a>

### Через Maven репозиторий
Добавьте в `build.gradle` вашего проекта строку:
```
allprojects {
    repositories {
        ...
        maven { url 'https://github.com/usedesk/Android_SDK_Maven/raw/master/' }
    }
}
```

Добавьте в `build.gradle` вашего модуля строку:
```
dependencies {
    ...
    implementation 'ru.usedesk:usedesk_sdk:1.0.2'
    ...
}
```

<a name="base"></a>

## Основные этапы работы/взаимодействия с библиотекой

[UsedeskSdk](https://github.com/usedesk/Android_SDK/blob/master/usedesk_sdk/src/main/java/ru/usedesk/sdk/external/UsedeskSdk.java) - главный класс взаимодействия и настройки SDK. Позволяет проинициализировать, получить или освободить другие классы, необходимые для работы.

<a name="base_setup"></a>

### 1. Настройка UsedeskChat


[UsedeskChat](https://github.com/usedesk/Android_SDK/blob/master/usedesk_sdk/src/main/java/ru/usedesk/sdk/external/UsedeskChat.java) - класс работы с чатом.

Перед началом работы его неоходимо проинициализировать, вызвав метод initChat со следующими параметрами:

| Переменная            | Тип                   | Описание                    |
|-----------------------|-----------------------|-----------------------------|
| appContext            | Context               | Контекст приложения         |
| usedeskConfiguration  | UsedeskConfiguration  | Конфигурация чата           |
| usedeskActionListener | UsedeskActionListener | Слушатель возможных событий |

Пример:

    UsedeskChat usedeskChat = UsedeskSdk.initChat(context, usedeskConfiguration, usedeskActionListener);

После этого можно получить объект класса в любом месте:

    UsedeskChat usedeskChat = UsedeskSdk.getChat();

Освободить объект:

    UsedeskSdk.releaseChat();

Попытка получить объект без инициализации или после освобожения вызовет исключение (RuntimeException).

- [UsedeskConfiguration](https://github.com/usedesk/Android_SDK/blob/master/usedesk_sdk/src/main/java/ru/usedesk/sdk/external/entity/chat/UsedeskConfiguration.java) - конфигуратор, который содержит все необходимые поля для инициализации SDK с сервером:

| Переменная     | Тип    | Описание                                 |
|----------------|--------|------------------------------------------|
| accountId      | String | ID базы знаний                           |
| email          | String | Почта клиента                            |
| url            | String | Адрес сервера с номером порта            |
| offlineFormUrl | String | Адрес сервера для отправки оффлайн формы |

- [UsedeskActionListener](https://github.com/usedesk/Android_SDK/blob/master/usedesk_sdk/src/main/java/ru/usedesk/sdk/external/entity/chat/UsedeskActionListener.java) - список возможных событий, которые может возвратить SDK при работе:

| Метод                    | Параметры     | Описание события                                 |
|--------------------------|---------------|--------------------------------------------------|
| onConnected              | -             | Успешное подключение к серверу                   |
| onMessageReceived        | Message       | Новое сообщение                                  |
| onMessagesReceived       | List&lt;Message&gt; | Список сообщений из в чата на момент подключения |
| onServiceMessageReceived | Message       | Сервисное сообщение                              |
| onOfflineFormExpected    | -             | Ожидается оффлайн форма                          |
| onDisconnected           | -             | Соединение разорвано                             |
| ~~onError~~                  | ~~int~~           | ~~ID текстового сообщения ошибки~~  |
| ~~onError~~                  | ~~Exception~~     | ~~Возникшее исключение~~       |
| onException                  | UsedeskException     | Возникшее исключение         |

- [UsedeskException](https://github.com/usedesk/Android_SDK/blob/master/usedesk_sdk/src/main/java/ru/usedesk/sdk/external/entity/exceptions/UsedeskException.java) - исключение, обработанное SDK, может быть UsedeskSocketException и UsedeskHttpException.

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

<a name="base_manage"></a>

### 2. Методы взаимодействия с UsedeskChat

- `sendMessage(String text, UsedeskFile usedeskFile)` - универсальный метод отправки сообщений, он может принимать на вход только текст, текст и файл или оба параметра одновременно.
[UsedeskFile](https://github.com/usedesk/Android_SDK/blob/master/usedesk_sdk/src/main/java/ru/usedesk/sdk/external/entity/chat/UsedeskFile.java) - класс файла для отправки посредством SDK:

| Переменная | Тип    | Описание                            |
|------------|--------|-------------------------------------|
| content    | String | Данные файла, кодированные в Base64 |
| type       | String | MIME-тип                            |
| size       | Long   | Размер файла                        |
| name       | String | Имя файла                           |

- `sendTextMessage(String text)` - метод отправки текстовых сообщений.
 
- `sendFileMessage(UsedeskFile usedeskFile)` - метод отправки файловых сообщений.

- `sendFeedbackMessage(Feedback feedback)` - метод отправки обратной формы, это форма для установки рейтинга ответа оператора (хорошо - LIKE,  плохо - DISLIKE).

- `sendOfflineForm(OfflineForm offlineForm)` - метод отправки оффлайн формы, это форма для отправки данных на сервер (для дальнейшей связи с пользователем) когда все операторы не в сети.
Возможные поля для заполнения:

| Переменная | Тип    | Описание               |
|------------|--------|------------------------|
| companyId  | String | ID компании            |
| name       | String | Имя пользователя       |
| email      | String | Почта пользователя     |
| message    | String | Сообщение пользователя |

<a name="knowledge_base"></a>

## База знаний

<a name="knowledge_base_setup"></a>

### 1. Настройка UsedeskKnowledgeBase

[UsedeskKnowledgeBase](https://github.com/usedesk/Android_SDK/blob/master/usedesk_sdk/src/main/java/ru/usedesk/sdk/external/UsedeskKnowledgeBase.java) - класс работы с базой знаний.

Перед началом работы его неоходимо проинициализировать:
```
UsedeskKnowledgeBase usedeskKnowledgeBase = UsedeskSdk.initKnowledgeBase(context);
```
После этого можно получить объект класса в любом месте:
```
UsedeskKnowledgeBase usedeskKnowledgeBase = UsedeskKnowledgeBase.getUsedeskKnowledgeBase();
```
Освободить объект:
```
UsedeskSdk.releaseUsedeskKnowledgeBase();
```
Попытка получить объект без инициализации или после освобожения вызовет исключение.

Для работы напрямую с API необходимо подключить библиотеку:

    implementation 'io.reactivex.rxjava2:rxjava:2.2.8'
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

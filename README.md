
# Android Usedesk SDK

## Требования к API

- compileSdkVersion = **28**
- buildToolsVersion = **"28.0.3"**
- minSdkVersion = **19**
- targetSdkVersion = **28**

## Подключение к проекту

**[Usedesk SDK](https://github.com/usedesk/Android_SDK/tree/master/usedesk_sdk)** - библиотека Usedesk.

**[Usedesk Sample App](https://github.com/usedesk/Android_SDK/tree/master/app)** - пример использования библиотеки с использованием готовых фрагментов из sdk.


### Импорт модуля в проект
Перенесите модуль в проект и добавьте в `build.gradle` вашего модуля строку:
```
dependencies {
    ...
    implementation project(':usedesk_sdk')
    ...
}
```

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

## Основные этапы работы/взаимодействия с библиотекой

[UsedeskSDK](https://github.com/usedesk/Android_SDK/blob/master/usedesk_sdk/src/main/java/ru/usedesk/sdk/external/UsedeskSDK.java) - главный класс взаимодействия и настройки SDK. Позволяет проинициализировать, получить или освободить другие классы, необходимые для работы.


### 1. Настройка UsedeskChat


[UsedeskChat](https://github.com/usedesk/Android_SDK/blob/master/usedesk_sdk/src/main/java/ru/usedesk/sdk/external/UsedeskChat.java) - класс работы с чатом.

Перед началом работы его неоходимо проинициализировать:

    UsedeskChat usedeskChat = UsedeskSdk.initChat(context, usedeskConfiguration, usedeskActionListener);

После этого можно получить объект класса в любом месте:

    UsedeskChat usedeskChat = UsedeskSdk.getChat();

Освободить объект:

    UsedeskSdk.releaseChat();

Попытка получить объект без инициализации или после освобожения вызовет исключение.

Метод инициализации принимает объекты следующих классов:

- [UsedeskConfiguration](https://github.com/usedesk/Android_SDK/blob/master/usedesk_sdk/src/main/java/ru/usedesk/sdk/external/entity/chat/UsedeskConfiguration.java) - конфигуратор, который содержит все необходимые поля для инициализации SDK с сервером.
```
private String accountId;
private String email;
private String url;
private String offlineFormUrl;
```

- [UsedeskActionListener](https://github.com/usedesk/Android_SDK/blob/master/usedesk_sdk/src/main/java/ru/usedesk/sdk/external/entity/chat/UsedeskActionListener.java) - список возможных событий, которые может возвратить SDK при работе.
```
onConnected()
onMessageReceived(Message message)
onMessagesReceived(List<Message> messages)
onServiceMessageReceived(Message message)
onOfflineFormExpected()
onDisconnected()
onError(int errorResId)
onError(Exception e)
```

### 2. Методы взаимодействия с UsedeskChat

- `sendMessage(String text, UsedeskFile usedeskFile)` - универсальный метод отправки сообщений, он может принимать на вход только текст, текст и файл или оба параметра одновременно.
[UsedeskFile](https://github.com/usedesk/Android_SDK/blob/master/usedesk_sdk/src/main/java/ru/usedesk/sdk/models/UsedeskFile.java) - объект файла для отправки посредством SDK. Он имеет все необходимые поля для отправки файла:
```
private String content;
private String type;
private Long size;
private String name;
```

- `sendTextMessage(String text)` - метод отправки текстовых сообщений.
 
- `sendFileMessage(UsedeskFile usedeskFile)` - метод отправки файловых сообщений.

- `sendFeedbackMessage(Feedback feedback)` - метод отправки обратной формы, это форма для установки рейтинга ответа оператора (хорошо - LIKE,  плохо - DISLIKE).

- `sendOfflineForm(OfflineForm offlineForm)` - метод отправки оффлайн формы, это форма для отправки данных на сервер (для дальнейшей связи с пользователем) когда все операторы не в сети.
Возможные поля для заполнения:
```
private String accountId;
private String name;
private String email; 
private String message;
```

## База знаний
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

### 2. Методы взаимодействия с UsedeskKnowledgeBase

- Получить список секций:
  ```
  @NonNull
  public Single<List<Section>> getSectionsSingle();
  ```

- Получить статью целиком по её идентификатору:
  ```
  @NonNull
  public Single<ArticleBody> getArticleSingle(long articleId);
  ```

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
private void onQuery(String s) {
    Fragment fragment = getCurrentFragment();
    if (fragment instanceof IUsedeskOnSearchQueryListener) {
        ((IUsedeskOnSearchQueryListener) fragment).onSearchQuery(s);
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
## Кастомизация UI
Для кастомизации интерфейса необходимо воспользоваться классом [UsedeskViewCustomizer](https://github.com/usedesk/Android_SDK/blob/master/usedesk_sdk/src/main/java/ru/usedesk/sdk/external/ui/UsedeskViewCustomizer.java).
### 1. Тема
Для кастомизации готового интерфейса через темы определите свою, унаследованную от `Usedesk.Theme`, переопределив нужные атрибуты.
Каждый из атрибутов указывает на тему для конкретного слоя.
Пример из семпла:
```
...

<style name="Usedesk.TextStyle.Chat.Time.Custom">
	<item name="android:textColor">#000000</item>
</style>

<style name="Usedesk.TextStyle.Chat.Massage.Custom">
	<item name="android:textColor">#000000</item>
</style>

...

<style name="Usedesk.Item.Message.Operator.Custom">
	<item name="usedesk_item_message_text_style">@style/Usedesk.TextStyle.Chat.Massage.Custom
	</item>
	<item name="usedesk_item_message_time_header_style">@style/Usedesk.TextStyle.Chat.Time.Custom</item>
</style>

...

<style name="Usedesk.Theme.Custom">
	<item name="usedesk_fragment_chat">@style/Usedesk.Fragment.Chat.Custom</item>
	<item name="usedesk_item_message_service">@style/Usedesk.Item.Message.Service.Custom</item>
	<item name="usedesk_item_message_operator">@style/Usedesk.Item.Message.Operator.Custom</item>
	<item name="usedesk_item_message_user">@style/Usedesk.Item.Message.User.Custom</item>
</style>
```

Далее необходимо указать эту тему:
```
UsedeskSdk.getUsedeskViewCustomizer()
	.setThemeId(R.style.Usedesk_Theme_Custom);
```

### 2. Подмена ресурса
Воспользовавшись следующим методом, можно полностью заменить слой на новый.
При этом есть необходимость устанавливать такие же id элементов, как в оригинале.

```
UsedeskSdk.getUsedeskViewCustomizer()
	.setLayoutId(ru.usedesk.sdk.R.layout.usedesk_item_category, R.layout.custom_item_category);
```


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
    protected Class<?> getServiceClass();//Нужно вернуть класс сервис
```
###
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

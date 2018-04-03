# Android Usedesk SDK


## Требования к API

- compileSdkVersion = **27**
- buildToolsVersion = **"27.0.2"**
- minSdkVersion = **19**
- targetSdkVersion = **27**


## Структура

**[Usedesk SDK](https://github.com/usedesk/Android_SDK/tree/master/usedesk_sdk)** - библиотека Usedesk, подключение в проект:
  ```
  compile project(':usedesk_sdk')
  ```

**[Usedesk Sample App](https://github.com/usedesk/Android_SDK/tree/master/app)** - пример использования библиотеки.


## Основные этапы работы/взаимодействия с библиотекой

### 1. Начальная настройка

Главный класс взаимодействия и настройки SDK: [UsedeskSDK](https://github.com/usedesk/Android_SDK/blob/master/usedesk_sdk/src/main/java/ru/usedesk/sdk/UsedeskSDK.java).

Инициализация и создание объекта SDK происходит только после установки всех обязательных объектов, которые обеспечивают дальнейшую работу с сервером:

- [UsedeskConfiguration](https://github.com/usedesk/Android_SDK/blob/master/usedesk_sdk/src/main/java/ru/usedesk/sdk/UsedeskConfiguration.java) - конфигуратор, который содержит все необходимые поля для инициализации SDK с сервером.
  ```
  private String companyId;
  private String email;
  private String url;
  ```

- [UsedeskActionListener](https://github.com/usedesk/Android_SDK/blob/master/usedesk_sdk/src/main/java/ru/usedesk/sdk/UsedeskActionListener.java) - список возможных событий, которые может возвратить SDK при работе.
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

После установки всех нужных данных, можно приступить к созданию объекта SDK:
  ```
  usedeskSDK = new UsedeskSDK.Builder(context)
          .usedeskConfiguration(usedeskConfiguration)
          .usedeskActionListener(usedeskActionListener)
          .build();
  ```

Объект создан, SDK готово к использованию в приложении.

### 2. Методы взаимодействия

Создан ранее объект Usedesk SDK позволяет выполнить следующие действия:

- `destroy()` - завершение работы с SDK и удаление объектов. Нужно его выполнять когда работа с SDK прекращается, иначе соединение с сервером будет продолжать работать.

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
  private String companyId;
  private String name;
  private String email;
  private String message;
  ```


## Описание Usedesk Sample App

В проекте приведены примеры компонентов дизайна, их взаимодействия с SDK.

**[ChatFragment](https://github.com/usedesk/Android_SDK/blob/master/app/src/main/java/ru/usedesk/sample/ui/ChatFragment.java)** - основной класс, который взаимодействует с SDK. Он реализует интерфейс [UsedeskActionListener](https://github.com/usedesk/Android_SDK/blob/master/usedesk_sdk/src/main/java/ru/usedesk/sdk/UsedeskActionListener.java) для дальнейшей реакции на события, выполняет инициализацию объекта SDK.

**[MessagesAdapter](https://github.com/usedesk/Android_SDK/blob/master/app/src/main/java/ru/usedesk/sample/ui/MessagesAdapter.java)** - адаптер, позволяет работать из любым типом сообщений:
  ```
  TYPE_USER_TEXT = 1;
  TYPE_USER_FILE = 11;
  TYPE_USER_TEXT_FILE = 111;

  TYPE_OPERATOR_TEXT = 2;
  TYPE_OPERATOR_FILE = 22;
  TYPE_OPERATOR_TEXT_FILE = 222;
  TYPE_OPERATOR_FEEDBACK = 2222;

  TYPE_SERVICE_TEXT = 3;
  ```

View-объекты для соответствующих типов сообщений расположены в layout-ресурсах:

- **[item_operator_feedback_message.xml](https://github.com/usedesk/Android_SDK/blob/master/app/src/main/res/layout/item_operator_feedback_message.xml)**

- **[item_operator_file_message.xml](https://github.com/usedesk/Android_SDK/blob/master/app/src/main/res/layout/item_operator_file_message.xml)**

- **[item_operator_text_file_message.xml](https://github.com/usedesk/Android_SDK/blob/master/app/src/main/res/layout/item_operator_text_file_message.xml)**

- **[item_operator_text_message.xml](https://github.com/usedesk/Android_SDK/blob/master/app/src/main/res/layout/item_operator_text_message.xml)**

- **[item_service_text_message.xml](https://github.com/usedesk/Android_SDK/blob/master/app/src/main/res/layout/item_service_text_message.xml)**

- **[item_user_file_message.xml](https://github.com/usedesk/Android_SDK/blob/master/app/src/main/res/layout/item_user_file_message.xml)**

- **[item_user_text_file_message.xml](https://github.com/usedesk/Android_SDK/blob/master/app/src/main/res/layout/item_user_text_file_message.xml)**

- **[item_user_text_message.xml](https://github.com/usedesk/Android_SDK/blob/master/app/src/main/res/layout/item_user_text_message.xml)**

**[AppSession](https://github.com/usedesk/Android_SDK/blob/master/app/src/main/java/ru/usedesk/sample/AppSession.java)** - объект для хранения промежуточных данных между компонентами приложения.

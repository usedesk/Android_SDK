package ru.usedesk.chat_sdk.external.service.notifications.presenter;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import ru.usedesk.chat_sdk.external.UsedeskChatSdk;
import ru.usedesk.chat_sdk.external.entity.IUsedeskActionListener;
import ru.usedesk.chat_sdk.external.entity.UsedeskActionListenerRx;
import ru.usedesk.chat_sdk.external.entity.UsedeskMessageType;

public class UsedeskNotificationsPresenter {

    private final UsedeskActionListenerRx actionListenerRx;
    private UsedeskNotificationsModel model;

    @Inject
    public UsedeskNotificationsPresenter() {
        this.actionListenerRx = new UsedeskActionListenerRx();
    }

    @NonNull
    public IUsedeskActionListener getActionListener() {
        return actionListenerRx;
    }

    @NonNull
    private Observable<UsedeskNotificationsModel> getNewModelObservable() {
        return actionListenerRx.getNewMessageObservable()
                .filter(message -> message.getType() == UsedeskMessageType.OPERATOR_TO_CLIENT)
                .map(UsedeskNotificationsModel::new);
    }

    @NonNull
    private UsedeskNotificationsModel reduce(@NonNull UsedeskNotificationsModel previousModel,
                                             @NonNull UsedeskNotificationsModel newModel) {
        return new UsedeskNotificationsModel(previousModel.getMessage(), previousModel.getCount() + 1);
    }

    @NonNull
    public Observable<UsedeskNotificationsModel> getModelObservable() {
        return getNewModelObservable().map(model -> this.model =
                this.model == null
                        ? model
                        : reduce(this.model, model));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    public void init() {
        actionListenerRx.getDisconnectedObservable()
                .delay(5, TimeUnit.SECONDS)
                .subscribe(event -> connect());

        connect();
    }

    private void connect() {
        UsedeskChatSdk.getInstance().connectRx()
                .subscribe();
    }
}

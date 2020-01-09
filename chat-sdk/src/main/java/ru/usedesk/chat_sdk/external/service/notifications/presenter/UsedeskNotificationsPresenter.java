package ru.usedesk.chat_sdk.external.service.notifications.presenter;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import io.reactivex.Observable;
import ru.usedesk.chat_sdk.external.entity.MessageType;
import ru.usedesk.chat_sdk.external.entity.UsedeskActionListener;
import ru.usedesk.chat_sdk.external.entity.UsedeskActionListenerRx;

public class UsedeskNotificationsPresenter {

    private final UsedeskActionListenerRx actionListenerRx;
    private UsedeskNotificationsModel model;

    @Inject
    public UsedeskNotificationsPresenter() {
        this.actionListenerRx = new UsedeskActionListenerRx();
    }

    @NonNull
    public UsedeskActionListener getActionListener() {
        return actionListenerRx;
    }

    @NonNull
    private Observable<UsedeskNotificationsModel> getNewModelObservable() {
        return actionListenerRx.getNewMessageObservable()
                .filter(message -> message.getType() == MessageType.OPERATOR_TO_CLIENT)
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
}

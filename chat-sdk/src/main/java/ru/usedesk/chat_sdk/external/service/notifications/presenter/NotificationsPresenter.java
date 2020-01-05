package ru.usedesk.chat_sdk.external.service.notifications.presenter;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import io.reactivex.Observable;
import ru.usedesk.sdk.external.entity.chat.MessageType;
import ru.usedesk.sdk.external.entity.chat.UsedeskActionListenerRx;
import ru.usedesk.sdk.mvi.MviPresenter;

public class NotificationsPresenter extends MviPresenter<NotificationsModel> {

    private final UsedeskActionListenerRx actionListenerRx;

    @Inject
    NotificationsPresenter(@NonNull UsedeskActionListenerRx actionListenerRx) {
        super(null);
        this.actionListenerRx = actionListenerRx;
    }

    @NonNull
    public UsedeskActionListenerRx getActionListenerRx() {
        return actionListenerRx;
    }

    @NonNull
    @Override
    protected Observable<NotificationsModel> getNewModelObservable() {
        return actionListenerRx.getNewMessageObservable()
                .filter(message -> message.getType() == MessageType.OPERATOR_TO_CLIENT)
                .map(NotificationsModel::new);
    }

    @NonNull
    @Override
    protected NotificationsModel reduce(@NonNull NotificationsModel previousModel,
                                        @NonNull NotificationsModel newModel) {
        return new NotificationsModel(previousModel.getMessage(), previousModel.getCount() + 1);
    }
}

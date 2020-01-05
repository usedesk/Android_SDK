package ru.usedesk.common_gui.internal.ui.mvi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.reactivex.Observable;

public abstract class MviPresenter<M> {

    private M model;

    public MviPresenter(@Nullable M initModel) {
        this.model = initModel;
    }

    @NonNull
    public Observable<M> getModelObservable() {
        return getNewModelObservable().map(model -> this.model =
                this.model == null
                        ? model
                        : reduce(this.model, model));
    }

    @NonNull
    protected abstract Observable<M> getNewModelObservable();

    @NonNull
    protected abstract M reduce(@NonNull M previousModel, @NonNull M newModel);
}

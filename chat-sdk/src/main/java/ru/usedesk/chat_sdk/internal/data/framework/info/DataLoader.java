package ru.usedesk.chat_sdk.internal.data.framework.info;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.usedesk.common_sdk.external.entity.exceptions.DataNotFoundException;

public abstract class DataLoader<T> {

    private T data;

    @Nullable
    protected abstract T loadData();

    protected abstract void saveData(@NonNull T data);

    @NonNull
    public final T getData() throws DataNotFoundException {
        if (data == null) {
            data = loadData();
            if (data == null) {
                throw new DataNotFoundException("Data not found");
            }
        }
        return data;
    }

    public final void setData(@Nullable T data) {
        this.data = data;
        saveData(data);
    }

    public void clearData() {
        data = null;
    }
}

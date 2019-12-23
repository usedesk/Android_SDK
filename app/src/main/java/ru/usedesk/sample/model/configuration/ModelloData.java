package ru.usedesk.sample.model.configuration;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ModelloData<DATA> {
    private final DATA data;

    public ModelloData(@NonNull DATA data) {
        this.data = data;
    }

    public DATA getData() {
        return data;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof ModelloData) {
            return ((ModelloData) obj).data.equals(data);
        }
        return super.equals(obj);
    }
}

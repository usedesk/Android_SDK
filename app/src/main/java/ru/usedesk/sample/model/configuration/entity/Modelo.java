package ru.usedesk.sample.model.configuration.entity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class Modelo<DATAKEY extends Modelo.DataKey, INTENTKEY> {
    private final Map<DATAKEY, BehaviorSubject> dataSubjectMap = new HashMap<>();
    private final Map<INTENTKEY, BehaviorSubject> intentSubjectMap = new HashMap<>();

    public Modelo(@NonNull DATAKEY[] dataKeys, @NonNull INTENTKEY[] intentKeys) {
        for (DATAKEY dataKey : dataKeys) {
            dataSubjectMap.put(dataKey, BehaviorSubject.create());
            setData(dataKey, dataKey.getDefault());
        }

        for (INTENTKEY intentKey : intentKeys) {
            intentSubjectMap.put(intentKey, BehaviorSubject.create());
        }
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public <T> T getData(@NonNull DATAKEY dataKey) {
        return (T) getDataSubject(dataKey).getValue();
    }

    @NonNull
    @SuppressWarnings({"ConstantConditions", "unchecked"})
    private <T> BehaviorSubject<T> getIntentSubject(@NonNull INTENTKEY intentKey) {
        return intentSubjectMap.get(intentKey);
    }

    @NonNull
    @SuppressWarnings({"ConstantConditions", "unchecked"})
    private <T> BehaviorSubject<T> getDataSubject(@NonNull DATAKEY dataKey) {
        return dataSubjectMap.get(dataKey);
    }

    @NonNull
    public <T> Observable<T> getIntentObservable(@NonNull INTENTKEY intentKey, @Nullable Class<T> tClass) {
        return getIntentSubject(intentKey);
    }

    @NonNull
    public <T> Observable<T> getIntentObservable(@NonNull INTENTKEY intentKey) {
        return getIntentObservable(intentKey, null);
    }

    @NonNull
    public <T> Observable<T> getDataObservable(@NonNull DATAKEY dataKey, @NonNull Class<T> tClass) {
        return getDataSubject(dataKey);
    }

    @NonNull
    public <T> void setIntent(@NonNull INTENTKEY intentKey, @NonNull T intent) {
        getIntentSubject(intentKey).onNext(intent);
    }

    @NonNull
    public <T> void setData(@NonNull DATAKEY dataKey, @NonNull T data) {
        getDataSubject(dataKey).onNext(data);
    }

    public interface DataKey {
        @NonNull
        Object getDefault();
    }


}

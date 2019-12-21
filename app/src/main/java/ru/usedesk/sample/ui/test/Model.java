package ru.usedesk.sample.ui.test;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

public class Model<KEY, INTENT> {
    private final Map<KEY, Subject> subjectDataMap;
    private final Map<KEY, LiveData> liveDataMap = new HashMap<>();
    private final Map<INTENT, Subject> subjectIntentMap;

    @SuppressWarnings("unchecked")
    public Model(@NonNull Map<KEY, Subject> subjectDataMap,
                 @NonNull Map<INTENT, Subject> subjectIntentMap) {
        this.subjectDataMap = subjectDataMap;
        this.subjectIntentMap = subjectIntentMap;

        for (KEY key : subjectDataMap.keySet()) {
            MutableLiveData liveData = new MutableLiveData();
            Disposable d = getSubject(key)
                    .subscribe(liveData::postValue, Throwable::printStackTrace);
            liveDataMap.put(key, liveData);
        }
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    public <T> void addIntent(@NonNull INTENT intent, @NonNull Observable<T> observable) {
        Subject<T> intentSubject = subjectIntentMap.get(intent);
        Disposable d = observable.subscribe(intentSubject::onNext, Throwable::printStackTrace);
    }

    @SuppressWarnings("unchecked")
    public <T> Observable<T> getIntent(@NonNull INTENT intent) {
        return subjectIntentMap.get(intent);
    }

    public <T> void setValue(@NonNull KEY key, @NonNull T value) {
        Subject<T> subject = getSubject(key);
        subject.onNext(value);
    }

    @NonNull
    @SuppressWarnings({"ConstantConditions", "unchecked"})
    private <T> Subject<T> getSubject(@NonNull KEY key) {
        return (Subject<T>) subjectDataMap.get(key);
    }

    @NonNull
    public <T> Observable<T> getObservable(@NonNull KEY key) {
        return getSubject(key);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public <T> LiveData<T> getLiveData(@NonNull KEY key) {
        return liveDataMap.get(key);
    }

    @Nullable
    public <T> T getValue(@NonNull KEY key) {
        Observable<T> observable = getObservable(key);
        if (observable instanceof BehaviorSubject) {
            return ((BehaviorSubject<T>) observable).getValue();
        }
        return null;
    }
}

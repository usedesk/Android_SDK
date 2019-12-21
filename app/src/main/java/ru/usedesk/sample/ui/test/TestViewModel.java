package ru.usedesk.sample.ui.test;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import ru.usedesk.sample.ui._common.TextInputLiveData;

public class TestViewModel extends ViewModel {
    private TextInputLiveData emailLiveData = new TextInputLiveData();
    private TextInputLiveData phoneNumberLiveData = new TextInputLiveData();

    private CompositeDisposable disposables = new CompositeDisposable();

    public TestViewModel() {
    }

    @NonNull
    public TextInputLiveData getEmailLiveData() {
        return emailLiveData;
    }

    @NonNull
    TextInputLiveData getEmailLiveData(@NonNull Observable<String> emailObservable) {
        disposables.add(emailObservable.subscribe(text -> {
            String error = text.contains("a")
                    ? "Please, delete 'a' symbol"
                    : "";
            emailLiveData.post(text, error);
        }, Throwable::printStackTrace));

        return emailLiveData;
    }

    @NonNull
    public TextInputLiveData getPhoneNumberLiveData() {
        return phoneNumberLiveData;
    }

    @NonNull
    TextInputLiveData getPhoneNumberLiveData(@NonNull Observable<String> phoneNumberObservable) {
        disposables.add(phoneNumberObservable.subscribe(text -> {
            String error = text.contains("7")
                    ? "Please, delete '7' symbol"
                    : "";
            phoneNumberLiveData.post(text, error);
        }, Throwable::printStackTrace));

        return phoneNumberLiveData;
    }
}

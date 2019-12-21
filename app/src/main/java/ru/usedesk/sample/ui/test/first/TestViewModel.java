package ru.usedesk.sample.ui.test.first;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import ru.usedesk.sample.ui._common.TextInputLiveData;
import ru.usedesk.sample.ui.test.TestInteractor;

public class TestViewModel extends ViewModel {
    private final TestInteractor testInteractor = TestInteractor.instance;

    private final TextInputLiveData emailLiveData = new TextInputLiveData();
    private final TextInputLiveData phoneNumberLiveData = new TextInputLiveData();
    private final TextInputLiveData selectLiveData = new TextInputLiveData();
    private final CompositeDisposable disposables = new CompositeDisposable();

    public TestViewModel() {
        initLiveData(testInteractor.getEmailText(),
                testInteractor.getEmailTextObservable(),
                testInteractor.getEmailErrorObservable(),
                emailLiveData);

        initLiveData(testInteractor.getPhoneNumberText(),
                testInteractor.getPhoneNumberTextObservable(),
                testInteractor.getPhoneNumberErrorObservable(),
                phoneNumberLiveData);

        initLiveData(testInteractor.getSelectText(),
                testInteractor.getSelectTextObservable(),
                testInteractor.getSelectErrorObservable(),
                selectLiveData);
    }

    private void initLiveData(@Nullable String initValue,
                              @NonNull Observable<String> textObservable,
                              @NonNull Observable<String> errorObservable,
                              @NonNull TextInputLiveData textInputLiveData) {
        if (initValue != null) {
            textInputLiveData.setText(initValue);
        }
        disposables.add(textObservable.subscribe(textInputLiveData::postText, Throwable::printStackTrace));
        disposables.add(errorObservable.subscribe(textInputLiveData::postError, Throwable::printStackTrace));
    }

    @NonNull
    public TextInputLiveData getEmailLiveData() {
        return emailLiveData;
    }

    @NonNull
    TextInputLiveData getEmailLiveData(@NonNull Observable<String> emailObservable) {
        testInteractor.addEmailObservable(emailObservable);

        return emailLiveData;
    }

    @NonNull
    public TextInputLiveData getPhoneNumberLiveData() {
        return phoneNumberLiveData;
    }

    @NonNull
    TextInputLiveData getPhoneNumberLiveData(@NonNull Observable<String> phoneNumberObservable) {
        testInteractor.addPhoneNumberObservable(phoneNumberObservable);

        return phoneNumberLiveData;
    }

    @NonNull
    public TextInputLiveData getSelectLiveData() {
        return selectLiveData;
    }

    @NonNull
    public TextInputLiveData getSelectLiveData(@NonNull Observable<String> selectObservable) {
        testInteractor.addSelectObservable(selectObservable);

        return selectLiveData;
    }
}

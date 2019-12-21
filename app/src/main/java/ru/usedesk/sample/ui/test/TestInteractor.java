package ru.usedesk.sample.ui.test;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class TestInteractor {
    public static final TestInteractor instance = new TestInteractor();

    private BehaviorSubject<String> emailText = BehaviorSubject.create();
    private BehaviorSubject<String> emailError = BehaviorSubject.create();
    private BehaviorSubject<String> phoneNumberText = BehaviorSubject.create();
    private BehaviorSubject<String> phoneNumberError = BehaviorSubject.create();
    private BehaviorSubject<String> selectText = BehaviorSubject.create();
    private BehaviorSubject<String> selectError = BehaviorSubject.create();

    public Observable<String> getEmailTextObservable() {
        return emailText;
    }

    public Observable<String> getEmailErrorObservable() {
        return emailError;
    }

    public Observable<String> getPhoneNumberTextObservable() {
        return phoneNumberText;
    }

    public Observable<String> getPhoneNumberErrorObservable() {
        return phoneNumberError;
    }

    public Observable<String> getSelectTextObservable() {
        return selectText;
    }

    public Observable<String> getSelectErrorObservable() {
        return selectError;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    public void addEmailObservable(@NonNull Observable<String> emailObservable) {
        emailObservable.subscribe(email -> {
            String error = email.contains("a")
                    ? "Please, delete 'a' symbol"
                    : "";
            emailText.onNext(email);
            emailError.onNext(error);
        }, Throwable::printStackTrace);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    public void addPhoneNumberObservable(@NonNull Observable<String> phoneNumberObservable) {
        phoneNumberObservable.subscribe(phoneNumber -> {
            String error = phoneNumber.contains("7")
                    ? "Please, delete '7' symbol"
                    : "";
            phoneNumberText.onNext(phoneNumber);
            phoneNumberError.onNext(error);
        }, Throwable::printStackTrace);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    public void addSelectObservable(@NonNull Observable<String> selectObservable) {
        selectObservable.subscribe(select -> {
            String error = select.length() > 3
                    ? "Please, delete " + (select.length() - 3) + " symbols"
                    : "";
            selectText.onNext(select);
            selectError.onNext(error);
        }, Throwable::printStackTrace);
    }

    @Nullable
    public String getEmailText() {
        return emailText.getValue();
    }

    @Nullable
    public String getPhoneNumberText() {
        return phoneNumberText.getValue();
    }

    @Nullable
    public String getSelectText() {
        return selectText.getValue();
    }
}

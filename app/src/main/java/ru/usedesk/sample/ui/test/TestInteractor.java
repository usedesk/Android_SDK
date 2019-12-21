package ru.usedesk.sample.ui.test;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import io.reactivex.Observable;
import ru.usedesk.sample.ui.test.TestModel.Key;

public class TestInteractor {
    public static final TestInteractor instance = new TestInteractor();

    private final TestModel testModel = new TestModel();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    public TestInteractor() {
        Observable<String> emailIntent = testModel.getIntent(TestModel.Intent.EMAIL);
        emailIntent.subscribe(email -> {
            String error = email.contains("a")
                    ? "Please, delete 'a' symbol"
                    : "";
            testModel.setValue(Key.EMAIL_TEXT, email);
            testModel.setValue(Key.EMAIL_ERROR, error);
        }, Throwable::printStackTrace);


        Observable<String> phoneNumberIntent = testModel.getIntent(TestModel.Intent.PHONE_NUMBER);
        phoneNumberIntent.subscribe(phoneNumber -> {
            String error = phoneNumber.contains("7")
                    ? "Please, delete '7' symbol"
                    : "";
            testModel.setValue(Key.PHONE_NUMBER_TEXT, phoneNumber);
            testModel.setValue(Key.PHONE_NUMBER_ERROR, error);
        }, Throwable::printStackTrace);

        Observable<String> selectIntent = testModel.getIntent(TestModel.Intent.SELECT);
        selectIntent.subscribe(select -> {
            String error = select.length() > 3
                    ? "Please, delete " + (select.length() - 3) + " symbols"
                    : "";
            testModel.setValue(Key.SELECT_TEXT, select);
            testModel.setValue(Key.SELECT_ERROR, error);
        }, Throwable::printStackTrace);
    }

    @NonNull
    public TestModel getTestModel() {
        return testModel;
    }
}

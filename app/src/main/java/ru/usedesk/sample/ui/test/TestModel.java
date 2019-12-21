package ru.usedesk.sample.ui.test;

import java.util.HashMap;

import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class TestModel extends Model<TestModel.Key, TestModel.Intent> {

    private static final HashMap<Key, Subject> DATA_SUBJECTS = new HashMap<Key, Subject>() {{
        put(Key.EMAIL_TEXT, BehaviorSubject.create());
        put(Key.EMAIL_ERROR, BehaviorSubject.create());
        put(Key.PHONE_NUMBER_TEXT, BehaviorSubject.create());
        put(Key.PHONE_NUMBER_ERROR, BehaviorSubject.create());
        put(Key.SELECT_TEXT, BehaviorSubject.create());
        put(Key.SELECT_ERROR, BehaviorSubject.create());
        put(Key.COMMON_ERROR, PublishSubject.create());
        put(Key.TOAST_SHOW, PublishSubject.create());
    }};

    private static final HashMap<Intent, Subject> INTENT_SUBJECTS = new HashMap<Intent, Subject>() {{
        put(Intent.EMAIL, PublishSubject.create());
        put(Intent.PHONE_NUMBER, PublishSubject.create());
        put(Intent.SELECT, PublishSubject.create());
    }};


    public TestModel() {
        super(DATA_SUBJECTS, INTENT_SUBJECTS);
    }

    public enum Key {
        EMAIL_TEXT,
        EMAIL_ERROR,
        PHONE_NUMBER_TEXT,
        PHONE_NUMBER_ERROR,
        SELECT_TEXT,
        SELECT_ERROR,
        COMMON_ERROR,
        TOAST_SHOW
    }

    public enum Intent {
        EMAIL,
        PHONE_NUMBER,
        SELECT
    }
}

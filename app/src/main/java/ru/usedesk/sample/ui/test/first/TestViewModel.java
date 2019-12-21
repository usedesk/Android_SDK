package ru.usedesk.sample.ui.test.first;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import ru.usedesk.sample.ui.test.TestInteractor;
import ru.usedesk.sample.ui.test.TestModel;

public class TestViewModel extends ViewModel {
    private final TestInteractor testInteractor = TestInteractor.instance;
    private final TestModel testModel;

    public TestViewModel() {
        testModel = testInteractor.getTestModel();
    }

    @NonNull
    public TestModel getTestModel() {
        return testModel;
    }
}

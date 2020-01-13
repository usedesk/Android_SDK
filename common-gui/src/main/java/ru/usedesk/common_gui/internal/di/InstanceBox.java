package ru.usedesk.common_gui.internal.di;

import javax.inject.Inject;

import io.reactivex.annotations.NonNull;
import ru.usedesk.common_gui.external.IUsedeskViewCustomizer;
import ru.usedesk.common_sdk.internal.appdi.InjectBox;

public class InstanceBox extends InjectBox {
    @Inject
    IUsedeskViewCustomizer usedeskViewCustomizer;

    public InstanceBox() {
        init(new MainModule());
    }

    @NonNull
    public IUsedeskViewCustomizer getUsedeskViewCustomizer() {
        return usedeskViewCustomizer;
    }
}

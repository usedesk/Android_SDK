package ru.usedesk.common_gui.internal.di;

import android.support.annotation.NonNull;

import javax.inject.Inject;

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

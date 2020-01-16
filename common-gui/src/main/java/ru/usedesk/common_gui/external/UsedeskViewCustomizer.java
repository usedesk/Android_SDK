package ru.usedesk.common_gui.external;

import androidx.annotation.NonNull;

import ru.usedesk.common_gui.internal.di.InstanceBox;

public class UsedeskViewCustomizer {
    private static InstanceBox instanceBox;

    @NonNull
    public static IUsedeskViewCustomizer getInstance() {
        if (instanceBox == null) {
            instanceBox = new InstanceBox();
        }
        return instanceBox.getUsedeskViewCustomizer();
    }

}

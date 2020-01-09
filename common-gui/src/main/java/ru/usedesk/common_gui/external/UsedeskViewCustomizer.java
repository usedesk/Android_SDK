package ru.usedesk.common_gui.external;

import io.reactivex.annotations.NonNull;
import ru.usedesk.common_gui.internal.di.InstanceBox;

public class UsedeskViewCustomizer {
    private static final InstanceBox instanceBox = new InstanceBox();

    @NonNull
    public static IUsedeskViewCustomizer getInstance() {
        return instanceBox.getUsedeskViewCustomizer();
    }

}

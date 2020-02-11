package ru.usedesk.common_gui.internal.di;

import ru.usedesk.common_gui.external.IUsedeskViewCustomizer;
import ru.usedesk.common_gui.internal.ViewCustomizer;
import toothpick.config.Module;

public class MainModule extends Module {
    public MainModule() {
        bind(IUsedeskViewCustomizer.class).to(ViewCustomizer.class).singleton();
    }
}

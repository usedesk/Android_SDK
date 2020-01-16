package ru.usedesk.common_sdk.internal.appdi;

import androidx.annotation.NonNull;

import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.config.Module;

public abstract class InjectBox {
    private Scope scope;

    protected void init(@NonNull Module... modules) {
        scope = Toothpick.openScope(this);
        scope.installModules(modules);

        Toothpick.inject(this, scope);
    }

    public void release() {
        if (scope != null) {
            Toothpick.closeScope(scope);
            scope = null;
        }
    }
}

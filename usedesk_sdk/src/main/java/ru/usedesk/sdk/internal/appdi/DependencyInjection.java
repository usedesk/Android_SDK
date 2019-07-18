package ru.usedesk.sdk.internal.appdi;

import android.content.Context;
import android.support.annotation.NonNull;

import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.config.Module;

public abstract class DependencyInjection {
    private Scope scope;

    DependencyInjection(final Object scopeObject, final Context context) {
        scope = Toothpick.openScope(scopeObject);
        scope.installModules(getModule(context.getApplicationContext()));
    }

    @NonNull
    protected abstract Module getModule(final Context appContext);

    @NonNull
    public Scope getScope() {
        return scope;
    }
}

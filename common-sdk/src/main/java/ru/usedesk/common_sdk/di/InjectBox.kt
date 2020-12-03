package ru.usedesk.common_sdk.di

import toothpick.Scope
import toothpick.Toothpick
import toothpick.config.Module

abstract class InjectBox {
    private var scope: Scope? = null

    protected fun init(vararg modules: Module) {
        scope = Toothpick.openScope(this).apply {
            installModules(*modules)
        }
        Toothpick.inject(this, scope)
    }

    open fun release() {
        if (scope != null) {
            Toothpick.closeScope(scope)
            scope = null
        }
    }
}
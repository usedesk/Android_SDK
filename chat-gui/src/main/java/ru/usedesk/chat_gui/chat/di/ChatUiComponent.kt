
package ru.usedesk.chat_gui.chat.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.BindsInstance
import dagger.Component
import dagger.MapKey
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Scope
import kotlin.reflect.KClass

@[ChatUiScope Component(modules = [ChatUiModule::class])]
internal interface ChatUiComponent {
    val viewModelFactory: ViewModelFactory
    val usedeskChat: IUsedeskChat
    val appContext: Context

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance appContext: Context,
            @BindsInstance usedeskChat: IUsedeskChat
        ): ChatUiComponent
    }

    companion object {
        private var chatUiComponent: ChatUiComponent? = null

        fun open(appContext: Context): ChatUiComponent = chatUiComponent
            ?: DaggerChatUiComponent.factory().create(
                appContext,
                UsedeskChatSdk.requireInstance()
            ).also {
                chatUiComponent = it
            }

        fun require(): ChatUiComponent = chatUiComponent
            ?: throw RuntimeException("ChatUiComponent is not initialized")

        fun close() {
            chatUiComponent = null
        }
    }
}

@Scope
annotation class ChatUiScope

internal class ViewModelFactory @Inject constructor(
    private val viewModelProviders: MutableMap<Class<out ViewModel>, Provider<ViewModel>>
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        viewModelProviders[modelClass]?.get() as? T
            ?: throw IllegalArgumentException("ViewModel $modelClass not found")
}

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)
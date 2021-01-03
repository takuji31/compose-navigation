package com.github.takuji31.compose.navigation

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import java.util.*

public data class BackStackEntry<S : Screen<*>>(
    public val id: UUID,
    public val screen: S,
) : ViewModelStoreOwner {
    private val viewModelStore = ViewModelStore()
    override fun getViewModelStore(): ViewModelStore = viewModelStore

    companion object {
        public fun <S : Screen<out ScreenId>> create(
            screen: S,
            uuid: UUID = UUID.randomUUID(),
        ): BackStackEntry<S> =
            BackStackEntry(UUID.randomUUID(), screen)
    }
}

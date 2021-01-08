package com.github.takuji31.compose.navigation

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import java.util.*
import kotlin.properties.Delegates

public data class BackStackEntry<S : Screen<out ScreenId>>(
    public val id: UUID,
    public val screen: S,
    private val viewModelStore: ViewModelStore,
    private val savedState: Bundle,
) : ViewModelStoreOwner, SavedStateRegistryOwner {
    private val lifecycle = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    init {
        savedStateRegistryController.performRestore(savedState)
    }

    override fun getViewModelStore(): ViewModelStore = viewModelStore

    companion object {
        internal fun <S : Screen<out ScreenId>> create(
            screen: S,
            navControllerViewModel: NavControllerViewModel,
            savedState: Bundle = Bundle(),
            uuid: UUID = UUID.randomUUID(),
        ): BackStackEntry<S> =
            BackStackEntry(
                UUID.randomUUID(),
                screen,
                navControllerViewModel.getViewModelStore(uuid),
                savedState,
            )
    }

    private var mHostLifecycle = Lifecycle.State.CREATED

    var maxLifecycle: Lifecycle.State by Delegates.observable(Lifecycle.State.RESUMED) { _, _, _ ->
        updateState()
    }

    fun handleLifecycleEvent(event: Lifecycle.Event) {
        mHostLifecycle = getStateAfter(event)
        updateState()
    }

    private fun updateState() {
        if (mHostLifecycle.ordinal < maxLifecycle.ordinal) {
            lifecycle.currentState = mHostLifecycle
        } else {
            lifecycle.currentState = maxLifecycle
        }
    }

    override fun getLifecycle(): Lifecycle = lifecycle

    override fun getSavedStateRegistry(): SavedStateRegistry =
        savedStateRegistryController.savedStateRegistry

    fun saveState(bundle: Bundle) {
        savedStateRegistryController.performSave(bundle)
    }

    private fun getStateAfter(event: Lifecycle.Event): Lifecycle.State {
        when (event) {
            Lifecycle.Event.ON_CREATE, Lifecycle.Event.ON_STOP -> return Lifecycle.State.CREATED
            Lifecycle.Event.ON_START, Lifecycle.Event.ON_PAUSE -> return Lifecycle.State.STARTED
            Lifecycle.Event.ON_RESUME -> return Lifecycle.State.RESUMED
            Lifecycle.Event.ON_DESTROY -> return Lifecycle.State.DESTROYED
            Lifecycle.Event.ON_ANY -> {
            }
        }
        throw IllegalArgumentException("Unexpected event value $event")
    }
}

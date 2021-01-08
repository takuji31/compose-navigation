package com.github.takuji31.compose.navigation

import android.os.Bundle
import android.os.Parcelable
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.parcelize.Parcelize
import java.util.*

public class NavController<S : Screen<out ScreenId>>(coroutineScope: CoroutineScope = GlobalScope) {
    private val _backStack: MutableStateFlow<List<BackStackEntry<S>>> =
        MutableStateFlow(emptyList())

    internal val backStack: StateFlow<List<BackStackEntry<S>>>
        get() = _backStack

    public val currentBackStackEntry: StateFlow<BackStackEntry<S>?> =
        backStack
            .map { it.lastOrNull() }
            .stateIn(coroutineScope, SharingStarted.Eagerly, null)

    public val currentScreen: StateFlow<S?> =
        currentBackStackEntry
            .map { it?.screen }
            .stateIn(coroutineScope, SharingStarted.Eagerly, null)

    private val onBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            pop()
        }
    }

    private var navControllerViewModel: NavControllerViewModel? = null
    private var lifecycleOwner: LifecycleOwner? = null

    private var statesToRestore: List<BackStackEntryState>? = null

    init {
        backStack
            .onEach {
                onBackPressedCallback.isEnabled = it.size > 1
            }
            .launchIn(coroutineScope)
    }

    fun setLifecycleOwner(lifecycleOwner: LifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner
        lifecycleOwner.lifecycle.addObserver(
            object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    backStack.value.forEach { it.handleLifecycleEvent(event) }
                }
            },
        )
    }

    fun setViewModelStoreOwner(viewModelStoreOwner: ViewModelStoreOwner) {
        navControllerViewModel =
            NavControllerViewModel.getInstance(viewModelStoreOwner.viewModelStore)
    }

    fun setOnBackPressedDispatcher(onBackPressedDispatcher: OnBackPressedDispatcher) {
        onBackPressedDispatcher.addCallback(checkNotNull(lifecycleOwner), onBackPressedCallback)
    }

    fun setInitialScreen(initialScreen: S) {
        val states = statesToRestore
        if (states != null) {
            _backStack.value = states.map { state ->
                @Suppress("UNCHECKED_CAST")
                BackStackEntry.create(
                    screen = state.screen as S,
                    navControllerViewModel = checkNotNull(navControllerViewModel),
                    uuid = state.id,
                    savedState = state.savedState,
                )
            }
            updateMaxLifecycles()
        }
        if (backStack.value.isEmpty()) {
            navigateTo(initialScreen)
        }
    }

    fun saveState(): Bundle {
        val bundle = Bundle()

        val currentBackStack = backStack.value
        bundle.putParcelableArrayList(
            BACKSTACK_KEY,
            arrayListOf(*currentBackStack.map { BackStackEntryState(it) }.toTypedArray()),
        )
        return bundle
    }

    fun restoreState(savedState: Bundle) {
        statesToRestore = savedState.getParcelableArrayList(BACKSTACK_KEY)
    }

    fun navigateTo(screen: S, popUpTo: ScreenId? = null, inclusive: Boolean = false) {
        val currentBackStack = _backStack.value
        val poppedBackStack = if (popUpTo != null) {
            val (_, poppedBackStack, _) = popBackStackInternal(
                popUpTo, inclusive, allowEmpty = true,
            )
            poppedBackStack
        } else {
            currentBackStack
        }
        _backStack.value = poppedBackStack + BackStackEntry.create(
            screen,
            checkNotNull(navControllerViewModel),
        )
        updateMaxLifecycles()
    }

    private fun popBackStackInternal(
        popUpTo: ScreenId?,
        inclusive: Boolean,
        allowEmpty: Boolean,
    ): PopResult<S> {
        val currentBackStack = backStack.value
        val isInclusive = inclusive || popUpTo == null
        val popUpToScreen = popUpTo ?: backStack.value.lastOrNull()?.screen?.id ?: return PopResult(
            false,
            currentBackStack,
            emptyList(),
        )
        val popUpToIndex = currentBackStack.indexOfLast { it.screen.id == popUpToScreen }
        val result = if (popUpToIndex == -1 || (popUpToIndex == 0 && !allowEmpty && isInclusive)) {
            PopResult(
                false,
                currentBackStack,
                emptyList(),
            )
        } else {
            val size = if (isInclusive) popUpToIndex else popUpToIndex + 1
            PopResult(
                true,
                currentBackStack.take(size),
                currentBackStack.drop(size),
            )
        }

        result.droppedBackStackEntries.reversed().forEach {
            it.maxLifecycle = Lifecycle.State.DESTROYED
            it.viewModelStore.clear()
        }

        return result
    }

    fun pop(popUpTo: ScreenId? = null, inclusive: Boolean = false): Boolean {
        val result = popBackStackInternal(popUpTo, inclusive, allowEmpty = false)
        if (result.popped) {
            _backStack.value = result.nextBackStack
            updateMaxLifecycles()
        }

        return result.popped
    }

    private fun updateMaxLifecycles() {
        val backStack = _backStack.value
        backStack.forEachIndexed { index, backStackEntry ->
            if (index == backStack.lastIndex) {
                backStackEntry.maxLifecycle = Lifecycle.State.RESUMED
            } else if (index != backStack.lastIndex) {
                backStackEntry.maxLifecycle = Lifecycle.State.STARTED
            }
        }
    }

    companion object {
        private const val BACKSTACK_KEY = "backStack"
    }

    data class PopResult<S : Screen<out ScreenId>>(
        val popped: Boolean,
        val nextBackStack: List<BackStackEntry<S>>,
        val droppedBackStackEntries: List<BackStackEntry<S>>,
    )

    @Parcelize
    data class BackStackEntryState(
        val id: UUID,
        val screen: Screen<*>,
        val savedState: Bundle,
    ) : Parcelable {
        constructor(backStackEntry: BackStackEntry<*>) : this(
            backStackEntry.id,
            backStackEntry.screen,
            Bundle().also { backStackEntry.saveState(it) },
        )
    }
}

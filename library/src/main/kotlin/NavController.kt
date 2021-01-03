package com.github.takuji31.compose.navigation

import androidx.activity.OnBackPressedCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

public class NavController<S : Screen<out ScreenId>>(coroutineScope: CoroutineScope) {
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

    internal val onBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            pop()
        }
    }

    init {
        backStack
            .onEach {
                onBackPressedCallback.isEnabled = it.size > 1
            }
            .launchIn(coroutineScope)
    }

    fun setup(startScreen: S, restoredBackStack: List<BackStackEntry<S>> = emptyList()) {
        _backStack.value = if (restoredBackStack.isNotEmpty()) {
            restoredBackStack
        } else {
            listOf(BackStackEntry.create(startScreen))
        }
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
        _backStack.value = poppedBackStack + BackStackEntry.create(screen)
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
            it.viewModelStore.clear()
        }

        return result
    }

    fun pop(popUpTo: ScreenId? = null, inclusive: Boolean = false): Boolean {
        val result = popBackStackInternal(popUpTo, inclusive, allowEmpty = false)
        if (result.popped) {
            _backStack.value = result.nextBackStack
        }

        return result.popped
    }

    data class PopResult<S : Screen<out ScreenId>>(
        val popped: Boolean,
        val nextBackStack: List<BackStackEntry<S>>,
        val droppedBackStackEntries: List<BackStackEntry<S>>,
    )
}

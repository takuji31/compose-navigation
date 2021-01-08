package com.github.takuji31.compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.savedinstancestate.Saver
import androidx.compose.runtime.savedinstancestate.rememberSavedInstanceState
import androidx.compose.ui.platform.AmbientLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope

@Composable
fun <S : Screen<out ScreenId>> rememberNavController(): NavController<S> {
    val coroutineScope = AmbientLifecycleOwner.current.lifecycleScope
    return rememberSavedInstanceState(coroutineScope, saver = navControllerSaver(coroutineScope)) {
        NavController(coroutineScope)
    }
}

private fun <S : Screen<out ScreenId>> navControllerSaver(coroutineScope: CoroutineScope): Saver<NavController<S>, *> =
    Saver(
        save = { it.saveState() },
        restore = { savedState -> NavController<S>(coroutineScope).also { it.restoreState(savedState) } },
    )

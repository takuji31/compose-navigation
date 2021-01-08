package com.github.takuji31.compose.navigation

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContextWrapper
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Providers
import androidx.compose.runtime.ambientOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.platform.AmbientLifecycleOwner
import androidx.compose.ui.platform.AmbientViewModelStoreOwner
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi

typealias ViewModelFactoryProducer = ((Application, BackStackEntry<*>) -> ViewModelProvider.Factory)

val AmbientViewModelFactoryProducer = ambientOf<ViewModelFactoryProducer?>()
val AmbientNavController = ambientOf<NavController<*>>()

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun <S : Screen<out ScreenId>> NavHost(
    navController: NavController<S>,
    initialScreen: S,
    viewModelFactoryProducer: ViewModelFactoryProducer = { application, backStackEntry ->
        SavedStateViewModelFactory(
            application,
            backStackEntry,
            bundleOf("screen" to backStackEntry.screen),
        )
    },
    builder: NavGraph<S>.() -> Unit,
) {
    val graph = remember(navController, builder) { NavGraph<S>().apply(builder) }

    val lifecycleOwner = AmbientLifecycleOwner.current
    val viewModelStoreOwner = AmbientViewModelStoreOwner.current
    var context = AmbientContext.current

    LaunchedEffect(navController, viewModelStoreOwner, lifecycleOwner) {
        navController.setLifecycleOwner(lifecycleOwner)
        navController.setViewModelStoreOwner(viewModelStoreOwner)
        navController.setInitialScreen(initialScreen)

        while (context is ContextWrapper) {
            if (context is OnBackPressedDispatcherOwner) {
                navController.setOnBackPressedDispatcher(
                    (context as OnBackPressedDispatcherOwner).onBackPressedDispatcher,
                )
                break
            }
            context = (context as ContextWrapper).baseContext
        }
    }

    val currentBackStackEntry by navController.currentBackStackEntry.collectAsState()
    currentBackStackEntry?.let {
        Providers(
            AmbientViewModelFactoryProducer provides viewModelFactoryProducer,
            AmbientNavController provides navController,
        ) {
            graph.navigate(backStackEntry = it)
        }
    }
}

typealias NavGraphContent<T> = @Composable ScreenScope<T>.() -> Unit

public class NavGraph<S : Screen<out ScreenId>> {
    private val screenContentMap: MutableMap<Class<out S>, NavGraphContent<*>> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    public fun <T : S> screen(clazz: Class<T>, content: NavGraphContent<T>): Unit {
        screenContentMap[clazz] = content as NavGraphContent<*>
    }

    public inline fun <reified T : S> screen(noinline content: NavGraphContent<T>) =
        screen(T::class.java, content)

    @SuppressLint("ComposableNaming")
    @Composable
    internal fun navigate(backStackEntry: BackStackEntry<S>) {
        val content = checkNotNull(screenContentMap[backStackEntry.screen.javaClass]) {
            "Screen ${backStackEntry.screen} does not matched any destination!"
        }
        Providers(
            AmbientViewModelStoreOwner provides backStackEntry,
            AmbientLifecycleOwner provides backStackEntry,
        ) {
            ScreenScope(backStackEntry).content()
        }
    }
}

class ScreenScope<S : Screen<out ScreenId>>(
    public val backStackEntry: BackStackEntry<S>,
) {
    public val screen: S
        get() = backStackEntry.screen
}

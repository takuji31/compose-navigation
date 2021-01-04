package com.github.takuji31.compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

@Composable
fun <S : Screen<out ScreenId>> NavHost(
    navViewModel: NavViewModel<S>,
    builder: NavGraph<S>.() -> Unit,
) {
    val graph = remember(navViewModel, builder) { NavGraph<S>().apply(builder) }
    val currentBackStackEntry by navViewModel.currentBackStackEntry.collectAsState()
    currentBackStackEntry?.let { graph.navigate(backStackEntry = it) }
}

typealias NavGraphContent<T> = @Composable ScreenScope<T>.() -> Unit

public class NavGraph<S : Screen<out ScreenId>> {
    private val screenContentMap: MutableMap<Class<out S>, NavGraphContent<S>> = mutableMapOf()

    public fun <T : S> screen(clazz: Class<T>, content: NavGraphContent<T>): Unit {
        screenContentMap[clazz] = content as NavGraphContent<*>
    }

    public inline fun <reified T : S> screen(noinline content: NavGraphContent<T>) =
        screen(T::class.java, content)

    @Composable
    internal fun navigate(backStackEntry: BackStackEntry<S>) {
        val content = checkNotNull(screenContentMap[backStackEntry.screen.javaClass]) {
            "Screen ${backStackEntry.screen} does not matched any destination!"
        }
        ScreenScope<S>(backStackEntry).content()
    }
}

class ScreenScope<S : Screen<out ScreenId>>(
    public val backStackEntry: BackStackEntry<S>,
) {
    public val screen: S
        get() = backStackEntry.screen
}

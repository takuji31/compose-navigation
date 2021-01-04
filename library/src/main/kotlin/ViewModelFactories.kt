package com.github.takuji31.compose.navigation

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.viewModel
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get

fun <S : Screen<out ScreenId>> AppCompatActivity.navViewModel(initialScreenProducer: () -> S): Lazy<NavViewModel<S>> =
    lazy {
        val viewModel =
            ViewModelProvider(this, this.defaultViewModelProviderFactory).get<NavViewModel<S>>()
        viewModel.setup(initialScreenProducer())
        viewModel
    }

fun <S : Screen<out ScreenId>> Fragment.navViewModel(initialScreenProducer: () -> S): Lazy<NavViewModel<S>> =
    lazy {
        val viewModel =
            ViewModelProvider(this, this.defaultViewModelProviderFactory).get<NavViewModel<S>>()
        viewModel.setup(initialScreenProducer())
        viewModel
    }

@Composable
fun <S : Screen<out ScreenId>> navViewModel(initialScreen: S): NavViewModel<S> {
    val viewModel = viewModel<NavViewModel<S>>()
    viewModel.setup(initialScreen)
    return viewModel
}

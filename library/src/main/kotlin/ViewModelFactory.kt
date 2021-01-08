package com.github.takuji31.compose.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.viewinterop.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Composable
inline fun <reified VM : ViewModel> screenViewModel(
    key: String? = null,
    factory: ViewModelProvider.Factory? = null,
): VM {
    return viewModel(key, factory ?: DefaultViewModelFactory())
}

@Composable
fun DefaultViewModelFactory(): ViewModelProvider.Factory? {
    val viewModelFactoryProducer = AmbientViewModelFactoryProducer.current
    val current = AmbientNavController.current
    val currentBackStackEntry = current.currentBackStackEntry.collectAsState()
    val application = AmbientContext.current.applicationContext as Application
    return remember(viewModelFactoryProducer, currentBackStackEntry.value, application) {
        currentBackStackEntry.value?.let {
            viewModelFactoryProducer?.invoke(application, it)
        }
    }
}

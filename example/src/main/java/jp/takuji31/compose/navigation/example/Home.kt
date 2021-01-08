package jp.takuji31.compose.navigation.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.takuji31.compose.navigation.screenViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Composable
fun Home() {
    val viewModel: HomeViewModel = screenViewModel()
    val count by viewModel.count.collectAsState()
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "This is Home.\n counter: $count",
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        TextButton(onClick = { viewModel.count.value += 1 }) {
            Text(text = "Increment")
        }
    }
}

class HomeViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    val count = MutableStateFlow(0)

    init {
        val initialCount: Int? = savedStateHandle["count"]
        initialCount?.let { count.value = it }
        viewModelScope.launch {
            count.collect { savedStateHandle["count"] = it }
        }
    }
}

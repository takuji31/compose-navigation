package jp.takuji31.compose.navigation.example

import com.github.takuji31.compose.navigation.Screen
import com.github.takuji31.compose.navigation.ScreenId
import kotlinx.parcelize.Parcelize

enum class ExampleScreenId : ScreenId {
    Home, MyPage, Settings
}

sealed class ExampleScreen(override val id: ExampleScreenId) : Screen<ExampleScreenId> {
    @Parcelize
    object Home : ExampleScreen(ExampleScreenId.Home)

    @Parcelize
    object MyPage : ExampleScreen(ExampleScreenId.MyPage)

    @Parcelize
    object Settings : ExampleScreen(ExampleScreenId.Settings)
}

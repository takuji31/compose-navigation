import com.github.takuji31.compose.navigation.Screen
import com.github.takuji31.compose.navigation.ScreenId
import kotlinx.parcelize.Parcelize

enum class TestScreenId : ScreenId {
    Home,
    User,
    Friends,
    Settings
}

sealed class TestScreen(override val id: TestScreenId) : Screen<TestScreenId> {
    @Parcelize
    object Home : TestScreen(TestScreenId.Home)

    @Parcelize
    data class User(val userId: String) : TestScreen(TestScreenId.User)

    @Parcelize
    data class Friends(val userId: String) : TestScreen(TestScreenId.Friends)

    @Parcelize
    object Settings : TestScreen(TestScreenId.Settings)
}

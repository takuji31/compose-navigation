package jp.takuji31.compose.navigation.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.setContent
import com.github.takuji31.compose.navigation.NavHost
import com.github.takuji31.compose.navigation.rememberNavController
import jp.takuji31.compose.navigation.ui.ComposeNavigationTheme

private val ExampleScreen.title: String
    get() = when (this) {
        ExampleScreen.Home -> "Home"
        ExampleScreen.MyPage -> "My Page"
        ExampleScreen.Settings -> "Settings"
    }

//@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController<ExampleScreen>()
            ComposeNavigationTheme {
                val currentScreen by navController.currentScreen.collectAsState()
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(text = currentScreen?.title ?: "")
                            },
                        )
                    },
                    bottomBar = {
                        BottomNavigation {
                            BottomNavigationItem(
                                icon = { Icon(Icons.Default.Home) },
                                label = { Text(text = ExampleScreen.Home.title) },
                                selected = currentScreen == ExampleScreen.Home,
                                onClick = {
                                    navController.navigateTo(
                                        ExampleScreen.Home,
                                        ExampleScreenId.Home,
                                        inclusive = true,
                                    )
                                },
                            )
                            BottomNavigationItem(
                                icon = { Icon(Icons.Default.Person) },
                                label = { Text(text = ExampleScreen.MyPage.title) },
                                selected = currentScreen == ExampleScreen.MyPage,
                                onClick = {
                                    navController.navigateTo(
                                        ExampleScreen.MyPage,
                                        ExampleScreenId.Home,
                                        inclusive = false,
                                    )
                                },
                            )
                            BottomNavigationItem(
                                icon = { Icon(Icons.Default.Settings) },
                                label = { Text(text = ExampleScreen.Settings.title) },
                                selected = currentScreen == ExampleScreen.Settings,
                                onClick = {
                                    navController.navigateTo(
                                        ExampleScreen.Settings,
                                        ExampleScreenId.Home,
                                        inclusive = false,
                                    )
                                },
                            )
                        }
                    },
                ) {

                }
                NavHost(
                    navController = navController,
                    initialScreen = ExampleScreen.Home,
                ) {
                    screen<ExampleScreen.Home> {
                        Home()
                    }
                    screen<ExampleScreen.MyPage> {
                        Box(Modifier.fillMaxSize()) {
                            Text(
                                text = "This is My Page",
                                modifier = Modifier.align(Alignment.Center),
                            )
                        }
                    }
                    screen<ExampleScreen.Settings> {
                        Box(Modifier.fillMaxSize()) {
                            Text(
                                text = "This is Setting",
                                modifier = Modifier.align(Alignment.Center),
                            )
                        }
                    }
                }
            }
        }
    }
}

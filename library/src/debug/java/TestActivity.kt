package com.github.takuji31.compose.navigation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.setContent

class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController<TestScreen>()
            NavHost(
                navController = navController,
                initialScreen = TestScreen.Home,
            ) {
                screen<TestScreen.Home> { Home() }
            }
        }
    }
}

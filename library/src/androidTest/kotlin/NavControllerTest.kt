package com.github.takuji31.compose.navigation

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Expect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class NavControllerTest {

    @get:Rule
    val expect: Expect = Expect.create()

    lateinit var testCoroutineDispatcher: TestCoroutineDispatcher

    @Before
    fun setUp() {
        testCoroutineDispatcher = TestCoroutineDispatcher()
        Dispatchers.setMain(testCoroutineDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testCoroutineDispatcher.cleanupTestCoroutines()
    }

    // TODO: Restore

    @Test
    fun navigateTo_screen_navigate() = navControllerlTest { navController ->
        navController.navigateTo(TestScreen.Settings)

        expect
            .that(navController.currentScreen.value)
            .isEqualTo(TestScreen.Settings)

        expect
            .that(navController.backStack.value.map { it.screen })
            .containsExactly(TestScreen.Settings)
            .inOrder()
    }

    @Test
    fun navigateTo_screen_navigate_popTo() = navControllerlTest { navController ->

        val screen1 = TestScreen.Home
        val screen2 = TestScreen.User("1234")
        val screen3 = TestScreen.Friends("1234")
        val screen4 = TestScreen.Settings

        navController.navigateTo(screen1)
        navController.navigateTo(screen2)
        navController.navigateTo(screen3)
        navController.navigateTo(screen4)

        val id = navController.currentBackStackEntry.value?.id

        val screen5 = TestScreen.Settings
        navController.navigateTo(screen5, TestScreenId.Settings, inclusive = true)

        expect
            .that(navController.currentScreen.value)
            .isEqualTo(screen5)

        expect
            .that(navController.currentBackStackEntry.value?.id)
            .isNotEqualTo(id)

        expect
            .that(navController.backStack.value.map { it.screen })
            .containsExactly(
                screen1,
                screen2,
                screen3,
                screen5,
            )
            .inOrder()

        val screen6 = TestScreen.Friends("2345")
        navController.navigateTo(screen6, TestScreenId.Friends, inclusive = true)

        expect
            .that(navController.currentScreen.value)
            .isEqualTo(screen6)

        expect
            .that(navController.backStack.value.map { it.screen })
            .containsExactly(
                screen1,
                screen2,
                screen6,
            )
            .inOrder()

        val screen7 = TestScreen.Friends("3456")
        navController.navigateTo(screen7, TestScreenId.User, inclusive = false)

        expect
            .that(navController.currentScreen.value)
            .isEqualTo(screen7)

        expect
            .that(navController.backStack.value.map { it.screen })
            .containsExactly(
                screen1,
                screen2,
                screen7,
            )
            .inOrder()

        val screen8 = TestScreen.User("4567")
        navController.navigateTo(screen8, TestScreenId.Home, inclusive = false)

        expect
            .that(navController.currentScreen.value)
            .isEqualTo(screen8)

        expect
            .that(navController.backStack.value.map { it.screen })
            .containsExactly(
                screen1,
                screen8,
            )
            .inOrder()

        val screen9 = TestScreen.Settings
        navController.navigateTo(screen9, TestScreenId.Home, inclusive = true)

        expect
            .that(navController.currentScreen.value)
            .isEqualTo(screen9)

        expect
            .that(navController.backStack.value.map { it.screen })
            .containsExactly(
                screen9,
            )
            .inOrder()
    }

    @Test
    fun pop_screen_popped() = navControllerlTest { navController ->
        navController.navigateTo(TestScreen.Home)
        navController.navigateTo(TestScreen.User("1234"))
        navController.navigateTo(TestScreen.Settings)
        val popped = navController.pop()

        expect
            .that(popped)
            .isTrue()

        expect
            .that(navController.currentScreen.value)
            .isEqualTo(TestScreen.User("1234"))

        expect
            .that(navController.backStack.value.map { it.screen })
            .containsExactly(TestScreen.Home, TestScreen.User("1234"))
            .inOrder()

        val popped2 = navController.pop()

        expect
            .that(popped2)
            .isTrue()

        expect
            .that(navController.currentScreen.value)
            .isEqualTo(TestScreen.Home)

        expect
            .that(navController.backStack.value.map { it.screen })
            .containsExactly(TestScreen.Home)
            .inOrder()
    }

    @Test
    fun pop_poppedLastScreen() = navControllerlTest { navController ->
        navController.navigateTo(TestScreen.Home)

        val popped = navController.pop()

        expect
            .that(popped)
            .isFalse()

        expect
            .that(navController.currentScreen.value)
            .isEqualTo(TestScreen.Home)

        expect
            .that(navController.backStack.value.map { it.screen })
            .containsExactly(TestScreen.Home)
            .inOrder()
    }

    @Test
    fun pop_popTo() = navControllerlTest { navController ->

        val screen1 = TestScreen.Settings
        val screen2 = TestScreen.User("1234")
        val screen3 = TestScreen.Friends("1234")
        val screen4 = TestScreen.Settings
        val screen5 = TestScreen.Home

        navController.navigateTo(screen1)
        navController.navigateTo(screen2)
        navController.navigateTo(screen3)
        navController.navigateTo(screen4)
        navController.navigateTo(screen5)

        val popped1 = navController.pop(TestScreenId.Settings, inclusive = false)

        expect
            .that(popped1)
            .isTrue()

        expect
            .that(navController.currentScreen.value)
            .isEqualTo(screen4)

        expect
            .that(navController.backStack.value.map { it.screen })
            .containsExactly(
                screen1,
                screen2,
                screen3,
                screen4,
            )
            .inOrder()

        val popped2 = navController.pop(TestScreenId.Settings, inclusive = true)

        expect
            .that(popped2)
            .isTrue()

        expect
            .that(navController.currentScreen.value)
            .isEqualTo(screen3)

        expect
            .that(navController.backStack.value.map { it.screen })
            .containsExactly(
                screen1,
                screen2,
                screen3,
            )
            .inOrder()

        val popped3 = navController.pop(TestScreenId.Settings, inclusive = false)

        expect
            .that(popped3)
            .isTrue()

        expect
            .that(navController.currentScreen.value)
            .isEqualTo(screen1)

        expect
            .that(navController.backStack.value.map { it.screen })
            .containsExactly(
                screen1,
            )
            .inOrder()
    }

    @Test
    fun pop_cannot_popUpTo_invalid_screen() = navControllerlTest { navController ->

        val screen1 = TestScreen.Home
        val screen2 = TestScreen.User("1234")
        val screen3 = TestScreen.Friends("1234")

        navController.navigateTo(screen1)
        navController.navigateTo(screen2)
        navController.navigateTo(screen3)

        val popped1 = navController.pop(TestScreenId.Settings, inclusive = false)

        expect
            .that(popped1)
            .isFalse()

        expect
            .that(navController.currentScreen.value)
            .isEqualTo(screen3)

        val popped2 = navController.pop(TestScreenId.Settings, inclusive = true)

        expect
            .that(popped2)
            .isFalse()

        expect
            .that(navController.currentScreen.value)
            .isEqualTo(screen3)

    }

    @Test
    fun pop_cannot_popUpTo_first_screen_inclusive() = navControllerlTest { navController ->

        val screen1 = TestScreen.Home
        val screen2 = TestScreen.User("1234")
        val screen3 = TestScreen.Friends("1234")

        navController.navigateTo(screen1)
        navController.navigateTo(screen2)
        navController.navigateTo(screen3)

        val popped1 = navController.pop(TestScreenId.Home, inclusive = true)

        expect
            .that(popped1)
            .isFalse()

        expect
            .that(navController.currentScreen.value)
            .isEqualTo(screen3)

        val popped2 = navController.pop(TestScreenId.Home, inclusive = false)

        expect
            .that(popped2)
            .isTrue()

        expect
            .that(navController.currentScreen.value)
            .isEqualTo(screen1)
    }

    private fun navControllerlTest(block: suspend TestCoroutineScope.(navController: NavController<TestScreen>) -> Unit) =
        testCoroutineDispatcher.runBlockingTest {
            val coroutineScope = CoroutineScope(testCoroutineDispatcher)
            val navController = NavController<TestScreen>(coroutineScope)
            block(navController)
            coroutineScope.cancel()
        }
}

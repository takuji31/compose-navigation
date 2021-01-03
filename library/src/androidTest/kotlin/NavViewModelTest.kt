import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takuji31.compose.navigation.BackStackEntry
import com.github.takuji31.compose.navigation.NavViewModel
import com.google.common.truth.Expect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class NavViewModelTest {

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

    @Test
    fun setup_currentScreen_is_startDestination() = navViewModelTest { viewModel ->

        viewModel.setup(TestScreen.Home)

        expect
            .that(viewModel.currentScreen.value)
            .isEqualTo(TestScreen.Home)
    }

    @Test
    fun init_currentScreen_is_restored() = navViewModelTest { viewModel ->

        viewModel.setup(
            TestScreen.Home,
            listOf(
                BackStackEntry.create(TestScreen.Home),
                BackStackEntry.create(TestScreen.Settings),
            ),
        )

        expect
            .that(viewModel.currentScreen.value)
            .isEqualTo(TestScreen.Settings)
    }

    @Test
    fun navigateTo_screen_navigate() = navViewModelTest { viewModel ->
        viewModel.setup(TestScreen.Home)


        viewModel.navigateTo(TestScreen.Settings)

        expect
            .that(viewModel.currentScreen.value)
            .isEqualTo(TestScreen.Settings)

        expect
            .that(viewModel.backStack.value.map { it.screen })
            .containsExactly(TestScreen.Home, TestScreen.Settings)
            .inOrder()
    }

    @Test
    fun navigateTo_screen_navigate_popTo() = navViewModelTest { viewModel ->

        val screen1 = TestScreen.Home
        val screen2 = TestScreen.User("1234")
        val screen3 = TestScreen.Friends("1234")
        val screen4 = TestScreen.Settings

        viewModel.setup(screen1)

        viewModel.navigateTo(screen2)
        viewModel.navigateTo(screen3)
        viewModel.navigateTo(screen4)

        val id = viewModel.currentBackStackEntry.value?.id

        val screen5 = TestScreen.Settings
        viewModel.navigateTo(screen5, TestScreenId.Settings, inclusive = true)

        expect
            .that(viewModel.currentScreen.value)
            .isEqualTo(screen5)

        expect
            .that(viewModel.currentBackStackEntry.value?.id)
            .isNotEqualTo(id)

        expect
            .that(viewModel.backStack.value.map { it.screen })
            .containsExactly(
                screen1,
                screen2,
                screen3,
                screen5,
            )
            .inOrder()

        val screen6 = TestScreen.Friends("2345")
        viewModel.navigateTo(screen6, TestScreenId.Friends, inclusive = true)

        expect
            .that(viewModel.currentScreen.value)
            .isEqualTo(screen6)

        expect
            .that(viewModel.backStack.value.map { it.screen })
            .containsExactly(
                screen1,
                screen2,
                screen6,
            )
            .inOrder()

        val screen7 = TestScreen.Friends("3456")
        viewModel.navigateTo(screen7, TestScreenId.User, inclusive = false)

        expect
            .that(viewModel.currentScreen.value)
            .isEqualTo(screen7)

        expect
            .that(viewModel.backStack.value.map { it.screen })
            .containsExactly(
                screen1,
                screen2,
                screen7,
            )
            .inOrder()

        val screen8 = TestScreen.User("4567")
        viewModel.navigateTo(screen8, TestScreenId.Home, inclusive = false)

        expect
            .that(viewModel.currentScreen.value)
            .isEqualTo(screen8)

        expect
            .that(viewModel.backStack.value.map { it.screen })
            .containsExactly(
                screen1,
                screen8,
            )
            .inOrder()

        val screen9 = TestScreen.Settings
        viewModel.navigateTo(screen9, TestScreenId.Home, inclusive = true)

        expect
            .that(viewModel.currentScreen.value)
            .isEqualTo(screen9)

        expect
            .that(viewModel.backStack.value.map { it.screen })
            .containsExactly(
                screen9,
            )
            .inOrder()
    }

    @Test
    fun pop_screen_popped() = navViewModelTest { viewModel ->
        viewModel.setup(TestScreen.Home)


        viewModel.navigateTo(TestScreen.User("1234"))
        viewModel.navigateTo(TestScreen.Settings)
        val popped = viewModel.pop()

        expect
            .that(popped)
            .isTrue()

        expect
            .that(viewModel.currentScreen.value)
            .isEqualTo(TestScreen.User("1234"))

        expect
            .that(viewModel.backStack.value.map { it.screen })
            .containsExactly(TestScreen.Home, TestScreen.User("1234"))
            .inOrder()

        val popped2 = viewModel.pop()

        expect
            .that(popped2)
            .isTrue()

        expect
            .that(viewModel.currentScreen.value)
            .isEqualTo(TestScreen.Home)

        expect
            .that(viewModel.backStack.value.map { it.screen })
            .containsExactly(TestScreen.Home)
            .inOrder()
    }

    @Test
    fun pop_poppedLastScreen() = navViewModelTest { viewModel ->
        viewModel.setup(TestScreen.Home)

        val popped = viewModel.pop()

        expect
            .that(popped)
            .isFalse()

        expect
            .that(viewModel.currentScreen.value)
            .isEqualTo(TestScreen.Home)

        expect
            .that(viewModel.backStack.value.map { it.screen })
            .containsExactly(TestScreen.Home)
            .inOrder()
    }

    @Test
    fun pop_popTo() = navViewModelTest { viewModel ->

        val screen1 = TestScreen.Settings
        val screen2 = TestScreen.User("1234")
        val screen3 = TestScreen.Friends("1234")
        val screen4 = TestScreen.Settings
        val screen5 = TestScreen.Home

        viewModel.setup(screen1)

        viewModel.navigateTo(screen2)
        viewModel.navigateTo(screen3)
        viewModel.navigateTo(screen4)
        viewModel.navigateTo(screen5)

        val popped1 = viewModel.pop(TestScreenId.Settings, inclusive = false)

        expect
            .that(popped1)
            .isTrue()

        expect
            .that(viewModel.currentScreen.value)
            .isEqualTo(screen4)

        expect
            .that(viewModel.backStack.value.map { it.screen })
            .containsExactly(
                screen1,
                screen2,
                screen3,
                screen4,
            )
            .inOrder()

        val popped2 = viewModel.pop(TestScreenId.Settings, inclusive = true)

        expect
            .that(popped2)
            .isTrue()

        expect
            .that(viewModel.currentScreen.value)
            .isEqualTo(screen3)

        expect
            .that(viewModel.backStack.value.map { it.screen })
            .containsExactly(
                screen1,
                screen2,
                screen3,
            )
            .inOrder()

        val popped3 = viewModel.pop(TestScreenId.Settings, inclusive = false)

        expect
            .that(popped3)
            .isTrue()

        expect
            .that(viewModel.currentScreen.value)
            .isEqualTo(screen1)

        expect
            .that(viewModel.backStack.value.map { it.screen })
            .containsExactly(
                screen1,
            )
            .inOrder()
    }

    @Test
    fun pop_cannot_popUpTo_invalid_screen() = navViewModelTest { viewModel ->

        val screen1 = TestScreen.Home
        val screen2 = TestScreen.User("1234")
        val screen3 = TestScreen.Friends("1234")

        viewModel.setup(screen1)

        viewModel.navigateTo(screen2)
        viewModel.navigateTo(screen3)

        val popped1 = viewModel.pop(TestScreenId.Settings, inclusive = false)

        expect
            .that(popped1)
            .isFalse()

        expect
            .that(viewModel.currentScreen.value)
            .isEqualTo(screen3)

        val popped2 = viewModel.pop(TestScreenId.Settings, inclusive = true)

        expect
            .that(popped2)
            .isFalse()

        expect
            .that(viewModel.currentScreen.value)
            .isEqualTo(screen3)

    }

    @Test
    fun pop_cannot_popUpTo_first_screen_inclusive() = navViewModelTest { viewModel ->

        val screen1 = TestScreen.Home
        val screen2 = TestScreen.User("1234")
        val screen3 = TestScreen.Friends("1234")

        viewModel.setup(screen1)

        viewModel.navigateTo(screen2)
        viewModel.navigateTo(screen3)

        val popped1 = viewModel.pop(TestScreenId.Home, inclusive = true)

        expect
            .that(popped1)
            .isFalse()

        expect
            .that(viewModel.currentScreen.value)
            .isEqualTo(screen3)

        val popped2 = viewModel.pop(TestScreenId.Home, inclusive = false)

        expect
            .that(popped2)
            .isTrue()

        expect
            .that(viewModel.currentScreen.value)
            .isEqualTo(screen1)
    }

    private fun navViewModelTest(block: suspend TestCoroutineScope.(viewModel: NavViewModel<TestScreen>) -> Unit) =
        testCoroutineDispatcher.runBlockingTest {
            val scenario = FragmentScenario.launchInContainer(Fragment::class.java)
            val viewModel = suspendCoroutine<NavViewModel<TestScreen>> { cont ->
                scenario.onFragment {
                    val viewModel =
                        ViewModelProvider(
                            it,
                            SavedStateViewModelFactory(it.requireActivity().application, it),
                        )
                            .get(NavViewModel::class.java)
                    @Suppress("UNCHECKED_CAST")
                    cont.resume(viewModel as NavViewModel<TestScreen>)
                }
            }
            block(viewModel)
            scenario.moveToState(Lifecycle.State.DESTROYED)
        }
}

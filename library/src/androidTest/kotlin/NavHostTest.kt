package com.github.takuji31.compose.navigation

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavHostTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestActivity>()

    @Test
    fun init_and_activity_recreated() {
        composeTestRule.onNodeWithContentDescription("Increment Button").performClick()

        composeTestRule.onNodeWithContentDescription("Home Label")
            .assertTextEquals("This is Home.\n counter: 1")

        composeTestRule.activityRule.scenario.run {
            recreate()
            moveToState(Lifecycle.State.RESUMED)
        }

        composeTestRule.onNodeWithContentDescription("Home Label")
            .assertTextEquals("This is Home.\n counter: 1")
    }
}

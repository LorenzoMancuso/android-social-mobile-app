package com.example.rathings


import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class SetTabsTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun setTabsTest() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(7000)

        val materialButton = onView(
            allOf(
                withId(R.id.google_sign_in_button),
                childAtPosition(
                    allOf(
                        withId(R.id.email_login_form),
                        childAtPosition(
                            withId(R.id.login_form),
                            0
                        )
                    ),
                    7
                )
            )
        )
        materialButton.perform(scrollTo(), click())

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(7000)

        val bottomNavigationItemView = onView(
            allOf(
                withId(R.id.action_tabs), withContentDescription("tabs"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.bottom_navigation),
                        0
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        bottomNavigationItemView.perform(click())

        val button = onView(
            allOf(
                withText("Videogiochi"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.container),
                        1
                    ),
                    1
                )
            )
        )
        button.perform(scrollTo(), click())

        val button2 = onView(
            allOf(
                withText("Scienza"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.container),
                        3
                    ),
                    1
                )
            )
        )
        button2.perform(scrollTo(), click())

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(7000)

        val button3 = onView(
            allOf(
                withText("Cinema"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.container),
                        2
                    ),
                    0
                )
            )
        )
        button3.perform(scrollTo(), click())

        val bottomNavigationItemView2 = onView(
            allOf(
                withId(R.id.action_profile), withContentDescription("profile"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.bottom_navigation),
                        0
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        bottomNavigationItemView2.perform(click())

        val materialButton2 = onView(
            allOf(
                withId(R.id.btn_signout), withText("Sign out"),
                childAtPosition(
                    allOf(
                        withId(R.id.info_buttons_section),
                        childAtPosition(
                            withId(R.id.info_section),
                            3
                        )
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        materialButton2.perform(click())
    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}

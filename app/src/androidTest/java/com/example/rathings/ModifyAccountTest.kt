package com.example.rathings


import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.roundToInt

@LargeTest
@RunWith(AndroidJUnit4::class)
class ModifyAccountTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun modifyAccountTest() {
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
        bottomNavigationItemView.perform(click())

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(5000)

        val materialButton2 = onView(
            allOf(
                withId(R.id.btn_edit),
                childAtPosition(
                    allOf(
                        withId(R.id.info_buttons_section),
                        childAtPosition(
                            withId(R.id.info_section),
                            3
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        materialButton2.perform(click())

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(7000)

        val appCompatEditText = onView(
            allOf(
                withId(R.id.txt_name),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.login_form),
                        0
                    ),
                    1
                )
            )
        )
        appCompatEditText.perform(scrollTo(), replaceText("Name " + (1..15).shuffled().first()), closeSoftKeyboard())

        val appCompatEditText3 = onView(
            allOf(
                withId(R.id.txt_surname),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.login_form),
                        0
                    ),
                    2
                )
            )
        )
        appCompatEditText3.perform(scrollTo(), replaceText("Surname " + (1..15).shuffled().first()), closeSoftKeyboard())

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(5000)

        val materialButton3 = onView(
            allOf(
                withId(R.id.confirm_button),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.login_form),
                        0
                    ),
                    7
                )
            )
        )
        materialButton3.perform(scrollTo(), click())

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(7000)

        val materialButton4 = onView(
            allOf(
                withId(R.id.btn_signout),
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
        materialButton4.perform(click())
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

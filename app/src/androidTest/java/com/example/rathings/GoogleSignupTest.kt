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

@LargeTest
@RunWith(AndroidJUnit4::class)
class GoogleSignupTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun googleSignupTest() {
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

        val appCompatEditText = onView(
            allOf(
                withId(R.id.txt_birthdate),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.login_form),
                        0
                    ),
                    3
                )
            )
        )
        appCompatEditText.perform(scrollTo(), replaceText("21/10/2019"), closeSoftKeyboard())

        val appCompatEditText2 = onView(
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
        appCompatEditText2.perform(scrollTo(), replaceText("nome"), closeSoftKeyboard())

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
        appCompatEditText3.perform(scrollTo(), replaceText("cognome"), closeSoftKeyboard())

        val materialButton2 = onView(
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
        materialButton2.perform(scrollTo(), click())

        val button = onView(
            allOf(
                withText("Motori"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.container),
                        0
                    ),
                    0
                )
            )
        )
        button.perform(scrollTo(), click())

        val button2 = onView(
            allOf(
                withText("Tecnologia"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.container),
                        0
                    ),
                    1
                )
            )
        )
        button2.perform(scrollTo(), click())

        val button3 = onView(
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
        button3.perform(scrollTo(), click())

        val bottomNavigationItemView = onView(
            allOf(
                withId(R.id.action_home), withContentDescription("home"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.bottom_navigation),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        bottomNavigationItemView.perform(click())
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

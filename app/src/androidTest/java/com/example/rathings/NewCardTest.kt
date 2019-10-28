package com.example.rathings


import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class NewCardTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Rule
    @JvmField
    var mGrantPermissionRule =
        GrantPermissionRule.grant(
            "android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE"
        )

    @Test
    fun newCardTest() {
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

        val floatingActionButton = onView(
            allOf(
                withId(R.id.add_new_card),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.container),
                        0
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        floatingActionButton.perform(click())

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(7000)

        val appCompatEditText = onView(
            allOf(
                withId(R.id.title_text),
                childAtPosition(
                    allOf(
                        withId(R.id.container),
                        childAtPosition(
                            withClassName(`is`("android.widget.LinearLayout")),
                            2
                        )
                    ),
                    1
                )
            )
        )
        appCompatEditText.perform(scrollTo(), replaceText("Title" + (1..15).shuffled().first()), closeSoftKeyboard())

        val appCompatEditText2 = onView(
            allOf(
                withId(R.id.desc_text),
                childAtPosition(
                    allOf(
                        withId(R.id.container),
                        childAtPosition(
                            withClassName(`is`("android.widget.LinearLayout")),
                            2
                        )
                    ),
                    2
                )
            )
        )
        appCompatEditText2.perform(scrollTo(), replaceText("Description" + (1..15).shuffled().first()), closeSoftKeyboard())

        val materialButton2 = onView(
            allOf(
                withId(R.id.add_categories),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.container_categories),
                        0
                    ),
                    2
                )
            )
        )
        materialButton2.perform(scrollTo(), click())

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(7000)

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
        button2.perform(scrollTo(), click())

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

        val materialButton3 = onView(
            allOf(
                withId(R.id.done),
                childAtPosition(
                    allOf(
                        withId(R.id.toolbar),
                        childAtPosition(
                            withId(R.id.container_toolbar),
                            0
                        )
                    ),
                    0
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
                withId(R.id.multimedia_btn),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.container_multimedia),
                        0
                    ),
                    2
                )
            )
        )
        materialButton4.perform(scrollTo(), click())

        val appCompatTextView = onView(
            allOf(
                withId(android.R.id.title), withText("Image"),
                childAtPosition(
                    childAtPosition(
                        withClassName(`is`("com.android.internal.view.menu.ListMenuItemView")),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        appCompatTextView.perform(click())

        val appCompatTextView2 = onData(anything())
            .inAdapterView(
                allOf(
                    withId(R.id.select_dialog_listview),
                    childAtPosition(
                        withId(R.id.contentPanel),
                        0
                    )
                )
            )
            .atPosition(1)
        appCompatTextView2.perform(click())

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(7000)

        val materialButton5 = onView(
            allOf(
                withId(R.id.publish_card),
                childAtPosition(
                    allOf(
                        withId(R.id.toolbar),
                        childAtPosition(
                            withClassName(`is`("android.widget.LinearLayout")),
                            0
                        )
                    ),
                    0
                )
            )
        )
        materialButton5.perform(scrollTo(), click())

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

        val materialButton6 = onView(
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
        materialButton6.perform(click())
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

package com.example.rathings.Tab

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.example.rathings.R
import java.util.*
import kotlin.collections.ArrayList


class TabsActivity : AppCompatActivity() {

    var tabsObs = TabController.tabsObs
    var listOfSelectedTabs: ArrayList<Tab> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabs)

        // Set data from the request activity
        Log.d("[TABS-ACTIVITY]", intent.extras.get("list_of_selected_tabs").toString())
        listOfSelectedTabs = intent.extras.get("list_of_selected_tabs") as ArrayList<Tab>

        val addCategories = findViewById(R.id.done) as Button
        addCategories.setOnClickListener(View.OnClickListener { addCategories() })

        initTabs()
    }

    fun initTabs() {
        val container = findViewById(R.id.container) as LinearLayout

        // Clean TableLayout
        container.removeAllViews()

        val value = tabsObs.getValue()
        if (value is java.util.ArrayList<*>) {
            val tabs: java.util.ArrayList<Tab> = ArrayList(value.filterIsInstance<Tab>())
            for (i in 0 until tabs.size) {
                var linearLayout = LinearLayout(this)

                if (i != 0) {
                    // Get the last layout If childCount exists
                    linearLayout = container.getChildAt(container.childCount - 1) as LinearLayout
                }

                if (linearLayout.childCount == 0 || linearLayout.childCount == 2) {
                    // If last layout.childCount == 2 OR It's the first tab set new layout
                    linearLayout = LinearLayout(this)
                    var params : LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    linearLayout.layoutParams = params
                    linearLayout.orientation = LinearLayout.HORIZONTAL
                    linearLayout.setPadding(5,5,5,5)
                    container.addView(linearLayout)
                }

                var button = Button(this)
                var params : LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (100 * resources.displayMetrics.density + 0.5f).toInt(), 1F)
                button.layoutParams = params
                button.gravity = Gravity.CENTER

                button.setBackgroundColor(Color.parseColor("#eeecec"))
                button.setTextColor(Color.BLACK)
                button.text = tabs[i].value
                button.setTypeface(button.typeface, Typeface.ITALIC)

                var selectedTab = false
                for (j in 0 until listOfSelectedTabs.size) {
                    if(listOfSelectedTabs[j].id.toInt() == tabs[i].id.toInt()) {
                        selectedTab = true
                        button.setBackgroundColor(Color.parseColor(listOfSelectedTabs[j].color))
                        button.setTextColor(Color.parseColor("#EEECEC"))
                    }
                }

                button.setOnClickListener(View.OnClickListener { setTab(tabs[i], selectedTab) })
                linearLayout.addView(button)
            }
        }
    }

    fun setTab(tab: Tab, value: Boolean) {
        if (value) {
            // I have to use the FOR loop instead of only .remove(tab) because
            // the data could come from the starting intent and have memory cells different from those taken from tabsObs
            for (i in 0 until listOfSelectedTabs.size) {
                if(listOfSelectedTabs[i].id == tab.id) {
                    listOfSelectedTabs.removeAt(i)
                    break
                }
            }
        } else {
            listOfSelectedTabs.add(tab)
        }
        initTabs()
    }

    fun addCategories() {
        val resultIntent = Intent()
        resultIntent.putExtra("added_categories", listOfSelectedTabs)
        setResult(5, resultIntent)
        finish()
    }
}

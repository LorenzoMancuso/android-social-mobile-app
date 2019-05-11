package com.example.rathings

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import java.util.*
import kotlin.collections.ArrayList


class TabsActivity : AppCompatActivity() {

    var tabsObs = FirebaseUtils.tabsObservable
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
        val tableLayout = findViewById(R.id.container) as TableLayout

        // Clean TableLayout
        tableLayout.removeAllViews()

        val value = tabsObs.getValue()
        if (value is java.util.ArrayList<*>) {
            val tabs: java.util.ArrayList<Tab> = ArrayList(value.filterIsInstance<Tab>())
            for (i in 0 until tabs.size) {
                // Add new Row
                var tableRow = TableRow(this)
                // tableLayout.setColumnStretchable(tableLayout.childCount - 1, true)
                tableLayout.addView(tableRow, tableLayout.childCount)

                var button = Button(this)
                var selectedTab = false
                var buttonText = tabs[i].value
                for (j in 0 until listOfSelectedTabs.size) {
                    if(listOfSelectedTabs[j].id.toInt() == tabs[i].id.toInt()) {
                        selectedTab = true
                        buttonText += "(remove)"
                    }
                }
                button.setBackgroundColor(Color.argb(255, 255, Random().nextInt(256), Random().nextInt(256)))
                button.text = buttonText

                // Set image to imageButton
                // imageButton.setImageResource(R.drawable.abc_ic_star_half_black_16dp)
                button.setOnClickListener(View.OnClickListener { setTab(tabs[i], selectedTab) })
                tableRow.addView(button)
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

package com.example.rathings.Tab

import android.util.Log
import com.example.rathings.utils.CustomObservable
import com.example.rathings.FirebaseUtils
import java.util.*

object TabController: Observer {

    var tabsObservable= FirebaseUtils.tabsObservable
    var tabsObs: CustomObservable = CustomObservable()

    init {
        tabsObservable.addObserver(this)
    }

    fun getTabs(){
        FirebaseUtils.getTabs()
    }

    override fun update(observableObj: Observable?, data: Any?) {
        when(observableObj) {
            tabsObservable -> {
                val tabs = tabsObservable.getValue()
                tabsObs.setValue(tabs)
                Log.d("[USER-CONTROLLER]", "observable " + tabs?.toString())
            }
            else -> Log.d("[USER-CONTROLLER]", "observable not recognized $data")
        }
    }

    fun toMutableMapForUser(interests: MutableList<Int>): MutableMap<String,Any> {
        val res:MutableMap<String,Any> = mutableMapOf<String,Any>()
        var i = 0
        while (i < interests.size) {
            res.set("${i}",interests[i])
            i++
        }
        return res
    }

}
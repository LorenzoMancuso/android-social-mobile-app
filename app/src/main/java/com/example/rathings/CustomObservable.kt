package com.example.rathings

import java.util.*

class CustomObservable : Observable() {

    private var value:Any? = null

    /**
     * @param value
     * the value to set
     */

    fun setValue(new_value: Any?) {
        value = new_value
        setChanged()
        notifyObservers()
    }

    fun getValue() :Any?{
        return value
    }
}
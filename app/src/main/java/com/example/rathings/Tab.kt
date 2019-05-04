package com.example.rathings

import android.util.Log

class Tab {
    public var id:String=""
    public var value:String=""

    constructor(id:String, value:String) {
        this.id = id
        this.value = value
    }

    override fun toString():String {
        var str="{"
        str+="'id': $id, "
        str+="'title': $value} "
        return str
    }

    fun toMutableMap() :MutableMap<String,Any> {
        val res:MutableMap<String,Any> = mutableMapOf<String,Any>();
        res.set("id",id)
        res.set("value",value)
        return res
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
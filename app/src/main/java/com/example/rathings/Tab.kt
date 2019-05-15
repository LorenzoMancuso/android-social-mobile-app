package com.example.rathings

import java.io.Serializable

class Tab(): Serializable {
    public var id:String=""
    public var value:String=""

    constructor(id:String) :this() {}
    constructor(id:String, value:String) : this(id) {
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

}
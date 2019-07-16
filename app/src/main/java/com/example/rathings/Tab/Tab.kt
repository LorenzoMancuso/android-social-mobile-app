package com.example.rathings.Tab

import java.io.Serializable

class Tab(): Serializable {
    public var id:Int=-1
    public var value:String=""
    public var color:String=""

    constructor(id:Int) :this() {}
    constructor(id:Int, value:String, color:String) : this(id) {
        this.id = id
        this.value = value
        this.color = color
    }

    override fun toString():String {
        var str="{"
        str+="'id': $id, "
        str+="'value': $value} "
        str+="'color': $color} "
        return str
    }

    fun toMutableMap() :MutableMap<String,Any> {
        val res:MutableMap<String,Any> = mutableMapOf<String,Any>();
        res.set("id",id)
        res.set("value",value)
        res.set("color",color)
        return res
    }

}
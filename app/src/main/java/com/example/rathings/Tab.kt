package com.example.rathings

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
}
package com.example.rathings

class Comment() {
    public var id:Long=0
    public var text:String=""
    public var timestamp:Int=0
    public var user:String=""
    public var userObj:User=User()

    constructor(id:String) :this() {}
    constructor(id:String, text: String, timestamp: String, user: String) : this(id) {}

    override fun toString():String {
        var str="{"
        str+="'id': $id, "
        str+="'text': $text, "
        str+="'timestamp': $timestamp, "
        str+="'user': $userObj} "
        return str
    }

    fun toMutableMap() :MutableMap<String,Any> {
        val res:MutableMap<String,Any> = mutableMapOf<String,Any>();
        res.set("id",id)
        res.set("text",text)
        res.set("user",user)
        res.set("timestamp",timestamp)
        res.set("user",user)

        return res
    }
}
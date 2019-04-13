package com.example.rathings

class User() {
    public var id:String=""
    public var name: String=""
    public var followed: MutableList<Any> = ArrayList()
    public var followers: MutableList<Any> = ArrayList()

    /**
     * USERNAME
     * EMAIL
     * NAME
     * SURNAME
     * BIRTHDATE
     * COUNTRY
     * CITY
     * PROFESSION
     * SUBSCRIPTION DATE
     * LISTA FOLLOWER
     * LISTA FOLLOWED*/
    constructor(id:String) :this() {}
    constructor(id:String, name: String, followed: Array<Any>, followers: Array<Any>) : this(id) {}

    override fun toString():String {
        var str="{"
        str+="'id': $id, "
        str+="'name': $name, "
        str+="'followed': ${followed}, "
        str+="'followers': ${followers}}"
        return str
    }
}
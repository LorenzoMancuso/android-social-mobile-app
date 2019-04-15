package com.example.rathings

class User() {
    public var id:String=""
    public var name: String=""
    public var surname: String=""
    public var birth_date: Int=0

    public var country: String=""
    public var city: String=""
    public var profession: String=""
    public var subscription_date: Int=0

    public var followed: MutableList<Any> = ArrayList()
    public var followers: MutableList<Any> = ArrayList()
    public var profile_image: String = ""

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
    constructor(id:String, name: String, surname: String, birth_date: String, country: String, city: String, profession: String, subscription_date: String, followed: Array<Any>, followers: Array<Any>, profile_image: String) : this(id) {}

    override fun toString():String {
        var str="{"
        str+="'id': $id, "
        str+="'name': $name, "
        str+="'surname': $surname, "
        str+="'birth_date': $birth_date, "
        str+="'country': $country, "
        str+="'city': $city, "
        str+="'profession': $profession, "
        str+="'subscription_date': $subscription_date, "
        str+="'followed': ${followed}, "
        str+="'followers': ${followers}, "
        str+="'profile_image': ${profile_image}}"
        return str
    }

    fun toMutableMap(user: User) :MutableMap<String,Any> {
        val res:MutableMap<String,Any> = mutableMapOf<String,Any>();
        res.set("id",id)
        res.set("name",name)
        res.set("surname",surname)
        res.set("birth_date",birth_date)
        res.set("country",country)
        res.set("city",city)
        res.set("profession",profession)
        res.set("subscription_date",subscription_date)
        res.set("followed",followed)
        res.set("followers",followers)
        res.set("profile_image",profile_image)

        return res
    }
}
package com.example.rathings

import com.example.rathings.User.User
import java.io.Serializable

class Notification(): Serializable {
    public var id:String=""
    public var idUser:String=""
    public var text:String=""
    public var timestamp:Int=0
    public var read:Boolean=false
    public var targetType:String=""
    public var targetId:String=""

    constructor(id:String) :this() {this.id = id}
    constructor(id:String, idUser:String, text: String, timestamp: Int, read: Boolean, targetType: String, targetId: String) : this(id) {
        this.idUser = idUser
        this.text = text
        this.timestamp = timestamp
        this.read = read
        this.targetType = targetType
        this.targetId = targetId
    }

    override fun toString():String {
        var str="{"
        str+="'id': $id, "
        str+="'idUser': $idUser, "
        str+="'text': $text, "
        str+="'timestamp': $timestamp, "
        str+="'targetType': $targetType, "
        str+="'targetId': $targetId, "
        str+="'read': $read} "
        return str
    }

    fun toMutableMap() :MutableMap<String,Any> {
        val res:MutableMap<String,Any> = mutableMapOf<String,Any>();
        res.set("id",id)
        res.set("idUser",idUser)
        res.set("text",text)
        res.set("timestamp",timestamp)
        res.set("targetType",targetType)
        res.set("targetId",targetId)
        res.set("read",read)

        return res
    }
}
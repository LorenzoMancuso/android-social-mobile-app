package com.example.rathings

import java.io.Serializable

class Card(): Serializable {
    /**
     * LISTA MULTIMEDIA
     * DESCRIZIONE
     * DATA CREAZIONE
     * LISTA COMMENTI (OGGETTO COMMENTO CON UTENTE,TESTO)
     * RATING
     * UTENTE
     * LISTA CATEGORIE DI APPARTENENZA
     * LISTA VOTI RICEVUTI (OGGETTO VOTO CON UTENTE,VALORE)*/

    // NOTA: Ogni oggetto è diventato Serializable, permettendoci così di passarlo come oggetto nei putExtra di un Intent
    public var id:String=""
    public var title:String=""
    public var description: String=""
    public var user: String=""
    public var timestamp: Int=0
    public var category: MutableList<Int> = ArrayList()
    // public var categoryObj: MutableList<Tab> = ArrayList()
    public var userObj:User=User()
    public var multimedia: MutableList<String> = ArrayList()
    public var comments: MutableList<Comment> = ArrayList()

    public var likelihood:Double = 0.0;

    constructor(id:String) :this() {}
    constructor(id:String, title: String, description: String, user: String, timestamp: Int, category: MutableList<Int>, comments: MutableList<Any>) : this(id) {}

    override fun toString():String {
        var str="{"
        str+="'id': $id, "
        str+="'title': $title, "
        str+="'description': $description, "
        str+="'timestamp': $timestamp, "
        str+="'category': $category, "
        str+="'multimedia': $multimedia, "
        str+="'comments': $comments, "
        str+="'user': $userObj} "
        return str
    }

    fun toMutableMap() :MutableMap<String,Any> {
        val res:MutableMap<String,Any> = mutableMapOf<String,Any>();
        res.set("id",id)
        res.set("title",title)
        res.set("description",description)
        res.set("user",user)
        res.set("timestamp",timestamp)
        res.set("category",category)
        res.set("multimedia",multimedia)
        res.set("comments",comments)

        return res
    }

}
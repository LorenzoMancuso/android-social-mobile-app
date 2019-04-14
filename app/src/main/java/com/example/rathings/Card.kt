package com.example.rathings

class Card() {
    /**
     * LISTA MULTIMEDIA
     * DESCRIZIONE
     * DATA CREAZIONE
     * LISTA COMMENTI (OGGETTO COMMENTO CON UTENTE,TESTO)
     * RATING
     * UTENTE
     * LISTA CATEGORIE DI APPARTENENZA
     * LISTA VOTI RICEVUTI (OGGETTO VOTO CON UTENTE,VALORE)*/

    public var id:String=""
    public var title:String=""
    public var description: String=""
    public var user: String=""
    public var timestamp: Int=0
    //public var multimedia: MutableList<Any> = ArrayList()

    constructor(id:String) :this() {}
    constructor(id:String, title: String, description: String, user: String, timestamp: Int) : this(id) {}

    override fun toString():String {
        var str="{"
        str+="'id': $id, "
        str+="'title': $title, "
        str+="'description': $description, "
        str+="'user': $user, "
        str+="'timestamp': $timestamp}"
        return str
    }

}
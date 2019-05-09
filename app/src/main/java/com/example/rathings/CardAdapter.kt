package com.example.rathings

import android.content.Intent
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

class CardAdapter(private val mDataList: ArrayList<Card>) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    var internalObservableCard :CustomObservable=CustomObservable()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card, parent, false)

        view?.findViewById<TextView>(R.id.user)!!.setOnClickListener {
            val uid=view.findViewById<TextView>(R.id.id_user).text.toString()
            if(FirebaseUtils.isCurrentUser(uid)){
                val intent = Intent(parent.context, HomeActivity::class.java)
                intent.putExtra("mode", "profile");
                parent.context.startActivity(intent)
            }else{
                val intent = Intent(parent.context, ProfileActivity::class.java)
                intent.putExtra("user", uid);
                parent.context.startActivity(intent)
            }
        }

        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.user.text = "${mDataList[position].userObj.name} ${mDataList[position].userObj.surname}"
        holder.id_user.text = mDataList[position].user
        holder.title.text = mDataList[position].title
        holder.description.text = mDataList[position].description

        holder.itemView.findViewById<CardView>(R.id.cv)!!.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailedCardActivity::class.java)
            val cards: ArrayList<Card> = ArrayList()
            cards.add(mDataList[position])
            intent.putExtra("card", cards)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var user: TextView
        internal var id_user: TextView
        internal var title: TextView
        internal var card: Card
        internal var description: TextView

        init {
            user = itemView.findViewById<View>(R.id.user) as TextView
            id_user = itemView.findViewById<View>(R.id.id_user) as TextView
            card = Card()
            title = itemView.findViewById<View>(R.id.title) as TextView
            description = itemView.findViewById<View>(R.id.description) as TextView
        }
    }
}
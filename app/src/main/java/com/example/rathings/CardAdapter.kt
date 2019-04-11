package com.example.rathings

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class CardAdapter(private val mDataList: ArrayList<Card>) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card, parent, false)
        return CardViewHolder(view)
    }


    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.title.text = mDataList[position].title
        holder.description.text = mDataList[position].description
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var title: TextView
        internal var description: TextView

        init {
            title = itemView.findViewById<View>(R.id.title) as TextView
            description = itemView.findViewById<View>(R.id.description) as TextView
        }
    }
}
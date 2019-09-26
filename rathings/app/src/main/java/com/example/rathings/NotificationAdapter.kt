package com.example.rathings

import android.content.Intent
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.rathings.Card.DetailedCardActivity
import com.example.rathings.R
import com.example.rathings.User.ProfileActivity
import com.squareup.picasso.Picasso
import java.util.*

class NotificationAdapter(private val mDataList: ArrayList<Notification>) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val dateTime = java.util.Date(mDataList[position].timestamp.toLong() * 1000)
        holder.date.text = java.text.SimpleDateFormat("dd-MM-yyyy' - 'HH:mm", Locale.ITALY).format(dateTime)
        holder.text.text = "${mDataList[position].text}"

        if(!mDataList[position].read){
            holder.layout?.setBackgroundColor(Color.parseColor("#FFFFFF"))
        }

        holder.itemView.findViewById<CardView>(R.id.cv)!!.setOnClickListener {
            val intent: Intent
            if (mDataList[position].targetType == "card"){
                intent = Intent(holder.itemView.context, DetailedCardActivity::class.java)
                intent.putExtra("idCard", mDataList[position].targetId)
            } else {
                intent = Intent(holder.itemView.context, ProfileActivity::class.java)
                intent.putExtra("user", mDataList[position].idUser)
            }
            holder.itemView.context.startActivity(intent)

        }
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var layout: LinearLayout? = null
        internal var date: TextView
        internal var text: TextView

        init {
            layout = itemView.findViewById<View>(R.id.layout) as LinearLayout
            date = itemView.findViewById<View>(R.id.date) as TextView
            text = itemView.findViewById<View>(R.id.text) as TextView
        }
    }
}
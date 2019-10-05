package com.example.rathings.Card

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.rathings.FirebaseUtils
import com.example.rathings.HomeActivity
import com.example.rathings.R
import com.example.rathings.User.ProfileActivity
import com.squareup.picasso.Picasso
import java.util.*

class CommentAdapter(private val mDataList: ArrayList<Comment>) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment, parent, false)
        /*view?.findViewById<CardView>(R.id.cv)!!.setOnClickListener {
            val intent = Intent(parent.context, DetailedCardActivity::class.java)
            intent.putExtra("card_position", view?.findViewById<TextView>(R.id.card_position).text);
            parent.context.startActivity(intent)
        }*/
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.user.text = "${mDataList[position].userObj.name} ${mDataList[position].userObj.surname}"
        val date = java.util.Date(mDataList[position].timestamp.toLong() * 1000)
        holder.date.text = java.text.SimpleDateFormat("dd-MM-yyyy' - 'HH:mm", Locale.ITALY).format(date)
        holder.text.text = mDataList[position].text
        if(mDataList[position].userObj.profile_image != "") {
            Picasso.get().load(mDataList[position].userObj.profile_image).into(holder.profile_image)
        }

        holder.profile_image.setOnClickListener { moveToUser(holder, position) }
        holder.user.setOnClickListener { moveToUser(holder, position) }
    }

    fun moveToUser(holder: CommentViewHolder, position: Int) {
        val uid = mDataList[position].userObj.id
        if(FirebaseUtils.isCurrentUser(uid)){
            val intent = Intent(holder.itemView.context, HomeActivity::class.java)
            intent.putExtra("mode", "profile");
            holder.itemView.context.startActivity(intent)
        }else{
            val intent = Intent(holder.itemView.context, ProfileActivity::class.java)
            intent.putExtra("user", uid);
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var profile_image: ImageView
        internal var user: TextView
        internal var date: TextView
        internal var text: TextView

        init {
            profile_image = itemView.findViewById<View>(R.id.profile_image) as ImageView
            user = itemView.findViewById<View>(R.id.user) as TextView
            date = itemView.findViewById<View>(R.id.date) as TextView
            text = itemView.findViewById<View>(R.id.text) as TextView
        }
    }
}
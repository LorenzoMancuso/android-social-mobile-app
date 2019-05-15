package com.example.rathings

import android.content.Intent
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_profile.*
import org.w3c.dom.Text
import java.util.*
import kotlin.collections.ArrayList

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
        holder.comments_size.text = "Comments: " + mDataList[position].comments.size
        holder.ratings.rating = mDataList[position].ratings_average
        holder.date.text =  java.text.SimpleDateFormat("yyyy-MM-dd' - 'HH:mm:ss", Locale.ITALY).format(Date(mDataList[position].timestamp.toLong() * 1000))
        Log.d("[PROFILE-IMAGE]", mDataList[position].userObj.profile_image)
        if(mDataList[position].userObj.profile_image != "") {
            Picasso.get().load(mDataList[position].userObj.profile_image).into(holder.profile_image)
        }

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
        internal var profile_image: CircleImageView
        internal var title: TextView
        internal var card: Card
        internal var description: TextView
        internal var comments_size: TextView
        internal var ratings: RatingBar
        internal var date: TextView

        init {
            user = itemView.findViewById<View>(R.id.user) as TextView
            id_user = itemView.findViewById<View>(R.id.id_user) as TextView
            card = Card()
            profile_image = itemView.findViewById<View>(R.id.profile_image) as CircleImageView
            title = itemView.findViewById<View>(R.id.title) as TextView
            description = itemView.findViewById<View>(R.id.description) as TextView
            comments_size = itemView.findViewById<View>(R.id.comments_size) as TextView
            ratings = itemView.findViewById<View>(R.id.ratings) as RatingBar
            date = itemView.findViewById<View>(R.id.date) as TextView
        }
    }
}
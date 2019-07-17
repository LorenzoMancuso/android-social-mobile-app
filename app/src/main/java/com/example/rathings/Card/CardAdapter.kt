package com.example.rathings.Card

import android.content.Intent
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import com.example.rathings.*
import com.example.rathings.User.ProfileActivity
import com.example.rathings.utils.CustomObservable
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*
import kotlin.collections.ArrayList

class CardAdapter(private val mDataList: ArrayList<Card>) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    var internalObservableCard : CustomObservable =
        CustomObservable()

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

        val scale = holder.itemView.resources.displayMetrics.density

        holder.setIsRecyclable(false)
        if(mDataList[position].userObj.profile_image != "") {
            Picasso.get().load(mDataList[position].userObj.profile_image).resize((50 * scale + 0.5f).toInt(), (50 * scale + 0.5f).toInt()).centerCrop().into(holder.profile_image)
        } else {
            Picasso.get().load(R.drawable.default_avatar).resize((50 * scale + 0.5f).toInt(), (50 * scale + 0.5f).toInt()).centerCrop().into(holder.profile_image)
        }
        var listOfImagesToShow: ArrayList<String> = ArrayList()
        if(mDataList[position].multimedia.size > 0) {
            for (i in mDataList[position].multimedia.indices) {
                if (mDataList[position].multimedia[i].contains("image")) {
                    listOfImagesToShow.add(mDataList[position].multimedia[i])
                }
            }

            /*for (i in 0..3) {
                if (i <= listOfImagesToShow.size-1) {
                    var imageView = ImageView(holder.itemView.context)
                    imageView.layoutParams = LinearLayout.LayoutParams((100 * scale + 0.5f).toInt(), (100 * scale + 0.5f).toInt(), 1F)
                    holder.container_multimedia.addView(imageView)
                    Picasso.get().load(mDataList[position].multimedia[i]).centerCrop().fit().into(imageView)
                } else break
            }*/
            for (i in listOfImagesToShow.indices) {
                if (i == 0) {
                    Picasso.get().load(mDataList[position].multimedia[i]).resize((300 * scale + 0.5f).toInt(), (300 * scale + 0.5f).toInt()).onlyScaleDown().centerInside().into(holder.first_image)
                } else if (i > 0 && i <= 3) {
                    var imageView = ImageView(holder.itemView.context)
                    imageView.setPadding(5, 5, 5, 5)
                    Picasso.get().load(mDataList[position].multimedia[i]).centerCrop().resize((100 * scale + 0.5f).toInt(), (100 * scale + 0.5f).toInt()).into(imageView)
                    holder.container_other_images.addView(imageView)
                } else {
                    holder.more_images_text.visibility = View.VISIBLE
                    holder.more_images_text.text = "and ${i-3} other images"
                }
            }

        }

        holder.itemView.findViewById<CardView>(R.id.cv)!!.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailedCardActivity::class.java)
            intent.putExtra("idCard", mDataList[position].id)
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
        internal var container_multimedia: LinearLayout
        internal var first_image: ImageView
        internal var container_other_images: LinearLayout
        internal var title: TextView
        internal var card: Card
        internal var description: TextView
        internal var comments_size: TextView
        internal var ratings: RatingBar
        internal var date: TextView
        internal var more_images_text: TextView

        init {
            user = itemView.findViewById<View>(R.id.user) as TextView
            id_user = itemView.findViewById<View>(R.id.id_user) as TextView
            card = Card()
            profile_image = itemView.findViewById<View>(R.id.profile_image) as CircleImageView
            container_multimedia = itemView.findViewById<View>(R.id.container_multimedia) as LinearLayout
            container_other_images = itemView.findViewById<View>(R.id.container_other_images) as LinearLayout
            title = itemView.findViewById<View>(R.id.title) as TextView
            description = itemView.findViewById<View>(R.id.description) as TextView
            comments_size = itemView.findViewById<View>(R.id.comments_size) as TextView
            ratings = itemView.findViewById<View>(R.id.ratings) as RatingBar
            date = itemView.findViewById<View>(R.id.date) as TextView
            more_images_text = itemView.findViewById<View>(R.id.more_images_text) as TextView
            first_image = itemView.findViewById<View>(R.id.first_image) as ImageView
        }
    }
}
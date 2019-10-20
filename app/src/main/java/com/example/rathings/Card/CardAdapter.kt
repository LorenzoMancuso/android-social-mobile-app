package com.example.rathings.Card

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.provider.Settings.Global.getString
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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.rathings.*
import com.example.rathings.Tab.Tab
import com.example.rathings.User.ProfileActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.util.*
import kotlin.collections.ArrayList

class CardAdapter(private val mDataList: ArrayList<Card>) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    var tabsObs = FirebaseUtils.tabsObservable

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
        // Set to false Recyclable to avoid delay on image download
        holder.setIsRecyclable(false)

        // Init all data
        holder.user.text = holder.itemView.context.resources.getString(R.string.name_surname, mDataList[position].userObj.name, mDataList[position].userObj.surname)
        holder.id_user.text = mDataList[position].user
        holder.title.text = mDataList[position].title
        if (mDataList[position].title == "") {
            holder.title.visibility = View.GONE
        }
        holder.title.text = mDataList[position].title
        holder.description.text = mDataList[position].description
        holder.comments_size.text = holder.itemView.context.resources.getString(R.string.comment_size, mDataList[position].comments.size)
        holder.ratings.rating = mDataList[position].ratings_average
        holder.date.text =  java.text.SimpleDateFormat("dd-MM-yyyy' - 'HH:mm", Locale.ITALY).format(Date(mDataList[position].timestamp.toLong() * 1000))
        Log.d("[PROFILE-IMAGE]", mDataList[position].userObj.profile_image)

        // Set Profile Image
        if(mDataList[position].userObj.profile_image != "") {
            Glide.with(holder.itemView.context).load(mDataList[position].userObj.profile_image)
                .centerCrop().circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.profile_image)
        } else {
            Glide.with(holder.itemView.context).load(R.drawable.default_avatar)
                .centerCrop().circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.profile_image)
        }

        // Set Categories
        var tabs = tabsObs.getValue() as java.util.ArrayList<Tab>
        for (i in mDataList[position].category.indices) {
            if (i <= 1) {
                for (j in 0 until tabs.size) {
                    if (mDataList[position].category[i] == tabs[j].id) {
                        var chip = Chip(holder.itemView.context)
                        chip.text = tabs[j].value
                        chip.chipBackgroundColor = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_enabled)), intArrayOf(Color.parseColor(tabs[j].color)))
                        chip.setTextColor(Color.WHITE)
                        holder.container_categories.addView(chip)
                    }
                }
            } else {
                holder.other_categories_text.visibility = View.VISIBLE
                holder.other_categories_text.text = holder.itemView.context.resources.getString(R.string.other_tabs, mDataList[position].category.size - i)
                break
            }
        }

        // Set Multimedia
        if(mDataList[position].multimedia.size > 0) {
            for (i in mDataList[position].multimedia.indices) {
                if (i <= 3) {
                    manageMedia(holder, position, i)
                } else {
                    holder.more_images_text.visibility = View.VISIBLE
                    holder.more_images_text.text = holder.itemView.context.resources.getString(R.string.other_media, mDataList[position].multimedia.size - i)
                }
            }

        }

        // Set on click listener for DetailedCardActivity
        holder.itemView.findViewById<CardView>(R.id.cv)!!.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailedCardActivity::class.java)
            intent.putExtra("idCard", mDataList[position].id)
            holder.itemView.context.startActivity(intent)
        }

    }

    fun manageMedia(holder: CardViewHolder, position: Int, index: Int) {
        val scale = holder.itemView.resources.displayMetrics.density

        var imageView = ImageView(holder.itemView.context)
        imageView.setPadding(5, 5, 5, 5)
        imageView.layoutParams = LinearLayout.LayoutParams((120 * scale + 0.5f).toInt(), (120 * scale + 0.5f).toInt(), 1F)

        if (mDataList[position].multimedia[index].contains("image")) {
            Glide.with(holder.itemView.context).load(mDataList[position].multimedia[index])
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)

        } else if (mDataList[position].multimedia[index].contains("video")) {
            val options = RequestOptions().frame(1000.toLong())
            Glide.with(holder.itemView.context).asBitmap().load(mDataList[position].multimedia[index]).apply(options)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)
        }

        holder.container_other_images.addView(imageView)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var user: TextView
        internal var id_user: TextView
        internal var profile_image: ImageView
        internal var container_multimedia: LinearLayout
        internal var container_other_images: LinearLayout
        internal var title: TextView
        internal var card: Card
        internal var description: TextView
        internal var comments_size: TextView
        internal var ratings: RatingBar
        internal var date: TextView
        internal var more_images_text: TextView
        internal var container_categories: ChipGroup
        internal var other_categories_text: TextView

        init {
            user = itemView.findViewById<View>(R.id.user) as TextView
            id_user = itemView.findViewById<View>(R.id.id_user) as TextView
            card = Card()
            profile_image = itemView.findViewById<View>(R.id.profile_image) as ImageView
            container_multimedia = itemView.findViewById<View>(R.id.container_multimedia) as LinearLayout
            container_other_images = itemView.findViewById<View>(R.id.container_other_images) as LinearLayout
            title = itemView.findViewById<View>(R.id.title) as TextView
            description = itemView.findViewById<View>(R.id.description) as TextView
            comments_size = itemView.findViewById<View>(R.id.comments_size) as TextView
            ratings = itemView.findViewById<View>(R.id.ratings) as RatingBar
            date = itemView.findViewById<View>(R.id.date) as TextView
            more_images_text = itemView.findViewById<View>(R.id.more_images_text) as TextView
            container_categories = itemView.findViewById<View>(R.id.container_categories) as ChipGroup
            other_categories_text = itemView.findViewById<View>(R.id.other_categories_text) as TextView
        }
    }

}
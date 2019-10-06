package com.example.rathings.Card

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
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
import com.example.rathings.Tab.Tab
import com.example.rathings.User.ProfileActivity
import com.example.rathings.utils.CustomObservable
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
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

    var listOfVideoPlayers: ArrayList<ExoPlayer> = ArrayList()
    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        // Set to false Recyclable to avoid delay on image download
        holder.setIsRecyclable(false)

        // Clean old video players
        // TODO: Find a method to RELEASE all players
        if (listOfVideoPlayers.size > 0) {
            for (player in listOfVideoPlayers) {
                player.release()
            }
        }

        // Init all data
        holder.user.text = "${mDataList[position].userObj.name} ${mDataList[position].userObj.surname}"
        holder.id_user.text = mDataList[position].user
        holder.title.text = mDataList[position].title
        if (mDataList[position].title == "") {
            holder.title.visibility = View.GONE
        }
        holder.title.text = mDataList[position].title
        holder.description.text = mDataList[position].description
        holder.comments_size.text = "Comments: " + mDataList[position].comments.size
        holder.ratings.rating = mDataList[position].ratings_average
        holder.date.text =  java.text.SimpleDateFormat("dd-MM-yyyy' - 'HH:mm", Locale.ITALY).format(Date(mDataList[position].timestamp.toLong() * 1000))
        Log.d("[PROFILE-IMAGE]", mDataList[position].userObj.profile_image)

        val scale = holder.itemView.resources.displayMetrics.density

        // Set Profile Image
        if(mDataList[position].userObj.profile_image != "") {
            Picasso.get().load(mDataList[position].userObj.profile_image).resize((50 * scale + 0.5f).toInt(), (50 * scale + 0.5f).toInt()).centerCrop().into(holder.profile_image)
        } else {
            Picasso.get().load(R.drawable.default_avatar).resize((50 * scale + 0.5f).toInt(), (50 * scale + 0.5f).toInt()).centerCrop().into(holder.profile_image)
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
                holder.other_categories_text.text = "+ ${mDataList[position].category.size - i} Tabs"
                break
            }
        }

        /*for (i in 0 until mDataList[position].category.size) {
            for (j in 0 until tabs.size) {
                if (mDataList[position].category[i] == tabs[j].id) {
                    var chip = Chip(holder.itemView.context)
                    chip.text = tabs[j].value
                    chip.chipBackgroundColor = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_enabled)), intArrayOf(Color.parseColor(tabs[j].color)))
                    chip.setTextColor(Color.WHITE)
                    holder.container_categories.addView(chip)
                }
            }
        }*/

        // Set Multimedia
        if(mDataList[position].multimedia.size > 0) {

            for (i in mDataList[position].multimedia.indices) {
                if (i <= 3) {
                    manageMedia(holder, position, i)
                } else {
                    holder.more_images_text.visibility = View.VISIBLE
                    holder.more_images_text.text = "+ ${mDataList[position].multimedia.size - i} media"
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

    fun retriveVideoFrameFromVideo(videoPath: String): Bitmap {
        var mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(videoPath, HashMap<String, String>())
        var bitmap = mediaMetadataRetriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST)
        mediaMetadataRetriever.release()
        return bitmap
    }

    fun manageMedia(holder: CardViewHolder, position: Int, index: Int) {
        val scale = holder.itemView.resources.displayMetrics.density

        if (mDataList[position].multimedia[index].contains("image")) {
            var imageView = ImageView(holder.itemView.context)
            imageView.setPadding(5, 5, 5, 5)
            imageView.layoutParams = LinearLayout.LayoutParams((120 * scale + 0.5f).toInt(), (120 * scale + 0.5f).toInt(), 1F)

            Picasso.get().load(mDataList[position].multimedia[index]).centerCrop().fit().into(imageView)
            holder.container_other_images.addView(imageView)

        } else if (mDataList[position].multimedia[index].contains("video")) {
            var playerView = PlayerView(holder.itemView.context)
            val player = ExoPlayerFactory.newSimpleInstance(holder.itemView.context, DefaultTrackSelector())
            var mediaSource = ExtractorMediaSource.Factory(DefaultDataSourceFactory(holder.itemView.context, "rathings")).createMediaSource(Uri.parse(mDataList[position].multimedia[index]))
            var thumbnail = ImageView(holder.itemView.context)

            playerView.layoutParams = LinearLayout.LayoutParams((120 * scale + 0.5f).toInt(), (120 * scale + 0.5f).toInt(), 1F)
            holder.container_other_images.addView(playerView)

            listOfVideoPlayers.add(player)
            playerView.setPadding(5,5,5,5)
            playerView.player = player
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM)
            playerView.useController = false

            thumbnail.setBackgroundColor(Color.parseColor("#90111111"))
            thumbnail.setImageResource(R.drawable.ic_slow_motion_video_white_48dp)
            thumbnail.scaleType = ImageView.ScaleType.CENTER_INSIDE

            playerView.overlayFrameLayout.addView(thumbnail)
            player.prepare(mediaSource)

            /* Version 2: Thumbnail with BITMAP
            * PROBLEM: Really slow
            **/
            /*var imageView = ImageView(holder.itemView.context)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            var bitmap = retriveVideoFrameFromVideo(mDataList[position].multimedia[index])
            bitmap = Bitmap.createScaledBitmap(bitmap, (100 * scale + 0.5f).toInt(), (100 * scale + 0.5f).toInt(), false)
            imageView.setPadding(5, 5, 5, 5)
            imageView.setImageBitmap(bitmap)
            holder.container_other_images.addView(imageView)*/
        }

    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var user: TextView
        internal var id_user: TextView
        internal var profile_image: ImageView
        internal var container_multimedia: LinearLayout
        internal var first_element: LinearLayout
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
            first_element = itemView.findViewById<View>(R.id.first_element) as LinearLayout
            container_categories = itemView.findViewById<View>(R.id.container_categories) as ChipGroup
            other_categories_text = itemView.findViewById<View>(R.id.other_categories_text) as TextView
        }
    }

}
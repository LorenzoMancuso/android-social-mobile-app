package com.example.rathings.User

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.rathings.FirebaseUtils
import com.example.rathings.HomeActivity
import com.example.rathings.R
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class UserAdapter(private val mDataList: ArrayList<User>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            val uid = mDataList[position].id
            if(FirebaseUtils.isCurrentUser(uid)){
                val intent = Intent(it.context, HomeActivity::class.java)
                intent.putExtra("mode", "profile");
                it.context.startActivity(intent)
            }else{
                val intent = Intent(it.context, ProfileActivity::class.java)
                intent.putExtra("user", uid)
                it.context.startActivity(intent)
            }
        }

        holder.name.text = "${mDataList[position].name} ${mDataList[position].surname}"
        holder.profession.text = mDataList[position].profession
        holder.country.text = mDataList[position].city + ", " + mDataList[position].country
        if(mDataList[position].profile_image != "") {
            Glide.with(holder.itemView.context).load(mDataList[position].profile_image)
                .centerCrop().circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.profile_image)
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var name: TextView
        internal var profession: TextView
        internal var country: TextView
        internal var profile_image: CircleImageView

        init {
            name = itemView.findViewById<View>(R.id.name) as TextView
            profession = itemView.findViewById<View>(R.id.profession) as TextView
            country = itemView.findViewById<View>(R.id.country) as TextView
            profile_image = itemView.findViewById<View>(R.id.profile_image) as CircleImageView
        }
    }
}
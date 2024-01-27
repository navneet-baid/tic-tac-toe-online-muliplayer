package com.navneet.tictactoe.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.navneet.tictactoe.R

class ProfileImagesRecyclerAdapter(
    val context: Context,
    private val imagesArray: MutableList<Int>,
    private val profileImage: ImageView
) :
    RecyclerView.Adapter<ProfileImagesRecyclerAdapter.ProfileImagesViewHolder>() {

    class ProfileImagesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgUserImage: ImageView = view.findViewById(R.id.profiles)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileImagesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_profile_images, parent, false)
        return ProfileImagesViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileImagesViewHolder, position: Int) {
        val sharedPreferences =
            context.getSharedPreferences("User Preferences", Context.MODE_PRIVATE)
        holder.imgUserImage.setImageResource(imagesArray[position])
        holder.imgUserImage.setOnClickListener {
            profileImage.setImageResource(imagesArray[position])
            profileImage.tag = imagesArray[position]
            sharedPreferences.edit().putInt("profilePictureTag", imagesArray[position]).apply()
        }
    }

    override fun getItemCount(): Int {
        return imagesArray.size
    }
}
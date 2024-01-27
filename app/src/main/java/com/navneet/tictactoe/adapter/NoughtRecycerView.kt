package com.navneet.tictactoe.adapter

import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView


class NoughtRecycerView(val context: Context,val noughtList: MutableList<Int>): RecyclerView.Adapter<NoughtRecycerView.NoughtImagesViewHolder>() {
    class NoughtImagesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgNoughtImage: ImageView = view.findViewById(com.navneet.tictactoe.R.id.noughts)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoughtImagesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(com.navneet.tictactoe.R.layout.recycler_view_noughts, parent, false)

        return NoughtRecycerView.NoughtImagesViewHolder(view)
    }
    val itemViewList:ArrayList<ImageView> =ArrayList()
    @SuppressLint("UseCompatLoadingForDrawables", "ResourceAsColor")
    override fun onBindViewHolder(holder: NoughtImagesViewHolder, position: Int) {
        val sharedPreferences =
            context.getSharedPreferences("User Preferences", Context.MODE_PRIVATE)
        val currentNought=sharedPreferences.getInt("NoughtPreference", com.navneet.tictactoe.R.drawable.circle)
            itemViewList.add(holder.imgNoughtImage)
        if(noughtList[position]==currentNought)
            holder.imgNoughtImage.setBackgroundColor(com.navneet.tictactoe.R.color.white)
        else
            holder.imgNoughtImage.setBackgroundColor(android.R.color.transparent)
        holder.imgNoughtImage.setImageResource(noughtList[position])
        holder.imgNoughtImage.setOnClickListener {
            sharedPreferences.edit().putInt("NoughtPreference", noughtList[position]).apply()
            for (tempItemView in itemViewList) {
                tempItemView.setBackgroundResource(R.color.transparent)
            }
            holder.imgNoughtImage.setBackgroundColor(com.navneet.tictactoe.R.color.white)
        }
    }

    override fun getItemCount(): Int {
        return noughtList.size
    }
}
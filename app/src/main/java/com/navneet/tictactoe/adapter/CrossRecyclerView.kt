package com.navneet.tictactoe.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.navneet.tictactoe.R

class CrossRecyclerView(val context: Context,val crossList: MutableList<Int>): RecyclerView.Adapter<CrossRecyclerView.CrossImagesViewHolder>()  {
    class CrossImagesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgCrossImage: ImageView = view.findViewById(R.id.cross)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrossImagesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_cross, parent, false)
        return CrossRecyclerView.CrossImagesViewHolder(view)
    }
    val itemViewList:ArrayList<ImageView> =ArrayList()
    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: CrossImagesViewHolder, position: Int) {
        val sharedPreferences =
            context.getSharedPreferences("User Preferences", Context.MODE_PRIVATE)
        val currentCross=sharedPreferences.getInt("CrossPreference",R.drawable.cross)
        itemViewList.add(holder.imgCrossImage)
        if(crossList[position]==currentCross)
            holder.imgCrossImage.setBackgroundColor(R.color.white)
        else
            holder.imgCrossImage.setBackgroundColor(android.R.color.transparent)
        holder.imgCrossImage.setImageResource(crossList[position])
        holder.imgCrossImage.setOnClickListener {
            for (tempItemView in itemViewList) {
                tempItemView.setBackgroundResource(android.R.color.transparent)
            }
            holder.imgCrossImage.setBackgroundColor(R.color.white)
            sharedPreferences.edit().putInt("CrossPreference", crossList[position]).apply()
        }
    }

    override fun getItemCount(): Int {
        return crossList.size
    }

}
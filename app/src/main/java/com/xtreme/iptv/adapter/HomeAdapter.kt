package com.xtreme.iptv.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xtreme.iptv.R

data class HomeCard(
    val id: Int,
    val title: String,
    val poster: String,
    val type: String, // "movie" or "series"
    val ext: String = "mp4"
)

class HomeAdapter(
    private val items: List<HomeCard>,
    private val onClick: (HomeCard) -> Unit
) : RecyclerView.Adapter<HomeAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val poster: ImageView = v.findViewById(R.id.homePoster)
        val name: TextView = v.findViewById(R.id.homeName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_home_card, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.name.text = item.title
        if (item.poster.isNotEmpty()) {
            Glide.with(holder.itemView).load(item.poster).into(holder.poster)
        } else {
            holder.poster.setImageDrawable(null)
        }
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size
}

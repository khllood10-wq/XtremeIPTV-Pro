package com.xtreme.iptv.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xtreme.iptv.R
import com.xtreme.iptv.model.SeriesItem

class SeriesAdapter(
    private val items: List<SeriesItem>,
    private val onClick: (SeriesItem) -> Unit
) : RecyclerView.Adapter<SeriesAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val cover: ImageView = v.findViewById(R.id.vodPoster)
        val name: TextView = v.findViewById(R.id.vodName)
        val rating: TextView = v.findViewById(R.id.vodRating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_vod, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val s = items[position]
        holder.name.text = s.name
        holder.rating.text = if (s.rating.isNotEmpty()) "★ ${s.rating}" else ""
        
        if (s.cover.isNotEmpty()) {
            Glide.with(holder.itemView).load(s.cover).into(holder.cover)
        } else {
            holder.cover.setImageDrawable(null)
        }
        
        holder.itemView.setOnClickListener { onClick(s) }
    }

    override fun getItemCount() = items.size
}

package com.xtreme.iptv.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xtreme.iptv.R
import com.xtreme.iptv.model.VodItem

class VodAdapter(
    private val items: List<VodItem>,
    private val onClick: (VodItem) -> Unit
) : RecyclerView.Adapter<VodAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val poster: ImageView = v.findViewById(R.id.vodPoster)
        val name: TextView = v.findViewById(R.id.vodName)
        val rating: TextView = v.findViewById(R.id.vodRating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_vod, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val v = items[position]
        holder.name.text = v.name
        holder.rating.text = if (v.rating.isNotEmpty()) "★ ${v.rating}" else ""
        
        if (v.streamIcon.isNotEmpty()) {
            Glide.with(holder.itemView).load(v.streamIcon).into(holder.poster)
        } else {
            holder.poster.setImageDrawable(null)
        }
        
        holder.itemView.setOnClickListener { onClick(v) }
    }

    override fun getItemCount() = items.size
}

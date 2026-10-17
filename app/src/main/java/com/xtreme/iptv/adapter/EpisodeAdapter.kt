package com.xtreme.iptv.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xtreme.iptv.R

data class Episode(
    val id: String,
    val num: Int,
    val title: String,
    val ext: String
)

class EpisodeAdapter(
    private val items: List<Episode>,
    private val onClick: (Episode) -> Unit
) : RecyclerView.Adapter<EpisodeAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val num: TextView = v.findViewById(R.id.epNum)
        val title: TextView = v.findViewById(R.id.epTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_episode, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ep = items[position]
        holder.num.text = ep.num.toString()
        holder.title.text = ep.title
        holder.itemView.setOnClickListener { onClick(ep) }
    }

    override fun getItemCount() = items.size
}

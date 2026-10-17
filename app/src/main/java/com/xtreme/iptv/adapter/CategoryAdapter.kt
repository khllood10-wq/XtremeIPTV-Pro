package com.xtreme.iptv.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xtreme.iptv.R
import com.xtreme.iptv.model.Category

class CategoryAdapter(
    private val items: List<Category>,
    private val onClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.VH>() {

    private var selectedPos = 0

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val txt: TextView = v.findViewById(R.id.catTxt)
        val root: View = v
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val c = items[position]
        holder.txt.text = c.name

        val isSelected = position == selectedPos
        if (isSelected) {
            holder.txt.setTextColor(Color.parseColor("#000000"))
            holder.txt.setBackgroundResource(R.drawable.bg_cat_selected)
        } else {
            holder.txt.setTextColor(Color.parseColor("#7C8699"))
            holder.txt.setBackgroundResource(R.drawable.bg_cat_default)
        }

        holder.root.setOnClickListener {
            val old = selectedPos
            selectedPos = holder.adapterPosition
            notifyItemChanged(old)
            notifyItemChanged(selectedPos)
            onClick(c)
        }
    }

    override fun getItemCount() = items.size
}

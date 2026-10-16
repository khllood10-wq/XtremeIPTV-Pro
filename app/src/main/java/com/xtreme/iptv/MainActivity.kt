package com.xtreme.iptv

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.xtreme.iptv.api.XtreamApi
import com.xtreme.iptv.model.Channel
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var rv: RecyclerView
    private lateinit var swipe: SwipeRefreshLayout
    private lateinit var empty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rv = findViewById(R.id.recycler)
        swipe = findViewById(R.id.swipe)
        empty = findViewById(R.id.emptyTxt)

        rv.layoutManager = LinearLayoutManager(this)
        swipe.setOnRefreshListener { loadChannels() }

        loadChannels()
    }

    private fun loadChannels() {
        swipe.isRefreshing = true
        scope.launch {
            val list = withContext(Dispatchers.IO) { XtreamApi.getLiveStreams() }
            swipe.isRefreshing = false
            empty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            rv.adapter = ChannelAdapter(list) { ch ->
                startActivity(Intent(this@MainActivity, PlayerActivity::class.java).apply {
                    putExtra("name", ch.name)
                    putExtra("url", XtreamApi.streamUrl(ch.streamId))
                    putExtra("logo", ch.streamIcon)
                })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    inner class ChannelAdapter(
        private val items: List<Channel>,
        private val onClick: (Channel) -> Unit
    ) : RecyclerView.Adapter<ChannelAdapter.VH>() {

        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val logo: ImageView = v.findViewById(R.id.channelLogo)
            val name: TextView = v.findViewById(R.id.channelName)
            val num: TextView = v.findViewById(R.id.channelNum)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_channel, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val c = items[position]
            holder.name.text = c.name
            holder.num.text = c.num.toString().padStart(3, '0')
            if (c.streamIcon.isNotEmpty()) {
                Glide.with(holder.itemView).load(c.streamIcon).into(holder.logo)
            }
            holder.itemView.setOnClickListener { onClick(c) }
        }

        override fun getItemCount() = items.size
    }
}

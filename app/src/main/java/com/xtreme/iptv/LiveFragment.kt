package com.xtreme.iptv

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.xtreme.iptv.adapter.CategoryAdapter
import com.xtreme.iptv.api.XtreamApi
import com.xtreme.iptv.model.Category
import com.xtreme.iptv.model.Channel
import com.xtreme.iptv.utils.FavManager
import kotlinx.coroutines.*

class LiveFragment : Fragment() {

    private var _binding: View? = null
    private val binding get() = _binding!!
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private lateinit var rv: RecyclerView
    private lateinit var categoryRv: RecyclerView
    private lateinit var swipe: SwipeRefreshLayout
    private lateinit var empty: TextView
    private lateinit var channelCount: TextView
    private lateinit var searchInput: EditText
    private lateinit var clearSearch: ImageView
    private lateinit var filterFavBtn: ImageView

    private var currentCategoryId: String? = null
    private var searchQuery: String = ""
    private var showOnlyFavs = false
    private var allChannels: List<Channel> = emptyList()
    private var filteredChannels: List<Channel> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = inflater.inflate(R.layout.fragment_live, container, false)
        return binding
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv = view.findViewById(R.id.recycler)
        categoryRv = view.findViewById(R.id.categoryRv)
        swipe = view.findViewById(R.id.swipe)
        empty = view.findViewById(R.id.emptyTxt)
        channelCount = view.findViewById(R.id.channelCount)
        searchInput = view.findViewById(R.id.searchInput)
        clearSearch = view.findViewById(R.id.clearSearch)
        filterFavBtn = view.findViewById(R.id.filterFavBtn)

        rv.layoutManager = LinearLayoutManager(requireContext())
        categoryRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        swipe.setOnRefreshListener { loadChannels(currentCategoryId) }
        setupSearch()

        filterFavBtn.setOnClickListener {
            showOnlyFavs = !showOnlyFavs
            filterFavBtn.setColorFilter(if (showOnlyFavs) Color.parseColor("#00D9FF") else Color.parseColor("#7C8699"))
            applyFilter()
        }

        loadCategories()
    }

    private fun setupSearch() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {
                searchQuery = s?.toString()?.trim() ?: ""
                clearSearch.visibility = if (searchQuery.isEmpty()) View.GONE else View.VISIBLE
                applyFilter()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        clearSearch.setOnClickListener {
            searchInput.setText("")
            searchQuery = ""
            clearSearch.visibility = View.GONE
            applyFilter()
        }
    }

    private fun loadCategories() {
        scope.launch {
            val list = withContext(Dispatchers.IO) { XtreamApi.getLiveCategories() }
            val allList = mutableListOf(Category("", "الكل"))
            allList.addAll(list)

            categoryRv.adapter = CategoryAdapter(allList) { cat ->
                currentCategoryId = if (cat.id.isEmpty()) null else cat.id
                searchInput.setText("")
                searchQuery = ""
                clearSearch.visibility = View.GONE
                loadChannels(currentCategoryId)
            }
            loadChannels(null)
        }
    }

    private fun loadChannels(categoryId: String?) {
        swipe.isRefreshing = true
        scope.launch {
            val list = withContext(Dispatchers.IO) { XtreamApi.getLiveStreams(categoryId) }
            swipe.isRefreshing = false
            allChannels = list
            applyFilter()
        }
    }

    private fun applyFilter() {
        val favs = FavManager.getFavs(requireContext())
        
        filteredChannels = allChannels.filter { ch ->
            val matchSearch = searchQuery.isEmpty() || ch.name.contains(searchQuery, ignoreCase = true)
            val matchFav = !showOnlyFavs || favs.contains(ch.streamId.toString())
            matchSearch && matchFav
        }

        empty.visibility = if (filteredChannels.isEmpty()) View.VISIBLE else View.GONE
        channelCount.text = filteredChannels.size.toString()

        rv.adapter = ChannelAdapter(filteredChannels) { ch ->
            startActivity(Intent(requireContext(), PlayerActivity::class.java).apply {
                putExtra("name", ch.name)
                putExtra("url", XtreamApi.streamUrl(ch.streamId))
                putExtra("streamId", ch.streamId)
                putExtra("logo", ch.streamIcon)
            })
        }
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null) applyFilter()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
            val favBtn: ImageView = v.findViewById(R.id.favBtn)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_channel, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val c = items[position]
            holder.name.text = c.name
            holder.num.text = c.num.toString().padStart(3, '0')
            
            if (c.streamIcon.isNotEmpty()) {
                Glide.with(holder.itemView).load(c.streamIcon).into(holder.logo)
            }

            val isFav = FavManager.isFav(requireContext(), c.streamId)
            holder.favBtn.setColorFilter(if (isFav) Color.parseColor("#FF4081") else Color.parseColor("#7C8699"))

            holder.favBtn.setOnClickListener {
                val added = FavManager.toggleFav(requireContext(), c.streamId)
                holder.favBtn.setColorFilter(if (added) Color.parseColor("#FF4081") else Color.parseColor("#7C8699"))
                Toast.makeText(requireContext(), if (added) "أضيف للمفضلة" else "أزيل من المفضلة", Toast.LENGTH_SHORT).show()
                if (showOnlyFavs && !added) applyFilter()
            }

            holder.itemView.setOnClickListener { onClick(c) }
        }

        override fun getItemCount() = items.size
    }
}

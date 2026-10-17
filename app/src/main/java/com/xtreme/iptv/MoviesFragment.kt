package com.xtreme.iptv

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xtreme.iptv.adapter.CategoryAdapter
import com.xtreme.iptv.adapter.VodAdapter
import com.xtreme.iptv.api.XtreamApi
import com.xtreme.iptv.model.Category
import kotlinx.coroutines.*

class MoviesFragment : Fragment() {

    private var _binding: View? = null
    private val binding get() = _binding!!
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = inflater.inflate(R.layout.fragment_movies, container, false)
        return binding
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val catRv = view.findViewById<RecyclerView>(R.id.vodCatRv)
        val rv = view.findViewById<RecyclerView>(R.id.vodRv)
        val empty = view.findViewById<TextView>(R.id.vodEmpty)

        catRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rv.layoutManager = GridLayoutManager(requireContext(), 3)

        scope.launch {
            val cats = withContext(Dispatchers.IO) { XtreamApi.getVodCategories() }
            val allList = mutableListOf(Category("", "الكل"))
            allList.addAll(cats)

            catRv.adapter = CategoryAdapter(allList) { cat ->
                loadVod(rv, empty, if (cat.id.isEmpty()) null else cat.id)
            }
            loadVod(rv, empty, null)
        }
    }

    private fun loadVod(rv: RecyclerView, empty: TextView, catId: String?) {
        scope.launch {
            val list = withContext(Dispatchers.IO) { XtreamApi.getVodStreams(catId) }
            if (_binding == null) return@launch
            
            empty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            rv.adapter = VodAdapter(list) { v ->
                startActivity(Intent(requireContext(), PlayerActivity::class.java).apply {
                    putExtra("name", v.name)
                    putExtra("url", XtreamApi.vodUrl(v.streamId, v.containerExtension))
                    putExtra("logo", v.streamIcon)
                    putExtra("streamId", 0)
                })
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        scope.cancel()
    }
}

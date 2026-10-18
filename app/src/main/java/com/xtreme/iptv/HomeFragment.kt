package com.xtreme.iptv

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xtreme.iptv.adapter.HomeAdapter
import com.xtreme.iptv.adapter.HomeCard
import com.xtreme.iptv.api.XtreamApi
import kotlinx.coroutines.*

class HomeFragment : Fragment() {

    private var _binding: View? = null
    private val binding get() = _binding!!
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = inflater.inflate(R.layout.fragment_home, container, false)
        return binding
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val moviesRv = view.findViewById<RecyclerView>(R.id.homeMoviesRv)
        val seriesRv = view.findViewById<RecyclerView>(R.id.homeSeriesRv)
        val moviesProgress = view.findViewById<ProgressBar>(R.id.moviesProgress)
        val seriesProgress = view.findViewById<ProgressBar>(R.id.seriesProgress)

        moviesRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        seriesRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // جلب أحدث الأفلام
        scope.launch {
            val movies = withContext(Dispatchers.IO) { XtreamApi.getVodStreams() }
            if (_binding != null) {
                moviesProgress.visibility = View.GONE
                if (movies.isNotEmpty()) {
                    val cards = movies.take(20).map { HomeCard(it.streamId, it.name, it.streamIcon, "movie", it.containerExtension) }
                    moviesRv.adapter = HomeAdapter(cards) { card ->
                        startActivity(Intent(requireContext(), PlayerActivity::class.java).apply {
                            putExtra("name", card.title)
                            putExtra("url", XtreamApi.vodUrl(card.id, card.ext))
                            putExtra("logo", card.poster)
                            putExtra("streamId", 0)
                        })
                    }
                }
            }
        }

        // جلب أحدث المسلسلات
        scope.launch {
            val series = withContext(Dispatchers.IO) { XtreamApi.getSeries() }
            if (_binding != null) {
                seriesProgress.visibility = View.GONE
                if (series.isNotEmpty()) {
                    val cards = series.take(20).map { HomeCard(it.seriesId, it.name, it.cover, "series") }
                    seriesRv.adapter = HomeAdapter(cards) { card ->
                        startActivity(Intent(requireContext(), SeriesDetailActivity::class.java).apply {
                            putExtra("seriesId", card.id)
                            putExtra("name", card.title)
                        })
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        scope.cancel()
    }
}

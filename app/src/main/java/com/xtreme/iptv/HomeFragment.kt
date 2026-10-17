package com.xtreme.iptv

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xtreme.iptv.adapter.SeriesAdapter
import com.xtreme.iptv.adapter.VodAdapter
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

        moviesRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        seriesRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // جلب أحدث الأفلام
        scope.launch {
            val movies = withContext(Dispatchers.IO) { XtreamApi.getVodStreams() }
            if (_binding != null && movies.isNotEmpty()) {
                moviesRv.adapter = VodAdapter(movies.take(20)) { v ->
                    startActivity(Intent(requireContext(), PlayerActivity::class.java).apply {
                        putExtra("name", v.name)
                        putExtra("url", XtreamApi.vodUrl(v.streamId, v.containerExtension))
                        putExtra("logo", v.streamIcon)
                        putExtra("streamId", 0)
                    })
                }
            }
        }

        // جلب أحدث المسلسلات
        scope.launch {
            val series = withContext(Dispatchers.IO) { XtreamApi.getSeries() }
            if (_binding != null && series.isNotEmpty()) {
                seriesRv.adapter = SeriesAdapter(series.take(20)) { s ->
                    Toast.makeText(requireContext(), "قريباً: حلقات ${s.name}", Toast.LENGTH_SHORT).show()
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

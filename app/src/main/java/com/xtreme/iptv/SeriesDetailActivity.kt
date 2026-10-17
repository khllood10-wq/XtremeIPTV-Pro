package com.xtreme.iptv

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xtreme.iptv.adapter.Episode
import com.xtreme.iptv.adapter.EpisodeAdapter
import com.xtreme.iptv.api.XtreamApi
import kotlinx.coroutines.*

class SeriesDetailActivity : AppCompatActivity() {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_series_detail)

        val seriesId = intent.getIntExtra("seriesId", 0)
        val seriesName = intent.getStringExtra("name") ?: ""

        findViewById<TextView>(R.id.seriesTitle).text = seriesName
        findViewById<ImageView>(R.id.seriesBack).setOnClickListener { finish() }

        val seasonSpinner = findViewById<Spinner>(R.id.seasonSpinner)
        val rv = findViewById<RecyclerView>(R.id.episodesRv)
        rv.layoutManager = LinearLayoutManager(this)

        scope.launch {
            val info = withContext(Dispatchers.IO) { XtreamApi.getSeriesInfo(seriesId) }
            if (info == null) {
                Toast.makeText(this@SeriesDetailActivity, "خطأ في جلب الحلقات", Toast.LENGTH_SHORT).show()
                return@launch
            }

            try {
                val episodesObj = info.optJSONObject("episodes") ?: return@launch
                val seasons = mutableListOf<String>()
                val seasonMap = mutableMapOf<String, List<Episode>>()

                val keys = episodesObj.keys()
                while (keys.hasNext()) {
                    val seasonKey = keys.next()
                    seasons.add(seasonKey)
                    val arr = episodesObj.getJSONArray(seasonKey)
                    val list = mutableListOf<Episode>()
                    for (i in 0 until arr.length()) {
                        val ep = arr.getJSONObject(i)
                        list.add(Episode(
                            id = ep.optString("id"),
                            num = ep.optInt("episode_num"),
                            title = ep.optString("title"),
                            ext = ep.optString("container_extension", "mp4")
                        ))
                    }
                    seasonMap[seasonKey] = list.sortedBy { it.num }
                }

                val sortedSeasons = seasons.sortedBy { it.toIntOrNull() ?: 0 }
                
                if (sortedSeasons.isEmpty()) {
                    Toast.makeText(this@SeriesDetailActivity, "لا توجد حلقات", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                seasonSpinner.adapter = ArrayAdapter(this@SeriesDetailActivity, android.R.layout.simple_spinner_dropdown_item, sortedSeasons.map { "الموسم $it" })

                fun showSeason(index: Int) {
                    val key = sortedSeasons[index]
                    val list = seasonMap[key] ?: return
                    rv.adapter = EpisodeAdapter(list) { ep ->
                        startActivity(Intent(this@SeriesDetailActivity, PlayerActivity::class.java).apply {
                            putExtra("name", "$seriesName - S${key}E${ep.num}")
                            putExtra("url", XtreamApi.seriesEpisodeUrl(ep.id, ep.ext))
                            putExtra("streamId", 0)
                        })
                    }
                }

                showSeason(0)
                seasonSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        showSeason(position)
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

            } catch (e: Exception) {
                Toast.makeText(this@SeriesDetailActivity, "خطأ في قراءة البيانات", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}

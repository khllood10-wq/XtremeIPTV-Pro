package com.xtreme.iptv

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xtreme.iptv.adapter.CategoryAdapter
import com.xtreme.iptv.adapter.VodAdapter
import com.xtreme.iptv.api.XtreamApi
import com.xtreme.iptv.model.Category
import kotlinx.coroutines.*

class VodActivity : AppCompatActivity() {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vod)

        findViewById<ImageView>(R.id.vodBack).setOnClickListener { finish() }

        val catRv = findViewById<RecyclerView>(R.id.vodCatRv)
        val rv = findViewById<RecyclerView>(R.id.vodRv)
        val empty = findViewById<TextView>(R.id.vodEmpty)

        catRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rv.layoutManager = GridLayoutManager(this, 3)

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
            empty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            rv.adapter = VodAdapter(list) { v ->
                startActivity(Intent(this@VodActivity, PlayerActivity::class.java).apply {
                    putExtra("name", v.name)
                    putExtra("url", XtreamApi.vodUrl(v.streamId, v.containerExtension))
                    putExtra("logo", v.streamIcon)
                    putExtra("streamId", 0)
                })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}

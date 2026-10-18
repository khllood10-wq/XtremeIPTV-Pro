package com.xtreme.iptv.api

import com.xtreme.iptv.model.Category
import com.xtreme.iptv.model.Channel
import com.xtreme.iptv.model.SeriesItem
import com.xtreme.iptv.model.VodItem
import com.xtreme.iptv.utils.Prefs
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object XtreamApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private fun get(url: String): String? = try {
        client.newCall(Request.Builder().url(url).build()).execute().body?.string()
    } catch (e: Exception) { null }

    fun login(): Boolean {
        val j = get("${Prefs.host}/player_api.php?username=${Prefs.user}&password=${Prefs.pass}") ?: return false
        return j.contains("user_info")
    }

    fun getLiveCategories(): List<Category> {
        val json = get("${Prefs.host}/player_api.php?username=${Prefs.user}&password=${Prefs.pass}&action=get_live_categories") ?: return emptyList()
        return parseCategories(json)
    }

    fun getLiveStreams(categoryId: String? = null): List<Channel> {
        if (categoryId.isNullOrEmpty()) {
            val cats = getLiveCategories()
            if (cats.isEmpty()) return emptyList()
            val all = mutableListOf<Channel>()
            for (cat in cats) { all.addAll(getLiveStreams(cat.id)) }
            return all.distinctBy { it.streamId }
        }
        val json = get("${Prefs.host}/player_api.php?username=${Prefs.user}&password=${Prefs.pass}&action=get_live_streams&category_id=$categoryId") ?: return emptyList()
        val list = mutableListOf<Channel>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(Channel(o.optInt("num"), o.optString("name"), o.optInt("stream_id"), o.optString("stream_icon"), o.optString("category_id")))
            }
        } catch (_: Exception) {}
        return list
    }

    fun getVodCategories(): List<Category> {
        val json = get("${Prefs.host}/player_api.php?username=${Prefs.user}&password=${Prefs.pass}&action=get_vod_categories") ?: return emptyList()
        return parseCategories(json)
    }

    fun getVodStreams(categoryId: String? = null): List<VodItem> {
        if (categoryId.isNullOrEmpty()) {
            val cats = getVodCategories()
            if (cats.isEmpty()) return emptyList()
            val all = mutableListOf<VodItem>()
            for (cat in cats) { all.addAll(getVodStreams(cat.id)) }
            return all.distinctBy { it.streamId }
        }
        val json = get("${Prefs.host}/player_api.php?username=${Prefs.user}&password=${Prefs.pass}&action=get_vod_streams&category_id=$categoryId") ?: return emptyList()
        val list = mutableListOf<VodItem>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(VodItem(o.optInt("stream_id"), o.optString("name"), o.optString("stream_icon"), o.optString("rating"), o.optString("container_extension", "mp4"), o.optString("category_id")))
            }
        } catch (_: Exception) {}
        return list
    }

    fun getSeriesCategories(): List<Category> {
        val json = get("${Prefs.host}/player_api.php?username=${Prefs.user}&password=${Prefs.pass}&action=get_series_categories") ?: return emptyList()
        return parseCategories(json)
    }

    fun getSeries(categoryId: String? = null): List<SeriesItem> {
        if (categoryId.isNullOrEmpty()) {
            val cats = getSeriesCategories()
            if (cats.isEmpty()) return emptyList()
            val all = mutableListOf<SeriesItem>()
            for (cat in cats) { all.addAll(getSeries(cat.id)) }
            return all.distinctBy { it.seriesId }
        }
        val json = get("${Prefs.host}/player_api.php?username=${Prefs.user}&password=${Prefs.pass}&action=get_series&category_id=$categoryId") ?: return emptyList()
        val list = mutableListOf<SeriesItem>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(SeriesItem(o.optInt("series_id"), o.optString("name"), o.optString("cover"), o.optString("rating"), o.optString("category_id")))
            }
        } catch (_: Exception) {}
        return list
    }

    fun getSeriesInfo(seriesId: Int): JSONObject? {
        val json = get("${Prefs.host}/player_api.php?username=${Prefs.user}&password=${Prefs.pass}&action=get_series_info&series_id=$seriesId") ?: return null
        return try { JSONObject(json) } catch (e: Exception) { null }
    }

    private fun parseCategories(json: String): List<Category> {
        val list = mutableListOf<Category>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(Category(o.optString("category_id"), o.optString("category_name")))
            }
        } catch (_: Exception) {}
        return list
    }

    fun streamUrl(streamId: Int): String = "${Prefs.host}/live/${Prefs.user}/${Prefs.pass}/$streamId.m3u8"
    fun streamUrlTs(streamId: Int): String = "${Prefs.host}/live/${Prefs.user}/${Prefs.pass}/$streamId.ts"
    fun vodUrl(streamId: Int, ext: String): String = "${Prefs.host}/movie/${Prefs.user}/${Prefs.pass}/$streamId.$ext"
    fun seriesEpisodeUrl(episodeId: String, ext: String): String = "${Prefs.host}/series/${Prefs.user}/${Prefs.pass}/$episodeId.$ext"
}

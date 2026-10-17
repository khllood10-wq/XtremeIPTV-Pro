package com.xtreme.iptv.api

import com.xtreme.iptv.model.Channel
import com.xtreme.iptv.utils.Prefs
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.util.concurrent.TimeUnit

object XtreamApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private fun get(url: String): String? = try {
        client.newCall(Request.Builder().url(url).build())
            .execute().body?.string()
    } catch (e: Exception) { null }

    fun login(): Boolean {
        val j = get("${Prefs.host}/player_api.php?username=${Prefs.user}&password=${Prefs.pass}")
            ?: return false
        return j.contains("user_info")
    }

    fun getLiveStreams(): List<Channel> {
        val json = get(
            "${Prefs.host}/player_api.php?username=${Prefs.user}&password=${Prefs.pass}&action=get_live_streams"
        ) ?: return emptyList()

        val list = mutableListOf<Channel>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(Channel(
                    num = o.optInt("num"),
                    name = o.optString("name"),
                    streamId = o.optInt("stream_id"),
                    streamIcon = o.optString("stream_icon"),
                    categoryId = o.optString("category_id")
                ))
            }
        } catch (_: Exception) {}
        return list
    }

    fun streamUrl(streamId: Int): String =
        "${Prefs.host}/live/${Prefs.user}/${Prefs.pass}/$streamId.m3u8"

    fun streamUrlTs(streamId: Int): String =
        "${Prefs.host}/live/${Prefs.user}/${Prefs.pass}/$streamId.ts"
}

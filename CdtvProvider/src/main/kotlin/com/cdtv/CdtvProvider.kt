package com.cdtv

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.Qualities
import org.jsoup.nodes.Element

class CdtvProvider : MainAPI() {
    override var mainUrl = "http://30.30.30.30"
    override var name = "CDTV (BDIX Live TV)"
    override val supportedTypes = setOf(TvType.Live)
    override var lang = "bn"
    override val hasMainPage = true

    // Stream server base (Flussonic media server)
    private val streamBase = "http://30.30.30.30:8088"

    companion object {
        // Categories match the CSS class names on the site
        private val CATEGORIES = listOf("Sports", "Bangla", "Hindi", "English", "others")

        private val CAT_DISPLAY = mapOf(
            "Sports"  to "[Sports]",
            "Bangla"  to "[Bangla]",
            "Hindi"   to "[Hindi]",
            "English" to "[English]",
            "others"  to "[Others]"
        )
    }

    // ---------------------------------------------------------------
    // Main page: one HomePageList per category
    // ---------------------------------------------------------------
    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(mainUrl).document
        val homeLists = ArrayList<HomePageList>()

        for (cat in CATEGORIES) {
            val channels = document.select("li.$cat a.channel").mapNotNull { it.toSearchResult() }
            if (channels.isNotEmpty()) {
                homeLists.add(
                    HomePageList(
                        name = CAT_DISPLAY[cat] ?: cat,
                        list = channels,
                        isHorizontalImages = true
                    )
                )
            }
        }
        return HomePageResponse(homeLists)
    }

    // ---------------------------------------------------------------
    // Helper: parse a channel <a> element into a LiveSearchResponse
    // ---------------------------------------------------------------
    private fun Element.toSearchResult(): LiveSearchResponse? {
        // onclick="view.location.href='img/play.php?stream=102'"
        val onclickAttr = this.attr("onclick")
        val streamId = Regex("stream=(\\d+)").find(onclickAttr)?.groupValues?.get(1) ?: return null

        val imgSrc = this.selectFirst("img")?.attr("src") ?: ""
        val imgName = imgSrc.substringAfterLast("/").substringBeforeLast(".")

        // Clean up filename into a human-readable channel name
        val channelName = imgName
            .replace(Regex("[_\\-]"), " ")
            .replace(Regex("\\d{7,}"), "")   // strip epoch timestamps embedded in filenames
            .trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
            .ifBlank { "Channel $streamId" }

        return LiveSearchResponse(
            name = channelName,
            url = "$mainUrl/img/play.php?stream=$streamId",
            apiName = this@CdtvProvider.name,
            type = TvType.Live,
            posterUrl = fixUrl(imgSrc)
        )
    }

    // ---------------------------------------------------------------
    // Search: filter channels by name
    // ---------------------------------------------------------------
    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get(mainUrl).document
        return document.select("li a.channel")
            .mapNotNull { it.toSearchResult() }
            .filter { it.name.contains(query, ignoreCase = true) }
    }

    // ---------------------------------------------------------------
    // Load: return a LiveStreamLoadResponse carrying the stream ID
    // ---------------------------------------------------------------
    override suspend fun load(url: String): LoadResponse {
        val streamId = Regex("stream=(\\d+)").find(url)?.groupValues?.get(1) ?: ""

        // Look up the channel name from the main page
        val mainDoc = app.get(mainUrl).document
        val channelEl = mainDoc.selectFirst("li a.channel[onclick*='stream=$streamId']")
        val channelName = channelEl?.toSearchResult()?.name ?: "Channel $streamId"
        val posterUrl = channelEl?.selectFirst("img")?.attr("src")?.let { fixUrl(it) }

        return LiveStreamLoadResponse(
            name = channelName,
            url = url,
            apiName = this.name,
            dataUrl = streamId,   // passed as `data` into loadLinks
            posterUrl = posterUrl,
            plot = "Live TV via BDIX (30.30.30.30) - Stream ID: $streamId"
        )
    }

    // ---------------------------------------------------------------
    // loadLinks: deliver the actual HLS stream URL(s)
    // ---------------------------------------------------------------
    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val streamId = data.trim()

        // Primary: direct HLS from Flussonic (no token required on BDIX)
        callback.invoke(
            ExtractorLink(
                source = name,
                name = "BDIX HLS - $streamId",
                url = "$streamBase/$streamId/index.m3u8",
                referer = "$mainUrl/",
                quality = Qualities.Unknown.value,
                isM3u8 = true
            )
        )

        // Fallback: tokenized HLS URL from the play.php embed iframe
        try {
            val playPage = app.get("$mainUrl/img/play.php?stream=$streamId").document
            val iframeSrc = playPage.selectFirst("iframe")?.attr("src") ?: ""
            val token = Regex("token=([^&]+)").find(iframeSrc)?.groupValues?.get(1)
            if (!token.isNullOrBlank()) {
                callback.invoke(
                    ExtractorLink(
                        source = name,
                        name = "BDIX HLS Tokenized - $streamId",
                        url = "$streamBase/$streamId/index.m3u8?token=$token",
                        referer = "$mainUrl/",
                        quality = Qualities.Unknown.value,
                        isM3u8 = true
                    )
                )
            }
        } catch (_: Exception) {
            // Ignore token fetch errors; primary link already provided
        }

        return true
    }
}


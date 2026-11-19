package com.streamforge.bingrone

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class BingrOneProvider : MainAPI() {
    override var mainUrl = "https://bingr.one"
    override var name = "BingrOne"
    override val hasMainPage = true
    override var lang = "hi"
    override val supportedTypes = setOf(
        TvType.Movie,
        TvType.TvSeries,
        TvType.Anime
    )

    override val mainPage = mainPageOf(
        "$mainUrl/movies" to "Movies",
        "$mainUrl/series" to "TV Series",
        "$mainUrl/trending" to "Trending"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url = if (page == 1) request.data else "${request.data}?page=$page"
        val document = app.get(url).document
        
        val home = document.select("div.item, article.movie-item, div.card").mapNotNull {
            val title = it.selectFirst("h2, h3, .title")?.text()?.trim() ?: return@mapNotNull null
            val href = fixUrlNull(it.selectFirst("a")?.attr("href")) ?: return@mapNotNull null
            val posterUrl = fixUrlNull(it.selectFirst("img")?.attr("src") ?: it.selectFirst("img")?.attr("data-src"))

            newMovieSearchResponse(title, href, TvType.Movie) {
                this.posterUrl = posterUrl
            }
        }
        return newHomePageResponse(request.name, home)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/?s=$query"
        val document = app.get(url).document

        return document.select("div.item, article.movie-item, div.card").mapNotNull {
            val title = it.selectFirst("h2, h3, .title")?.text()?.trim() ?: return@mapNotNull null
            val href = fixUrlNull(it.selectFirst("a")?.attr("href")) ?: return@mapNotNull null
            val posterUrl = fixUrlNull(it.selectFirst("img")?.attr("src") ?: it.selectFirst("img")?.attr("data-src"))

            newMovieSearchResponse(title, href, TvType.Movie) {
                this.posterUrl = posterUrl
            }
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        val title = document.selectFirst("h1, .entry-title")?.text()?.trim() ?: return null
        val poster = fixUrlNull(document.selectFirst("img.poster, .thumb img")?.attr("src"))
        val description = document.selectFirst(".description, .entry-content p")?.text()?.trim()

        return newMovieLoadResponse(title, url, TvType.Movie, url) {
            this.posterUrl = poster
            this.plot = description
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(data).document

        document.select("iframe").forEach { iframe ->
            val iframeUrl = fixUrlNull(iframe.attr("src") ?: iframe.attr("data-src"))
            if (!iframeUrl.isNullOrEmpty()) {
                loadExtractor(iframeUrl, data, subtitleCallback, callback)
            }
        }
        return true
    }
}

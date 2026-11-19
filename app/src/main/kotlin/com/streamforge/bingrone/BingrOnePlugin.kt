package com.streamforge.bingrone

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class BingrOnePlugin: Plugin() {
    override fun load(context: Context) {
        registerMainAPI(BingrOneProvider())
    }
}

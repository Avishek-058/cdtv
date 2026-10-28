package com.cdtv

import android.content.Context
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin

@CloudstreamPlugin
class CdtvPlugin : Plugin() {
    override fun load(context: Context) {
        registerMainAPI(CdtvProvider())
    }
}


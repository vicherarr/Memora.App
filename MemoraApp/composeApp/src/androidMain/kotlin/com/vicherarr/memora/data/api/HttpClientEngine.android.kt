package com.vicherarr.memora.data.api

import io.ktor.client.engine.*
import io.ktor.client.engine.android.*

/**
 * Implementación Android del HttpClientEngine
 */
actual fun getHttpClientEngine(): HttpClientEngine = Android.create()
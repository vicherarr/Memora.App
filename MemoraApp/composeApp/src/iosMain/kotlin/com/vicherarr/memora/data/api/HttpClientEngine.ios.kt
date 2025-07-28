package com.vicherarr.memora.data.api

import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*

/**
 * Implementación iOS del HttpClientEngine
 */
actual fun getHttpClientEngine(): HttpClientEngine = Darwin.create()
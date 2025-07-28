package com.vicherarr.memora.data.api

import io.ktor.client.engine.*

/**
 * Factory para obtener el engine HTTP específico de cada plataforma
 */
expect fun getHttpClientEngine(): HttpClientEngine
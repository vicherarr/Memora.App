package com.vicherarr.memora

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
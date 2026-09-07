package com.geosid.simplephysics

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
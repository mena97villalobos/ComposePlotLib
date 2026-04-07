package com.mena97villalobos.composeplotlib

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

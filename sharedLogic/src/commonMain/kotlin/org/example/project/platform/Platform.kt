package org.example.project.platform

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
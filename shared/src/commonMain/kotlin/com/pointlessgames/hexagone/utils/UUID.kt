package com.pointlessgames.hexagone.utils

import kotlin.random.Random

fun generateUUID(): String {
    val chars = "0123456789abcdef"
    val randomHex = { length: Int ->
        (1..length).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }
    return "${randomHex(8)}-${randomHex(4)}-${randomHex(4)}-${randomHex(4)}-${randomHex(12)}"
}

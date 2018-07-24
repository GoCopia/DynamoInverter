package com.gocopia.repo

/**
 * Local function to escape the result if doing string comparisons and leave it untouched if not
 */
fun Any.escapeResultIfNeeded(): Any {
    return when(this::class.java) {
        String::class.java -> "\'$this\'"
        else -> this
    }
}


package com.gocopia.repo

import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator

/**
 * Local function to escape the result if doing string comparisons and leave it untouched if not
 */
internal fun Any.escapeResultIfNeeded(): Any {
    return when(this::class.java) {
        String::class.java -> "\'$this\'"
        else -> this
    }
}






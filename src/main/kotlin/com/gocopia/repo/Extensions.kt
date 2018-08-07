package com.gocopia.repo

import com.amazonaws.services.dynamodbv2.model.ComparisonOperator

/**
 * Local function to escape the result if doing string comparisons and leave it untouched if not
 */
internal fun Any.escapeIfNeeded(): Any {
    return when(this::class.java) {
        String::class.java -> "\'$this\'"
        else -> this
    }
}

/**
 * Fetches the symbol associated with the comparison operator with an O(1) lookup. Exists for code cleanliness
 */
internal fun ComparisonOperator.toSymbol(): String {
    return SymbolMap.getValue(this)
}

/**
 * Singleton (all objects in kotlin are singletons) that holds lazily initialized map of Comparison operator names to
 * their corresponding SQL Query symbol
 * @property symbols the map that contains the names of dynamo Comparison operators to their corresponding SQL symbol
 */
object SymbolMap {
    // Mappings of Dynamo ComparisonOperator names to SQL symbols
    val symbols by lazy {
        mapOf(
            "EQ" to "=",
            "BETWEEN" to "BETWEEN",
            "GE" to ">=",
            "LE" to "<=",
            "LT" to "<",
            "GT" to ">",
            "NE" to "!="
//          "IN" to "IN",
//          "CONTAINS" to "CONTAINS",
//          "NOT_CONTAINS" to "NOT CONTAINS",
//          "NULL" to "NOT EXISTS",
//          "NULL" to "EXISTS",
        )
    }

    /**
     * Fetches the symbol that corresponds to the comparison operator
     * @param operator the Dynamo Comparison operator that you want to fetch the symbol for
     * @return a symbol as a string
     * @throws IllegalArgumentException if the operator does not exist in the symbols map
     */
    fun getValue(operator: ComparisonOperator): String {
        return this.symbols.get(operator.name)
                ?: throw IllegalArgumentException("${operator.name} is not a supported comparison operator")
    }
}





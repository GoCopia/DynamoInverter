package com.gocopia.repo

import com.amazonaws.services.dynamodbv2.document.internal.Filter
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator

/**
 * Translates a Dynamo QuerySpec object into a SQL compatible string that can be run using JDBC. Currently only supports
 * simple query operations:
 * SELECT
 * WHERE
 * LIMIT
 * @param tableName the name of the table you wish to query, as dynamo does not support
 */
fun QuerySpec.toSqlString(tableName: String): String {

    // Pull out hash key if it exists
    var whereString = this.hashKey?.let { "${it.name} = ${it.value.escapeIfNeeded()}" }

    // Pull Query Filters
    val qfWhereExpr = this.queryFilters?.buildWhereExpr()
    whereString = if(whereString != null) {
        // If qfWhereExpr is not null, append it to whereString, else just return whereString
        qfWhereExpr?.let { "$whereString $it" } ?: whereString
    } else {
        // If whereString is null, just return whatever qfWhereExpr is
        qfWhereExpr
    }

    // Pull out RangeKey conditions
    this.rangeKeyCondition?.let {
        // Interpolate range key condition into whereString
        val rangeKeyString = "${it.attrName} ${buildWhereExpr(it.keyCondition.toComparisonOperator(), it.values)}"

        // Build out whereString. If Query filters were populated, append rangeKeyString else just use rangeKeyString
        whereString = if(whereString != null) "$whereString, $rangeKeyString" else rangeKeyString
    }

    // Return result
    return buildSqlString(this.projectionExpression, tableName, whereString, this.maxResultSize)
}

/**
 * Translate a Dynamo ScanSpec object into a SQL compatible string that can be run using JDBC. Currently only supports
 * simple query operations:
 * SELECT<\n>
 * WHERE<\n>
 * LIMIT<\n>
 */
fun ScanSpec.toSqlString(tableName: String): String {
    // Return result
    return buildSqlString(this.projectionExpression, tableName, this.scanFilters?.buildWhereExpr(), this.maxResultSize)
}

/**
 * Performs the final assembly from the data to build a string that can be executed with JDBC. It uses Kotlin string
 * interpolation to assemble the SELECT FROM WHERE LIMIT string.
 * @param projectionExpression a comma separated list of attributes of an object that want to be fetched. For example
 * "attr1, attr2, attr3" would be a valid projection expression. If the param is null, "*" will be used instead.
 * @param tableName the name of the table you want to query as QuerySpec/ScanSpec objects do not contain references to
 * what table they will be run against.
 * @param whereClause string representing a list of comparison clauses separated by SQL logical operators. For Example,
 * "attr1 > 5 AND attr2 < 10" such that this clause does not contain the word "WHERE".
 * @param maxResults the maximum number of results you want returned from the database.
 * @return a string that can be run against JDBC.
 */
internal fun buildSqlString(projectionExpression: String?, tableName: String, whereClause: String?, maxResults: Int?): String {
    // Build resulting Query string
    var sqlQueryString = "${buildSelectFromProjectionExpr(projectionExpression)} FROM $tableName"

    // Build in optional WHERE and LIMIT clause:
    (if(whereClause != null) "WHERE $whereClause" else null)?.let { sqlQueryString = "$sqlQueryString $it" }
    buildLimitClause(maxResults)?.let { sqlQueryString = "$sqlQueryString $it" }

    // Return result
    return sqlQueryString
}

/**
 * Builds the SELECT portion of a sql statement from a projection expression. If the projection expression is null "*"
 * will be used
 * @param projectionExpr the values of the object stored in the DB you want returned
 * @return a string of format "SELECT projectionExpr" or "SELECT *" if projectionExpr is null
 * @throws IllegalArgumentException if the projection expression is invalid. (Empty string or only spaces)
 */
internal fun buildSelectFromProjectionExpr(projectionExpr: String?): String {
    if(projectionExpr == "" || (projectionExpr != null && projectionExpr.trim().isEmpty())) {
        throw IllegalArgumentException("$projectionExpr is not a valid projectionExpression")
    }

    // Return result, prune leading and trailing whitespace if needed
    return "SELECT ${projectionExpr?.trim() ?: "*"}"
}

/**
 * Builds a SQL where expression from a collection of Filters that are applied to a Query or Scan Spec object.
 * @return string representing a list of comparison clauses separated by SQL logical operators. For Example,
 * "attr1 > 5 AND attr2 < 10" such that this clause does not contain the word "WHERE".
 */
internal fun Collection<Filter<*>?>.buildWhereExpr(): String {
    // Build result
    return this.mapNotNull {
        // Join the attribute and the associated logical expression
        "${it?.getAttribute()} ${buildWhereExpr(it?.getComparisonOperator(), it?.getValues())}"
    }.reduceRight {
            s, acc -> "$s AND $acc"
    }
}

/**
 * Builds the SQL logical operator portion of the query string from a comparison operator at the associated values.
 * @param comparisonOperator the Dynamo comparison operator
 * @param array the values associated with the comparison operator
 * @return a SQL string of logical operations separated by AND as that is what Dynamo supports.
 * For example "attr1 > 4 AND attr2 < 5" would be a valid result
 */
private fun buildWhereExpr(comparisonOperator: ComparisonOperator?, array: Array<out Any>?): String? {

    // Check if comparison operator is valid
    if(comparisonOperator == null) {
        return null
    }

    // Build up values; this is for operations like BETWEEN that contain two values in array. Most comparison operators
    // only contain one value, so the reduce clause will not be executed
    val a = array?.map { it.escapeIfNeeded() }?.reduceRight { any, acc -> "$any AND $acc" }
    // Build result
    return if(a == null) {
        // Return the symbol if one value exists. For operations like EXISTS or NOT EXISTS
        comparisonOperator.toSymbol()
    } else {
        // Else insert the comparison operator in front of the associated value(s)
        "${comparisonOperator.toSymbol()} $a"
    }
}

/**
 * Builds the limit clause of a sql statement
 * @param size the max number of results you want to return
 * @return a string of format "LIMIT size"
 */
internal fun buildLimitClause(size: Int?): String? {
    return size?.let { "LIMIT $it" }
}





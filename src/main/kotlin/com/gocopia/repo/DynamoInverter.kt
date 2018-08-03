package com.gocopia.repo

import com.amazonaws.services.dynamodbv2.document.internal.Filter
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator

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


fun ScanSpec.toSqlString(tableName: String): String {
    // Return result
    return buildSqlString(this.projectionExpression, tableName, this.scanFilters?.buildWhereExpr(), this.maxResultSize)
}

internal fun buildSqlString(projectionExpression: String?, tableName: String, whereClause: String?, maxResults: Int?): String {

    // Build where clause
    val whereString = if(whereClause != null) "WHERE $whereClause" else null

    // Build resulting Query string
    var sqlQueryString = "${buildSelectFromProjectionExpr(projectionExpression)} FROM $tableName"

    // Build in optional WHERE and LIMIT clause:
    whereString?.let { sqlQueryString = "$sqlQueryString $it" }
    buildLimitClause(maxResults)?.let { sqlQueryString = "$sqlQueryString $it" }

    // Return result
    return sqlQueryString
}

internal fun buildLimitClause(size: Int?): String? {
    return size?.let { "LIMIT $it" }
}

internal fun Collection<Filter<*>?>.buildWhereExpr(): String? {
    return this.mapNotNull {
        "${it?.getAttribute()} ${buildWhereExpr(it?.getComparisonOperator(), it?.getValues())}"
    }.reduceRight {
            s, acc -> "$s AND $acc"
    }
}


internal fun buildWhereExpr(comparisonOperator: ComparisonOperator?, array: Array<out Any>?): String? {

    if(comparisonOperator == null) return null

    val a = array?.map { it.escapeIfNeeded() }?.reduceRight { any, acc -> "$any AND $acc" }
    return if(a == null) {
        comparisonOperator.toSymbol()
    } else {
        "${comparisonOperator.toSymbol()} $a"
    }
}

internal fun buildSelectFromProjectionExpr(projectionExpr: String?) = "SELECT ${projectionExpr ?: "*"}"




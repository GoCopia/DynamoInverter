package com.gocopia.repo

import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator

fun QuerySpec.toSqlString(tableName: String): String {
    // Pull out primary key
    val hashKey = this.hashKey ?: null
    val hashKeyString = hashKey?.let { "${it.name} = ${it.value}" }

    // Pull out other Query Filters
    val queryFilterString = this.queryFilters?.map {
        "${it.attribute} ${buildExpr(it.comparisonOperator, it.values)}"
    }?.reduceRight { s, acc -> "$acc, $s" }

    // Pull out RangeKey conditions
    val rangeKey = this.rangeKeyCondition ?: null
    val rangeKeyString = rangeKey?.let {
        "${it.attrName} ${buildExpr(it.keyCondition.toComparisonOperator(), it.values)}"
    }

    // Build where clause
    val whereString = listOf(hashKeyString, queryFilterString, rangeKeyString).reduceRight { s, acc -> "$s, $acc" }?.let { "WHERE $it" }

    // Pull out max result size
    val limitClause = this.maxResultSize?.let { "LIMIT $it" }

    // Pull out select clause from projection expression
    val selectClause = "SELECT ${this.projectionExpression ?: "*"}"

    // Build resulting Query string
    var sqlQueryString = "$selectClause FROM $tableName"

    // Build in optional WHERE and LIMIT clauses
    whereString?.let { sqlQueryString = "$sqlQueryString $it" }
    limitClause?.let { sqlQueryString = "$sqlQueryString $it" }

    // Return result
    return sqlQueryString
}

fun ScanSpec.toSqlString(tableName: String): String {

    return ""
}

private fun buildExpr(comparisonOperator: ComparisonOperator, array: Array<out Any>): String {
    return "${comparisonOperator.toSymbol()} ${array.map { it }.reduceRight { any, acc -> "$any AND $acc" }}"
}

private fun ComparisonOperator.toSymbol(): String {
    val name = this.name

    when(name) {
        "EQ" -> return "="
        "BETWEEN" -> return name
        "GE" -> return "=>"
        else -> throw IllegalArgumentException("$name is not a valid comparison operator")
    }
}

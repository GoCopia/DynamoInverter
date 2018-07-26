package com.gocopia.repo

import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator

fun QuerySpec.toSqlString(tableName: String): String {
    // Store strings that belong in the WHERE clause
    val whereStrings = mutableListOf<String>()

    // Pull out primary key
    val hashKey = this.hashKey ?: null
    hashKey?.let { "${it.name} = ${it.value.escapeIfNeeded()}" }?.let { whereStrings.add(it) }


    // Pull out other Query Filters
    this.queryFilters?.mapNotNull {
        //   println( "${it.attribute} ${buildExpr(it.comparisonOperator, it.values)}")
        "${it.attribute} ${buildWhereExpr(it.comparisonOperator, it.values)}"

    }?.reduceRight { s, acc -> "$acc, $s" }?.let { whereStrings.add(it) }

    // Pull out RangeKey conditions
    this.rangeKeyCondition?.let {
        whereStrings.add("${it.attrName} ${buildWhereExpr(it.keyCondition.toComparisonOperator(), it.values)}")
    }

    // Build where clause
    val whereString = if(whereStrings.isNotEmpty()) whereStrings.reduceRight { s, acc -> "$s, $acc" }?.let { "WHERE $it" } else null

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

internal fun buildWhereExpr(comparisonOperator: ComparisonOperator, array: Array<out Any>?): String {

    val a = array?.map { it.escapeIfNeeded() }?.reduceRight { any, acc -> "$any AND $acc" }
    return if(a == null){
        comparisonOperator.toSymbol()
    } else {
        "${comparisonOperator.toSymbol()} $a"
    }
}




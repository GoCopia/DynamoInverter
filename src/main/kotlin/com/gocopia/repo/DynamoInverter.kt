package com.gocopia.repo

import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator

fun QuerySpec.toSqlString(tableName: String): String {

    return ""
}

fun ScanSpec.toSqlString(tableName: String): String {

    return ""
}



package com.gocopia.repo

import com.amazonaws.services.dynamodbv2.model.ComparisonOperator
import io.kotlintest.forAll
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.FunSpec
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class BuildWhereExprTest : FunSpec(){

    init {
        buildWhereExprTest()
    }

    fun buildWhereExprTest() = test("Test building a where expression for a single comparison operator and value") {

        forAll(ComparisonOperator::class.java.enumConstants) {
            try {
                println(buildWhereExpr(it, listOf(1).toTypedArray()))
            } catch (ie: IllegalArgumentException) {

            }
        }
    }
}
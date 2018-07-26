package com.gocopia.repo

import com.amazonaws.services.dynamodbv2.model.ComparisonOperator
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.FunSpec
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class BuildWhereExprTest : FunSpec(){

    init {
        buildWhereExprTest()
    }

    fun buildWhereExprTest() = test("Testy test") {
        ComparisonOperator::class.java.enumConstants.forEach {
            try {
                println(it.toSymbol())
            } catch (ie: IllegalArgumentException) {
                println(ie.localizedMessage)

            }
        }
    }
}
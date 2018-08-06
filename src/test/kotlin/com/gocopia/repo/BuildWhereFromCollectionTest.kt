package com.gocopia.repo

import com.amazonaws.services.dynamodbv2.document.QueryFilter
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec

class BuildWhereFromCollectionTest : FunSpec() {

    init {
        testBuildingSimpleWhere()
        testBuildingWhereWithAnd()
    }

    private fun testBuildingSimpleWhere() = test("Test building simple where expression") {
        val attributeName = "attr"
        val symbols = SymbolMap.symbols.keys.
            mapIndexed {
                    index, _ -> listOf(QueryFilter("$attributeName$index"))
            }

        // Generate data
        val comparisonAmount = 1
        symbols[0][0].eq(comparisonAmount)
        symbols[1][0].between(comparisonAmount, 2)
        symbols[2][0].ge(comparisonAmount)
        symbols[3][0].le(comparisonAmount)
        symbols[4][0].lt(comparisonAmount)
        symbols[5][0].gt(comparisonAmount)
        symbols[6][0].ne(comparisonAmount)

        // Test values are correct
        symbols[0].buildWhereExpr() shouldBe "${attributeName}0 = $comparisonAmount"
        symbols[1].buildWhereExpr() shouldBe "${attributeName}1 BETWEEN $comparisonAmount AND 2"
        symbols[2].buildWhereExpr() shouldBe "${attributeName}2 => $comparisonAmount"
        symbols[3].buildWhereExpr() shouldBe "${attributeName}3 <= $comparisonAmount"
        symbols[4].buildWhereExpr() shouldBe "${attributeName}4 < $comparisonAmount"
        symbols[5].buildWhereExpr() shouldBe "${attributeName}5 > $comparisonAmount"
        symbols[6].buildWhereExpr() shouldBe "${attributeName}6 != $comparisonAmount"
    }

    private fun testBuildingWhereWithAnd() = test("Test building where expression with AND") {
        val attributeName = "attr"
        val symbols = SymbolMap.symbols.keys.
            mapIndexed {
                    index, _ -> mutableListOf(QueryFilter("$attributeName$index"))
            }

        // Generate data
        val comparisonAmount1 = 1
        val comparisonAmount2 = 2

        symbols[0][0].eq(comparisonAmount1)
        symbols[0].add(QueryFilter("${attributeName}1").ne(comparisonAmount2))

        symbols[1][0].between(comparisonAmount1, 2)
        symbols[1].add(QueryFilter("${attributeName}1").eq(comparisonAmount2))

        symbols[2][0].ge(comparisonAmount1)
        symbols[2].add(QueryFilter("${attributeName}2").le(comparisonAmount2))

        symbols[3][0].gt(comparisonAmount1)
        symbols[3].add(QueryFilter("${attributeName}2").lt(comparisonAmount2))

        // Test values are correct
        symbols[0].buildWhereExpr() shouldBe "${attributeName}0 = $comparisonAmount1 AND ${attributeName}1 != $comparisonAmount2"

        symbols[1].buildWhereExpr() shouldBe "${attributeName}1 BETWEEN $comparisonAmount1 AND 2 AND ${attributeName}1 = $comparisonAmount2"

        symbols[2].buildWhereExpr() shouldBe "${attributeName}2 => $comparisonAmount1 AND ${attributeName}2 <= $comparisonAmount2"

        symbols[3].buildWhereExpr() shouldBe "${attributeName}3 > $comparisonAmount1 AND ${attributeName}2 < $comparisonAmount2"
    }
}
package com.gocopia.repo

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.FunSpec

/**
 * Handles testing building of the SELECT clause of SQL statements from a Dynamo projection expression
 * @author Mackenzie Bligh
 */
class BuildSelectTest: FunSpec() {

    // Where tests get run
    init {
        testBuildingSelectString()
        testBuildingSelectString_trimmingNeeded()
        testBuildingSelectString_nullProjection()
        testBuildingSelectString_emptyString()
        testBuildingSelectString_spaces()
   }

    /**
     * Tests building a select string with a valid ProjectionExpression
     */
    private fun testBuildingSelectString() = test("Test building select string") {
        val projectionExpr = "attr1, attr2, attr3"
        buildSelectFromProjectionExpr(projectionExpr) shouldBe  "SELECT $projectionExpr"
    }

    /**
     * Tests building a select string with a valid ProjectionExpression, that contains leading and trailing whitespace
     */
    private fun testBuildingSelectString_trimmingNeeded() =
        test("Test building select string with whitespace to trim") {
        val projectionExpr = "  attr1, attr2, attr3  "
        buildSelectFromProjectionExpr(projectionExpr) shouldBe  "SELECT ${projectionExpr.trim()}"
    }

    /**
     * Tests building a select string with a null ProjectionExpression
     */
    private fun testBuildingSelectString_nullProjection() =
        test("Test building select string with null projection expression") {
            buildSelectFromProjectionExpr(null) shouldBe "SELECT *"
        }

    /**
     * Tests building a select string with an empty string ("") ProjectionExpression
     */
    private fun testBuildingSelectString_emptyString() = test("Test building select string with \"\"") {
        val projectionExpr = ""
        shouldThrow<IllegalArgumentException> {
            buildSelectFromProjectionExpr(projectionExpr)
        }
    }

    /**
     * Tests building a select string with a ProjectionExpression filled with spaces
     */
    private fun testBuildingSelectString_spaces() = test("Test building select string with \"   \"") {
        val projectionExpr = "   "
        shouldThrow<IllegalArgumentException> {
            buildSelectFromProjectionExpr(projectionExpr)
        }
    }
}
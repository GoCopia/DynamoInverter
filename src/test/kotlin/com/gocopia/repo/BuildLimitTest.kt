package com.gocopia.repo

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.FunSpec

/**
 * Handles testing building of the LIMIT clause of SQL statements from a Dynamo with max results size
 * @author Mackenzie Bligh
 */
class BuildLimitTest : FunSpec() {

    // Where tests get run
    init {
        testBuildingLimit()
        testBuildingLimit_neg()
    }


    /**
     * Tests building a LIMIT clause with a large range of values
     */
    private fun testBuildingLimit() = test("Tests building a limit expression with a variety of values") {
        (0..9000).forEach { buildLimitClause(it) shouldBe "LIMIT $it" }
    }

    /**
     * Tests building a LIMIT clause with an invalid (Negative) value
     */
    private fun testBuildingLimit_neg() = test("Tests building a limit expression with negative value") {
        shouldThrow<IllegalArgumentException> {
            buildLimitClause(-1)
        }
    }
}
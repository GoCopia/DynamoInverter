package com.gocopia.repo

import io.kotlintest.forAll
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.FunSpec

class BuildLimitTest : FunSpec() {

    init {
        testBuildingLimit()
        testBuildingLimit_neg()
    }


    private fun testBuildingLimit() = test("Tests building a limit expression with a variety of values") {
        (0..9000).forEach { buildLimitClause(it) shouldBe "LIMIT $it" }
    }

    private fun testBuildingLimit_neg() = test("Tests building a limit expression with negative value") {
        shouldThrow<IllegalArgumentException> {
            buildLimitClause(-1)
        }
    }
}
package com.gocopia.repo

import com.amazonaws.services.dynamodbv2.model.ComparisonOperator
import io.kotlintest.forAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec

/**
 * Handles testing the misc helper functions written as Kotlin extension functions.
 * @author Mackenzie Bligh
 */
internal class ExtensionsTest : FunSpec() {

    // Where tests get run
    init {
        testComparionOperatorToSymbol()
        testEscapeIfNeeded()
    }

    /**
     * Handles testing the conversion of all Dynamo Comparison operators in to symbols that can be processed as a SQL
     * string
     */
    private fun testComparionOperatorToSymbol() = test("Test that comparison operators are converted " +
            "to symbols") {

        // Use reflection to access all enum members of the class
        forAll(ComparisonOperator::class.java.enumConstants) {
            // Try converting it to a symbol
            it.toSymbol()
        }
    }

    /**
     * Handles testing that strings are escaped for insertion into a SQL string and that all other classes are left
     * untouched
     */
    private fun testEscapeIfNeeded() = test("Test extension test function that converts strings to " +
            "escaped strings") {

        // Test that any object that isn't a string will remain untouched
        val any = Any()
        any.escapeIfNeeded() shouldBe any

        // Test that strings are given the correct escaping
        "hello".escapeIfNeeded() shouldBe "\'hello\'"
        "hello world".escapeIfNeeded() shouldBe "\'hello world\'"
    }
}



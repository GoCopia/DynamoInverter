package com.gocopia.repo

import com.amazonaws.services.dynamodbv2.document.KeyAttribute
import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import io.kotlintest.forAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec

/**
 * Handles testing the DynamoInverter extension functions to ensure that SQL Strings are properly built
 *
 * @author Mackenzie Bligh
 */
class DynamoInverterTest : FunSpec() {

    // For storing constant values in testing; Equivalent to a final static variable in Java
    companion object {

        const val tableName = "TestTable"
    }

    // Where test functions are called to be ran
    init {
        // Basic test cases
        basicHashKeyTest()
        basicProjectionExpressionTest()
        basicRangeKeyTest()
    }

    /**
     * Handles testing parsing QuerySpec objects with various HashKey values to SQL statements
     */
    private fun basicHashKeyTest() = test("Test Query Spec with variety of Hash Keys") {

        // Test with String attribute
        var qs = QuerySpec().withHashKey(KeyAttribute("PrimaryKey", "1234"))
        qs.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE PrimaryKey = \'1234\'"

        // Test with Int attribute
        qs = QuerySpec().withHashKey(KeyAttribute("PrimaryKey", 1234))

        // Assert result is correct
        qs.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE PrimaryKey = 1234"
    }

    /**
     * Handles testing that attribute names are inserted into the SELECT portion properly
     */
    private fun basicProjectionExpressionTest() = test("Test Query Spec with projectionExpression") {
        // Define select statement/projection expression
        val selectStatement = "PrimaryKey, OtherKey, OneMoreKey"

        // Create query spec with projection expression
        val qs = QuerySpec().withProjectionExpression(selectStatement)

        // Assert result is correct
        qs.toSqlString(tableName) shouldBe "SELECT $selectStatement FROM $tableName"
    }

    /**
     * Handles testing range keys conditions using numbers
     */
    private fun basicRangeKeyTest() = test("Test QuerySpec with RangeKeyCondition with " +
            "number Range Keys") {

        // Define test attribute
        val testAttribute = "TestAttribute"

        // Define range key condition
        val rangeKeyCondition = RangeKeyCondition(testAttribute)

        // Iterate test cases through several possible values
        forAll(listOf(12, 12.2, 12L)) {

            // Define query spec for testing less than equal
            var qs = QuerySpec().withRangeKeyCondition(rangeKeyCondition.le(it))
            // Assert Result is correct
            qs.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE $testAttribute <= $it"

            // Define query spec for testing less than
            qs = QuerySpec().withRangeKeyCondition(rangeKeyCondition.lt(it))
            // Assert Result is correct
            qs.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE $testAttribute < $it"

            // Define query spec for testing equals
            qs = QuerySpec().withRangeKeyCondition(rangeKeyCondition.eq(it))
            // Assert Result is correct
            qs.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE $testAttribute = $it"

            // Define query spec for testing between
            qs = QuerySpec().withRangeKeyCondition(rangeKeyCondition.between(it, 15))
            // Assert Result is correct
            qs.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE $testAttribute BETWEEN $it AND 15"

            // Define query spec for testing greater than
            qs = QuerySpec().withRangeKeyCondition(rangeKeyCondition.gt(it))
            // Assert Result is correct
            qs.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE $testAttribute > $it"

            // Define query spec for testing greater than equal
            qs = QuerySpec().withRangeKeyCondition(rangeKeyCondition.ge(it))
            // Assert Result is correct
            qs.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE $testAttribute => $it"
        }

        // Define query spec
        val qs = QuerySpec().withRangeKeyCondition(rangeKeyCondition.beginsWith("Hello"))
        // Assert Result is correct
        qs.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE $testAttribute NOT SURE YET"

    }
}
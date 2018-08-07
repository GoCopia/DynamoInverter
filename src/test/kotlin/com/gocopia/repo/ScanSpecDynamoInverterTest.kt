package com.gocopia.repo

import com.amazonaws.services.dynamodbv2.document.QueryFilter
import com.amazonaws.services.dynamodbv2.document.ScanFilter
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec
import io.kotlintest.forAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec

/**
 * Handles testing the DynamoInverter extension functions to ensure that SQL Strings are properly built
 *
 * @author Mackenzie Bligh
 */
internal class ScanSpecDynamoInverterTest: FunSpec() {

    // For storing constant values in testing; Equivalent to a final static variable in Java
    companion object {

        const val tableName = "TestTable"
    }

    // Where test functions are called to be ran
    init {
        // Basic test cases
        basicProjectionExpressionTest()
        basicScanFilterTest()
        basicLimitTest()

        // More advanced test cases
        multipleScanFilterTest()

    }

    /*------------------------------------------- Basic Test Cases ---------------------------------------------------*/

    /**
     * Handles testing that attribute names are inserted into the SELECT portion properly
     */
    private fun basicProjectionExpressionTest() = test("Test Query Spec with projectionExpression") {
        // Define select statement/projection expression
        val selectStatement = "PrimaryKey, OtherKey, OneMoreKey"

        // Create query spec with projection expression
        val ss = ScanSpec().withProjectionExpression(selectStatement)

        // Assert result is correct
        ss.toSqlString(tableName) shouldBe "SELECT $selectStatement FROM $tableName"
    }

    /**
     * Tests basic assembly of a QuerySpec into a where clause of a SQL statement
     */
    private fun basicScanFilterTest() = test("Test QuerySpec with single QueryFilter") {
        // Define test attribute
        val testAttribute = "TestAttribute"

        val scanFilter = ScanFilter(testAttribute)

        // TODO Disabled because operators aren't supported yet
//        var qs = QuerySpec().withQueryFilters(queryFilter.notExist())
//        qs.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE NOT EXISTS $testAttribute"
//        var qs = QuerySpec().withQueryFilters(queryFilter.exists())
//        qs.toSqlString(tableName)

        // Test with Numbers
        genericBasicScanFilterTest(scanFilter, testAttribute, listOf(12, 12.1, 12L))

        // Test with strings
        genericBasicScanFilterTest(scanFilter, testAttribute, listOf("12", "12.1", "12L"))
    }

    /**
     * Handles testing that attribute names are inserted into the SELECT portion properly
     */
    private fun basicLimitTest() = test("Test Query Spec with maxResultSize") {
        // Create query spec with projection expression
        val qs = QuerySpec().withMaxResultSize(10)

        // Assert result is correct
        println(        qs.toSqlString(tableName))
        qs.toSqlString(tableName) shouldBe "SELECT * FROM $tableName LIMIT 10"
    }

    /*------------------------------------------- Advanced Test Cases ------------------------------------------------*/

    /**
     * Handles testing that multiple query filters will be joined with an AND clause
     */
    private fun multipleScanFilterTest() = test("test QuerySpec with multiple QueyFilters") {
        // Define test attribute
        val testAttribute = "TestAttribute"

        val queryFilter1 = ScanFilter(testAttribute + "1")
        val queryFilter2 = ScanFilter(testAttribute + "2")

        // Disabled because operators aren't supported by snowflake
//        var qs = QuerySpec().withQueryFilters(queryFilter.notExist())
//        qs.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE NOT EXISTS $testAttribute"
//        var qs = QuerySpec().withQueryFilters(queryFilter.exists())
//        qs.toSqlString(tableName)

        // Test with Numbers
        multipleScanFilterTest(queryFilter1, queryFilter2, "${testAttribute}1", "${testAttribute}2", listOf(12, 12.1, 12L))

        // Test with strings
        multipleScanFilterTest(queryFilter1, queryFilter2, "${testAttribute}1", "${testAttribute}2", listOf("12", "12.1", "12L"))

    }

    /*---------------------------------------------- Helpers ---------------------------------------------------------*/
   /**
     * Handles testing operations for all basic logical operators that can be applied to a QueryFilter (<,>, <=, >=, =)
     */
    private fun genericBasicScanFilterTest(queryFilter: ScanFilter, testAttribute: String, list: List<Any>) {

        // Iterate test cases through several possible values
        forAll(list) {
            // Define query spec for testing less than equal
            var ss = ScanSpec().withScanFilters(queryFilter.le(it))
            // Assert Result is correct
            ss.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE " +
                    "$testAttribute <= ${it.escapeIfNeeded()}"

            // Define query spec for testing less than
            ss = ScanSpec().withScanFilters(queryFilter.lt(it))
            // Assert Result is correct
            ss.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE " +
                    "$testAttribute < ${it.escapeIfNeeded()}"

            // Define query spec for testing equals
            ss = ScanSpec().withScanFilters(queryFilter.eq(it))
            // Assert Result is correct
            ss.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE " +
                    "$testAttribute = ${it.escapeIfNeeded()}"

            // Define query spec for testing less than equal
            ss = ScanSpec().withScanFilters(queryFilter.ne(it))
            // Assert Result is correct
            ss.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE " +
                    "$testAttribute != ${it.escapeIfNeeded()}"

            // Define query spec for testing greater than
            ss = ScanSpec().withScanFilters(queryFilter.gt(it))
            // Assert Result is correct
            ss.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE " +
                    "$testAttribute > ${it.escapeIfNeeded()}"

            // Define query spec for testing greater than equal
            ss = ScanSpec().withScanFilters(queryFilter.ge(it))
            // Assert Result is correct
            ss.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE " +
                    "$testAttribute >= ${it.escapeIfNeeded()}"
        }
    }

    /**
     * handles testing operations for joining query filters with an AND clause
     */
    private fun multipleScanFilterTest(scanFilter1: ScanFilter, scanFilter2: ScanFilter, testAttribute1: String, testAttribute2: String, list: List<Any>) {

        // Iterate test cases through several possible values
        (list).forEach {

            // Define query spec for testing less than equal and greater than equal
            var ss = ScanSpec().withScanFilters(scanFilter1.le(it), scanFilter2.ge(it))
            // Assert Result is correct

            ss.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE " +
                    "$testAttribute1 <= ${it.escapeIfNeeded()} AND $testAttribute2 >= ${it.escapeIfNeeded()}"

            // Define query spec for testing less than and greater than
            ss = ScanSpec().withScanFilters(scanFilter1.lt(it), scanFilter2.gt(it))
            // Assert Result is correct
            ss.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE " +
                    "$testAttribute1 < ${it.escapeIfNeeded()} AND $testAttribute2 > ${it.escapeIfNeeded()}"

            // Define query spec for testing equals and greater than
            ss = ScanSpec().withScanFilters(scanFilter1.eq(it), scanFilter2.gt(it))
            // Assert Result is correct
            ss.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE " +
                    "$testAttribute1 = ${it.escapeIfNeeded()} AND $testAttribute2 > ${it.escapeIfNeeded()}"

        }
    }
}
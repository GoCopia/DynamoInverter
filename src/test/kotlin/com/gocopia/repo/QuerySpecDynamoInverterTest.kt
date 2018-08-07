package com.gocopia.repo

import com.amazonaws.services.dynamodbv2.document.KeyAttribute
import com.amazonaws.services.dynamodbv2.document.QueryFilter
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
internal class QuerySpecDynamoInverterTest : FunSpec() {

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
        basicQueryFilterTest()
        basicLimitTest()

        // More advanced test cases
        multipleQueryFilterTest()

    }

    /*------------------------------------------- Basic Test Cases ---------------------------------------------------*/
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
     * Handles testing range keys conditions using numbers and
     */
    private fun basicRangeKeyTest() = test("Test QuerySpec with RangeKeyCondition with " +
            "number and string range Keys") {

        // Define test attribute
        val testAttribute = "TestAttribute"

        // Define range key condition
        val rangeKeyCondition = RangeKeyCondition(testAttribute)

        // Test all common operations on numbers
        genericBasicRangeKeyTest(rangeKeyCondition, testAttribute, listOf(12, 12.2, 12L))
        // Define query spec for testing between
        var qs = QuerySpec().withRangeKeyCondition(rangeKeyCondition.between(1,10 ))
        // Assert Result is correct
        qs.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE $testAttribute BETWEEN 1 AND 10"

        // Test all operations on strings
        genericBasicRangeKeyTest(rangeKeyCondition, testAttribute, listOf("12", "12.2", "12L"))
        // Define query spec for testing between
        qs = QuerySpec().withRangeKeyCondition(rangeKeyCondition.between("1","10"))
        // Assert Result is correct
        qs.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE $testAttribute BETWEEN \'1\' AND \'10\'"
        // Define querySpec for testing beginsWith

        // TODO Disabled as there isn't a clear equivalent in Snowflake at the moment
//        qs = QuerySpec().withRangeKeyCondition(rangeKeyCondition.beginsWith("1"))
//        // Assert Result is correct
//        qs.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE $testAttribute  UNDETERMINED"

    }

    /**
     * Tests basic assembly of a QuerySpec into a where clause of a SQL statement
     */
    private fun basicQueryFilterTest() = test("Test QuerySpec with single QueryFilter") {
        // Define test attribute
        val testAttribute = "TestAttribute"

        val queryFilter = QueryFilter(testAttribute)

        // TODO Disabled because operators aren't supported yet
//        var qs = QuerySpec().withQueryFilters(queryFilter.notExist())
//        qs.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE NOT EXISTS $testAttribute"
//        var qs = QuerySpec().withQueryFilters(queryFilter.exists())
//        qs.toSqlString(tableName)

        // Test with Numbers
        genericBasicQueryFilterTest(queryFilter, testAttribute, listOf(12, 12.1, 12L))

        // Test with strings
        genericBasicQueryFilterTest(queryFilter, testAttribute, listOf("12", "12.1", "12L"))
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
    private fun multipleQueryFilterTest() = test("test QuerySpec with multiple QueyFilters") {
        // Define test attribute
        val testAttribute = "TestAttribute"

        val queryFilter1 = QueryFilter(testAttribute + "1")
        val queryFilter2 = QueryFilter(testAttribute + "2")

        // Disabled because operators aren't supported by snowflake
//        var qs = QuerySpec().withQueryFilters(queryFilter.notExist())
//        qs.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE NOT EXISTS $testAttribute"
//        var qs = QuerySpec().withQueryFilters(queryFilter.exists())
//        qs.toSqlString(tableName)

        // Test with Numbers
        multipleQueryFilterTest(queryFilter1, queryFilter2, "${testAttribute}1", "${testAttribute}2", listOf(12, 12.1, 12L))

        // Test with strings
        multipleQueryFilterTest(queryFilter1, queryFilter2, "${testAttribute}1", "${testAttribute}2", listOf("12", "12.1", "12L"))

    }

    /*---------------------------------------------- Helpers ---------------------------------------------------------*/
    /**
     * Handles testing operations for all basic logical operators that can be applied with a RangeKeyCondition (<,>, <=, >=, =)
     */
    private fun genericBasicRangeKeyTest(rangeKeyCondition: RangeKeyCondition, testAttribute: String, list: List<Any>) {

        // Iterate test cases through several possible values
        forAll(list) {

            // Define query spec for testing less than equal
            var qs = QuerySpec().withRangeKeyCondition(rangeKeyCondition.le(it))
            // Assert Result is correct
            qs.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE " +
                    "$testAttribute <= ${it.escapeIfNeeded()}"

            // Define query spec for testing less than
            qs = QuerySpec().withRangeKeyCondition(rangeKeyCondition.lt(it))
            // Assert Result is correct
            qs.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE " +
                    "$testAttribute < ${it.escapeIfNeeded()}"

            // Define query spec for testing equals
            qs = QuerySpec().withRangeKeyCondition(rangeKeyCondition.eq(it))
            // Assert Result is correct
            qs.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE " +
                    "$testAttribute = ${it.escapeIfNeeded()}"

           // Define query spec for testing greater than
            qs = QuerySpec().withRangeKeyCondition(rangeKeyCondition.gt(it))
            // Assert Result is correct
            qs.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE " +
                    "$testAttribute > ${it.escapeIfNeeded()}"

            // Define query spec for testing greater than equal
            qs = QuerySpec().withRangeKeyCondition(rangeKeyCondition.ge(it))
            // Assert Result is correct
            qs.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE " +
                    "$testAttribute >= ${it.escapeIfNeeded()}"
        }
    }

    /**
     * Handles testing operations for all basic logical operators that can be applied to a QueryFilter (<,>, <=, >=, =)
     */
    private fun genericBasicQueryFilterTest(queryFilter: QueryFilter, testAttribute: String, list: List<Any>) {

        // Iterate test cases through several possible values
        forAll(list) {
            // Define query spec for testing less than equal
            var qs = QuerySpec().withQueryFilters(queryFilter.le(it))
            // Assert Result is correct
            qs.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE " +
                    "$testAttribute <= ${it.escapeIfNeeded()}"

            // Define query spec for testing less than
            qs = QuerySpec().withQueryFilters(queryFilter.lt(it))
            // Assert Result is correct
            qs.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE " +
                    "$testAttribute < ${it.escapeIfNeeded()}"

            // Define query spec for testing equals
            qs = QuerySpec().withQueryFilters(queryFilter.eq(it))
            // Assert Result is correct
            qs.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE " +
                    "$testAttribute = ${it.escapeIfNeeded()}"

            // Define query spec for testing less than equal
            qs = QuerySpec().withQueryFilters(queryFilter.ne(it))
            // Assert Result is correct
            qs.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE " +
                    "$testAttribute != ${it.escapeIfNeeded()}"

            // Define query spec for testing greater than
            qs = QuerySpec().withQueryFilters(queryFilter.gt(it))
            // Assert Result is correct
            qs.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE " +
                    "$testAttribute > ${it.escapeIfNeeded()}"

            // Define query spec for testing greater than equal
            qs = QuerySpec().withQueryFilters(queryFilter.ge(it))
            // Assert Result is correct
            qs.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE " +
                    "$testAttribute >= ${it.escapeIfNeeded()}"
        }
    }

    /**
     * handles testing operations for joining query filters with an AND clause
     */
    private fun multipleQueryFilterTest(queryFilter1: QueryFilter, queryFilter2: QueryFilter, testAttribute1: String, testAttribute2: String, list: List<Any>) {

        // Iterate test cases through several possible values
        (list).forEach {

            // Define query spec for testing less than equal and greater than equal
            var qs = QuerySpec().withQueryFilters(queryFilter1.le(it), queryFilter2.ge(it))
            // Assert Result is correct

            qs.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE " +
                    "$testAttribute1 <= ${it.escapeIfNeeded()} AND $testAttribute2 >= ${it.escapeIfNeeded()}"

            // Define query spec for testing less than and greater than
            qs = QuerySpec().withQueryFilters(queryFilter1.lt(it), queryFilter2.gt(it))
            // Assert Result is correct
            qs.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE " +
                    "$testAttribute1 < ${it.escapeIfNeeded()} AND $testAttribute2 > ${it.escapeIfNeeded()}"

            // Define query spec for testing equals and greater than
            qs = QuerySpec().withQueryFilters(queryFilter1.eq(it), queryFilter2.gt(it))
            // Assert Result is correct
            qs.toSqlString(tableName) shouldBe "SELECT * FROM $tableName WHERE " +
                    "$testAttribute1 = ${it.escapeIfNeeded()} AND $testAttribute2 > ${it.escapeIfNeeded()}"

        }
    }
}
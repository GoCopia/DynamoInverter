package com.gocopia.repo

import com.amazonaws.services.dynamodbv2.document.QueryFilter
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator
import io.kotlintest.specs.FunSpec
import sun.jvm.hotspot.debugger.cdbg.Sym

class BuildWhereFromCollection : FunSpec() {

    init {

    }

    private fun testBuilding() = test("Test building") {
        val symbols = SymbolMap.symbols.keys.mapIndexed{ index, _ -> QueryFilter("attr$index") }
        val comparisonAmount = 1
        symbols[0].eq(comparisonAmount)
        symbols[1].between(comparisonAmount, 2)
        symbols[2].ge(comparisonAmount)
        symbols[3].le(comparisonAmount)
        symbols[4].lt(comparisonAmount)
        symbols[5].gt(comparisonAmount)
        symbols[6].ne(comparisonAmount)

   }

}
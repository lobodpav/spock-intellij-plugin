package io.github.lobodpav.spock.inspection.block

import spock.lang.Specification

class BlockSpec extends Specification {

    def "Gets enum value by lowercase string"() {
        expect:
        Block.@Companion.valueOfOrNull(blockName) == Block.valueOf(blockName.toUpperCase())

        where:
        blockName << Block.entries.collect { it.name().toLowerCase() }
    }

    def "Returns null for an unknown block"() {
        expect:
        !Block.@Companion.valueOfOrNull("blah")
    }

    def "Returns Spock block name"() {
        expect:
        block.spockName == block.name().toLowerCase()

        where:
        block << Block.entries
    }

    def "A block can be followed by another one"() {
        expect:
        currentBlock.mustNotFollow(previousBlock) == mustNotFollow

        where:
        previousBlock  | currentBlock   || mustNotFollow
        Block.@THEN    | Block.@EXPECT  || false
        Block.@WHEN    | Block.@THEN    || false
        Block.@WHEN    | Block.@WHERE   || true
        Block.@WHEN    | Block.@WHEN    || true
        Block.@CLEANUP | Block.@WHERE   || false
        Block.@WHERE   | Block.@CLEANUP || true
    }
}

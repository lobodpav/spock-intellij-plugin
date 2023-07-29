package io.github.lobodpav.spock.block

interface BlockSurrounding {
    val allowedDirectPredecessors: Set<Block>
    val allowedDirectSuccessors: Set<Block>

    val canBeFirstBlock: Boolean
    val canBeLastBlock: Boolean
}

/**
 * Spock block (essentially a Groovy label).
 *
 * Enum values cannot reference other ones that are defined afterwards due to not being intialised yet.
 *
 * Hence the [BlockSurrounding] interface combined with [lazy] values.
 */
enum class Block : BlockSurrounding {
    GIVEN {
        override val allowedDirectPredecessors = emptySet<Block>()
        override val allowedDirectSuccessors by lazy { setOf(EXPECT, WHEN, CLEANUP, WHERE, AND) }

        override val canBeFirstBlock = true
        override val canBeLastBlock = true
    },
    SETUP {
        override val allowedDirectPredecessors by lazy { GIVEN.allowedDirectPredecessors }
        override val allowedDirectSuccessors by lazy { GIVEN.allowedDirectSuccessors }

        override val canBeFirstBlock = true
        override val canBeLastBlock = true
    },
    EXPECT {
        override val allowedDirectPredecessors by lazy { setOf(GIVEN, SETUP, EXPECT, THEN) }
        override val allowedDirectSuccessors by lazy { setOf(EXPECT, WHEN, CLEANUP, WHERE, AND) }

        override val canBeFirstBlock = true
        override val canBeLastBlock = true
    },
    WHEN {
        override val allowedDirectPredecessors by lazy { setOf(GIVEN, SETUP, EXPECT, THEN) }
        override val allowedDirectSuccessors by lazy { setOf(THEN, AND) }

        override val canBeFirstBlock = true
        override val canBeLastBlock = false
    },
    THEN {
        override val allowedDirectPredecessors by lazy { setOf(WHEN, THEN) }
        override val allowedDirectSuccessors by lazy { setOf(EXPECT, WHEN, THEN, CLEANUP, WHERE, AND) }

        override val canBeFirstBlock = false
        override val canBeLastBlock = true
    },
    CLEANUP {
        override val allowedDirectPredecessors by lazy { setOf(GIVEN, SETUP, EXPECT, THEN) }
        override val allowedDirectSuccessors by lazy { setOf(WHERE, AND) }

        override val canBeFirstBlock = true
        override val canBeLastBlock = true
    },
    WHERE {
        override val allowedDirectPredecessors by lazy { setOf(GIVEN, SETUP, EXPECT, THEN, CLEANUP) }
        override val allowedDirectSuccessors by lazy { setOf(AND) }

        override val canBeFirstBlock = true
        override val canBeLastBlock = true
    },
    AND {
        override val allowedDirectPredecessors: Set<Block>
            get() = throw UnsupportedOperationException("""
                AND blocks can be successors and predecessors of any other block.
                Hence, they are treated as whitespace and are skipped during block visiting.
            """.trimIndent())
        override val allowedDirectSuccessors: Set<Block>
            get() = allowedDirectPredecessors

        override val canBeFirstBlock = false
        override val canBeLastBlock = true
    };

    companion object {
        private val blockByName = entries.associateBy { it.name.lowercase() }

        /** Maps a string to a [Block] (case-insensitive). Returns `null` for unknown strings. */
        fun valueOfOrNull(blockName: String): Block? =
            blockByName[blockName.lowercase()]
    }

    /** String name of the Spock block */
    val spockName: String = name.lowercase()

    fun mustNotFollow(previousBlock: Block): Boolean = !allowedDirectPredecessors.contains(previousBlock)
}

package stacks.concurrent.perfomance

import org.junit.jupiter.api.BeforeEach
import stacks.concurrent.ConcurrentStackWithElimination

class ConcurrentStackWithEliminationPerfomanceTest : ConcurrentStackPerfomanceTest() {
    @BeforeEach
    override fun init() {
        stack = ConcurrentStackWithElimination()
    }
}
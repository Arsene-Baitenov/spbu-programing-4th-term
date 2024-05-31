package stacks.concurrent.perfomance

import org.junit.jupiter.api.BeforeEach
import stacks.concurrent.TreiberStack

class TreiberStackPerfomanceTest : ConcurrentStackPerfomanceTest() {
    @BeforeEach
    override fun init() {
        stack = TreiberStack()
    }
}
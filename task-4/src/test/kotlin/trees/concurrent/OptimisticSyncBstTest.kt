package trees.concurrent

import org.junit.jupiter.api.BeforeEach

class OptimisticSyncBstTest : ConcurrentBstTest() {
    @BeforeEach
    override fun init() {
        tree = OptimisticSyncBst()
    }
}
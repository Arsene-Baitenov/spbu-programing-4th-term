package trees.concurrent

import org.junit.jupiter.api.BeforeEach

class CoarseGrainedSyncBstTest : ConcurrentBstTest() {
    @BeforeEach
    override fun init() {
        tree = CoarseGrainedSyncBst()
    }
}
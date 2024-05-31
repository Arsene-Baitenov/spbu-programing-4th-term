package trees.concurrent

import org.junit.jupiter.api.BeforeEach

class FineGrainedSyncBstTest : ConcurrentBstTest() {
    @BeforeEach
    override fun init() {
        tree = FineGrainedSyncBst()
    }
}
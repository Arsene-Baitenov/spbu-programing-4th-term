package trees.concurrent

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

abstract class CoarseGrainedSyncBstImpl<K : Comparable<K>, V, E : Edge<K, V, E>>(
    newEdge: (Vertex<K, V, E>?) -> E
) : BstImpl<K, V, E>(newEdge) {
    private val mutex = Mutex()

    override suspend fun put(key: K, value: V): V? {
        mutex.withLock {
            return super.put(key, value)
        }
    }

    override suspend fun remove(key: K): V? {
        mutex.withLock {
            return super.remove(key)
        }
    }

    override suspend fun get(key: K): V? {
        mutex.withLock {
            return super.get(key)
        }
    }
}

class BstEdge<K, V>(
    vertex: Vertex<K, V, BstEdge<K, V>>?
) : Edge<K, V, BstEdge<K, V>>(vertex)

class CoarseGrainedSyncBst<K : Comparable<K>, V> : CoarseGrainedSyncBstImpl<K, V, BstEdge<K, V>>(
    { vertex -> BstEdge(vertex) }
)
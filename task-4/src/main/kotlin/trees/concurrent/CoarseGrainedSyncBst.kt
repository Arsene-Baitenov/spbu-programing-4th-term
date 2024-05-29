package trees.concurrent

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import trees.Bst
import trees.BstEdge
import trees.Edge
import trees.Vertex

class CoarseGrainedSyncBst<K : Comparable<K>, V, E : Edge<K, V, E>>(
    newEdge: (Vertex<K, V, E>?) -> E
) : Bst<K, V, E>(newEdge) {
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

fun <K : Comparable<K>, V> coarseGrainedSyncBst() =
    CoarseGrainedSyncBst<K, V, BstEdge<K, V>> { vertex -> BstEdge(vertex) }
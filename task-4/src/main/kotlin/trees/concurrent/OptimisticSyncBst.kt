package trees.concurrent

import trees.Vertex

open class OptimisticSyncBst<K : Comparable<K>, V, E : EdgeWithMutex<K, V, E>>(
    newEdge: (Vertex<K, V, E>?) -> E
) : FineGrainedSyncBst<K, V, E>(newEdge) {
    private fun validate(key: K, target: E): Boolean {
        val edge = getEdgeToVertexWithKey(key)

        return (edge === target)
    }

    override suspend fun getMutexedEdgeToVertexWithKey(key: K): E {
        while (true) {
            val edge = getEdgeToVertexWithKey(key)

            edge.mutex.lock()
            if (validate(key, edge)) {
                return edge
            } else {
                edge.mutex.unlock()
            }
        }
    }

    private fun validateRightLeft(startEdge: E, target: E): Boolean {
        val edge = getRightLeftEdge(startEdge)

        return edge === target
    }

    override suspend fun getRightLeftMutexedEdge(startEdge: E): E {
        while (true) {
            val edge = getRightLeftEdge(startEdge)

            edge.mutex.lock()
            if (validateRightLeft(startEdge, edge)) {
                return edge
            } else {
                edge.mutex.unlock()
            }
        }
    }
}


fun <K : Comparable<K>, V> optimisticSyncBst() =
    OptimisticSyncBst<K, V, BstEdgeWithMutex<K, V>> { vertex -> BstEdgeWithMutex(vertex) }
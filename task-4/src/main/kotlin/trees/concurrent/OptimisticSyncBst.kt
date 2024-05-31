package trees.concurrent

abstract class OptimisticSyncBstImpl<K : Comparable<K>, V, E : EdgeWithMutex<K, V, E>>(
    newEdge: (Vertex<K, V, E>?) -> E
) : FineGrainedSyncBstImpl<K, V, E>(newEdge) {
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

    private fun validateLeft(startEdge: E, target: E): Boolean {
        val edge = getLeftEdge(startEdge)

        return edge === target
    }

    override suspend fun getLeftMutexedEdge(startEdge: E): E {
        while (true) {
            val edge = getLeftEdge(startEdge)

            edge.mutex.lock()
            if (validateLeft(startEdge, edge)) {
                return edge
            } else {
                edge.mutex.unlock()
            }
        }
    }
}

class OptimisticSyncBst<K : Comparable<K>, V> : OptimisticSyncBstImpl<K, V, BstEdgeWithMutex<K, V>>(
    { vertex -> BstEdgeWithMutex(vertex) }
)
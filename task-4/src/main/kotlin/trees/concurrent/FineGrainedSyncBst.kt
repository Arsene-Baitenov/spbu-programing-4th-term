package trees.concurrent

import kotlinx.coroutines.sync.Mutex
import trees.Bst
import trees.Edge
import trees.Vertex

open class FineGrainedSyncBst<K : Comparable<K>, V, E : EdgeWithMutex<K, V, E>>(
    newEdge: (Vertex<K, V, E>?) -> E
) : Bst<K, V, E>(newEdge) {
    protected open suspend fun getMutexedEdgeToVertexWithKey(key: K): E {
        var edge = rootEdge
        edge.mutex.lock()
        var vertex = edge.vertex

        while (vertex != null && vertex.key != key) {
            if (vertex.key < key) {
                vertex.leftEdge.mutex.lock()
                edge.mutex.unlock()
                edge = vertex.leftEdge
            } else {
                vertex.rightEdge.mutex.lock()
                edge.mutex.unlock()
                edge = vertex.rightEdge
            }
            vertex = edge.vertex
        }

        return edge
    }

    override suspend fun put(key: K, value: V): V? {
        val edge = getMutexedEdgeToVertexWithKey(key)
        try {
            edge.vertex?.let { vertex ->
                return vertex.value.also { vertex.value = value }
            } ?: run {
                edge.vertex = newVertex(key, value)
                return null
            }
        } finally {
            edge.mutex.unlock()
        }
    }

    protected open suspend fun getRightLeftMutexedEdge(startEdge: E): E {
        var edge = startEdge
        edge.mutex.lock()
        var vertex = edge.vertex

        while (vertex != null) {
            vertex.leftEdge.mutex.lock()
            edge.mutex.unlock()
            edge = vertex.leftEdge
            vertex = edge.vertex
        }

        return edge
    }

    override suspend fun remove(key: K): V? {
        val edge = getMutexedEdgeToVertexWithKey(key)

        try {
            return edge.vertex?.let { vertex ->
                vertex.value.also {
                    vertex.leftEdge.mutex.lock()
                    vertex.rightEdge.mutex.lock()

                    try {
                        vertex.leftEdge.vertex?.let { left ->
                            vertex.rightEdge.vertex?.let { right ->
                                edge.vertex = right
                                val rightLeftEdge = getRightLeftMutexedEdge(right.leftEdge)
                                try {
                                    rightLeftEdge.vertex = vertex
                                } finally {
                                    rightLeftEdge.mutex.unlock()
                                }
                            } ?: run {
                                edge.vertex = left
                            }
                        } ?: run {
                            edge.vertex = vertex.rightEdge.vertex
                        }
                    } finally {
                        vertex.rightEdge.mutex.lock()
                        vertex.leftEdge.mutex.unlock()
                    }
                }
            }
        } finally {
            edge.mutex.unlock()
        }

    }

    override suspend fun get(key: K): V? {
        val edge = getMutexedEdgeToVertexWithKey(key)
        try {
            return edge.vertex?.value
        } finally {
            edge.mutex.unlock()
        }
    }

}

open class EdgeWithMutex<K, V, E : EdgeWithMutex<K, V, E>>(
    vertex: Vertex<K, V, E>?
) : Edge<K, V, E>(vertex) {
    val mutex = Mutex()
}

class BstEdgeWithMutex<K, V>(
    vertex: Vertex<K, V, BstEdgeWithMutex<K, V>>?
) : EdgeWithMutex<K, V, BstEdgeWithMutex<K, V>>(vertex)

fun <K : Comparable<K>, V> fineGrainedSyncBst() =
    FineGrainedSyncBst<K, V, BstEdgeWithMutex<K, V>> { vertex -> BstEdgeWithMutex(vertex) }
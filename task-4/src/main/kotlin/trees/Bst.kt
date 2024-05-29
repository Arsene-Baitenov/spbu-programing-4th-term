package trees

open class Bst<K : Comparable<K>, V, E : Edge<K, V, E>>(
    protected val newEdge: (Vertex<K, V, E>?) -> E
) {
    protected val rootEdge = newEdge(null)

    protected fun newVertex(key: K, value: V, leftEdge: E = newEdge(null), rightEdge: E = newEdge(null)) =
        Vertex(key, value, leftEdge, rightEdge)

    protected fun getEdgeToVertexWithKey(key: K): E {
        var edge = rootEdge
        var vertex = edge.vertex

        while (vertex != null && vertex.key != key) {
            edge = if (vertex.key < key) {
                vertex.leftEdge
            } else {
                vertex.rightEdge
            }
            vertex = edge.vertex
        }

        return edge
    }

    open suspend fun put(key: K, value: V): V? {
        val edge = getEdgeToVertexWithKey(key)

        edge.vertex?.let { vertex ->
            return vertex.value.also { vertex.value = value }
        } ?: run {
            edge.vertex = newVertex(key, value)
            return null
        }
    }

    protected fun getRightLeftEdge(startEdge: E): E {
        var edge = startEdge
        var vertex = edge.vertex

        while (vertex != null) {
            edge = vertex.leftEdge
            vertex = edge.vertex
        }

        return edge
    }

    open suspend fun remove(key: K): V? {
        val edge = getEdgeToVertexWithKey(key)

        return edge.vertex?.let { vertex ->
            vertex.value.also {
                vertex.leftEdge.vertex?.let { left ->
                    vertex.rightEdge.vertex?.let { right ->
                        edge.vertex = right
                        val rightLeftEdge = getRightLeftEdge(right.leftEdge)
                        rightLeftEdge.vertex = left
                    } ?: run {
                        edge.vertex = left
                    }
                } ?: run {
                    edge.vertex = vertex.rightEdge.vertex
                }
            }
        }
    }

    open suspend fun get(key: K) = getEdgeToVertexWithKey(key).vertex?.value
}

class Vertex<K, V, E : Edge<K, V, E>>(
    val key: K, var value: V, val leftEdge: E, val rightEdge: E
)

open class Edge<K, V, E : Edge<K, V, E>>(
    var vertex: Vertex<K, V, E>?
)

class BstEdge<K, V>(
    vertex: Vertex<K, V, BstEdge<K, V>>?
) : Edge<K, V, BstEdge<K, V>>(vertex)

fun <K : Comparable<K>, V> bst() = Bst<K, V, BstEdge<K, V>> { vertex -> BstEdge(vertex) }
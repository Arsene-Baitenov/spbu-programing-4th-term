package stacks.concurrent

interface ConcurrentStack<T> {
    fun push(value: T)

    fun pop(): T?

    fun top(): T?
}

data class Node<T>(
    val value: T,
    val next: Node<T>? = null
)
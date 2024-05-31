package stacks.concurrent

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.loop

open class TreiberStack<T> : ConcurrentStack<T> {
    private val head = atomic<Node<T>?>(null)

    override fun push(value: T): Unit = this.head.loop { head ->
        val newHead = Node(value, head)

        if (this.head.compareAndSet(head, newHead)) {
            return
        }
    }

    override fun pop(): T? = this.head.loop { head ->
        val newHead = head?.next

        if (this.head.compareAndSet(head, newHead)) {
            return head?.value
        }
    }

    override fun top(): T? = head.value?.value

}
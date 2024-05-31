package stacks.concurrent

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.loop
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicStampedReference
import kotlin.random.Random

const val EliminationArraySize = 10

const val MaxAttempts = 50


class ConcurrentStackWithElimination<T> : ConcurrentStack<T> {
    private val head = atomic<Node<T>?>(null)

    private val eliminationArray = EliminationArray<T>()

    override fun push(value: T): Unit = this.head.loop { head ->
        val newHead = Node(value, head)
        if (this.head.compareAndSet(head, newHead)) {
            return
        }

        val visitResult = eliminationArray.visit(value)
        if (visitResult.isSuccess && visitResult.getOrNull() == null) {
            return
        }
    }

    override fun pop(): T? = this.head.loop { head ->
        val newHead = head?.next
        if (this.head.compareAndSet(head, newHead)) {
            return head?.value
        }

        val visitResult = eliminationArray.visit(null)
        if (visitResult.isSuccess) {
            visitResult.getOrNull()?.let { return it }
        }
    }

    override fun top(): T? = head.value?.value
}

class EliminationArray<T>(
    private val capacity: Int = EliminationArraySize,
    private val maxAttempts: Int = MaxAttempts
) {
    private val exchanger = Array(capacity) { LockFreeExchanger<T>(maxAttempts) }

    fun visit(value: T?): Result<T?> {
        val slot = Random.nextInt(capacity)

        return exchanger[slot].exchange(value)
    }
}

class LockFreeExchanger<T>(
    private val maxAttempts: Int
) {
    private enum class State { EMPTY, WAITING, BUSY }

    private val slot = AtomicStampedReference<T>(null, State.EMPTY.ordinal)

    fun exchange(item: T?): Result<T?> {
        val stampHolder = IntArray(1) { State.EMPTY.ordinal }

        repeat(maxAttempts) {
            val curr = slot.get(stampHolder)
            when (stampHolder.first()) {
                State.EMPTY.ordinal -> {
                    if (slot.compareAndSet(curr, item, State.EMPTY.ordinal, State.WAITING.ordinal)) {
                        repeat(maxAttempts) {
                            val curr = slot.get(stampHolder)
                            if (stampHolder.first() == State.BUSY.ordinal) {
                                slot.set(null, State.EMPTY.ordinal)
                                return Result.success(curr)
                            }
                        }

                        if (slot.compareAndSet(item, null, State.WAITING.ordinal, State.EMPTY.ordinal)) {
                            return Result.failure(TimeoutException())
                        }
                        val curr = slot.get(stampHolder)
                        slot.set(null, State.EMPTY.ordinal)
                        return Result.success(curr)
                    }
                }

                State.WAITING.ordinal -> {
                    if (slot.compareAndSet(curr, item, State.WAITING.ordinal, State.BUSY.ordinal)) {
                        return Result.success(curr)
                    }
                }
            }
        }

        return Result.failure(TimeoutException())
    }
}
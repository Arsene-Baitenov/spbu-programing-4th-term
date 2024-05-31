package stacks.concurrent

class SequentialStack {
    private val stack = mutableListOf<Int>()

    fun push(value: Int): Unit {
        stack.add(value)
    }

    fun pop(): Int? = stack.removeLastOrNull()

    fun top(): Int? = stack.lastOrNull()
}
package stacks.concurrent

import org.jetbrains.kotlinx.lincheck.LoggingLevel
import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.check
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

abstract class ConcurrentStackTest(
    private val stack: ConcurrentStack<Int>
) {
    @Operation
    fun push(value: Int) = stack.push(value)

    @Operation
    fun pop(): Int? = stack.pop()

    @Operation
    fun top(): Int? = stack.top()

    @ParameterizedTest
    @MethodSource("threadNumsProvider")
    fun modelTest(threadsNum: Int) = ModelCheckingOptions()
        .iterations(50)
        .invocationsPerIteration(1000)
        .threads(threadsNum)
        .actorsPerThread(3)
        .sequentialSpecification(SequentialStack::class.java)
        .checkObstructionFreedom()
        .logLevel(LoggingLevel.INFO)
        .check(this::class.java)

    @ParameterizedTest
    @MethodSource("threadNumsProvider")
    fun stressTest(threadsNum: Int) = StressOptions()
        .iterations(50)
        .invocationsPerIteration(1000)
        .threads(threadsNum)
        .actorsPerThread(3)
        .sequentialSpecification(SequentialStack::class.java)
        .logLevel(LoggingLevel.INFO)
        .check(this::class.java)

    companion object {
        @JvmStatic
        fun threadNumsProvider(): List<Arguments> =
            listOf(Arguments.of(1), Arguments.of(2), Arguments.of(4))
    }
}
package stacks.concurrent.perfomance

import kotlinx.coroutines.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import stacks.concurrent.ConcurrentStack
import kotlin.random.Random
import kotlin.system.measureTimeMillis

abstract class ConcurrentStackPerfomanceTest {
    protected lateinit var stack: ConcurrentStack<Int>

    @BeforeEach
    abstract fun init()

    @Disabled
    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    @ParameterizedTest
    @MethodSource("threadNumsProvider")
    fun performanceTest(threadsNum: Int, iterationsNum: Int) {
        var executionTimeMs = 0L
        repeat(10) {
            val jobs = mutableListOf<Job>()
            val timeMs = measureTimeMillis {
                runBlocking {
                    repeat(threadsNum) { id ->
                        jobs.add(launch(newSingleThreadContext("Thread$id")) {
                            repeat(iterationsNum) {
                                if (id % 2 == 0) {
                                    stack.push(Random.nextInt(1e9.toInt()))
                                } else {
                                    stack.pop()
                                }
                            }
                        })

                    }

                    jobs.joinAll()
                }
            }
            executionTimeMs += timeMs
        }
        executionTimeMs /= 10

        println("Thread num: $threadsNum, iterations: $iterationsNum")
        println("Execution time: $executionTimeMs ms")
    }

    companion object {
        @JvmStatic
        fun threadNumsProvider(): List<Arguments> =
            listOf(
                Arguments.of(1, 1e6.toInt()),
                Arguments.of(2, 1e6.toInt()),
                Arguments.of(4, 1e6.toInt()),
                Arguments.of(12, 1e6.toInt()),
            )
    }
}
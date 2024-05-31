package trees.concurrent

import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.random.Random

abstract class ConcurrentBstTest {
    private val randomizer = Random(0)
    private val elementsCount = 10000
    protected lateinit var tree: ConcurrentBst<Int, Int>

    @BeforeEach
    abstract fun init()

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    @ParameterizedTest
    @MethodSource("threadNumsProvider")
    fun `Function put adds all elements with unique keys`(threadsNum: Int) {
        val values =
            List(threadsNum) { threadId ->
                List(elementsCount) {
                    val rndInt = randomizer.nextInt(5000)
                    Pair(rndInt - (rndInt % threadsNum) + threadId, rndInt)
                }
            }

        runBlocking {
            values.forEachIndexed { id, list ->
                launch(newSingleThreadContext("Thread$id")) {
                    list.forEach { tree.put(it.first, it.second) }
                }
            }
        }

        runBlocking {
            values.forEachIndexed { id, list ->
                launch(newSingleThreadContext("Thread$id")) {
                    list.reversed().distinctBy { it.first }.forEach {
                        assertEquals(it.second, tree.get(it.first))
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    @ParameterizedTest
    @MethodSource("threadNumsProvider")
    fun `Function remove deletes the existing element correctly`(threadsNum: Int) {
        val values =
            List(threadsNum) { threadId ->
                List(elementsCount) {
                    val rndInt = randomizer.nextInt(5000)
                    Pair(rndInt - (rndInt % threadsNum) + threadId, rndInt)
                }
            }

        runBlocking {
            values.forEachIndexed { id, list ->
                launch(newSingleThreadContext("Thread$id")) {
                    list.forEach { tree.put(it.first, it.second) }
                }
            }
        }

        runBlocking {
            values.forEachIndexed { id, list ->
                launch(newSingleThreadContext("Thread$id")) {
                    list.forEach {
                        tree.remove(it.first)
                    }
                }
            }
        }

        runBlocking {
            values.forEachIndexed { id, list ->
                launch(newSingleThreadContext("Thread$id")) {
                    list.forEach {
                        assertNull(tree.get(it.first))
                    }
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun threadNumsProvider(): List<Arguments> =
            listOf(Arguments.of(1), Arguments.of(2), Arguments.of(4))
    }
}
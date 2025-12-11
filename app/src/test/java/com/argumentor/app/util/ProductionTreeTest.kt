package com.argumentor.app.util

import android.util.Log
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ProductionTreeTest {

    private lateinit var productionTree: ProductionTree
    private lateinit var mockedLog: MockedStatic<Log>

    @Before
    fun setup() {
        productionTree = ProductionTree()
        ProductionTree.clearContext()

        // Mock Android Log
        mockedLog = Mockito.mockStatic(Log::class.java)
        mockedLog.`when`<Int> { Log.w(Mockito.anyString(), Mockito.anyString(), Mockito.any()) }.thenReturn(0)
        mockedLog.`when`<Int> { Log.e(Mockito.anyString(), Mockito.anyString(), Mockito.any()) }.thenReturn(0)
        mockedLog.`when`<Int> { Log.wtf(Mockito.anyString(), Mockito.anyString(), Mockito.any()) }.thenReturn(0)
    }

    @After
    fun tearDown() {
        mockedLog.close()
        ProductionTree.clearContext()
    }

    @Test
    fun `getCrashContext returns empty list initially`() {
        assertThat(ProductionTree.getCrashContext()).isEmpty()
    }

    @Test
    fun `clearContext removes all entries`() {
        // Add some entries via the tree (we need to access internal state)
        productionTree.w("Test warning")

        assertThat(ProductionTree.getCrashContext()).isNotEmpty()

        ProductionTree.clearContext()

        assertThat(ProductionTree.getCrashContext()).isEmpty()
    }

    @Test
    fun `getCrashContextString formats entries correctly`() {
        ProductionTree.clearContext()

        val contextString = ProductionTree.getCrashContextString()

        assertThat(contextString).contains("=== Recent Log Context")
        assertThat(contextString).contains("=== End of Context ===")
    }

    @Test
    fun `LogEntry format includes all components`() {
        val entry = ProductionTree.LogEntry(
            timestamp = 1704067200000L, // 2024-01-01 00:00:00 UTC
            priority = Log.ERROR,
            tag = "TestTag",
            message = "Test message",
            throwable = null
        )

        val formatted = entry.format()

        assertThat(formatted).contains("E")
        assertThat(formatted).contains("[TestTag]")
        assertThat(formatted).contains("Test message")
    }

    @Test
    fun `LogEntry format includes stack trace when throwable present`() {
        val exception = RuntimeException("Test exception")
        val entry = ProductionTree.LogEntry(
            timestamp = System.currentTimeMillis(),
            priority = Log.ERROR,
            tag = "TestTag",
            message = "Error occurred",
            throwable = exception
        )

        val formatted = entry.format()

        assertThat(formatted).contains("RuntimeException")
        assertThat(formatted).contains("Test exception")
    }

    @Test
    fun `LogEntry formats all priority levels correctly`() {
        val priorities = mapOf(
            Log.VERBOSE to "V",
            Log.DEBUG to "D",
            Log.INFO to "I",
            Log.WARN to "W",
            Log.ERROR to "E",
            Log.ASSERT to "A"
        )

        priorities.forEach { (priority, expected) ->
            val entry = ProductionTree.LogEntry(
                timestamp = System.currentTimeMillis(),
                priority = priority,
                tag = null,
                message = "Test",
                throwable = null
            )

            assertThat(entry.format()).contains(" $expected ")
        }
    }

    @Test
    fun `LogEntry handles null tag`() {
        val entry = ProductionTree.LogEntry(
            timestamp = System.currentTimeMillis(),
            priority = Log.WARN,
            tag = null,
            message = "No tag message",
            throwable = null
        )

        val formatted = entry.format()

        assertThat(formatted).contains("No tag message")
        assertThat(formatted).doesNotContain("[]")
    }
}

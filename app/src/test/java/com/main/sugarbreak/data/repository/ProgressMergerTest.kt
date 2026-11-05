package com.main.sugarbreak.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class ProgressMergerTest {
    private data class Record(val id: String, val value: String, val updatedAt: LocalDateTime)

    @Test fun `merge keeps disjoint records from both sources`() {
        val local = Record("local", "a", LocalDateTime.parse("2026-01-01T10:00:00"))
        val remote = Record("remote", "b", LocalDateTime.parse("2026-01-01T11:00:00"))
        assertEquals(listOf(local, remote), mergeByNewest(listOf(local), listOf(remote), { it.id }, { it.updatedAt }))
    }

    @Test fun `merge selects newest matching record`() {
        val local = Record("same", "local", LocalDateTime.parse("2026-01-01T10:00:00"))
        val remote = Record("same", "remote", LocalDateTime.parse("2026-01-01T11:00:00"))
        assertEquals(listOf(remote), mergeByNewest(listOf(local), listOf(remote), { it.id }, { it.updatedAt }))
    }

    @Test fun `merge keeps local record when timestamps tie`() {
        val time = LocalDateTime.parse("2026-01-01T10:00:00")
        val local = Record("same", "local", time)
        val remote = Record("same", "remote", time)
        assertEquals(listOf(local), mergeByNewest(listOf(local), listOf(remote), { it.id }, { it.updatedAt }))
    }
}

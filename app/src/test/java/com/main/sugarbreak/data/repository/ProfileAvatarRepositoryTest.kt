package com.main.sugarbreak.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileAvatarRepositoryTest {
    @Test
    fun accountSwitchClearsPendingAvatarAndIgnoresLateConfirmation() = runTest {
        val accounts = MutableStateFlow<String?>("a")
        val first = MutableSharedFlow<ProfileAvatar>(extraBufferCapacity = 1)
        val second = MutableSharedFlow<ProfileAvatar>(extraBufferCapacity = 1)
        val received = mutableListOf<ProfileAvatar>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            observeAccountAvatars(accounts) { if (it == "a") first else second }
                .collect { received.add(it) }
        }
        runCurrent()
        first.tryEmit(ProfileAvatar("a", byteArrayOf(1), pending = true))
        runCurrent()
        assertEquals(true, received.last().pending)

        accounts.value = "b"
        runCurrent()
        assertEquals(ProfileAvatar("b"), received.last())
        first.tryEmit(ProfileAvatar("a", byteArrayOf(1)))
        runCurrent()
        assertEquals(ProfileAvatar("b"), received.last())

        accounts.value = null
        runCurrent()
        assertNull(received.last().uid)
        assertNull(received.last().jpeg)
    }
}

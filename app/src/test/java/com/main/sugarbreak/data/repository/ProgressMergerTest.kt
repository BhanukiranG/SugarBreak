package com.main.sugarbreak.data.repository

import com.main.sugarbreak.domain.model.AuthenticatedUser
import com.main.sugarbreak.domain.usecase.resolveAppRoute
import com.main.sugarbreak.data.preferences.claimLegacyOnboardingValue
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class ProgressMergerTest {
    @Test fun `in flight sync preserves newer admin profile corrections`() {
        val proposed = mapOf<String, Any?>("displayName" to "Old", "displayNameUpdatedAt" to 10L,
            "challengeBehavior" to "CONTINUE", "challengeBehaviorUpdatedAt" to 10L,
            "onboardingCompleted" to false, "updatedAt" to 30L)
        val latest = mapOf<String, Any?>("displayName" to "Corrected", "displayNameUpdatedAt" to 20L,
            "challengeBehavior" to "RESET_STREAK", "challengeBehaviorUpdatedAt" to 20L,
            "onboardingCompleted" to true, "updatedAt" to 20L)
        val merged = mergeCloudProfileWrite(proposed, latest)
        assertEquals("Corrected", merged["displayName"])
        assertEquals("RESET_STREAK", merged["challengeBehavior"])
        assertEquals(true, merged["onboardingCompleted"])
        assertEquals(30L, merged["updatedAt"])
    }
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

    @Test fun `explicit remote onboarding false keeps an account in onboarding even with an unfinished challenge`() {
        assertEquals(false, resolveOnboardingCompletion(localCompletion = null, remoteCompletion = false, hasRemoteChallenges = true))
    }

    @Test fun `legacy cloud account with challenges is treated as onboarded`() {
        assertEquals(true, resolveOnboardingCompletion(localCompletion = null, remoteCompletion = null, hasRemoteChallenges = true))
    }

    @Test fun `local completed status survives a stale remote false`() {
        assertEquals(true, resolveOnboardingCompletion(localCompletion = true, remoteCompletion = false, hasRemoteChallenges = false))
    }

    @Test fun `fresh account without local or cloud progress starts onboarding`() {
        assertEquals(false, resolveOnboardingCompletion(localCompletion = null, remoteCompletion = null, hasRemoteChallenges = false))
    }

    @Test fun `account completion does not leak to a different fresh account`() {
        val accountACompleted = resolveOnboardingCompletion(true, true, hasRemoteChallenges = true)
        val accountBFresh = resolveOnboardingCompletion(null, null, hasRemoteChallenges = false)

        assertEquals("home", resolveAppRoute(AuthenticatedUser("a", "a@example.test", null, true), accountACompleted))
        assertEquals("onboarding", resolveAppRoute(AuthenticatedUser("b", "b@example.test", null, true), accountBFresh))
    }

    @Test fun `legacy onboarding completion is claimed only by the first account`() {
        val migratedAccount = claimLegacyOnboardingValue(accountValue = null, legacyValue = true)
        val laterAccount = claimLegacyOnboardingValue(accountValue = null, legacyValue = null)

        assertEquals(true, migratedAccount)
        assertEquals(null, laterAccount)
    }

    @Test fun `existing account onboarding value takes precedence over legacy value`() {
        assertEquals(false, claimLegacyOnboardingValue(accountValue = false, legacyValue = true))
    }

    @Test fun `newer remote profile name replaces an older local value`() {
        assertEquals(
            "Remote name" to 20L,
            resolveTimestampedValue("Local name", 10L, "Remote name", 20L)
        )
    }

    @Test fun `newer local profile name is uploaded over stale cloud value`() {
        assertEquals(
            "Local name" to 20L,
            resolveTimestampedValue("Local name", 20L, "Remote name", 10L)
        )
    }

    @Test fun `remote challenge behavior restores when this account has no local value`() {
        assertEquals(
            "RESET_STREAK" to 15L,
            resolveChallengeBehavior(null, 0L, "RESET_STREAK", 15L)
        )
    }

    @Test fun `newer challenge behavior wins across devices`() {
        assertEquals(
            "ADD_RECOVERY_DAY" to 30L,
            resolveChallengeBehavior("CONTINUE", 20L, "ADD_RECOVERY_DAY", 30L)
        )
    }

    @Test fun `pending reset suppresses only check-ins for its challenge`() {
        val cutoff = LocalDateTime.parse("2026-01-10T09:00:00")
        val pending = mapOf("challenge-a" to cutoff)
        assertEquals(true, isCheckInSuppressedByPendingReset("challenge-a", cutoff, pending))
        assertEquals(false, isCheckInSuppressedByPendingReset("challenge-a", cutoff.plusSeconds(1), pending))
        assertEquals(false, isCheckInSuppressedByPendingReset("challenge-b", cutoff, pending))
        assertEquals(false, isCheckInSuppressedByPendingReset(null, cutoff, pending))
        assertEquals(true, isCheckInSuppressedByPendingReset("challenge-a", null, pending))
    }
}

package com.main.nosugar.domain.model

enum class ChallengeStatus {
    ACTIVE, COMPLETED, PAUSED, CANCELLED
}

enum class CheckInStatus {
    PENDING, SUCCESS, SLIP, SKIPPED
}

enum class SlipReason {
    SWEET, DESSERT, SUGARY_DRINK, TEA_OR_COFFEE, OTHER
}

enum class ChallengeBehavior {
    CONTINUE, ADD_RECOVERY_DAY, RESET_STREAK
}

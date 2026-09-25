package com.main.sugarbreak.util

import java.time.LocalDate
import java.time.LocalDateTime

object DateUtils {
    fun today(): LocalDate = LocalDate.now()
    fun now(): LocalDateTime = LocalDateTime.now()
}

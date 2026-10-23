package com.main.sugarbreak.notification

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class ReminderSchedulerTimeTest {

    private fun createCalendar(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int = 0, millisecond: Int = 0): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, second)
            set(Calendar.MILLISECOND, millisecond)
        }
    }

    @Test
    fun `test scheduling later today sets alarm on same day`() {
        // Current: 2026-10-23 at 10:30:00
        val now = createCalendar(2026, Calendar.OCTOBER, 23, 10, 30, 0, 0).timeInMillis
        // Schedule for 20:00
        val alarmTime = ReminderSchedulerImpl.calculateNextAlarmTime(20, 0, now)

        val resultCal = Calendar.getInstance().apply { timeInMillis = alarmTime }
        assertEquals(2026, resultCal.get(Calendar.YEAR))
        assertEquals(Calendar.OCTOBER, resultCal.get(Calendar.MONTH))
        assertEquals(23, resultCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(20, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, resultCal.get(Calendar.MINUTE))
        assertEquals(0, resultCal.get(Calendar.SECOND))
        assertEquals(0, resultCal.get(Calendar.MILLISECOND))
    }

    @Test
    fun `test scheduling earlier today rolls over to next day`() {
        // Current: 2026-10-23 at 21:15:00
        val now = createCalendar(2026, Calendar.OCTOBER, 23, 21, 15, 0, 0).timeInMillis
        // Schedule for 20:00 (already passed today)
        val alarmTime = ReminderSchedulerImpl.calculateNextAlarmTime(20, 0, now)

        val resultCal = Calendar.getInstance().apply { timeInMillis = alarmTime }
        assertEquals(2026, resultCal.get(Calendar.YEAR))
        assertEquals(Calendar.OCTOBER, resultCal.get(Calendar.MONTH))
        assertEquals(24, resultCal.get(Calendar.DAY_OF_MONTH)) // Tomorrow
        assertEquals(20, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, resultCal.get(Calendar.MINUTE))
    }

    @Test
    fun `test scheduling at exact same minute and second rolls over to tomorrow`() {
        // Current: 2026-10-23 at 20:00:00.000
        val now = createCalendar(2026, Calendar.OCTOBER, 23, 20, 0, 0, 0).timeInMillis
        // Schedule for 20:00
        val alarmTime = ReminderSchedulerImpl.calculateNextAlarmTime(20, 0, now)

        val resultCal = Calendar.getInstance().apply { timeInMillis = alarmTime }
        assertEquals(24, resultCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(20, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, resultCal.get(Calendar.MINUTE))
    }

    @Test
    fun `test midnight boundary scheduling (00_00)`() {
        // Current: 2026-10-23 at 23:45:00
        val now = createCalendar(2026, Calendar.OCTOBER, 23, 23, 45, 0, 0).timeInMillis
        // Schedule for 00:00 (midnight)
        val alarmTime = ReminderSchedulerImpl.calculateNextAlarmTime(0, 0, now)

        val resultCal = Calendar.getInstance().apply { timeInMillis = alarmTime }
        assertEquals(24, resultCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, resultCal.get(Calendar.MINUTE))
    }

    @Test
    fun `test end of day boundary scheduling (23_59)`() {
        // Current: 2026-10-23 at 23:50:00
        val now = createCalendar(2026, Calendar.OCTOBER, 23, 23, 50, 0, 0).timeInMillis
        // Schedule for 23:59 (still today)
        val alarmTime = ReminderSchedulerImpl.calculateNextAlarmTime(23, 59, now)

        val resultCal = Calendar.getInstance().apply { timeInMillis = alarmTime }
        assertEquals(23, resultCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(23, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, resultCal.get(Calendar.MINUTE))
    }

    @Test
    fun `test month boundary rollover`() {
        // Current: 2026-02-28 at 22:00:00 (non-leap year, last day of Feb)
        val now = createCalendar(2026, Calendar.FEBRUARY, 28, 22, 0, 0, 0).timeInMillis
        // Schedule for 20:00 (past)
        val alarmTime = ReminderSchedulerImpl.calculateNextAlarmTime(20, 0, now)

        val resultCal = Calendar.getInstance().apply { timeInMillis = alarmTime }
        assertEquals(Calendar.MARCH, resultCal.get(Calendar.MONTH))
        assertEquals(1, resultCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(20, resultCal.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun `test year boundary rollover on New Year eve`() {
        // Current: 2026-12-31 at 21:00:00
        val now = createCalendar(2026, Calendar.DECEMBER, 31, 21, 0, 0, 0).timeInMillis
        // Schedule for 20:00 (past)
        val alarmTime = ReminderSchedulerImpl.calculateNextAlarmTime(20, 0, now)

        val resultCal = Calendar.getInstance().apply { timeInMillis = alarmTime }
        assertEquals(2027, resultCal.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, resultCal.get(Calendar.MONTH))
        assertEquals(1, resultCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(20, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, resultCal.get(Calendar.MINUTE))
    }

    @Test
    fun `test year boundary same day on New Year eve`() {
        // Current: 2026-12-31 at 09:00:00
        val now = createCalendar(2026, Calendar.DECEMBER, 31, 9, 0, 0, 0).timeInMillis
        // Schedule for 20:00 (later today)
        val alarmTime = ReminderSchedulerImpl.calculateNextAlarmTime(20, 0, now)

        val resultCal = Calendar.getInstance().apply { timeInMillis = alarmTime }
        assertEquals(2026, resultCal.get(Calendar.YEAR))
        assertEquals(Calendar.DECEMBER, resultCal.get(Calendar.MONTH))
        assertEquals(31, resultCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(20, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, resultCal.get(Calendar.MINUTE))
    }
}

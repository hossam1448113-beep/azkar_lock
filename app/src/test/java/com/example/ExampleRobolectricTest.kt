package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("أذكار القفل", appName)
  }

  @Test
  fun `session state persistence retains progress across reboot`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = com.example.data.prefs.AppPreferences(context)

    prefs.saveActiveSession(
        source = "unlock",
        queueIds = listOf(1L, 2L, 3L),
        queueIndex = 1,
        currentCount = 15,
        elapsedSeconds = 20
    )

    assertEquals(true, prefs.hasActiveSession)
    assertEquals(listOf(1L, 2L, 3L), prefs.getActiveSessionQueueIds())
    assertEquals(1, prefs.activeSessionQueueIndex)
    assertEquals(15, prefs.activeSessionCurrentCount)
    assertEquals(20, prefs.activeSessionElapsedSeconds)

    prefs.updateActiveSessionProgress(count = 16, elapsedSeconds = 21, queueIndex = 1)
    assertEquals(16, prefs.activeSessionCurrentCount)
    assertEquals(21, prefs.activeSessionElapsedSeconds)

    prefs.clearActiveSession()
    assertEquals(false, prefs.hasActiveSession)
    assertEquals(emptyList<Long>(), prefs.getActiveSessionQueueIds())
  }

  @Test
  fun `deferred timer persistence works when screen is off`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = com.example.data.prefs.AppPreferences(context)

    prefs.setPendingTimerTrigger(42L)
    assertEquals(true, prefs.hasPendingTimerTrigger)
    assertEquals(42L, prefs.pendingTimerDhikrId)

    prefs.clearPendingTimerTrigger()
    assertEquals(false, prefs.hasPendingTimerTrigger)
    assertEquals(-1L, prefs.pendingTimerDhikrId)
  }

  @Test
  fun `dedicated session report persistence test`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = com.example.data.prefs.AppPreferences(context)

    prefs.saveLastSessionReport(
        dhikrText = "أستغفر الله العظيم وأتوب إليه",
        count = 100,
        seconds = 120
    )

    assertEquals("أستغفر الله العظيم وأتوب إليه", prefs.lastReportDhikrText)
    assertEquals(100, prefs.lastReportCount)
    assertEquals(120, prefs.lastReportSeconds)
    assertEquals(true, prefs.lastReportTimestamp > 0L)
  }

  @Test
  fun `time formatting produces elegant Arabic format`() {
    val formatted30s = com.example.ui.screens.formatDurationArabic(30)
    assertEquals("30 ثانية", formatted30s)

    val formatted90s = com.example.ui.screens.formatDurationArabic(90)
    assertEquals("1 د و 30 ث", formatted90s)

    val formatted120s = com.example.ui.screens.formatDurationArabic(120)
    assertEquals("2 دقيقة", formatted120s)
  }
}

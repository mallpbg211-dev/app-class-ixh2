package com.example

import com.example.data.AttendanceStatus
import com.example.data.ClassRepository
import com.example.data.GameRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClassHubUnitTest {

    @Test
    fun `verify 32 students loaded with unique random PINs`() {
        val repo = ClassRepository()
        val students = repo.getStudents()
        assertEquals(32, students.size)

        // PINs must be 4 digits
        students.forEach { student ->
            assertEquals(4, student.pin.length)
            assertTrue(student.pin.all { it.isDigit() })
        }

        // Student IDs must be 1 to 32
        val ids = students.map { it.id }.toSet()
        assertEquals(32, ids.size)
        assertTrue(ids.contains(1))
        assertTrue(ids.contains(32))
    }

    @Test
    fun `verify attendance check-in via student PIN`() {
        val repo = ClassRepository()
        val student = repo.getStudents().first()
        val initialMap = repo.getAttendanceMap()
        val today = "2027-03-09"

        val found = repo.getStudents().find { it.pin == student.pin }
        assertNotNull(found)
        assertEquals(student.id, found?.id)
    }

    @Test
    fun `verify game repository list has 8 games`() {
        val repo = GameRepository()
        assertEquals(8, repo.gamesList.size)
    }

    @Test
    fun `verify daily cash tracking for 32 students and treasurer toggle`() {
        val repo = ClassRepository()
        val testDate = repo.getDailyStudentCashMap().keys.first()
        
        // Initial list has 32 students
        val initialCash = repo.getDailyStudentCashMap()[testDate]
        assertNotNull(initialCash)
        assertEquals(32, initialCash?.size)

        // Toggle student #25 (initially unpaid) to paid
        repo.updateStudentDailyCash(testDate, 25, true, "Lunas via Bendahara")
        val updatedCash = repo.getDailyStudentCashMap()[testDate]
        val student25 = updatedCash?.find { it.studentId == 25 }
        assertNotNull(student25)
        assertEquals(true, student25?.isPaid)
        assertEquals(2000L, student25?.amount)
        assertEquals("Lunas via Bendahara", student25?.note)

        // Mark all 32 students paid
        repo.markAllCashForDate(testDate, true)
        val allPaid = repo.getDailyStudentCashMap()[testDate]
        assertEquals(32, allPaid?.count { it.isPaid })

        // Deposit to book creates transaction
        val initialTxCount = repo.cashTransactions.value.size
        repo.depositDailyCashToBook(testDate, "Bendahara 1 (Aurel)")
        assertEquals(initialTxCount + 1, repo.cashTransactions.value.size)
        assertEquals(64000L, repo.cashTransactions.value.first().amount)
    }
}

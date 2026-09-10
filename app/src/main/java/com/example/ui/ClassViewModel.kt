package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AppNavTab(val title: String) {
    PROFIL("Profil"),
    JADWAL("Jadwal"),
    KAS("Uang Kas"),
    ABSENSI("Absensi"),
    GAME_HUB("Game Hub")
}

class ClassViewModel(
    val repository: ClassRepository = ClassRepository(),
    val gameRepository: GameRepository = GameRepository()
) : ViewModel() {

    // Current Role
    private val _currentRole = MutableStateFlow(UserRole.ADMIN) // Default to Admin for full exploration, with quick switcher
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    fun switchRole(role: UserRole) {
        _currentRole.value = role
    }

    // Active Bottom Navigation Tab
    private val _currentTab = MutableStateFlow(AppNavTab.PROFIL)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    fun selectTab(tab: AppNavTab) {
        _currentTab.value = tab
    }

    // Active Sub-view modal/dialog (Seating, Gallery, Countdown, Social, Export)
    private val _activeModal = MutableStateFlow<String?>(null)
    val activeModal: StateFlow<String?> = _activeModal.asStateFlow()

    fun openModal(modalName: String?) {
        _activeModal.value = modalName
    }

    // Active Game being played in Game Hub (null means Game Hub menu)
    private val _activeGameId = MutableStateFlow<String?>(null)
    val activeGameId: StateFlow<String?> = _activeGameId.asStateFlow()

    fun launchGame(gameId: String?) {
        _activeGameId.value = gameId
    }

    // Repository Flows
    val students = repository.students
    val classSettings = repository.classSettings
    val nominalKasPerHari = repository.nominalKasPerHari
    val cashTransactions = repository.cashTransactions
    val dailyStudentCash = repository.dailyStudentCash
    val attendance = repository.attendance
    val picketSchedule = repository.picketSchedule
    val lessonSchedules = repository.lessonSchedules
    val seating = repository.seating
    val gallery = repository.gallery
    val countdownEvents = repository.countdownEvents

    fun updateCountdownEvent(event: CountdownEvent) {
        repository.updateCountdownEvent(event)
    }

    fun addCountdownEvent(event: CountdownEvent) {
        repository.addCountdownEvent(event)
    }

    fun deleteCountdownEvent(eventId: String) {
        repository.deleteCountdownEvent(eventId)
    }

    // Admin Master Controls
    fun updateClassSettings(settings: ClassSettings) {
        repository.updateClassSettings(settings)
    }

    fun updateAdminPin(newPin: String) {
        repository.updateAdminPin(newPin)
    }

    fun updateStudent(student: Student) {
        repository.updateStudent(student)
    }

    fun addStudent(student: Student) {
        repository.addStudent(student)
    }

    fun deleteStudent(studentId: Int) {
        repository.deleteStudent(studentId)
    }

    fun addLessonSchedule(schedule: LessonSchedule) {
        repository.addLessonSchedule(schedule)
    }

    fun updateLessonSchedule(index: Int, schedule: LessonSchedule) {
        repository.updateLessonSchedule(index, schedule)
    }

    fun deleteLessonSchedule(index: Int) {
        repository.deleteLessonSchedule(index)
    }

    fun updatePicketSchedule(dayName: String, studentIds: List<Int>) {
        repository.updatePicketSchedule(dayName, studentIds)
    }

    fun resetPicketStatus(dayName: String) {
        repository.resetPicketStatus(dayName)
    }

    fun deleteCashTransaction(txId: String) {
        repository.deleteCashTransaction(txId)
    }

    fun deleteGalleryItem(itemId: String) {
        repository.deleteGalleryItem(itemId)
    }

    // Selected Date for Attendance
    private val _selectedAttendanceDate = MutableStateFlow(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )
    val selectedAttendanceDate: StateFlow<String> = _selectedAttendanceDate.asStateFlow()

    fun selectAttendanceDate(date: String) {
        _selectedAttendanceDate.value = date
    }

    // Attendance Actions
    fun updateAttendanceStatus(studentId: Int, status: AttendanceStatus, notes: String = "") {
        repository.updateStudentAttendance(_selectedAttendanceDate.value, studentId, status, notes)
    }

    fun submitPinAttendance(pin: String): Pair<Boolean, String> {
        return repository.submitPinAttendance(_selectedAttendanceDate.value, pin)
    }

    // Cash Actions
    fun addCashTransaction(type: CashType, amount: Long, category: String, desc: String, recorder: String) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val newTx = CashTransaction(
            id = "TX-${System.currentTimeMillis() % 10000}",
            date = sanitizeNonSunday(todayStr),
            type = type,
            amount = amount,
            category = category,
            description = desc,
            recordedBy = recorder
        )
        repository.addCashTransaction(newTx)
    }

    fun updateNominalKas(nominal: Long) {
        repository.updateNominalKas(nominal)
    }

    // Daily Student Cash Tracking (Hari Senin - Sabtu, tanpa hari Minggu)
    private val _selectedCashDate = MutableStateFlow(getInitialCashDate())
    val selectedCashDate: StateFlow<String> = _selectedCashDate.asStateFlow()

    fun selectCashDate(date: String) {
        _selectedCashDate.value = sanitizeNonSunday(date)
    }

    companion object {
        fun getInitialCashDate(): String {
            val cal = Calendar.getInstance()
            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                cal.add(Calendar.DAY_OF_YEAR, 1) // Minggu dialihkan ke Senin
            }
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        }

        fun sanitizeNonSunday(dateStr: String): String {
            return try {
                val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val cal = Calendar.getInstance().apply {
                    time = parser.parse(dateStr) ?: Date()
                }
                if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                    cal.add(Calendar.DAY_OF_YEAR, 1) // Jika Minggu, otomatis geser ke Senin
                }
                parser.format(cal.time)
            } catch (e: Exception) {
                dateStr
            }
        }
    }

    fun toggleStudentDailyCash(studentId: Int, isPaid: Boolean, note: String = "") {
        repository.updateStudentDailyCash(_selectedCashDate.value, studentId, isPaid, note)
    }

    fun markAllCashForSelectedDate(isPaid: Boolean) {
        repository.markAllCashForDate(_selectedCashDate.value, isPaid)
    }

    fun depositDailyCashToBook(recorder: String = "Bendahara 1 (Aurel)") {
        repository.depositDailyCashToBook(_selectedCashDate.value, recorder)
    }

    // Picket Duty Actions
    fun togglePicketTask(dayName: String, taskName: String, checkerName: String) {
        repository.togglePicketTask(dayName, taskName, checkerName)
    }

    // Seating Plan Actions
    fun swapSeats(deskA: Int, isLeftA: Boolean, deskB: Int, isLeftB: Boolean) {
        repository.swapSeats(deskA, isLeftA, deskB, isLeftB)
    }

    // Student Dream School Actions
    fun updateStudentDream(studentId: Int, newDream: String) {
        repository.updateStudentDream(studentId, newDream)
    }

    // Gallery Actions
    fun addGalleryItem(title: String, desc: String, category: String, officialNotes: String = "") {
        val newItem = GalleryItem(
            id = "GL-${System.currentTimeMillis() % 10000}",
            title = title,
            description = desc,
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            category = category,
            officialNotes = officialNotes
        )
        repository.addGalleryItem(newItem)
    }

    // Game stats
    val playerStats = gameRepository.playerStats
    val highScores = gameRepository.highScores
    val gamesList = gameRepository.gamesList

    fun finishGame(gameId: String, score: Int) {
        gameRepository.updateScore(gameId, score)
    }
}

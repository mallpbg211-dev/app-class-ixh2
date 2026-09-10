package com.example.data

enum class UserRole(val label: String, val badge: String) {
    STUDENT("Siswa", "Read-Only + Game"),
    PICKET("Piket / Ketua", "Input Absen & Piket"),
    ADMIN("Admin Kelas", "Akses Penuh")
}

enum class AttendanceStatus(val label: String, val shortCode: String) {
    HADIR("Hadir", "H"),
    IZIN("Izin", "I"),
    SAKIT("Sakit", "S"),
    ALPA("Alpa", "A")
}

enum class CashType {
    PEMASUKAN,
    PENGELUARAN
}

data class Student(
    val id: Int,
    val name: String,
    val nickname: String,
    val gender: String, // "L" or "P"
    val role: String,   // "Ketua Kelas", "Bendahara 1", "Anggota", etc.
    val pin: String,    // Unique 4-digit PIN for daily attendance verification
    var dreamSchool: String,
    val avatarEmoji: String = "🎓",
    val photoUrl: String = "",
    val bio: String = "",
    val birthPlaceDate: String = "Jakarta, 2011"
)

data class ClassSettings(
    val className: String = "IX-H",
    val academicYear: String = "2026/2027",
    val homeroomTeacher: String = "Dra. Hj. Sri Wahyuni, M.Pd.",
    val homeroomTeacherSubject: String = "Guru Pengampu IPA / Fisika",
    val classMotto: String = "Kompak Tanpa Batas, Berprestasi Berkelas, Menggapai Masa Depan Emas",
    val announcement: String = "Gladi bersih Ujian Sekolah & foto ijazah dilaksanakan hari Kamis pukul 08:00 WIB di Ruang Aula. Wajib seragam batik lengkap!",
    val adminPin: String = "1945"
)

data class AttendanceRecord(
    val studentId: Int,
    val date: String,
    val status: AttendanceStatus,
    val notes: String = ""
)

data class PicketDuty(
    val dayName: String,
    val studentIds: List<Int>,
    var isDoneToday: Boolean = false,
    var checkedBy: String = "",
    var tasksCompleted: Set<String> = emptySet()
)

data class LessonSchedule(
    val dayName: String,
    val timeSlot: String,
    val subject: String,
    val teacher: String,
    val room: String = "Ruang IX-H"
)

data class CashTransaction(
    val id: String,
    val date: String,
    val type: CashType,
    val amount: Long,
    val category: String,
    val description: String,
    val recordedBy: String
)

data class StudentDailyCash(
    val studentId: Int,
    val date: String,
    val isPaid: Boolean,
    val amount: Long,
    val paidAt: String = "",
    val note: String = ""
)

data class GalleryItem(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val category: String, // "Dokumentasi Foto" or "Berita Acara"
    val officialNotes: String = ""
)

data class SeatPosition(
    val deskIndex: Int, // 0 to 15 (16 double desks)
    val leftStudentId: Int?,
    val rightStudentId: Int?
)

data class GameScore(
    val gameId: String,
    val gameName: String,
    val highScore: Int,
    val timesPlayed: Int
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val isUnlocked: Boolean
)

data class CountdownEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val targetDate: String, // Format "yyyy-MM-dd", contoh: "2027-05-18"
    val category: String = "Ujian", // "Ujian", "Wisuda", "Acara", "Libur", "Lainnya"
    val note: String = ""
)

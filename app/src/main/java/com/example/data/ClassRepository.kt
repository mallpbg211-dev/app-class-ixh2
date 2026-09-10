package com.example.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClassRepository {

    // Class Global Settings & Announcements
    private val _classSettings = MutableStateFlow(ClassSettings())
    val classSettings: StateFlow<ClassSettings> = _classSettings.asStateFlow()

    fun updateClassSettings(newSettings: ClassSettings) {
        _classSettings.value = newSettings
    }

    fun updateAdminPin(newPin: String) {
        _classSettings.value = _classSettings.value.copy(adminPin = newPin)
    }

    // Initial 32 Students of Class IX-H with HD Photos & Bios
    private val _students = MutableStateFlow<List<Student>>(
        listOf(
            Student(1, "Aditya Pratama Putra", "Adit", "L", "Ketua Kelas", "4821", "SMAN 1 (MIPA)", "👑", "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=400&auto=format&fit=crop&q=80", "Diplomat / Duta Besar RI"),
            Student(2, "Aisyah Nur Salsabila", "Aisyah", "P", "Wakil Ketua Kelas", "7392", "SMAN 3 (MIPA)", "✨", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80", "Dokter Spesialis Anak"),
            Student(3, "Alif Danendra Wicaksono", "Alif", "L", "Seksi Keamanan", "1583", "SMKN 2 (Teknik Mesin)", "🛡️", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&auto=format&fit=crop&q=80", "Insinyur Robotika & Mesin"),
            Student(4, "Amalia Rizki Utami", "Amel", "P", "Sekretaris 1", "6204", "SMAN 1 (Bahasa)", "📝", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&auto=format&fit=crop&q=80", "Jurnalis Internasional & Penulis"),
            Student(5, "Ananda Bagus Saputra", "Bagus", "L", "Seksi Perlengkapan", "9147", "SMKN 1 (RPL)", "⚙️", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400&auto=format&fit=crop&q=80", "Software Architect & AI Engineer"),
            Student(6, "Annisa Zahra Maharani", "Zahra", "P", "Sekretaris 2", "3850", "SMAN 2 (IPS)", "📋", "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=400&auto=format&fit=crop&q=80", "Notaris & Dosen Ilmu Hukum"),
            Student(7, "Arkan Fadhil Rahman", "Arkan", "L", "Seksi Kebersihan", "8472", "SMAN 1 (MIPA)", "🧹", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=400&auto=format&fit=crop&q=80", "Arsitek Lingkungan Hijau"),
            Student(8, "Aurellia Cinta Cantika", "Aurel", "P", "Bendahara 1", "2916", "SMAN 3 (IPS)", "💰", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400&auto=format&fit=crop&q=80", "Konsultan Keuangan & Pengusaha"),
            Student(9, "Bima Sakti Yudhistira", "Bima", "L", "Anggota", "5038", "SMAN 5 (Olahraga)", "⚡", "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=400&auto=format&fit=crop&q=80", "Atlet Bulutangkis Nasional"),
            Student(10, "Cantika Dwi Lestari", "Cantika", "P", "Bendahara 2", "7164", "SMKN 3 (Akuntansi)", "💵", "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=400&auto=format&fit=crop&q=80", "Akuntan Publik & Konsultan Pajak"),
            Student(11, "Daffa Arya Kusuma", "Daffa", "L", "Seksi Rohani", "4391", "MAN 1 (Keagamaan)", "🕌", "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=400&auto=format&fit=crop&q=80", "Dosen Kajian Islam & Diplomat"),
            Student(12, "Dewi Anggraini Putri", "Dewi", "P", "Anggota", "8205", "SMAN 2 (MIPA)", "🌸", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=400&auto=format&fit=crop&q=80", "Dokter Gigi & Peneliti Medis"),
            Student(13, "Dimas Wahyu Ramadhan", "Dimas", "L", "Anggota", "6732", "SMKN 2 (Otomotif)", "🔧", "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=400&auto=format&fit=crop&q=80", "Ahli Mekatronika Otomotif"),
            Student(14, "Fakhri Fauzan Kamil", "Fakhri", "L", "Anggota", "1948", "SMAN 1 (MIPA)", "📚", "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=400&auto=format&fit=crop&q=80", "Dosen Matematika & Data Scientist"),
            Student(15, "Farah Nabila Azzahra", "Farah", "P", "Seksi Mading", "5823", "SMAN 4 (Desain)", "🎨", "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?w=400&auto=format&fit=crop&q=80", "Creative Director & Desainer Grafis"),
            Student(16, "Galih Rakha Pratama", "Galih", "L", "Anggota", "3490", "SMAN 3 (MIPA)", "🎯", "https://images.unsplash.com/photo-1513956589380-bad6acb9b9d4?w=400&auto=format&fit=crop&q=80", "Pilot Pesawat Komersial"),
            Student(17, "Hafiz Ridho Illahi", "Hafiz", "L", "Seksi Rohani", "7615", "MAN Insan Cendekia", "📖", "https://images.unsplash.com/photo-1501196354995-cbb51c65aaea?w=400&auto=format&fit=crop&q=80", "Diplomat Kebudayaan Internasional"),
            Student(18, "Indah Permata Sari", "Indah", "P", "Anggota", "2389", "SMAN 2 (Farmasi)", "🌿", "https://images.unsplash.com/photo-1509967419530-da38b4704bc6?w=400&auto=format&fit=crop&q=80", "Apoteker Klinis & Formulator Kosmetik"),
            Student(19, "Kaisar Bintang Ramadhani", "Bintang", "L", "Anggota", "9054", "SMKN 1 (TKJ)", "💻", "https://images.unsplash.com/photo-1534308983496-4fabb1a015ee?w=400&auto=format&fit=crop&q=80", "Cyber Security Specialist"),
            Student(20, "Larasati Kusumaningrum", "Laras", "P", "Anggota", "4172", "SMAN 1 (MIPA)", "🌷", "https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=400&auto=format&fit=crop&q=80", "Dokter Bedah Umum"),
            Student(21, "Muhammad Fauzi Akbar", "Ozi", "L", "Anggota", "8620", "SMAN 3 (IPS)", "🚀", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=400&auto=format&fit=crop&q=80", "Founder Startup Edukasi & Bisnis"),
            Student(22, "Nabila Syifa Wardhani", "Syifa", "P", "Seksi Kesehatan (PMR)", "3701", "SMK Kesehatan", "🩺", "https://images.unsplash.com/photo-1524502397800-2eeaad7c3fe5?w=400&auto=format&fit=crop&q=80", "Perawat Spesialis Gawat Darurat"),
            Student(23, "Naufal Raditya Hadi", "Naufal", "L", "Anggota", "5943", "SMAN 1 (MIPA)", "🔬", "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=400&auto=format&fit=crop&q=80", "Peneliti Bioteknologi & Genetika"),
            Student(24, "Putri Ayuningtyas", "Putri", "P", "Anggota", "1826", "SMAN 2 (Bahasa)", "🎭", "https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?w=400&auto=format&fit=crop&q=80", "Penerjemah Bahasa & Diplomat PBB"),
            Student(25, "Rafi Ahmad Kurniawan", "Rafi", "L", "Anggota", "6498", "SMKN 2 (Elektronika)", "💡", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400&auto=format&fit=crop&q=80", "Teknisi Instrumentasi Energi Hijau"),
            Student(26, "Rania Putri Maharani", "Rania", "P", "Anggota", "7531", "SMAN 3 (MIPA)", "✨", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=400&auto=format&fit=crop&q=80", "Psikolog Klinis & Konselor"),
            Student(27, "Rizky Dwi Saputro", "Kiki", "L", "Anggota", "2084", "SMAN 5 (MIPA)", "⚽", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&auto=format&fit=crop&q=80", "Fisioterapis Olahraga Profesional"),
            Student(28, "Salma Nur Azizah", "Salma", "P", "Anggota", "9317", "SMAN 1 (IPS)", "🌟", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&auto=format&fit=crop&q=80", "Hakim Perdata & Pembela HAM"),
            Student(29, "Satria Maulana Ihsan", "Satria", "L", "Anggota", "4652", "SMKN 1 (Multimedia)", "🎬", "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=400&auto=format&fit=crop&q=80", "Sutradara Film & Animator 3D"),
            Student(30, "Tiara Salsabila Anjani", "Tiara", "P", "Anggota", "8739", "SMAN 2 (MIPA)", "🌺", "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=400&auto=format&fit=crop&q=80", "Peneliti Biologi & Konservasi Laut"),
            Student(31, "Yusuf Habib Al-Fatih", "Habib", "L", "Anggota", "1265", "SMAN 1 (MIPA)", "🔭", "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=400&auto=format&fit=crop&q=80", "Astrofisikawan & Ahli Antariksa"),
            Student(32, "Zahrotul Jannah", "Zahro", "P", "Anggota", "5409", "SMKN 3 (Tata Busana)", "🧵", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400&auto=format&fit=crop&q=80", "Fashion Designer & Pemilik Butik")
        )
    )
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    fun updateStudent(updatedStudent: Student) {
        _students.value = _students.value.map {
            if (it.id == updatedStudent.id) updatedStudent else it
        }
    }

    fun addStudent(student: Student) {
        _students.value = _students.value + student
    }

    fun deleteStudent(studentId: Int) {
        _students.value = _students.value.filterNot { it.id == studentId }
    }

    // Daily nominal kas setting (default: Rp 2.000 / hari atau Rp 5.000 / minggu)
    private val _nominalKasPerHari = MutableStateFlow<Long>(2000L)
    val nominalKasPerHari: StateFlow<Long> = _nominalKasPerHari.asStateFlow()

    fun updateNominalKas(newNominal: Long) {
        _nominalKasPerHari.value = newNominal
    }

    fun setNominalKasPerHari(nominal: Long) {
        _nominalKasPerHari.value = nominal
    }

    fun setStudents(newStudents: List<Student>) {
        _students.value = newStudents
    }

    fun setDailyCashMap(map: Map<String, List<StudentDailyCash>>) {
        _dailyStudentCash.value = map
    }

    fun setCashTransactions(list: List<CashTransaction>) {
        _cashTransactions.value = list
    }

    fun setPicketSchedule(list: List<PicketDuty>) {
        _picketSchedule.value = list
    }

    fun getStudents(): List<Student> = _students.value

    // Daily Student Cash Tracking: Map of Date (YYYY-MM-DD) -> List<StudentDailyCash>
    private val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private val _dailyStudentCash = MutableStateFlow<Map<String, List<StudentDailyCash>>>(
        run {
            val initialToday = (1..32).map { id ->
                val isPaid = id <= 22 // 22 siswa sudah bayar, 10 belum bayar
                StudentDailyCash(
                    studentId = id,
                    date = todayStr,
                    isPaid = isPaid,
                    amount = if (isPaid) 2000L else 0L,
                    paidAt = if (isPaid) "07:${String.format(Locale.getDefault(), "%02d", 10 + (id % 35))}" else "",
                    note = if (isPaid) "Lunas" else "Belum bayar"
                )
            }
            mapOf(todayStr to initialToday)
        }
    )
    val dailyStudentCash: StateFlow<Map<String, List<StudentDailyCash>>> = _dailyStudentCash.asStateFlow()

    fun getDailyStudentCashMap(): Map<String, List<StudentDailyCash>> = _dailyStudentCash.value

    fun createDefaultDailyCash(date: String): List<StudentDailyCash> {
        return (1..32).map { id ->
            StudentDailyCash(
                studentId = id,
                date = date,
                isPaid = false,
                amount = 0L,
                paidAt = "",
                note = "Belum bayar"
            )
        }
    }

    fun updateStudentDailyCash(date: String, studentId: Int, isPaid: Boolean, note: String = "") {
        val currentMap = _dailyStudentCash.value.toMutableMap()
        val nominal = _nominalKasPerHari.value
        val currentList = (currentMap[date] ?: createDefaultDailyCash(date)).toMutableList()

        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val index = currentList.indexOfFirst { it.studentId == studentId }
        val updated = StudentDailyCash(
            studentId = studentId,
            date = date,
            isPaid = isPaid,
            amount = if (isPaid) nominal else 0L,
            paidAt = if (isPaid) timeStr else "",
            note = note.ifEmpty { if (isPaid) "Lunas" else "Belum bayar" }
        )

        if (index >= 0) {
            currentList[index] = updated
        } else {
            currentList.add(updated)
        }
        currentMap[date] = currentList
        _dailyStudentCash.value = currentMap
    }

    fun markAllCashForDate(date: String, isPaid: Boolean) {
        val currentMap = _dailyStudentCash.value.toMutableMap()
        val nominal = _nominalKasPerHari.value
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val updatedList = (1..32).map { id ->
            StudentDailyCash(
                studentId = id,
                date = date,
                isPaid = isPaid,
                amount = if (isPaid) nominal else 0L,
                paidAt = if (isPaid) timeStr else "",
                note = if (isPaid) "Lunas (Serentak)" else "Belum bayar"
            )
        }
        currentMap[date] = updatedList
        _dailyStudentCash.value = currentMap
    }

    fun depositDailyCashToBook(date: String, recorder: String = "Bendahara 1 (Aurel)") {
        val list = _dailyStudentCash.value[date] ?: emptyList()
        val paidCount = list.count { it.isPaid }
        val totalAmount = list.filter { it.isPaid }.sumOf { it.amount }
        if (totalAmount > 0) {
            val tx = CashTransaction(
                id = "TX-${System.currentTimeMillis() % 10000}",
                date = date,
                type = CashType.PEMASUKAN,
                amount = totalAmount,
                category = "Setoran Kas Harian",
                description = "Setoran kas harian tanggal $date ($paidCount dari 32 siswa)",
                recordedBy = recorder
            )
            addCashTransaction(tx)
        }
    }

    // Cash Transactions
    private val _cashTransactions = MutableStateFlow<List<CashTransaction>>(
        listOf(
            CashTransaction("TX-001", "2026-09-01", CashType.PEMASUKAN, 160000L, "Iuran Kas Rutin", "Iuran kas minggu pertama bulan September (32 siswa)", "Bendahara 1 (Aurel)"),
            CashTransaction("TX-002", "2026-09-02", CashType.PENGELUARAN, 35000L, "Beli Spidol & Penghapus", "Isi ulang tinta spidol whiteboard Snowman 3 buah", "Bendahara 2 (Cantika)"),
            CashTransaction("TX-003", "2026-09-04", CashType.PENGELUARAN, 45000L, "Beli Alat Kebersihan", "Sapu lantai ijuk 2 buah dan serokan sampah", "Seksi Kebersihan (Arkan)"),
            CashTransaction("TX-004", "2026-09-08", CashType.PEMASUKAN, 160000L, "Iuran Kas Rutin", "Iuran kas minggu kedua bulan September (32 siswa)", "Bendahara 1 (Aurel)"),
            CashTransaction("TX-005", "2026-09-09", CashType.PENGELUARAN, 50000L, "Kasus Sosial / Menjenguk", "Bingkisan buah untuk anggota kelas yang sakit opname", "Wali Kelas & Bendahara")
        )
    )
    val cashTransactions: StateFlow<List<CashTransaction>> = _cashTransactions.asStateFlow()

    fun addCashTransaction(transaction: CashTransaction) {
        _cashTransactions.value = listOf(transaction) + _cashTransactions.value
    }

    fun deleteCashTransaction(txId: String) {
        _cashTransactions.value = _cashTransactions.value.filterNot { it.id == txId }
    }

    // Attendance Records: Map of Date (YYYY-MM-DD) -> List<AttendanceRecord>
    private val _attendance = MutableStateFlow<Map<String, List<AttendanceRecord>>>(
        mapOf(
            todayStr to (1..32).map { id ->
                val status = when (id) {
                    9 -> AttendanceStatus.IZIN
                    18 -> AttendanceStatus.SAKIT
                    else -> AttendanceStatus.HADIR
                }
                AttendanceRecord(id, todayStr, status, if (id == 9) "Lomba Renang Antar-Sekolah" else if (id == 18) "Demam / Surat Dokter" else "")
            }
        )
    )
    val attendance: StateFlow<Map<String, List<AttendanceRecord>>> = _attendance.asStateFlow()

    fun getAttendanceMap(): Map<String, List<AttendanceRecord>> = _attendance.value

    fun updateStudentAttendance(date: String, studentId: Int, newStatus: AttendanceStatus, notes: String = "") {
        val currentMap = _attendance.value.toMutableMap()
        val currentList = currentMap[date]?.toMutableList() ?: (1..32).map { 
            AttendanceRecord(it, date, AttendanceStatus.HADIR) 
        }.toMutableList()

        val index = currentList.indexOfFirst { it.studentId == studentId }
        if (index >= 0) {
            currentList[index] = AttendanceRecord(studentId, date, newStatus, notes)
        } else {
            currentList.add(AttendanceRecord(studentId, date, newStatus, notes))
        }
        currentMap[date] = currentList
        _attendance.value = currentMap
    }

    fun submitPinAttendance(date: String, pin: String): Pair<Boolean, String> {
        val student = _students.value.find { it.pin == pin }
            ?: return Pair(false, "Kode unik PIN '$pin' tidak ditemukan.")

        updateStudentAttendance(date, student.id, AttendanceStatus.HADIR, "Check-in via PIN")
        return Pair(true, "Presensi berhasil dicatat untuk ${student.name} (Absen #${student.id})")
    }

    // Picket Duty Schedule (Senin - Sabtu)
    private val _picketSchedule = MutableStateFlow<List<PicketDuty>>(
        listOf(
            PicketDuty("Senin", listOf(1, 2, 3, 4, 5, 6), isDoneToday = true, checkedBy = "Aditya (Ketua)", tasksCompleted = setOf("Sapu Lantai", "Hapus Papan Tulis", "Rapikan Meja Guru")),
            PicketDuty("Selasa", listOf(7, 8, 9, 10, 11, 12), isDoneToday = false),
            PicketDuty("Rabu", listOf(13, 14, 15, 16, 17, 18), isDoneToday = false),
            PicketDuty("Kamis", listOf(19, 20, 21, 22, 23, 24), isDoneToday = false),
            PicketDuty("Jumat", listOf(25, 26, 27, 28), isDoneToday = false),
            PicketDuty("Sabtu", listOf(29, 30, 31, 32), isDoneToday = false)
        )
    )
    val picketSchedule: StateFlow<List<PicketDuty>> = _picketSchedule.asStateFlow()

    fun togglePicketTask(dayName: String, taskName: String, checkerName: String) {
        _picketSchedule.value = _picketSchedule.value.map { duty ->
            if (duty.dayName.equals(dayName, ignoreCase = true)) {
                val newTasks = duty.tasksCompleted.toMutableSet()
                if (newTasks.contains(taskName)) {
                    newTasks.remove(taskName)
                } else {
                    newTasks.add(taskName)
                }
                val isDone = newTasks.size >= 4
                duty.copy(tasksCompleted = newTasks, isDoneToday = isDone, checkedBy = checkerName)
            } else duty
        }
    }

    fun updatePicketSchedule(dayName: String, studentIds: List<Int>) {
        _picketSchedule.value = _picketSchedule.value.map {
            if (it.dayName.equals(dayName, ignoreCase = true)) {
                it.copy(studentIds = studentIds)
            } else it
        }
    }

    fun resetPicketStatus(dayName: String) {
        _picketSchedule.value = _picketSchedule.value.map {
            if (it.dayName.equals(dayName, ignoreCase = true)) {
                it.copy(tasksCompleted = emptySet(), isDoneToday = false, checkedBy = "")
            } else it
        }
    }

    // Lesson Schedules (Mutable StateFlow for Admin Customization)
    private val _lessonSchedules = MutableStateFlow<List<LessonSchedule>>(
        listOf(
            // Senin
            LessonSchedule("Senin", "07:00 - 07:45", "Upacara Bendera", "Seluruh Dewan Guru", "Lapangan Utama"),
            LessonSchedule("Senin", "07:45 - 09:05", "Matematika", "Drs. Bambang Sudarsono", "Ruang IX-H"),
            LessonSchedule("Senin", "09:20 - 10:40", "Bahasa Indonesia", "Siti Aminah, M.Pd.", "Ruang IX-H"),
            LessonSchedule("Senin", "10:55 - 12:15", "Pendidikan Agama & Budi Pekerti", "Ust. Ahmad Syafii, S.Ag.", "Ruang IX-H / Masjid"),
            LessonSchedule("Senin", "12:45 - 14:00", "Pendidikan Jasmani (PJOK)", "Coach Hendra Wijaya", "GOR Sekolah"),

            // Selasa
            LessonSchedule("Selasa", "07:00 - 07:30", "Literasi Pagi & Doa", "Wali Kelas (Dra. Sri Wahyuni)", "Ruang IX-H"),
            LessonSchedule("Selasa", "07:30 - 09:30", "Ilmu Pengetahuan Alam (Fisika)", "Dra. Sri Wahyuni, M.Pd.", "Lab IPA"),
            LessonSchedule("Selasa", "09:45 - 11:45", "Bahasa Inggris", "Miss Jessica Sarah, S.Pd.", "Ruang IX-H"),
            LessonSchedule("Selasa", "12:30 - 14:00", "Informatika / Komputer", "Budi Hermawan, S.Kom.", "Lab Komputer 2"),

            // Rabu
            LessonSchedule("Rabu", "07:00 - 07:30", "Bina Karakter / Asmaul Husna", "Tim Keagamaan", "Ruang IX-H"),
            LessonSchedule("Rabu", "07:30 - 09:30", "Ilmu Pengetahuan Sosial (IPS)", "Drs. Eko Prasetyo", "Ruang IX-H"),
            LessonSchedule("Rabu", "09:45 - 11:45", "Pendidikan Pancasila (PPKn)", "Nurhadi, S.Pd.", "Ruang IX-H"),
            LessonSchedule("Rabu", "12:30 - 14:00", "Seni Budaya & Keterampilan", "Dewi Kusuma, M.Sn.", "Ruang Kesenian"),

            // Kamis
            LessonSchedule("Kamis", "07:00 - 07:30", "Senam Pagi / Pembiasaan", "Tim Kesiswaan", "Lapangan"),
            LessonSchedule("Kamis", "07:30 - 09:30", "IPA (Biologi)", "Dr. Nurul Hidayah, M.Si.", "Lab Biologi"),
            LessonSchedule("Kamis", "09:45 - 11:45", "Matematika (Pendalaman Soal)", "Drs. Bambang Sudarsono", "Ruang IX-H"),
            LessonSchedule("Kamis", "12:30 - 14:00", "Prakarya & Kewirausahaan", "Ibu Endang Rahayu, S.Pd.", "Ruang Prakarya"),

            // Jumat
            LessonSchedule("Jumat", "07:00 - 07:45", "Jumat Bersih & Rohani", "Pembina OSIS & Wali Kelas", "Area Kelas IX-H"),
            LessonSchedule("Jumat", "07:45 - 09:15", "Bahasa Daerah (Jawa/Sunda)", "Pak Gunawan, S.Pd.", "Ruang IX-H"),
            LessonSchedule("Jumat", "09:30 - 11:00", "Bimbingan Konseling (BK)", "Dra. Retno Palupi", "Ruang IX-H"),

            // Sabtu
            LessonSchedule("Sabtu", "07:00 - 08:30", "Ekstrakurikuler & Pramuka", "Pembina Pramuka / Pelatih", "Lapangan / Aula"),
            LessonSchedule("Sabtu", "08:45 - 10:15", "Pengembangan Karakter & Minat", "Tim Kesiswaan", "Ruang IX-H"),
            LessonSchedule("Sabtu", "10:30 - 11:30", "Evaluasi Mingguan & Kebersihan", "Wali Kelas IX-H", "Ruang IX-H")
        )
    )
    val lessonSchedules: StateFlow<List<LessonSchedule>> = _lessonSchedules.asStateFlow()

    fun addLessonSchedule(schedule: LessonSchedule) {
        _lessonSchedules.value = _lessonSchedules.value + schedule
    }

    fun updateLessonSchedule(index: Int, schedule: LessonSchedule) {
        if (index in _lessonSchedules.value.indices) {
            val list = _lessonSchedules.value.toMutableList()
            list[index] = schedule
            _lessonSchedules.value = list
        }
    }

    fun deleteLessonSchedule(index: Int) {
        if (index in _lessonSchedules.value.indices) {
            val list = _lessonSchedules.value.toMutableList()
            list.removeAt(index)
            _lessonSchedules.value = list
        }
    }

    // Seating Positions (16 Desks = 32 seats: 4 Columns x 4 Rows)
    private val _seating = MutableStateFlow<List<SeatPosition>>(
        (0 until 16).map { desk ->
            SeatPosition(desk, desk * 2 + 1, desk * 2 + 2)
        }
    )
    val seating: StateFlow<List<SeatPosition>> = _seating.asStateFlow()

    fun swapSeats(deskA: Int, isLeftA: Boolean, deskB: Int, isLeftB: Boolean) {
        val current = _seating.value.toMutableList()
        val itemA = current[deskA]
        val itemB = current[deskB]

        val valA = if (isLeftA) itemA.leftStudentId else itemA.rightStudentId
        val valB = if (isLeftB) itemB.leftStudentId else itemB.rightStudentId

        val newItemA = if (isLeftA) itemA.copy(leftStudentId = valB) else itemA.copy(rightStudentId = valB)
        current[deskA] = newItemA

        val currentItemB = current[deskB]
        val newItemB = if (isLeftB) currentItemB.copy(leftStudentId = valA) else currentItemB.copy(rightStudentId = valA)
        current[deskB] = newItemB

        _seating.value = current
    }

    // Gallery & News Events
    private val _gallery = MutableStateFlow<List<GalleryItem>>(
        listOf(
            GalleryItem(
                "GL-01",
                "Juara 1 Lomba Kebersihan & Dekorasi Kelas",
                "Kelas IX-H berhasil meraih Juara 1 lomba kebersihan dan keindahan kelas tingkat sekolah dalam rangka HUT Kemerdekaan RI ke-81.",
                "2026-08-17",
                "Dokumentasi Foto",
                "Piala disimpan di lemari etalase kelas IX-H. Terima kasih atas kerja keras seluruh siswa & seksi kebersihan!"
            ),
            GalleryItem(
                "GL-02",
                "Rapat Pembentukan Pengurus & Struktur IX-H",
                "Musyawarah mufakat penentuan susunan pengurus kelas, komitmen iuran kas kelas Rp 2.000/hari, dan aturan piket harian.",
                "2026-07-20",
                "Berita Acara",
                "Dihadiri oleh Wali Kelas Dra. Sri Wahyuni dan 32 siswa. Keputusan diambil secara bulat."
            ),
            GalleryItem(
                "GL-03",
                "Proyek Penguatan Profil Pelajar Pancasila (P5)",
                "Pameran produk ecobrick dan inovasi hidroponik ramah lingkungan karya siswa-siswi kelas IX-H.",
                "2026-08-28",
                "Dokumentasi Foto",
                "Ditinjau langsung oleh Kepala Sekolah dan mendapat apresiasi terbaik."
            ),
            GalleryItem(
                "GL-04",
                "Peringatan Hari Guru Nasional Bersama Wali Kelas",
                "Pemberian buket bunga, kartu ucapan berkesan, dan syukuran tumpeng mini untuk Ibu Dra. Sri Wahyuni, M.Pd.",
                "2026-11-25",
                "Dokumentasi Foto",
                "Suasana haru dan penuh kehangatan antar seluruh keluarga besar IX-H."
            ),
            GalleryItem(
                "GL-05",
                "Kesepakatan Desain Jaket Angkatan & Kaos Kelas IX-H",
                "Voting final warna dan logo jaket angkatan kelas IX-H: warna Navy Charcoal dengan sablon logo emas kreasi tim mading.",
                "2026-09-02",
                "Berita Acara",
                "Total pemesanan 33 pcs (32 siswa + 1 wali kelas). DP disetorkan ke Bendahara 2."
            )
        )
    )
    val gallery: StateFlow<List<GalleryItem>> = _gallery.asStateFlow()

    fun addGalleryItem(item: GalleryItem) {
        _gallery.value = listOf(item) + _gallery.value
    }

    fun deleteGalleryItem(itemId: String) {
        _gallery.value = _gallery.value.filterNot { it.id == itemId }
    }

    fun updateStudentDream(studentId: Int, newDream: String) {
        _students.value = _students.value.map {
            if (it.id == studentId) it.copy(dreamSchool = newDream) else it
        }
    }

    // Countdown Events
    private val _countdownEvents = MutableStateFlow<List<CountdownEvent>>(
        listOf(
            CountdownEvent(
                id = "event_exam",
                title = "Asesmen Akhir Sekolah (Ujian)",
                targetDate = "2027-05-18",
                category = "Ujian",
                note = "Gladi dan Asesmen Akhir Sekolah Penentu Kelulusan."
            ),
            CountdownEvent(
                id = "event_graduation",
                title = "Wisuda & Pelepasan Kelas IX-H",
                targetDate = "2027-06-25",
                category = "Wisuda",
                note = "Momen perpisahan akbar dan pelepasan siswa angkatan 2026/2027."
            )
        )
    )
    val countdownEvents: StateFlow<List<CountdownEvent>> = _countdownEvents.asStateFlow()

    fun updateCountdownEvent(event: CountdownEvent) {
        _countdownEvents.value = _countdownEvents.value.map { if (it.id == event.id) event else it }
    }

    fun addCountdownEvent(event: CountdownEvent) {
        _countdownEvents.value = _countdownEvents.value + event
    }

    fun deleteCountdownEvent(eventId: String) {
        _countdownEvents.value = _countdownEvents.value.filterNot { it.id == eventId }
    }

    fun setCountdownEvents(events: List<CountdownEvent>) {
        _countdownEvents.value = events
    }
}

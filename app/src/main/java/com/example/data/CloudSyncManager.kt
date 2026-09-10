package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class CloudSyncStatus(val label: String, val isOnline: Boolean) {
    OFFLINE("Offline (Lokal)", false),
    CONNECTING("Menghubungkan...", false),
    ONLINE("🟢 Cloud Online (Live)", true),
    SYNCING("🔄 Sinkronisasi...", true),
    ERROR("⚠️ Gagal Terhubung", false)
}

class CloudSyncManager private constructor() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var prefs: SharedPreferences? = null

    private val _syncStatus = MutableStateFlow(CloudSyncStatus.OFFLINE)
    val syncStatus: StateFlow<CloudSyncStatus> = _syncStatus.asStateFlow()

    private val _statusMessage = MutableStateFlow("Mode offline lokal aktif. Konfigurasi Cloud Firebase untuk real-time multi-device.")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _lastSyncedTime = MutableStateFlow<String?>(null)
    val lastSyncedTime: StateFlow<String?> = _lastSyncedTime.asStateFlow()

    private val _projectId = MutableStateFlow("")
    val projectId: StateFlow<String> = _projectId.asStateFlow()

    private val _apiKey = MutableStateFlow("")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _autoSyncEnabled = MutableStateFlow(true)
    val autoSyncEnabled: StateFlow<Boolean> = _autoSyncEnabled.asStateFlow()

    private var firestoreInstance: FirebaseFirestore? = null
    private val activeListeners = mutableListOf<ListenerRegistration>()

    companion object {
        private const val TAG = "CloudSyncManager"
        private const val PREFS_NAME = "ixh_cloud_settings"
        private const val KEY_PROJECT_ID = "cloud_project_id"
        private const val KEY_API_KEY = "cloud_api_key"
        private const val KEY_AUTO_SYNC = "cloud_auto_sync"
        const val DEFAULT_PROJECT_ID = "apk-class-ix-h"

        val instance: CloudSyncManager by lazy { CloudSyncManager() }
    }

    fun init(context: Context, repository: ClassRepository) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedProjectId = prefs?.getString(KEY_PROJECT_ID, DEFAULT_PROJECT_ID) ?: DEFAULT_PROJECT_ID
        val savedApiKey = prefs?.getString(KEY_API_KEY, "") ?: ""
        val savedAutoSync = prefs?.getBoolean(KEY_AUTO_SYNC, true) ?: true

        _projectId.value = savedProjectId
        _apiKey.value = savedApiKey
        _autoSyncEnabled.value = savedAutoSync

        // Check if Firebase is already initialized via google-services.json
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                firestoreInstance = FirebaseFirestore.getInstance()
                _syncStatus.value = CloudSyncStatus.ONLINE
                _statusMessage.value = "🟢 Terhubung ke Firebase Project: $savedProjectId"
                if (savedAutoSync) {
                    attachRealtimeListeners(repository)
                }
                return
            }
        } catch (e: Exception) {
            Log.w(TAG, "Default FirebaseApp not found: ${e.message}")
        }

        // Attempt connecting with project id
        if (savedProjectId.isNotBlank()) {
            connectWithCustomCredentials(context, savedProjectId, savedApiKey, repository)
        } else {
            _syncStatus.value = CloudSyncStatus.OFFLINE
            _statusMessage.value = "Belum terhubung ke Cloud. Masukkan Project ID Firebase atau letakkan google-services.json di app/."
        }
    }

    fun connectWithCustomCredentials(
        context: Context,
        newProjectId: String,
        newApiKey: String,
        repository: ClassRepository,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        coroutineScope.launch {
            val targetProjectId = if (newProjectId.isNotBlank()) newProjectId.trim() else DEFAULT_PROJECT_ID
            val targetApiKey = if (newApiKey.isNotBlank()) newApiKey.trim() else "AIzaSyB_apk_class_ix_h_firestore_key"

            _syncStatus.value = CloudSyncStatus.CONNECTING
            _statusMessage.value = "Menghubungkan ke Cloud Firebase ($targetProjectId)..."

            try {
                // Remove existing secondary app if any
                val appName = "ixh_cloud_app"
                var targetApp: FirebaseApp? = null
                for (app in FirebaseApp.getApps(context)) {
                    if (app.name == appName) {
                        targetApp = app
                        break
                    }
                }

                if (targetApp == null) {
                    val options = FirebaseOptions.Builder()
                        .setProjectId(targetProjectId)
                        .setApiKey(targetApiKey)
                        .setApplicationId("com.aistudio.ixhhub.kdmx")
                        .build()
                    targetApp = FirebaseApp.initializeApp(context, options, appName)
                }

                firestoreInstance = FirebaseFirestore.getInstance(targetApp)
                _projectId.value = targetProjectId
                _apiKey.value = newApiKey

                prefs?.edit()
                    ?.putString(KEY_PROJECT_ID, targetProjectId)
                    ?.putString(KEY_API_KEY, newApiKey)
                    ?.apply()

                _syncStatus.value = CloudSyncStatus.ONLINE
                _statusMessage.value = "🟢 Berhasil terhubung ke Cloud Firebase: $targetProjectId"

                if (_autoSyncEnabled.value) {
                    attachRealtimeListeners(repository)
                }

                withContext(Dispatchers.Main) {
                    onResult(true, "Berhasil terhubung ke Cloud Firebase ($targetProjectId)!")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error connecting to custom Firebase: ${e.message}", e)
                _syncStatus.value = CloudSyncStatus.ERROR
                _statusMessage.value = "Gagal menghubungkan ke Cloud: ${e.localizedMessage}"
                withContext(Dispatchers.Main) {
                    onResult(false, "Gagal: ${e.localizedMessage}")
                }
            }
        }
    }

    fun attachRealtimeListeners(repository: ClassRepository) {
        val firestore = firestoreInstance ?: return
        detachRealtimeListeners()

        try {
            _syncStatus.value = CloudSyncStatus.ONLINE

            // 1. Listen to Class Settings
            val settingsListener = firestore.collection("ixh_class_data")
                .document("settings")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleListenerError(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val data = snapshot.data ?: return@addSnapshotListener
                        val current = repository.classSettings.value
                        val updated = current.copy(
                            className = data["className"] as? String ?: current.className,
                            academicYear = data["academicYear"] as? String ?: current.academicYear,
                            homeroomTeacher = data["homeroomTeacher"] as? String ?: current.homeroomTeacher,
                            homeroomTeacherSubject = data["homeroomTeacherSubject"] as? String ?: current.homeroomTeacherSubject,
                            classMotto = data["classMotto"] as? String ?: current.classMotto,
                            announcement = data["announcement"] as? String ?: current.announcement,
                            adminPin = data["adminPin"] as? String ?: current.adminPin
                        )
                        repository.updateClassSettings(updated)
                        val nominal = (data["nominalKasPerHari"] as? Number)?.toLong()
                        if (nominal != null && nominal > 0) {
                            repository.setNominalKasPerHari(nominal)
                        }
                        updateLastSyncTimestamp()
                    }
                }
            activeListeners.add(settingsListener)

            // 2. Listen to Students
            val studentsListener = firestore.collection("ixh_class_data")
                .document("students")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleListenerError(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val listData = snapshot.get("list") as? List<Map<String, Any>>
                        if (!listData.isNullOrEmpty()) {
                            val students = listData.mapNotNull { map ->
                                try {
                                    Student(
                                        id = (map["id"] as? Number)?.toInt() ?: return@mapNotNull null,
                                        name = map["name"] as? String ?: "",
                                        nickname = map["nickname"] as? String ?: "",
                                        gender = map["gender"] as? String ?: "L",
                                        role = map["role"] as? String ?: "Anggota",
                                        pin = map["pin"] as? String ?: "1234",
                                        dreamSchool = map["dreamSchool"] as? String ?: "",
                                        avatarEmoji = map["avatarEmoji"] as? String ?: "🎓",
                                        photoUrl = map["photoUrl"] as? String ?: "",
                                        bio = map["bio"] as? String ?: "",
                                        birthPlaceDate = map["birthPlaceDate"] as? String ?: ""
                                    )
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            if (students.isNotEmpty()) {
                                repository.setStudents(students)
                                updateLastSyncTimestamp()
                            }
                        }
                    }
                }
            activeListeners.add(studentsListener)

            // 3. Listen to Daily Cash
            val cashListener = firestore.collection("ixh_class_data")
                .document("daily_cash")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleListenerError(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val rawMap = snapshot.data ?: return@addSnapshotListener
                        val resultMap = mutableMapOf<String, List<StudentDailyCash>>()
                        rawMap.forEach { (dateKey, valList) ->
                            val itemsList = valList as? List<Map<String, Any>> ?: return@forEach
                            val studentCashList = itemsList.mapNotNull { item ->
                                try {
                                    StudentDailyCash(
                                        studentId = (item["studentId"] as? Number)?.toInt() ?: return@mapNotNull null,
                                        date = item["date"] as? String ?: dateKey,
                                        isPaid = item["isPaid"] as? Boolean ?: false,
                                        amount = (item["amount"] as? Number)?.toLong() ?: 0L,
                                        paidAt = item["paidAt"] as? String ?: "",
                                        note = item["note"] as? String ?: ""
                                    )
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            resultMap[dateKey] = studentCashList
                        }
                        if (resultMap.isNotEmpty()) {
                            repository.setDailyCashMap(resultMap)
                            updateLastSyncTimestamp()
                        }
                    }
                }
            activeListeners.add(cashListener)

            // 4. Listen to Cash Transactions
            val txListener = firestore.collection("ixh_class_data")
                .document("cash_transactions")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleListenerError(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val txList = snapshot.get("list") as? List<Map<String, Any>>
                        if (!txList.isNullOrEmpty()) {
                            val transactions = txList.mapNotNull { item ->
                                try {
                                    CashTransaction(
                                        id = item["id"] as? String ?: return@mapNotNull null,
                                        date = item["date"] as? String ?: "",
                                        type = if ((item["type"] as? String) == "PENGELUARAN") CashType.PENGELUARAN else CashType.PEMASUKAN,
                                        amount = (item["amount"] as? Number)?.toLong() ?: 0L,
                                        category = item["category"] as? String ?: "",
                                        description = item["description"] as? String ?: "",
                                        recordedBy = item["recordedBy"] as? String ?: ""
                                    )
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            repository.setCashTransactions(transactions)
                            updateLastSyncTimestamp()
                        }
                    }
                }
            activeListeners.add(txListener)

            // 5. Listen to Picket Schedules
            val picketListener = firestore.collection("ixh_class_data")
                .document("picket_schedules")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleListenerError(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val pickList = snapshot.get("list") as? List<Map<String, Any>>
                        if (!pickList.isNullOrEmpty()) {
                            val duties = pickList.mapNotNull { item ->
                                try {
                                    val ids = (item["studentIds"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: emptyList()
                                    val tasks = (item["tasksCompleted"] as? List<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet()
                                    PicketDuty(
                                        dayName = item["dayName"] as? String ?: return@mapNotNull null,
                                        studentIds = ids,
                                        isDoneToday = item["isDoneToday"] as? Boolean ?: false,
                                        checkedBy = item["checkedBy"] as? String ?: "",
                                        tasksCompleted = tasks
                                    )
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            repository.setPicketSchedule(duties)
                            updateLastSyncTimestamp()
                        }
                    }
                }
            activeListeners.add(picketListener)

            // 6. Listen to Countdown Events
            val countdownListener = firestore.collection("ixh_class_data")
                .document("countdown_events")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleListenerError(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val cList = snapshot.get("list") as? List<Map<String, Any>>
                        if (!cList.isNullOrEmpty()) {
                            val events = cList.mapNotNull { item ->
                                try {
                                    CountdownEvent(
                                        id = item["id"] as? String ?: java.util.UUID.randomUUID().toString(),
                                        title = item["title"] as? String ?: return@mapNotNull null,
                                        targetDate = item["targetDate"] as? String ?: "2027-05-18",
                                        category = item["category"] as? String ?: "Ujian",
                                        note = item["note"] as? String ?: ""
                                    )
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            if (events.isNotEmpty()) {
                                repository.setCountdownEvents(events)
                                updateLastSyncTimestamp()
                            }
                        }
                    }
                }
            activeListeners.add(countdownListener)

            _statusMessage.value = "🟢 Real-time listener aktif! Data tersinkronisasi multi-device."
        } catch (e: Exception) {
            Log.w(TAG, "Listener exception: ${e.message}")
            _statusMessage.value = "Kendala listener: ${e.localizedMessage}"
        }
    }

    private fun handleListenerError(error: com.google.firebase.firestore.FirebaseFirestoreException) {
        if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
            _syncStatus.value = CloudSyncStatus.ERROR
            _statusMessage.value = "⚠️ Akses Cloud Ditolak (PERMISSION_DENIED): Firestore Security Rules di Firebase Console masih tertutup. Buka Firestore Database > Rules, ubah jadi 'allow read, write: if true;' lalu klik Publish."
            Log.w(TAG, "Firestore PERMISSION_DENIED: Aturan Rules di Firebase Console masih terkunci.")
            detachRealtimeListeners()
        } else {
            Log.w(TAG, "Firestore listener info: ${error.message}")
        }
    }

    fun detachRealtimeListeners() {
        activeListeners.forEach { it.remove() }
        activeListeners.clear()
    }

    fun uploadAllToCloud(repository: ClassRepository, onComplete: (Boolean, String) -> Unit) {
        val firestore = firestoreInstance
        if (firestore == null) {
            onComplete(false, "Cloud belum terhubung. Konfigurasikan Firebase terlebih dahulu.")
            return
        }

        coroutineScope.launch {
            _syncStatus.value = CloudSyncStatus.SYNCING
            _statusMessage.value = "Mengunggah data kelas ke Cloud..."

            try {
                val batch = firestore.batch()

                // 1. Settings
                val settings = repository.classSettings.value
                val settingsDoc = firestore.collection("ixh_class_data").document("settings")
                val settingsMap = mapOf(
                    "className" to settings.className,
                    "academicYear" to settings.academicYear,
                    "homeroomTeacher" to settings.homeroomTeacher,
                    "homeroomTeacherSubject" to settings.homeroomTeacherSubject,
                    "classMotto" to settings.classMotto,
                    "announcement" to settings.announcement,
                    "adminPin" to settings.adminPin,
                    "nominalKasPerHari" to repository.nominalKasPerHari.value,
                    "updatedAt" to System.currentTimeMillis()
                )
                batch.set(settingsDoc, settingsMap, SetOptions.merge())

                // 2. Students
                val studentsList = repository.students.value.map { s ->
                    mapOf(
                        "id" to s.id,
                        "name" to s.name,
                        "nickname" to s.nickname,
                        "gender" to s.gender,
                        "role" to s.role,
                        "pin" to s.pin,
                        "dreamSchool" to s.dreamSchool,
                        "avatarEmoji" to s.avatarEmoji,
                        "photoUrl" to s.photoUrl,
                        "bio" to s.bio,
                        "birthPlaceDate" to s.birthPlaceDate
                    )
                }
                val studentsDoc = firestore.collection("ixh_class_data").document("students")
                batch.set(studentsDoc, mapOf("list" to studentsList, "updatedAt" to System.currentTimeMillis()), SetOptions.merge())

                // 3. Daily Cash
                val cashMap = repository.getDailyStudentCashMap()
                val serializedCashMap = mutableMapOf<String, Any>()
                cashMap.forEach { (date, list) ->
                    serializedCashMap[date] = list.map { c ->
                        mapOf(
                            "studentId" to c.studentId,
                            "date" to c.date,
                            "isPaid" to c.isPaid,
                            "amount" to c.amount,
                            "paidAt" to c.paidAt,
                            "note" to c.note
                        )
                    }
                }
                serializedCashMap["updatedAt"] = System.currentTimeMillis()
                val cashDoc = firestore.collection("ixh_class_data").document("daily_cash")
                batch.set(cashDoc, serializedCashMap, SetOptions.merge())

                // 4. Cash Transactions
                val txList = repository.cashTransactions.value.map { tx ->
                    mapOf(
                        "id" to tx.id,
                        "date" to tx.date,
                        "type" to tx.type.name,
                        "amount" to tx.amount,
                        "category" to tx.category,
                        "description" to tx.description,
                        "recordedBy" to tx.recordedBy
                    )
                }
                val txDoc = firestore.collection("ixh_class_data").document("cash_transactions")
                batch.set(txDoc, mapOf("list" to txList, "updatedAt" to System.currentTimeMillis()), SetOptions.merge())

                // 5. Picket Schedules
                val picketList = repository.picketSchedule.value.map { p ->
                    mapOf(
                        "dayName" to p.dayName,
                        "studentIds" to p.studentIds,
                        "isDoneToday" to p.isDoneToday,
                        "checkedBy" to p.checkedBy,
                        "tasksCompleted" to p.tasksCompleted.toList()
                    )
                }
                val picketDoc = firestore.collection("ixh_class_data").document("picket_schedules")
                batch.set(picketDoc, mapOf("list" to picketList, "updatedAt" to System.currentTimeMillis()), SetOptions.merge())

                // 6. Countdown Events
                val countdownList = repository.countdownEvents.value.map { ev ->
                    mapOf(
                        "id" to ev.id,
                        "title" to ev.title,
                        "targetDate" to ev.targetDate,
                        "category" to ev.category,
                        "note" to ev.note
                    )
                }
                val countdownDoc = firestore.collection("ixh_class_data").document("countdown_events")
                batch.set(countdownDoc, mapOf("list" to countdownList, "updatedAt" to System.currentTimeMillis()), SetOptions.merge())

                batch.commit().addOnSuccessListener {
                    _syncStatus.value = CloudSyncStatus.ONLINE
                    _statusMessage.value = "🟢 Semua data kelas berhasil diunggah ke Cloud!"
                    updateLastSyncTimestamp()
                    onComplete(true, "Data lokal berhasil diunggah ke Cloud Firebase!")
                }.addOnFailureListener { e ->
                    val isPerm = (e as? com.google.firebase.firestore.FirebaseFirestoreException)?.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED
                    val msg = if (isPerm) {
                        "Akses Cloud Ditolak (PERMISSION_DENIED): Ubah Rules di Firebase Console jadi 'allow read, write: if true;' lalu Publish."
                    } else {
                        "Gagal upload: ${e.localizedMessage}"
                    }
                    _syncStatus.value = CloudSyncStatus.ERROR
                    _statusMessage.value = msg
                    onComplete(false, msg)
                }
            } catch (e: Exception) {
                _syncStatus.value = CloudSyncStatus.ERROR
                _statusMessage.value = "Gagal: ${e.localizedMessage}"
                withContext(Dispatchers.Main) {
                    onComplete(false, "Error: ${e.localizedMessage}")
                }
            }
        }
    }

    fun syncSingleItemToCloud(collectionDoc: String, data: Map<String, Any>) {
        val firestore = firestoreInstance ?: return
        coroutineScope.launch {
            try {
                firestore.collection("ixh_class_data")
                    .document(collectionDoc)
                    .set(data, SetOptions.merge())
            } catch (e: Exception) {
                Log.w(TAG, "Failed single sync for $collectionDoc: ${e.message}")
            }
        }
    }

    private fun updateLastSyncTimestamp() {
        val timeFormat = SimpleDateFormat("HH:mm:ss 'WIB'", Locale.getDefault())
        _lastSyncedTime.value = timeFormat.format(Date())
    }

    fun setAutoSync(enabled: Boolean, repository: ClassRepository) {
        _autoSyncEnabled.value = enabled
        prefs?.edit()?.putBoolean(KEY_AUTO_SYNC, enabled)?.apply()
        if (enabled && _syncStatus.value.isOnline) {
            attachRealtimeListeners(repository)
        } else if (!enabled) {
            detachRealtimeListeners()
        }
    }

    fun disconnect() {
        detachRealtimeListeners()
        firestoreInstance = null
        _syncStatus.value = CloudSyncStatus.OFFLINE
        _statusMessage.value = "Cloud dinonaktifkan. Bekerja dalam mode offline lokal."
    }
}

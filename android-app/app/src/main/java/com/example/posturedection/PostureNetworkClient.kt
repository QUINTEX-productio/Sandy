package com.example.posturedection

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class PostureDetectionBox(
    val label: String,       // "sitting_good" or "sitting_bad"
    val confidence: Float,   // 0.0f .. 1.0f
    val xmin: Float = 0.25f, // normalized 0..1
    val ymin: Float = 0.15f,
    val xmax: Float = 0.75f,
    val ymax: Float = 0.88f
)

data class PostureHistoryItem(
    val id: String,
    val timeText: String,
    val isBad: Boolean,
    val confidencePercent: Int,
    val slouchSeconds: Int
)

data class FirebaseAuthSession(
    val uid: String,
    val email: String,
    val idToken: String,
    val emailVerified: Boolean = false
)

data class DailyAnalyticsRecord(
    val date: String,
    val backScore: Int,
    val goodFrames: Int,
    val badFrames: Int,
    val slouchEvents: Int,
    val avgConfidence: Int
)

data class AiChatMsg(
    val role: String, // "user" or "assistant"
    val content: String
)

data class FrameUpdateResult(
    val bitmap: Bitmap? = null,
    val detections: List<PostureDetectionBox> = emptyList(),
    val latencyMs: Long = 0L
)

object PostureNetworkClient {

    /**
     * Polls the Google Colab Gateway (/api/status), direct ESP32 Wi-Fi (/capture),
     * or Firebase Firestore relay (esp32_stream/latest) in the background.
     */
    suspend fun fetchLatestPostureFrame(): FrameUpdateResult = withContext(Dispatchers.IO) {
        val startMs = System.currentTimeMillis()

        // 1. Try Colab Gateway /api/status
        try {
            val statusUrl = URL("${BackendConfig.COLAB_API_URL.trimEnd('/')}/api/status")
            val conn = (statusUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 2200
                readTimeout = 2500
                setRequestProperty("ngrok-skip-browser-warning", "true")
            }
            if (conn.responseCode == 200) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(body)
                val b64 = json.optString("frame_base64", "")
                val bmp = decodeBase64Bitmap(b64)
                val dets = parseDetectionsJson(json.optJSONArray("detections"))
                if (bmp != null || dets.isNotEmpty()) {
                    return@withContext FrameUpdateResult(
                        bitmap = bmp,
                        detections = dets,
                        latencyMs = System.currentTimeMillis() - startMs
                    )
                }
            }
        } catch (_: Exception) {}

        // 2. Try Direct ESP32 Wi-Fi Snapshot (http://192.168.4.1/capture)
        try {
            val espUrl = URL("${BackendConfig.ESP32_DEFAULT_WIFI_URL}?t=${System.currentTimeMillis()}")
            val conn = (espUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 1500
                readTimeout = 2000
            }
            if (conn.responseCode == 200) {
                val bytes = conn.inputStream.use { it.readBytes() }
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bmp != null) {
                    val dets = runColabInferenceOnJpeg(bytes)
                    return@withContext FrameUpdateResult(
                        bitmap = bmp,
                        detections = dets,
                        latencyMs = System.currentTimeMillis() - startMs
                    )
                }
            }
        } catch (_: Exception) {}

        // 3. Fallback: Check Firebase Firestore Relay (esp32_stream/latest)
        try {
            val firestoreUrl = URL(
                "https://firestore.googleapis.com/v1/projects/${BackendConfig.FIREBASE_PROJECT_ID}" +
                    "/databases/(default)/documents/esp32_stream/latest?key=${BackendConfig.FIREBASE_API_KEY}"
            )
            val conn = (firestoreUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 2000
                readTimeout = 2000
            }
            if (conn.responseCode == 200) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(body)
                val fields = root.optJSONObject("fields")
                val b64 = fields?.optJSONObject("frame_base64")?.optString("stringValue", "") ?: ""
                val statusStr = fields?.optJSONObject("status")?.optString("stringValue", "") ?: ""
                val bmp = decodeBase64Bitmap(b64)
                val dets = if (statusStr.contains("BAD", ignoreCase = true)) {
                    listOf(PostureDetectionBox("sitting_bad", 0.92f))
                } else if (statusStr.contains("GOOD", ignoreCase = true)) {
                    listOf(PostureDetectionBox("sitting_good", 0.95f))
                } else {
                    emptyList()
                }
                if (bmp != null || dets.isNotEmpty()) {
                    return@withContext FrameUpdateResult(
                        bitmap = bmp,
                        detections = dets,
                        latencyMs = System.currentTimeMillis() - startMs
                    )
                }
            }
        } catch (_: Exception) {}

        FrameUpdateResult()
    }

    /**
     * Sends raw JPEG bytes to Google Colab (/infer or /predict) and parses detections.
     */
    suspend fun runColabInferenceOnJpeg(jpegBytes: ByteArray): List<PostureDetectionBox> =
        withContext(Dispatchers.IO) {
            try {
                val inferUrl = URL("${BackendConfig.COLAB_API_URL.trimEnd('/')}/infer")
                val conn = (inferUrl.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    connectTimeout = 2500
                    readTimeout = 3000
                    setRequestProperty("Content-Type", "image/jpeg")
                    setRequestProperty("ngrok-skip-browser-warning", "true")
                }
                conn.outputStream.use { it.write(jpegBytes) }
                if (conn.responseCode == 200) {
                    val respText = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(respText)
                    val arr = json.optJSONArray("detections")
                    if (arr != null && arr.length() > 0) {
                        return@withContext parseDetectionsJson(arr)
                    }
                    val label = json.optString("label", "")
                    val conf = json.optDouble("confidence", 0.88).toFloat()
                    if (label.isNotEmpty() && label != "none") {
                        return@withContext listOf(PostureDetectionBox(label, conf))
                    }
                }
            } catch (_: Exception) {}
            emptyList()
        }

    /**
     * Syncs hardware buzzer/LED command to Firebase Firestore (hardware_control/esp32_cam)
     * so the ESP32 device can buzz when slouching >= 5 seconds.
     */
    suspend fun syncHardwareAlertToFirebase(alertActive: Boolean, slouchSeconds: Int) =
        withContext(Dispatchers.IO) {
            try {
                val url = URL(
                    "https://firestore.googleapis.com/v1/projects/${BackendConfig.FIREBASE_PROJECT_ID}" +
                        "/databases/(default)/documents/hardware_control/esp32_cam?key=${BackendConfig.FIREBASE_API_KEY}"
                )
                val payload = JSONObject().apply {
                    put("fields", JSONObject().apply {
                        put("alert_active", JSONObject().put("booleanValue", alertActive))
                        put("command", JSONObject().put("stringValue", if (alertActive) "BUZZ" else "CLEAR"))
                        put("slouch_seconds", JSONObject().put("integerValue", slouchSeconds.toString()))
                    })
                }
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "PATCH"
                    doOutput = true
                    connectTimeout = 2000
                    readTimeout = 2000
                    setRequestProperty("Content-Type", "application/json")
                }
                OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
                conn.responseCode
            } catch (_: Exception) {}
        }

    /**
     * Saves a posture log record to Firebase Firestore (posture_logs collection).
     */
    suspend fun savePostureLogToFirebase(isBad: Boolean, confidencePercent: Int, slouchSeconds: Int) =
        withContext(Dispatchers.IO) {
            try {
                val url = URL(
                    "https://firestore.googleapis.com/v1/projects/${BackendConfig.FIREBASE_PROJECT_ID}" +
                        "/databases/(default)/documents/posture_logs?key=${BackendConfig.FIREBASE_API_KEY}"
                )
                val statusText = if (isBad) "SLOUCHING DETECTED" else "GOOD POSTURE"
                val payload = JSONObject().apply {
                    put("fields", JSONObject().apply {
                        put("status", JSONObject().put("stringValue", statusText))
                        put("confidence", JSONObject().put("integerValue", confidencePercent.toString()))
                        put("slouch_seconds", JSONObject().put("integerValue", slouchSeconds.toString()))
                        put("created_at_ms", JSONObject().put("integerValue", System.currentTimeMillis().toString()))
                    })
                }
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    connectTimeout = 2500
                    readTimeout = 2500
                    setRequestProperty("Content-Type", "application/json")
                }
                OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
                conn.responseCode
            } catch (_: Exception) {}
        }

    /**
     * Loads recent posture history from Firebase Firestore (posture_logs).
     */
    suspend fun fetchRecentHistoryFromFirebase(): List<PostureHistoryItem> =
        withContext(Dispatchers.IO) {
            val result = mutableListOf<PostureHistoryItem>()
            try {
                val url = URL(
                    "https://firestore.googleapis.com/v1/projects/${BackendConfig.FIREBASE_PROJECT_ID}" +
                        "/databases/(default)/documents/posture_logs?pageSize=12&key=${BackendConfig.FIREBASE_API_KEY}"
                )
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 2500
                    readTimeout = 2500
                }
                if (conn.responseCode == 200) {
                    val text = conn.inputStream.bufferedReader().use { it.readText() }
                    val root = JSONObject(text)
                    val docs = root.optJSONArray("documents") ?: return@withContext emptyList()
                    val fmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    for (i in 0 until docs.length()) {
                        val doc = docs.getJSONObject(i)
                        val fields = doc.optJSONObject("fields") ?: continue
                        val status = fields.optJSONObject("status")?.optString("stringValue", "GOOD POSTURE") ?: "GOOD POSTURE"
                        val conf = fields.optJSONObject("confidence")?.optString("integerValue", "95")?.toIntOrNull() ?: 95
                        val slouch = fields.optJSONObject("slouch_seconds")?.optString("integerValue", "0")?.toIntOrNull() ?: 0
                        val ms = fields.optJSONObject("created_at_ms")?.optString("integerValue", "0")?.toLongOrNull()
                            ?: System.currentTimeMillis()
                        result.add(
                            PostureHistoryItem(
                                id = doc.optString("name", "$i"),
                                timeText = fmt.format(Date(ms)),
                                isBad = status.contains("BAD", true) || status.contains("SLOUCH", true),
                                confidencePercent = conf,
                                slouchSeconds = slouch
                            )
                        )
                    }
                }
            } catch (_: Exception) {}
            result
        }

    /**
     * Authenticates with Firebase Authentication REST API and returns uid, email, idToken & emailVerified.
     */
    suspend fun firebaseEmailAuthSession(
        email: String,
        password: String,
        isRegister: Boolean
    ): Result<FirebaseAuthSession> = withContext(Dispatchers.IO) {
        try {
            val action = if (isRegister) "signUp" else "signInWithPassword"
            val url = URL("https://identitytoolkit.googleapis.com/v1/accounts:$action?key=${BackendConfig.FIREBASE_API_KEY}")
            val payload = JSONObject().apply {
                put("email", email)
                put("password", password)
                put("returnSecureToken", true)
            }
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("Content-Type", "application/json")
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            if (conn.responseCode in 200..299) {
                val resp = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                val token = resp.optString("idToken", "")
                val uid = resp.optString("localId", "")
                val verified = if (token.isNotEmpty()) checkEmailVerified(token) else false
                Result.success(
                    FirebaseAuthSession(
                        uid = uid,
                        email = resp.optString("email", email),
                        idToken = token,
                        emailVerified = verified
                    )
                )
            } else {
                val errText = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "Authentication failed"
                val msg = runCatching {
                    JSONObject(errText).optJSONObject("error")?.optString("message") ?: "Check your email & password"
                }.getOrDefault("Check your email & password")
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun firebaseEmailAuth(email: String, password: String, isRegister: Boolean): Result<String> =
        firebaseEmailAuthSession(email, password, isRegister).map { it.email }

    suspend fun sendFirebaseVerificationEmail(idToken: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key=${BackendConfig.FIREBASE_API_KEY}")
            val payload = JSONObject().apply {
                put("requestType", "VERIFY_EMAIL")
                put("idToken", idToken)
            }
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("Content-Type", "application/json")
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            if (conn.responseCode in 200..299) Result.success(Unit)
            else Result.failure(Exception("Could not send verification email (HTTP ${conn.responseCode})"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkEmailVerified(idToken: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://identitytoolkit.googleapis.com/v1/accounts:lookup?key=${BackendConfig.FIREBASE_API_KEY}")
            val payload = JSONObject().put("idToken", idToken)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 3500
                readTimeout = 3500
                setRequestProperty("Content-Type", "application/json")
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            if (conn.responseCode in 200..299) {
                val root = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                val users = root.optJSONArray("users")
                if (users != null && users.length() > 0) {
                    return@withContext users.getJSONObject(0).optBoolean("emailVerified", false)
                }
            }
        } catch (_: Exception) {}
        false
    }

    suspend fun changeFirebasePassword(idToken: String, newPassword: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://identitytoolkit.googleapis.com/v1/accounts:update?key=${BackendConfig.FIREBASE_API_KEY}")
            val payload = JSONObject().apply {
                put("idToken", idToken)
                put("password", newPassword)
                put("returnSecureToken", true)
            }
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("Content-Type", "application/json")
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            if (conn.responseCode in 200..299) {
                val resp = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                Result.success(resp.optString("idToken", idToken))
            } else {
                Result.failure(Exception("Password update failed. Please sign in again and retry."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun wakeupBackend(baseUrl: String, service: String) = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank()) return@withContext
        try {
            val url = URL("${baseUrl.trimEnd('/')}/api/wakeup")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 2500
                readTimeout = 2500
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("ngrok-skip-browser-warning", "true")
            }
            OutputStreamWriter(conn.outputStream).use { it.write(JSONObject().put("service", service).toString()) }
            conn.responseCode
        } catch (_: Exception) {}
    }

    suspend fun fetchYoloAnalyticsDays(): List<DailyAnalyticsRecord> = withContext(Dispatchers.IO) {
        val list = mutableListOf<DailyAnalyticsRecord>()
        try {
            val url = URL("${BackendConfig.COLAB_API_URL.trimEnd('/')}/api/analytics")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 3000
                readTimeout = 3500
                setRequestProperty("ngrok-skip-browser-warning", "true")
            }
            if (conn.responseCode == 200) {
                val root = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                val arr = root.optJSONArray("days") ?: return@withContext emptyList()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    list.add(
                        DailyAnalyticsRecord(
                            date = o.optString("date", ""),
                            backScore = o.optInt("back_score", 80),
                            goodFrames = o.optInt("good_frames", 0),
                            badFrames = o.optInt("bad_frames", 0),
                            slouchEvents = o.optInt("slouch_events", 0),
                            avgConfidence = o.optInt("avg_confidence", 94)
                        )
                    )
                }
            }
        } catch (_: Exception) {}
        list
    }

    suspend fun askQwenAnalyzeReport(
        timeframe: String,
        currentScore: Int,
        previousScore: Int,
        goodFrames: Int,
        badFrames: Int,
        slouchEvents: Int,
        userGoal: String,
        medicalNotes: String,
        question: String
    ): String = withContext(Dispatchers.IO) {
        val endpoint = BackendConfig.QWEN_API_URL.ifBlank { BackendConfig.COLAB_API_URL }.trimEnd('/')
        try {
            val url = URL("$endpoint/api/ai-analyze")
            val payload = JSONObject().apply {
                put("timeframe", timeframe)
                put("current_score", currentScore)
                put("previous_score", previousScore)
                put("delta", currentScore - previousScore)
                put("good_frames", goodFrames)
                put("bad_frames", badFrames)
                put("slouch_events", slouchEvents)
                put("user_goal", userGoal)
                put("medical_notes", medicalNotes)
                put("question", question)
            }
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 5000
                readTimeout = 45000
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("ngrok-skip-browser-warning", "true")
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            if (conn.responseCode in 200..299) {
                val resp = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                val ans = resp.optString("answer", "")
                if (ans.isNotBlank()) return@withContext ans
            }
        } catch (_: Exception) {}

        val rating = if (currentScore >= 80) "Good (Green)" else if (currentScore >= 55) "Average (Yellow)" else "Bad (Red)"
        val delta = currentScore - previousScore
        val trend = if (delta >= 0) "up +$delta%" else "down $delta%"
        "[${timeframe.uppercase()} REPORT — $currentScore/100 ($rating)]\n\n" +
            "• Your average back score is $trend compared to the previous period ($previousScore/100).\n" +
            "• Goal ($userGoal): Keep your screen at eye level and take a 60-second shoulder-roll break every 45 minutes.\n" +
            "• Medical Notes ($medicalNotes): Aim for relaxed neutral spine alignment without rigid strain."
    }

    suspend fun askQwenChat(
        history: List<AiChatMsg>,
        currentScore: Int,
        userGoal: String,
        medicalNotes: String
    ): String = withContext(Dispatchers.IO) {
        val last = history.lastOrNull()?.content?.lowercase(Locale.getDefault()) ?: ""
        if (last.isNotBlank() && BackendConfig.ON_TOPIC_KEYWORDS.none { last.contains(it) }) {
            return@withContext BackendConfig.OFF_TOPIC_REFUSAL
        }

        val endpoint = BackendConfig.QWEN_API_URL.ifBlank { BackendConfig.COLAB_API_URL }.trimEnd('/')
        try {
            val url = URL("$endpoint/api/chat")
            val arr = JSONArray()
            history.takeLast(10).forEach { msg ->
                arr.put(JSONObject().put("role", msg.role).put("content", msg.content))
            }
            val payload = JSONObject().apply {
                put("messages", arr)
                put("current_score", currentScore)
                put("user_goal", userGoal)
                put("medical_notes", medicalNotes)
            }
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 5000
                readTimeout = 45000
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("ngrok-skip-browser-warning", "true")
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            if (conn.responseCode in 200..299) {
                val resp = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                val ans = resp.optString("answer", "")
                if (ans.isNotBlank()) return@withContext ans
            }
        } catch (_: Exception) {}

        "Based on your back score ($currentScore/100) and goal ($userGoal):\n\n" +
            "• Align your ears directly above your shoulders and keep your screen at eye level.\n" +
            "• Take a 1-minute chin-tuck & chest-opener stretch every 45 minutes."
    }

    private fun decodeBase64Bitmap(b64: String): Bitmap? {
        if (b64.isBlank()) return null
        return try {
            val clean = if (b64.contains(",")) b64.substringAfter(",") else b64
            val bytes = Base64.decode(clean, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (_: Exception) {
            null
        }
    }

    private fun parseDetectionsJson(arr: JSONArray?): List<PostureDetectionBox> {
        if (arr == null) return emptyList()
        val list = mutableListOf<PostureDetectionBox>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val name = obj.optString("name", "sitting_good")
            val conf = obj.optDouble("confidence", 0.90).toFloat()
            val xmin = (obj.optDouble("xmin", 160.0) / 640.0).toFloat().coerceIn(0.05f, 0.95f)
            val ymin = (obj.optDouble("ymin", 80.0) / 480.0).toFloat().coerceIn(0.05f, 0.95f)
            val xmax = (obj.optDouble("xmax", 480.0) / 640.0).toFloat().coerceIn(0.10f, 0.98f)
            val ymax = (obj.optDouble("ymax", 420.0) / 480.0).toFloat().coerceIn(0.10f, 0.98f)
            list.add(PostureDetectionBox(name, conf, xmin, ymin, xmax, ymax))
        }
        return list
    }
}

/**
 * Bluetooth Low Energy (BLE) Helper for pairing directly with PostureAI-ESP32S3,
 * receiving live JPEG chunks + posture telemetry, and sending Wi-Fi credentials.
 */
@SuppressLint("MissingPermission")
class PostureBleClient(
    private val context: Context,
    private val onStatusChange: (String) -> Unit,
    private val onFrameReceived: (Bitmap) -> Unit,
    private val onDetectionReceived: (PostureDetectionBox) -> Unit
) {
    private var bluetoothGatt: BluetoothGatt? = null
    private var cmdCharacteristic: BluetoothGattCharacteristic? = null
    private val frameBuffer = ByteArrayOutputStream()
    private var isScanning = false

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        manager?.adapter
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device: BluetoothDevice = result.device ?: return
            val name = device.name ?: result.scanRecord?.deviceName ?: ""
            if (name.startsWith(BackendConfig.BLE_DEVICE_NAME_PREFIX, ignoreCase = true)) {
                stopScan()
                onStatusChange("Connecting to $name...")
                bluetoothGatt = device.connectGatt(context, false, gattCallback)
            }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                onStatusChange("Bluetooth Connected")
                gatt.requestMtu(512)
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                onStatusChange("Ready to Connect")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            val service = gatt.getService(UUID.fromString(BackendConfig.BLE_SERVICE_UUID)) ?: return
            cmdCharacteristic = service.getCharacteristic(UUID.fromString(BackendConfig.BLE_CMD_CHAR_UUID))

            val frameChar = service.getCharacteristic(UUID.fromString(BackendConfig.BLE_FRAME_CHAR_UUID))
            if (frameChar != null) {
                enableNotifications(gatt, frameChar)
            }

            val statusChar = service.getCharacteristic(UUID.fromString(BackendConfig.BLE_STATUS_CHAR_UUID))
            if (statusChar != null) {
                enableNotifications(gatt, statusChar)
            }
        }

        @Deprecated("Deprecated in API 33")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val bytes = characteristic.value ?: return
            handleIncomingBleBytes(characteristic.uuid.toString(), bytes)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            handleIncomingBleBytes(characteristic.uuid.toString(), value)
        }
    }

    private fun handleIncomingBleBytes(charUuid: String, bytes: ByteArray) {
        val text = String(bytes, Charsets.UTF_8)
        if (charUuid.equals(BackendConfig.BLE_FRAME_CHAR_UUID, ignoreCase = true)) {
            when {
                text.startsWith("START:") -> frameBuffer.reset()
                text == "END" -> {
                    val data = frameBuffer.toByteArray()
                    frameBuffer.reset()
                    if (data.isNotEmpty()) {
                        val bmp = BitmapFactory.decodeByteArray(data, 0, data.size)
                        if (bmp != null) onFrameReceived(bmp)
                    }
                }
                else -> frameBuffer.write(bytes)
            }
        } else if (charUuid.equals(BackendConfig.BLE_STATUS_CHAR_UUID, ignoreCase = true)) {
            runCatching {
                val json = JSONObject(text)
                val state = json.optString("state", "")
                val conf = json.optDouble("conf", 0.92).toFloat()
                if (state == "SITTING_BAD") {
                    onDetectionReceived(PostureDetectionBox("sitting_bad", conf))
                } else if (state == "SITTING_GOOD") {
                    onDetectionReceived(PostureDetectionBox("sitting_good", conf))
                }
            }
        }
    }

    private fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        gatt.setCharacteristicNotification(characteristic, true)
        val cccd = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
        if (cccd != null) {
            cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(cccd)
        }
    }

    fun startScanAndConnect() {
        val adapter = bluetoothAdapter
        if (adapter == null || !adapter.isEnabled) {
            onStatusChange("Turn on Bluetooth")
            return
        }
        if (isScanning) return
        isScanning = true
        onStatusChange("Searching Bluetooth...")
        adapter.bluetoothLeScanner?.startScan(scanCallback)
    }

    fun stopScan() {
        if (!isScanning) return
        isScanning = false
        runCatching { bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback) }
    }

    fun sendWifiCredentials(ssid: String, pass: String): Boolean {
        val char = cmdCharacteristic ?: return false
        val gatt = bluetoothGatt ?: return false
        char.value = "WIFI:$ssid:$pass".toByteArray(Charsets.UTF_8)
        return gatt.writeCharacteristic(char)
    }

    fun sendBuzzCommand() {
        val char = cmdCharacteristic ?: return
        val gatt = bluetoothGatt ?: return
        char.value = "BUZZ".toByteArray(Charsets.UTF_8)
        gatt.writeCharacteristic(char)
    }

    fun disconnect() {
        stopScan()
        runCatching { bluetoothGatt?.disconnect() }
        runCatching { bluetoothGatt?.close() }
        bluetoothGatt = null
        cmdCharacteristic = null
    }
}

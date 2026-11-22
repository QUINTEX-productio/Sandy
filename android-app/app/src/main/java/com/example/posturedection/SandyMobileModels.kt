package com.example.posturedection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Color
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SandySection(val label: String, val iconGlyph: String) {
    MENU("Menu", "⊞"),
    DEVICES("Devices", "ᛒ"),
    INTERFACE("Live", "◉"),
    CHATBOT("AI", "✦"),
    REPORT("Report", "▥"),
    SETTINGS("Set", "⚙")
}

enum class SandyAccentTheme(val key: String, val displayName: String, val color: Color) {
    CYAN("cyan", "Cyber Cyan", Color(0xFF00F0FF)),
    MAGENTA("magenta", "Neon Magenta", Color(0xFFFF007F)),
    GOLD("gold", "Solar Gold", Color(0xFFFFE600)),
    EMERALD("emerald", "Emerald Pulse", Color(0xFF00FF9D));

    companion object {
        fun fromKey(k: String?): SandyAccentTheme =
            entries.firstOrNull { it.key == k } ?: CYAN
    }
}

enum class DeviceConnState(val label: String) {
    PAIRING("Pairing"),
    CONNECTED("Connected"),
    DISCONNECTED("Disconnected");

    companion object {
        fun fromString(s: String?): DeviceConnState = when (s?.lowercase(Locale.US)) {
            "pairing" -> PAIRING
            "connected" -> CONNECTED
            else -> DISCONNECTED
        }
    }
}

data class SandyUserProfile(
    val uid: String = "",
    val idToken: String = "",
    val displayName: String = "",
    val username: String = "",
    val email: String = "",
    val dob: String = "",
    val gender: String = "Prefer not to say",
    val medicalConditions: List<String> = emptyList(),
    val medicalNotes: String = "",
    val seatHours: String = "6–8 hours",
    val deskSetup: String = "Desktop / External Monitor",
    val goal: String = "Build Upright Sitting Habit",
    val planPath: String = "Daily Habit Path",
    val signedIn: Boolean = false
) {
    val initials: String
        get() {
            val parts = displayName.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
            return if (parts.isEmpty()) "S" else parts.take(2).joinToString("") { it.take(1).uppercase() }
        }

    val combinedMedicalText: String
        get() = listOf(
            medicalConditions.joinToString(", "),
            medicalNotes.trim()
        ).filter { it.isNotBlank() }.joinToString(" — ").ifBlank { "None reported" }
}

data class PostureDeviceItem(
    val id: String,
    val name: String,
    val model: String = "ESP32-S3-WROOM-1-N16R8",
    val dateMade: String = "2026-10",
    val firmware: String = "v2.4.0-sandy",
    val protocol: String = "Bluetooth LE 5.0",
    val hardwareId: String = "SND-ESP32S3",
    val streamUrl: String = "",
    val status: DeviceConnState = DeviceConnState.DISCONNECTED,
    val lastConnected: String = "Never"
)

data class RecordedMediaEntry(
    val id: String,
    val mediaType: String, // "image" or "video"
    val filePath: String,
    val timestampMs: Long,
    val dateKey: String,
    val posture: String,   // "GOOD" or "BAD"
    val deviceName: String
)

data class SandyMobileSettings(
    val theme: SandyAccentTheme = SandyAccentTheme.CYAN,
    val chimeAndVibrate: Boolean = true,
    val autoRecordMedia: Boolean = true,
    val autoWakeColab: Boolean = true,
    val slouchAlertSeconds: Int = 5
)

object SandyLocalStore {
    private const val PREFS_NAME = "sandy_mobile_v2_prefs"

    fun loadProfile(context: Context): SandyUserProfile {
        val p = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val medRaw = p.getString("medicalConditions", "") ?: ""
        return SandyUserProfile(
            uid = p.getString("uid", "") ?: "",
            idToken = p.getString("idToken", "") ?: "",
            displayName = p.getString("displayName", "") ?: "",
            username = p.getString("username", "") ?: "",
            email = p.getString("email", "") ?: "",
            dob = p.getString("dob", "") ?: "",
            gender = p.getString("gender", "Prefer not to say") ?: "Prefer not to say",
            medicalConditions = medRaw.split("|").filter { it.isNotBlank() },
            medicalNotes = p.getString("medicalNotes", "") ?: "",
            seatHours = p.getString("seatHours", "6–8 hours") ?: "6–8 hours",
            deskSetup = p.getString("deskSetup", "Desktop / External Monitor") ?: "Desktop / External Monitor",
            goal = p.getString("goal", "Build Upright Sitting Habit") ?: "Build Upright Sitting Habit",
            planPath = p.getString("planPath", "Daily Habit Path") ?: "Daily Habit Path",
            signedIn = p.getBoolean("signedIn", false)
        )
    }

    fun saveProfile(context: Context, profile: SandyUserProfile) {
        val p = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        p.edit()
            .putString("uid", profile.uid)
            .putString("idToken", profile.idToken)
            .putString("displayName", profile.displayName)
            .putString("username", profile.username)
            .putString("email", profile.email)
            .putString("dob", profile.dob)
            .putString("gender", profile.gender)
            .putString("medicalConditions", profile.medicalConditions.joinToString("|"))
            .putString("medicalNotes", profile.medicalNotes)
            .putString("seatHours", profile.seatHours)
            .putString("deskSetup", profile.deskSetup)
            .putString("goal", profile.goal)
            .putString("planPath", profile.planPath)
            .putBoolean("signedIn", profile.signedIn)
            .apply()
    }

    fun loadSettings(context: Context): SandyMobileSettings {
        val p = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return SandyMobileSettings(
            theme = SandyAccentTheme.fromKey(p.getString("theme", "cyan")),
            chimeAndVibrate = p.getBoolean("chimeAndVibrate", true),
            autoRecordMedia = p.getBoolean("autoRecordMedia", true),
            autoWakeColab = p.getBoolean("autoWakeColab", true),
            slouchAlertSeconds = p.getInt("slouchAlertSeconds", 5)
        )
    }

    fun saveSettings(context: Context, s: SandyMobileSettings) {
        val p = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        p.edit()
            .putString("theme", s.theme.key)
            .putBoolean("chimeAndVibrate", s.chimeAndVibrate)
            .putBoolean("autoRecordMedia", s.autoRecordMedia)
            .putBoolean("autoWakeColab", s.autoWakeColab)
            .putInt("slouchAlertSeconds", s.slouchAlertSeconds)
            .apply()
    }

    fun loadDevices(context: Context): List<PostureDeviceItem> {
        val p = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = p.getString("devices_json", "[]") ?: "[]"
        val out = mutableListOf<PostureDeviceItem>()
        runCatching {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                out.add(
                    PostureDeviceItem(
                        id = o.optString("id", "dev_$i"),
                        name = o.optString("name", "Sandy Sensor"),
                        model = o.optString("model", "ESP32-S3-WROOM-1-N16R8"),
                        dateMade = o.optString("dateMade", "2026-10"),
                        firmware = o.optString("firmware", "v2.4.0-sandy"),
                        protocol = o.optString("protocol", "Bluetooth LE 5.0"),
                        hardwareId = o.optString("hardwareId", "SND-ESP32S3"),
                        streamUrl = o.optString("streamUrl", ""),
                        status = DeviceConnState.fromString(o.optString("status", "disconnected")),
                        lastConnected = o.optString("lastConnected", "Never")
                    )
                )
            }
        }
        return out
    }

    fun saveDevices(context: Context, list: List<PostureDeviceItem>) {
        val arr = JSONArray()
        list.forEach { d ->
            arr.put(
                JSONObject().apply {
                    put("id", d.id)
                    put("name", d.name)
                    put("model", d.model)
                    put("dateMade", d.dateMade)
                    put("firmware", d.firmware)
                    put("protocol", d.protocol)
                    put("hardwareId", d.hardwareId)
                    put("streamUrl", d.streamUrl)
                    put("status", d.status.label)
                    put("lastConnected", d.lastConnected)
                }
            )
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("devices_json", arr.toString())
            .apply()
    }

    fun loadDailyAnalytics(context: Context): Map<String, DailyAnalyticsRecord> {
        val p = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = p.getString("analytics_json", "{}") ?: "{}"
        val map = mutableMapOf<String, DailyAnalyticsRecord>()
        runCatching {
            val obj = JSONObject(raw)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val o = obj.getJSONObject(k)
                map[k] = DailyAnalyticsRecord(
                    date = k,
                    backScore = o.optInt("backScore", 0),
                    goodFrames = o.optInt("goodFrames", 0),
                    badFrames = o.optInt("badFrames", 0),
                    slouchEvents = o.optInt("slouchEvents", 0),
                    avgConfidence = o.optInt("avgConfidence", 90)
                )
            }
        }
        return map
    }

    fun saveDailyAnalytics(context: Context, map: Map<String, DailyAnalyticsRecord>) {
        val obj = JSONObject()
        map.forEach { (k, v) ->
            obj.put(
                k,
                JSONObject().apply {
                    put("backScore", v.backScore)
                    put("goodFrames", v.goodFrames)
                    put("badFrames", v.badFrames)
                    put("slouchEvents", v.slouchEvents)
                    put("avgConfidence", v.avgConfidence)
                }
            )
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("analytics_json", obj.toString())
            .apply()
    }

    fun saveRecordedSnapshot(
        context: Context,
        bitmap: Bitmap,
        posture: String,
        deviceName: String,
        mediaType: String = "image"
    ): RecordedMediaEntry? {
        return runCatching {
            val dir = File(context.filesDir, "sandy_media").apply { mkdirs() }
            val id = "rec_${System.currentTimeMillis()}"
            val file = File(dir, "$id.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 78, out)
            }
            val now = System.currentTimeMillis()
            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))
            val entry = RecordedMediaEntry(
                id = id,
                mediaType = mediaType,
                filePath = file.absolutePath,
                timestampMs = now,
                dateKey = dateKey,
                posture = posture,
                deviceName = deviceName
            )
            val existing = loadRecordedMedia(context).toMutableList()
            existing.add(0, entry)
            persistMediaList(context, existing.take(150))
            entry
        }.getOrNull()
    }

    fun loadRecordedMedia(context: Context): List<RecordedMediaEntry> {
        val p = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = p.getString("media_json", "[]") ?: "[]"
        val out = mutableListOf<RecordedMediaEntry>()
        runCatching {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val path = o.optString("filePath", "")
                if (path.isNotBlank() && File(path).exists()) {
                    out.add(
                        RecordedMediaEntry(
                            id = o.optString("id", "rec_$i"),
                            mediaType = o.optString("mediaType", "image"),
                            filePath = path,
                            timestampMs = o.optLong("timestampMs", System.currentTimeMillis()),
                            dateKey = o.optString("dateKey", ""),
                            posture = o.optString("posture", "GOOD"),
                            deviceName = o.optString("deviceName", "Sandy Sensor")
                        )
                    )
                }
            }
        }
        return out
    }

    fun deleteRecordedMedia(context: Context, id: String): List<RecordedMediaEntry> {
        val current = loadRecordedMedia(context)
        current.firstOrNull { it.id == id }?.let { runCatching { File(it.filePath).delete() } }
        val updated = current.filterNot { it.id == id }
        persistMediaList(context, updated)
        return updated
    }

    fun loadBitmapSafe(path: String): Bitmap? =
        runCatching { BitmapFactory.decodeFile(path) }.getOrNull()

    private fun persistMediaList(context: Context, list: List<RecordedMediaEntry>) {
        val arr = JSONArray()
        list.forEach { m ->
            arr.put(
                JSONObject().apply {
                    put("id", m.id)
                    put("mediaType", m.mediaType)
                    put("filePath", m.filePath)
                    put("timestampMs", m.timestampMs)
                    put("dateKey", m.dateKey)
                    put("posture", m.posture)
                    put("deviceName", m.deviceName)
                }
            )
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("media_json", arr.toString())
            .apply()
    }
}

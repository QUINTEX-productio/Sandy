package com.example.posturedection

/**
 * ============================================================================
 * DEVELOPER BACKEND CONFIGURATION (Editable in Code Only)
 * ============================================================================
 */
object BackendConfig {
    const val APP_NAME = "Sandy"
    const val APP_VERSION_LABEL = "ver Alpha"
    const val APP_SEMVER = "0.1.0-alpha"
    const val WEBSITE_BASE_URL = "https://quintex-productio.github.io/sandy.io"

    // 1. Google Colab Endpoints (Notebook 1: YOLOv5 | Notebook 2: Qwen3.6-35B)
    const val COLAB_API_URL = "https://plastic-smudgy-occupant.ngrok-free.dev"
    const val QWEN_API_URL  = "https://referenced-temperatures-wit-stay.trycloudflare.com"

    // 2. ESP32-S3 / ESP32-CAM Default Wi-Fi Snapshot Endpoint
    const val ESP32_DEFAULT_WIFI_URL = "http://192.168.4.1/capture"

    // 3. Default Detection Sensitivity & Alert Delay
    const val DEFAULT_CONFIDENCE_THRESHOLD = 0.40f
    const val SLOUCH_ALERT_SECONDS = 5

    // 4. ESP32-S3 Bluetooth Low Energy (BLE) Service & Characteristic UUIDs
    const val BLE_DEVICE_NAME_PREFIX = "PostureAI"
    const val BLE_SERVICE_UUID       = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
    const val BLE_FRAME_CHAR_UUID    = "beb5483e-36e1-4688-b7f5-ea07361b26a8"
    const val BLE_CMD_CHAR_UUID      = "beb5483e-36e1-4688-b7f5-ea07361b26a9"
    const val BLE_STATUS_CHAR_UUID   = "beb5483e-36e1-4688-b7f5-ea07361b26aa"

    // 5. Firebase Project Configuration (Project: posturedection)
    const val FIREBASE_API_KEY       = "AIzaSyD6O32oglKmqtjZNlSXOOBjY5wIp1x77UY"
    const val FIREBASE_AUTH_DOMAIN   = "posturedection.firebaseapp.com"
    const val FIREBASE_PROJECT_ID    = "posturedection"
    const val FIREBASE_STORAGE_BUCKET = "posturedection.firebasestorage.app"
    const val FIREBASE_APP_ID        = "1:377446077231:web:9d1541d24b0c08711b07da"

    // 6. Strict Off-Topic Guardrail Refusal (Exact match with website & Windows app)
    const val OFF_TOPIC_REFUSAL =
        "I'm Sandy AI, and I can only help with posture, ergonomics, your Sandy back score reports, and spine or health-related questions. How can I help with your posture today?"

    val ON_TOPIC_KEYWORDS = listOf(
        "posture", "back", "spine", "spinal", "neck", "shoulder", "slouch", "sit", "sitting",
        "ergonomic", "desk", "chair", "monitor", "stretch", "pain", "ache", "stiff", "scoliosis",
        "disc", "lumbar", "cervical", "kyphosis", "score", "report", "yolo", "sandy", "health",
        "medical", "doctor", "physio", "therapy", "rehab", "exercise", "muscle", "headache",
        "fatigue", "break", "standing", "pillow", "sleep", "hip", "pelvis", "wrist", "eye",
        "hi", "hello", "hey", "help", "good", "bad", "improve", "strain", "hunch", "upright"
    )
}

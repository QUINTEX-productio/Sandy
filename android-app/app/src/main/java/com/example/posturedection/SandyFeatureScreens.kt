package com.example.posturedection

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Shared Mobile Cyberpunk Night City Tokens
internal val VoidBg = Color(0xFF050814)
internal val GlassBg = Color(0xCC0B1122)
internal val SubGlassBg = Color(0xCC0F172E)
internal val BorderGlass = Color(0x26FFFFFF)
internal val NeonRed = Color(0xFFFF2A6D)
internal val NeonGreen = Color(0xFF00FF66)
internal val NeonAmber = Color(0xFFFFE600)
internal val NeonMagenta = Color(0xFFFF007F)
internal val TextHi = Color(0xFFF8FAFC)
internal val TextSoft = Color(0xFF94A3B8)
internal val TextDim = Color(0xFF64748B)
internal val DarkInk = Color(0xFF050814)

@Composable
fun SandyLogoBadge(accent: Color, size: Dp = 76.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.28f))
            .background(
                Brush.linearGradient(
                    listOf(accent.copy(alpha = 0.22f), NeonMagenta.copy(alpha = 0.18f))
                )
            )
            .border(1.8.dp, accent.copy(alpha = 0.72f), RoundedCornerShape(size * 0.28f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.62f)) {
            val w = this.size.width
            val h = this.size.height
            drawCircle(
                color = accent,
                radius = w * 0.13f,
                center = Offset(w * 0.5f, h * 0.20f),
                style = Stroke(width = w * 0.06f)
            )
            drawLine(
                color = accent,
                start = Offset(w * 0.5f, h * 0.34f),
                end = Offset(w * 0.5f, h * 0.68f),
                strokeWidth = w * 0.07f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = NeonMagenta,
                start = Offset(w * 0.24f, h * 0.48f),
                end = Offset(w * 0.76f, h * 0.48f),
                strokeWidth = w * 0.065f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = NeonGreen,
                start = Offset(w * 0.5f, h * 0.68f),
                end = Offset(w * 0.30f, h * 0.88f),
                strokeWidth = w * 0.065f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = NeonGreen,
                start = Offset(w * 0.5f, h * 0.68f),
                end = Offset(w * 0.70f, h * 0.88f),
                strokeWidth = w * 0.065f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun sandyFieldColors(accent: Color) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = accent,
    unfocusedBorderColor = Color(0x33FFFFFF),
    focusedTextColor = TextHi,
    unfocusedTextColor = TextHi,
    cursorColor = accent,
    focusedLabelColor = accent,
    unfocusedLabelColor = TextSoft,
    focusedContainerColor = Color(0xCC060A16),
    unfocusedContainerColor = Color(0xCC060A16)
)

@Composable
private fun RequiredLabel(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text, color = TextHi, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Text(" *", color = NeonRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CompactDropdownBox(
    label: String,
    value: String,
    options: List<String>,
    accent: Color,
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clickable { expanded = true },
            shape = RoundedCornerShape(10.dp),
            color = Color(0xCC060A16),
            border = BorderStroke(1.dp, if (expanded) accent else Color(0x33FFFFFF))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = value.ifBlank { label },
                    color = if (value.isBlank()) TextDim else TextHi,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text("▾", color = accent, fontSize = 12.sp)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFF0B1122))
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt, color = TextHi, fontSize = 12.sp) },
                    onClick = {
                        onSelect(opt)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ============================================================================
// 1. FIRST-LAUNCH SPLASH (LOGO ON TOP -> SANDY BELOW -> LOGIN / SIGN UP)
// ============================================================================
@Composable
fun SandySplashScreen(
    accent: Color,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBg)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = GlassBg,
            border = BorderStroke(1.dp, accent.copy(alpha = 0.34f))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 34.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SandyLogoBadge(accent = accent, size = 86.dp)
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "SANDY",
                    color = TextHi,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "AI Posture Detection & Spine Wellness Companion",
                    color = TextSoft,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(26.dp))
                Button(
                    onClick = onSignInClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = DarkInk)
                ) {
                    Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onSignUpClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0x38FFFFFF)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextHi)
                ) {
                    Text("Create Account (Sign Up)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "Sandy Mobile • ${BackendConfig.APP_VERSION_LABEL}",
                    color = TextDim,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// ============================================================================
// 2. SIGN IN & SIGN UP (EMAIL WEBSITE->FIREBASE SYNC + GOOGLE/GMAIL SIGN-IN)
// ============================================================================
@Composable
fun SandyAuthScreen(
    accent: Color,
    isSignUpInitial: Boolean,
    onBack: () -> Unit,
    onSignInSuccess: (SandyUserProfile) -> Unit,
    onSignUpContinueToSurvey: (SandyUserProfile) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isSignUp by remember { mutableStateOf(isSignUpInitial) }
    var displayName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var dobMonth by remember { mutableStateOf("") }
    var dobDay by remember { mutableStateOf("") }
    var dobYear by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var tosAccepted by remember { mutableStateOf(false) }
    var isGooglePrefilled by remember { mutableStateOf(false) }

    var isBusy by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    var infoMsg by remember { mutableStateOf("") }

    val months = listOf("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12")
    val days = (1..31).map { it.toString().padStart(2, '0') }
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = (currentYear downTo 1930).map { it.toString() }
    val genders = listOf("Male", "Female", "Non-binary", "Prefer not to say")

    val pwScore = remember(password) {
        var s = 0
        if (password.length >= 6) s++
        if (password.any { it.isUpperCase() }) s++
        if (password.any { it.isDigit() }) s++
        if (password.length >= 10) s++
        s
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBg)
            .padding(horizontal = 14.dp, vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(20.dp),
            color = GlassBg,
            border = BorderStroke(1.dp, accent.copy(alpha = 0.28f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
                        Text("← Back", color = TextSoft, fontSize = 12.sp)
                    }
                    Text(
                        text = if (isSignUp) "CREATE ACCOUNT" else "SIGN IN",
                        color = accent,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = if (isSignUp) "Join Sandy" else "Welcome Back",
                    color = TextHi,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isSignUp)
                        "Fill out email to sync through sandy.io to Firebase, or sign in with Google / Gmail."
                    else
                        "Sign in with your email (synced to Firebase) or Google / Gmail.",
                    color = TextSoft,
                    fontSize = 11.sp
                )
                Spacer(Modifier.height(12.dp))

                if (errorMsg.isNotBlank()) {
                    Surface(
                        color = NeonRed.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, NeonRed.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            errorMsg,
                            color = Color(0xFFFF85A8),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }
                if (infoMsg.isNotBlank()) {
                    Surface(
                        color = accent.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, accent.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            infoMsg,
                            color = TextHi,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }

                // Google / Gmail Sign-In Button
                Button(
                    onClick = {
                        // Launch standard Firebase Google Sign-In on sandy.io in browser
                        val target = "${BackendConfig.WEBSITE_BASE_URL}/${if (isSignUp) "signup.html" else "signin.html"}"
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(target)))
                        }
                        if (isSignUp) {
                            isGooglePrefilled = true
                            infoMsg = "Opened Gmail / Google Firebase Sign-In. You can also finish your profile details below."
                        } else {
                            infoMsg = "Opened Gmail / Google Firebase Sign-In on sandy.io."
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(11.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF111827))
                ) {
                    Text("G  Continue with Google / Gmail", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    text = "— OR EMAIL (FIREBASE SYNC) —",
                    color = TextDim,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))

                if (isSignUp) {
                    RequiredLabel("Display Name")
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        placeholder = { Text("Alex Rivera", fontSize = 12.sp, color = TextDim) },
                        singleLine = true,
                        colors = sandyFieldColors(accent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    RequiredLabel("Username")
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it.replace(" ", "") },
                        placeholder = { Text("alex_posture", fontSize = 12.sp, color = TextDim) },
                        singleLine = true,
                        colors = sandyFieldColors(accent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                }

                RequiredLabel("Email Address")
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it.trim() },
                    placeholder = { Text("you@example.com", fontSize = 12.sp, color = TextDim) },
                    singleLine = true,
                    colors = sandyFieldColors(accent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                if (isSignUp) {
                    RequiredLabel("Date of Birth")
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        CompactDropdownBox("Month", dobMonth, months, accent, Modifier.weight(1f)) { dobMonth = it }
                        CompactDropdownBox("Day", dobDay, days, accent, Modifier.weight(0.9f)) { dobDay = it }
                        CompactDropdownBox("Year", dobYear, years, accent, Modifier.weight(1.1f)) { dobYear = it }
                    }
                    Spacer(Modifier.height(8.dp))

                    RequiredLabel("Gender")
                    Spacer(Modifier.height(4.dp))
                    CompactDropdownBox("Select gender", gender, genders, accent, Modifier.fillMaxWidth()) { gender = it }
                    Spacer(Modifier.height(8.dp))
                }

                if (!isGooglePrefilled || !isSignUp) {
                    RequiredLabel("Password")
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Min. 6 characters", fontSize = 12.sp, color = TextDim) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = sandyFieldColors(accent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (isSignUp && !isGooglePrefilled) {
                    Spacer(Modifier.height(6.dp))
                    // 4-bar password strength meter
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                        val barColor = when (pwScore) {
                            1 -> NeonRed
                            2 -> NeonAmber
                            3 -> accent
                            4 -> NeonGreen
                            else -> Color(0x22FFFFFF)
                        }
                        for (i in 1..4) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(if (i <= pwScore) barColor else Color(0x22FFFFFF))
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    RequiredLabel("Confirm Password")
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = { Text("Repeat password", fontSize = 12.sp, color = TextDim) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = sandyFieldColors(accent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (isSignUp) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = tosAccepted,
                            onCheckedChange = { tosAccepted = it },
                            colors = CheckboxDefaults.colors(checkedColor = accent, uncheckedColor = TextSoft)
                        )
                        Text("I agree to Sandy Terms & Privacy ", color = TextSoft, fontSize = 11.sp)
                        Text("*", color = NeonRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Text("*", color = NeonRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(" means required", color = TextSoft, fontSize = 11.sp)
                }

                Spacer(Modifier.height(8.dp))
                Button(
                    enabled = !isBusy,
                    onClick = {
                        errorMsg = ""
                        if (email.isBlank() || (!isGooglePrefilled && password.length < 6)) {
                            errorMsg = "Please enter a valid email and password (min 6 chars)."
                            return@Button
                        }
                        if (isSignUp) {
                            if (displayName.isBlank() || username.isBlank()) {
                                errorMsg = "Display Name and Username are required."
                                return@Button
                            }
                            if (dobMonth.isBlank() || dobDay.isBlank() || dobYear.isBlank() || gender.isBlank()) {
                                errorMsg = "Please select Date of Birth (Month/Day/Year) and Gender."
                                return@Button
                            }
                            if (!isGooglePrefilled && password != confirmPassword) {
                                errorMsg = "Passwords do not match."
                                return@Button
                            }
                            if (!tosAccepted) {
                                errorMsg = "Please agree to the Terms of Service."
                                return@Button
                            }
                        }

                        isBusy = true
                        scope.launch {
                            val res = PostureNetworkClient.firebaseEmailAuthSession(
                                email = email,
                                password = password.ifBlank { "SandyGooglePass1!" },
                                isRegister = isSignUp
                            )
                            isBusy = false
                            res.onSuccess { session ->
                                if (isSignUp) {
                                    PostureNetworkClient.sendFirebaseVerificationEmail(session.idToken)
                                    val newProfile = SandyUserProfile(
                                        uid = session.uid,
                                        idToken = session.idToken,
                                        displayName = displayName.trim(),
                                        username = username.trim().removePrefix("@"),
                                        email = session.email,
                                        dob = "$dobYear-$dobMonth-$dobDay",
                                        gender = gender,
                                        signedIn = true
                                    )
                                    onSignUpContinueToSurvey(newProfile)
                                } else {
                                    val saved = SandyLocalStore.loadProfile(context)
                                    val merged = saved.copy(
                                        uid = session.uid,
                                        idToken = session.idToken,
                                        email = session.email,
                                        displayName = saved.displayName.ifBlank { session.email.substringBefore("@") },
                                        username = saved.username.ifBlank { session.email.substringBefore("@") },
                                        signedIn = true
                                    )
                                    onSignInSuccess(merged)
                                }
                            }.onFailure { err ->
                                errorMsg = err.message ?: "Authentication failed."
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(11.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = DarkInk)
                ) {
                    Text(
                        text = when {
                            isBusy -> "Connecting to Firebase..."
                            isSignUp -> "Sign Up & Continue to Survey"
                            else -> "Sign In"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isSignUp) "Already have an account? " else "New to Sandy? ",
                        color = TextSoft,
                        fontSize = 12.sp
                    )
                    Text(
                        text = if (isSignUp) "Sign In" else "Sign Up",
                        color = accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            errorMsg = ""
                            infoMsg = ""
                            isGooglePrefilled = false
                            isSignUp = !isSignUp
                        }
                    )
                }
            }
        }
    }
}

// ============================================================================
// 3. 3-STEP PERSONALIZATION SURVEY (INCLUDED AFTER SIGN UP)
// ============================================================================
@Composable
fun SandyOnboardingSurveyScreen(
    accent: Color,
    initialProfile: SandyUserProfile,
    onComplete: (SandyUserProfile) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    val selectedMeds = remember { mutableStateListOf<String>() }
    var medNotes by remember { mutableStateOf("") }
    var seatHours by remember { mutableStateOf("6–8 hours") }
    var deskSetup by remember { mutableStateOf("Desktop / External Monitor") }
    var selectedGoal by remember { mutableStateOf("") }
    var selectedPlan by remember { mutableStateOf("") }
    var goalErr by remember { mutableStateOf(false) }

    val medOptions = listOf(
        "Scoliosis", "Lower Back Pain", "Neck Strain",
        "Slipped Disc", "Rounded Shoulders", "None"
    )
    val goalOptions = listOf(
        Triple("Relieve Neck & Back Pain", "Gentle Relief Path", NeonRed),
        Triple("Build Upright Sitting Habit", "Daily Habit Path", accent),
        Triple("Improve Focus & Ergonomics", "Deep Work Path", NeonAmber),
        Triple("Gentle Posture Recovery", "Rehab Recovery Path", NeonGreen)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBg)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(20.dp),
            color = GlassBg,
            border = BorderStroke(1.dp, accent.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = when (step) {
                        1 -> "STEP 1 OF 3 • OPTIONAL"
                        2 -> "STEP 2 OF 3 • OPTIONAL"
                        else -> "STEP 3 OF 3 • REQUIRED"
                    },
                    color = accent,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when (step) {
                        1 -> "Medical Background"
                        2 -> "Daily Desk Routine"
                        else -> "Primary Posture Goal"
                    },
                    color = TextHi,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))

                if (step == 1) {
                    Text(
                        "Select any spine/back conditions so AI coaching stays safe.",
                        color = TextSoft,
                        fontSize = 11.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    medOptions.chunked(2).forEach { rowItems ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            rowItems.forEach { label ->
                                val picked = selectedMeds.contains(label)
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            if (label == "None") {
                                                selectedMeds.clear()
                                                selectedMeds.add("None")
                                            } else {
                                                selectedMeds.remove("None")
                                                if (picked) selectedMeds.remove(label) else selectedMeds.add(label)
                                            }
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (picked) accent.copy(alpha = 0.18f) else SubGlassBg,
                                    border = BorderStroke(1.dp, if (picked) accent else BorderGlass)
                                ) {
                                    Text(
                                        label,
                                        color = if (picked) TextHi else TextSoft,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = medNotes,
                        onValueChange = { medNotes = it },
                        placeholder = { Text("Optional medical notes...", fontSize = 12.sp, color = TextDim) },
                        colors = sandyFieldColors(accent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        OutlinedButton(onClick = { step = 2 }, border = BorderStroke(1.dp, BorderGlass)) {
                            Text("Skip", color = TextSoft, fontSize = 12.sp)
                        }
                        Button(
                            onClick = { step = 2 },
                            colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = DarkInk)
                        ) {
                            Text("Next →", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                } else if (step == 2) {
                    Text("Sitting Hours per Day", color = TextHi, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    CompactDropdownBox(
                        label = "Select hours",
                        value = seatHours,
                        options = listOf("Under 4 hours", "4–6 hours", "6–8 hours", "8+ hours (Heavy Desk)"),
                        accent = accent,
                        modifier = Modifier.fillMaxWidth()
                    ) { seatHours = it }
                    Spacer(Modifier.height(12.dp))
                    Text("Primary Desk Setup", color = TextHi, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    CompactDropdownBox(
                        label = "Select setup",
                        value = deskSetup,
                        options = listOf("Laptop Only", "Desktop / External Monitor", "Dual Monitors", "Standing Desk"),
                        accent = accent,
                        modifier = Modifier.fillMaxWidth()
                    ) { deskSetup = it }
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        OutlinedButton(onClick = { step = 1 }, border = BorderStroke(1.dp, BorderGlass)) {
                            Text("← Back", color = TextSoft, fontSize = 12.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { step = 3 }, border = BorderStroke(1.dp, BorderGlass)) {
                                Text("Skip", color = TextSoft, fontSize = 12.sp)
                            }
                            Button(
                                onClick = { step = 3 },
                                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = DarkInk)
                            ) {
                                Text("Next →", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    if (goalErr) {
                        Text("Please choose a goal to continue.", color = NeonRed, fontSize = 11.sp)
                        Spacer(Modifier.height(6.dp))
                    }
                    goalOptions.forEach { (title, plan, col) ->
                        val picked = selectedGoal == title
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clickable {
                                    selectedGoal = title
                                    selectedPlan = plan
                                    goalErr = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (picked) accent.copy(alpha = 0.16f) else SubGlassBg,
                            border = BorderStroke(1.dp, if (picked) accent else BorderGlass)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(col)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(title, color = TextHi, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(plan, color = TextSoft, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        OutlinedButton(onClick = { step = 2 }, border = BorderStroke(1.dp, BorderGlass)) {
                            Text("← Back", color = TextSoft, fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                if (selectedGoal.isBlank()) {
                                    goalErr = true
                                    return@Button
                                }
                                onComplete(
                                    initialProfile.copy(
                                        medicalConditions = selectedMeds.toList(),
                                        medicalNotes = medNotes.trim(),
                                        seatHours = seatHours,
                                        deskSetup = deskSetup,
                                        goal = selectedGoal,
                                        planPath = selectedPlan,
                                        signedIn = true
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = DarkInk)
                        ) {
                            Text("Submit & Launch", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 4. INTERACTIVE POST-SIGNUP APP TUTORIAL (ALL 7 MECHANICS)
// ============================================================================
@Composable
fun SandyTutorialDialog(
    accent: Color,
    onDismiss: () -> Unit
) {
    val steps = remember {
        listOf(
            "Welcome to Sandy!" to "Quick tour of every mechanic inside Sandy Mobile (ver Alpha).",
            "Left Rail & Scroll Circle" to "Tap any icon on the left bar—the glowing circle glides while the app scrolls right to that screen.",
            "Home Menu Hub" to "First launch after sign in opens Menu so you can jump anywhere or replay this tutorial.",
            "Devices Manager" to "Tap the red Add Device button to pair Bluetooth sensors. Use the top-left (⋮) menu on each beveled bar for Preferences, Rename, or Delete.",
            "Minimal Live Interface" to "Shows only Camera, Live Reading (GOOD / BAD), center circle start button, and Choose device dropdown. Colab YOLOv5 & recording run silently in the backend.",
            "Sandy AI Chatbot" to "Auto-wakes Colab Qwen3.6-35B and strictly stays on-topic for posture, ergonomics, and spine wellness.",
            "Reports & Profile Bar" to "Tap Daily/Weekly/Monthly/Yearly boxes for Day-1 comparisons, YOLO→Qwen summaries & recorded media. Top-right avatar holds User Profile & Sign Out."
        )
    }
    var idx by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0B1122),
        titleContentColor = TextHi,
        textContentColor = TextSoft,
        title = {
            Column {
                Text(
                    "TUTORIAL ${idx + 1} / ${steps.size}",
                    color = accent,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(Modifier.height(4.dp))
                Text(steps[idx].first, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column {
                Text(steps[idx].second, fontSize = 12.sp, lineHeight = 18.sp, color = Color(0xFFCBD5E1))
                Spacer(Modifier.height(14.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    steps.indices.forEach { i ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .height(6.dp)
                                .width(if (i == idx) 18.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (i == idx) accent else Color(0x33FFFFFF))
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (idx < steps.lastIndex) idx++ else onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = DarkInk)
            ) {
                Text(if (idx == steps.lastIndex) "Done" else "Next →", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = { if (idx > 0) idx-- else onDismiss() }) {
                Text(if (idx > 0) "Back" else "Skip", color = TextSoft, fontSize = 12.sp)
            }
        }
    )
}

// ============================================================================
// 5. SECTION 1: MENU (FIRST-LAUNCH MOBILE HOME HUB)
// ============================================================================
@Composable
fun SandyMenuSection(
    accent: Color,
    profile: SandyUserProfile,
    devices: List<PostureDeviceItem>,
    yoloReady: Boolean,
    qwenReady: Boolean,
    onNavigate: (SandySection) -> Unit,
    onReplayTutorial: () -> Unit
) {
    val connectedNames = devices.filter { it.status == DeviceConnState.CONNECTED }.joinToString(", ") { it.name }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 12.dp, end = 12.dp, top = 56.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("HOME MENU", color = accent, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Text(
            text = "Hi, ${profile.displayName.ifBlank { "Sandy User" }}",
            color = TextHi,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusChip(
                text = if (yoloReady) "YOLOv5: Ready" else "YOLOv5: Auto",
                color = if (yoloReady) NeonGreen else accent
            )
            StatusChip(
                text = if (qwenReady) "Qwen3.6: Ready" else "Qwen3.6: Auto",
                color = if (qwenReady) NeonGreen else accent
            )
        }

        // Active Plan Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = GlassBg,
            border = BorderStroke(1.dp, accent.copy(alpha = 0.34f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("ACTIVE PLAN", color = accent, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text(profile.goal, color = TextHi, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "Bluetooth: ${connectedNames.ifBlank { "No device connected" }}",
                    color = TextSoft,
                    fontSize = 11.sp
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onNavigate(SandySection.INTERFACE) },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = DarkInk)
                    ) {
                        Text("Start Live", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = onReplayTutorial,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        border = BorderStroke(1.dp, BorderGlass)
                    ) {
                        Text("Tutorial", color = TextHi, fontSize = 12.sp)
                    }
                }
            }
        }

        // Compact 2-column quick jump grid
        val quickTiles = listOf(
            Triple(SandySection.DEVICES, "Devices", "Bluetooth pair & history"),
            Triple(SandySection.INTERFACE, "Interface", "Camera & live reading"),
            Triple(SandySection.CHATBOT, "AI Chatbot", "Qwen3.6 posture coach"),
            Triple(SandySection.REPORT, "Reports", "Media & Day-1 analysis"),
            Triple(SandySection.SETTINGS, "Settings", "ver Alpha & themes")
        )
        quickTiles.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                pair.forEach { (sec, title, desc) ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigate(sec) },
                        shape = RoundedCornerShape(14.dp),
                        color = SubGlassBg,
                        border = BorderStroke(1.dp, BorderGlass)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(sec.iconGlyph, color = accent, fontSize = 18.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(title, color = TextHi, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(desc, color = TextSoft, fontSize = 10.sp, maxLines = 2)
                        }
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatusChip(text: String, color: Color) {
    Surface(
        shape = CircleShape,
        color = color.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.45f))
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// ============================================================================
// 6. SECTION 2: DEVICES (RED ADD DEVICE + HOW TO ADD + SIDEWAYS BEVELED LIST + TOP-LEFT 3-DOT MENU)
// ============================================================================
@Composable
fun SandyDevicesSection(
    accent: Color,
    devices: List<PostureDeviceItem>,
    bleStatusText: String,
    onScanBle: () -> Unit,
    onAddManualDevice: (name: String, model: String, dateMade: String, streamUrl: String) -> Unit,
    onToggleConnect: (String) -> Unit,
    onRenameDevice: (String, String) -> Unit,
    onDeleteDevice: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showHowToDialog by remember { mutableStateOf(false) }
    var prefsDevice by remember { mutableStateOf<PostureDeviceItem?>(null) }
    var renameDevice by remember { mutableStateOf<PostureDeviceItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 12.dp, end = 12.dp, top = 56.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("BLUETOOTH HARDWARE", color = accent, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Text("Devices", color = TextHi, fontSize = 22.sp, fontWeight = FontWeight.Bold)

        // Red "Add Device" button + "How to Add Device" right beside it
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(11.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonRed, contentColor = Color.White),
                modifier = Modifier.weight(1f)
            ) {
                Text("+ Add Device", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
            }
            OutlinedButton(
                onClick = { showHowToDialog = true },
                shape = RoundedCornerShape(11.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                border = BorderStroke(1.dp, BorderGlass),
                modifier = Modifier.weight(1f)
            ) {
                Text("How to Add Device", color = TextHi, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        if (bleStatusText.isNotBlank()) {
            Text(bleStatusText, color = accent, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }

        // Empty State: "No device"
        if (devices.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                shape = RoundedCornerShape(16.dp),
                color = SubGlassBg,
                border = BorderStroke(1.dp, BorderGlass)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("ᛒ", color = TextDim, fontSize = 34.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("No device", color = TextHi, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "No device history yet. Tap the red Add Device button above to pair.",
                        color = TextSoft,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Sideways Beveled List of all devices
            devices.forEach { dev ->
                var menuOpen by remember(dev.id) { mutableStateOf(false) }
                val stateColor = when (dev.status) {
                    DeviceConnState.CONNECTED -> NeonGreen
                    DeviceConnState.PAIRING -> NeonAmber
                    DeviceConnState.DISCONNECTED -> TextSoft
                }
                val bevelShape = CutCornerShape(topStart = 14.dp, bottomEnd = 14.dp, topEnd = 4.dp, bottomStart = 4.dp)

                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = bevelShape,
                        color = GlassBg,
                        border = BorderStroke(1.2.dp, stateColor.copy(alpha = 0.55f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 38.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    dev.name,
                                    color = TextHi,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    dev.model,
                                    color = TextSoft,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(5.dp))
                                StatusChip(text = dev.status.label, color = stateColor)
                            }
                            Spacer(Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = { onToggleConnect(dev.id) },
                                enabled = dev.status != DeviceConnState.PAIRING,
                                shape = RoundedCornerShape(9.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                border = BorderStroke(1.dp, stateColor.copy(alpha = 0.55f))
                            ) {
                                Text(
                                    text = if (dev.status == DeviceConnState.CONNECTED) "Disconnect" else if (dev.status == DeviceConnState.PAIRING) "Pairing..." else "Connect",
                                    color = TextHi,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // TOP-LEFT 3-DOT BUTTON + DROPDOWN (Preferences, Rename, Delete)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 6.dp, top = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(Color(0xDD050814))
                                .border(1.dp, BorderGlass, RoundedCornerShape(7.dp))
                                .clickable { menuOpen = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⋮", color = TextHi, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        DropdownMenu(
                            expanded = menuOpen,
                            onDismissRequest = { menuOpen = false },
                            modifier = Modifier.background(Color(0xFF0B1122))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Preferences", color = TextHi, fontSize = 12.sp) },
                                onClick = {
                                    menuOpen = false
                                    prefsDevice = dev
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Rename", color = TextHi, fontSize = 12.sp) },
                                onClick = {
                                    menuOpen = false
                                    renameDevice = dev
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = NeonRed, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    menuOpen = false
                                    onDeleteDevice(dev.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Device Dialog
    if (showAddDialog) {
        var devName by remember { mutableStateOf("") }
        var devModel by remember { mutableStateOf("ESP32-S3-WROOM-1-N16R8") }
        var devMade by remember { mutableStateOf("2026-10") }
        var devStream by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = Color(0xFF0B1122),
            title = { Text("Add Posture Device", color = TextHi, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            onScanBle()
                            showAddDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed, contentColor = Color.White)
                    ) {
                        Text("Scan Bluetooth ESP32-S3", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Text("Or register manually:", color = TextSoft, fontSize = 11.sp)
                    OutlinedTextField(
                        value = devName,
                        onValueChange = { devName = it },
                        label = { Text("Device Name", fontSize = 11.sp) },
                        singleLine = true,
                        colors = sandyFieldColors(accent),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = devModel,
                        onValueChange = { devModel = it },
                        label = { Text("Model Type", fontSize = 11.sp) },
                        singleLine = true,
                        colors = sandyFieldColors(accent),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = devMade,
                        onValueChange = { devMade = it },
                        label = { Text("Date Made (YYYY-MM)", fontSize = 11.sp) },
                        singleLine = true,
                        colors = sandyFieldColors(accent),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = devStream,
                        onValueChange = { devStream = it },
                        label = { Text("Optional Wi-Fi Stream URL", fontSize = 11.sp) },
                        singleLine = true,
                        colors = sandyFieldColors(accent),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (devName.isNotBlank()) {
                            onAddManualDevice(devName.trim(), devModel.trim(), devMade.trim(), devStream.trim())
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = DarkInk)
                ) {
                    Text("Pair & Add", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel", color = TextSoft) }
            }
        )
    }

    // How to Add Device Guide Dialog
    if (showHowToDialog) {
        AlertDialog(
            onDismissRequest = { showHowToDialog = false },
            containerColor = Color(0xFF0B1122),
            title = { Text("How to Add a Device", color = TextHi, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. Power on your Sandy / PostureAI ESP32-S3 via USB-C until the LED blinks cyan.", color = TextSoft, fontSize = 12.sp)
                    Text("2. Turn on Android Bluetooth & grant Nearby Devices permission.", color = TextSoft, fontSize = 12.sp)
                    Text("3. Tap the red Add Device button → Scan Bluetooth ESP32-S3 (or enter manually).", color = TextSoft, fontSize = 12.sp)
                    Text("4. Once Connected, open the Interface tab and select it from the Choose device dropdown.", color = TextSoft, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHowToDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = DarkInk)
                ) { Text("Got It", fontWeight = FontWeight.Bold) }
            }
        )
    }

    // Device Preferences Dialog (detailed model specs)
    prefsDevice?.let { dev ->
        AlertDialog(
            onDismissRequest = { prefsDevice = null },
            containerColor = Color(0xFF0B1122),
            title = { Text("Device Preferences", color = TextHi, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        "Name" to dev.name,
                        "Model Type" to dev.model,
                        "Date Made" to dev.dateMade,
                        "Firmware" to dev.firmware,
                        "Protocol" to dev.protocol,
                        "Hardware ID" to dev.hardwareId,
                        "Status" to dev.status.label,
                        "Last Connected" to dev.lastConnected
                    ).forEach { (k, v) ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(k, color = TextSoft, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text(v, color = TextHi, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { prefsDevice = null },
                    colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = DarkInk)
                ) { Text("Close", fontWeight = FontWeight.Bold) }
            }
        )
    }

    // Rename Device Dialog
    renameDevice?.let { dev ->
        var newName by remember(dev.id) { mutableStateOf(dev.name) }
        AlertDialog(
            onDismissRequest = { renameDevice = null },
            containerColor = Color(0xFF0B1122),
            title = { Text("Rename Device", color = TextHi, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New Device Name", fontSize = 11.sp) },
                    singleLine = true,
                    colors = sandyFieldColors(accent),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) onRenameDevice(dev.id, newName.trim())
                        renameDevice = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = DarkInk)
                ) { Text("Save", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { renameDevice = null }) { Text("Cancel", color = TextSoft) }
            }
        )
    }
}

// ============================================================================
// 7. SECTION 3: MINIMAL LIVE INTERFACE (ONLY CAMERA + GOOD/BAD + CIRCLE ICON + CHOOSE DEVICE)
// ============================================================================
@Composable
fun SandyLiveInterfaceSection(
    accent: Color,
    isMonitoring: Boolean,
    liveBitmap: Bitmap?,
    liveReading: String, // "GOOD" | "BAD" | "WAITING"
    connectedDevices: List<PostureDeviceItem>,
    selectedDeviceId: String,
    onSelectDevice: (String) -> Unit,
    onToggleMonitor: () -> Unit
) {
    val readingColor = when (liveReading) {
        "GOOD" -> NeonGreen
        "BAD" -> NeonRed
        else -> TextSoft
    }

    val selectedName = connectedDevices.firstOrNull { it.id == selectedDeviceId }?.name
        ?: connectedDevices.firstOrNull()?.name
        ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 12.dp, end = 12.dp, top = 56.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. Live Camera Feed ONLY
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF03060F),
            border = BorderStroke(
                2.dp,
                when (liveReading) {
                    "GOOD" -> NeonGreen.copy(alpha = 0.75f)
                    "BAD" -> NeonRed.copy(alpha = 0.85f)
                    else -> accent.copy(alpha = 0.38f)
                }
            )
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (liveBitmap != null) {
                    Image(
                        bitmap = liveBitmap.asImageBitmap(),
                        contentDescription = "Live Posture Camera",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                        Text("◉", color = accent.copy(alpha = 0.65f), fontSize = 36.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (isMonitoring) "Receiving camera stream..." else "Camera Standby",
                            color = TextHi,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        // 2. Live Posture Reading (GOOD / BAD)
        Surface(
            shape = CircleShape,
            color = GlassBg,
            border = BorderStroke(1.4.dp, readingColor.copy(alpha = 0.65f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 26.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(11.dp)
                        .clip(CircleShape)
                        .background(readingColor)
                )
                Text(
                    text = liveReading,
                    color = readingColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // 3. Centered Circular Interface Icon Below
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            (if (isMonitoring) NeonGreen else accent).copy(alpha = 0.28f),
                            GlassBg
                        )
                    )
                )
                .border(2.dp, if (isMonitoring) NeonGreen else accent, CircleShape)
                .clickable { onToggleMonitor() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isMonitoring) "■" else "◉",
                color = if (isMonitoring) NeonGreen else accent,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(14.dp))

        // 4. "Choose device" label + Bluetooth connected device dropdown
        Text(
            text = "CHOOSE DEVICE",
            color = TextSoft,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        Spacer(Modifier.height(6.dp))
        CompactDropdownBox(
            label = if (connectedDevices.isEmpty()) "No Bluetooth device connected" else "Select device",
            value = selectedName,
            options = connectedDevices.map { it.name },
            accent = accent,
            modifier = Modifier.fillMaxWidth(0.86f)
        ) { chosenName ->
            connectedDevices.firstOrNull { it.name == chosenName }?.let { onSelectDevice(it.id) }
        }
    }
}

// ============================================================================
// 8. SECTION 4: AI CHATBOT (QWEN3.6-35B + EXACT STRICT OFF-TOPIC GUARDRAIL)
// ============================================================================
@Composable
fun SandyChatbotSection(
    accent: Color,
    profile: SandyUserProfile,
    todayScore: Int?,
    onWakeQwen: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val messages = remember {
        mutableStateListOf(
            AiChatMsg(
                "assistant",
                "Hello! I'm Sandy AI (Qwen3.6-35B). Ask me about your posture, desk ergonomics, stretches, or spine wellness!"
            )
        )
    }
    var inputText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { onWakeQwen() }

    fun sendMsg(prompt: String) {
        val clean = prompt.trim()
        if (clean.isBlank() || isSending) return
        inputText = ""
        messages.add(AiChatMsg("user", clean))
        isSending = true
        scope.launch {
            val reply = PostureNetworkClient.askQwenChat(
                history = messages.toList(),
                currentScore = todayScore ?: 80,
                userGoal = profile.goal,
                medicalNotes = profile.combinedMedicalText
            )
            messages.add(AiChatMsg("assistant", reply))
            isSending = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 12.dp, end = 12.dp, top = 56.dp, bottom = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("QWEN3.6-35B COACH", color = accent, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("AI Chatbot", color = TextHi, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            StatusChip(text = "Score: ${todayScore ?: "--"}", color = NeonGreen)
        }
        Spacer(Modifier.height(6.dp))

        // Quick prompt chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "2-min neck stretch",
                "Monitor & chair height",
                "Why do I slouch?"
            ).forEach { chip ->
                Surface(
                    modifier = Modifier.clickable { sendMsg(chip) },
                    shape = CircleShape,
                    color = SubGlassBg,
                    border = BorderStroke(1.dp, BorderGlass)
                ) {
                    Text(chip, color = TextHi, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        // Messages feed
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = GlassBg,
            border = BorderStroke(1.dp, BorderGlass)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                messages.forEach { msg ->
                    val isUser = msg.role == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isUser) accent.copy(alpha = 0.22f) else SubGlassBg,
                            border = BorderStroke(1.dp, if (isUser) accent.copy(alpha = 0.5f) else BorderGlass),
                            modifier = Modifier.fillMaxWidth(0.86f)
                        ) {
                            Text(
                                text = msg.content,
                                color = TextHi,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
                if (isSending) {
                    Text("Sandy AI is thinking...", color = accent, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Ask posture or spine question...", fontSize = 12.sp, color = TextDim) },
                singleLine = true,
                colors = sandyFieldColors(accent),
                shape = RoundedCornerShape(11.dp),
                modifier = Modifier.weight(1f)
            )
            Button(
                enabled = !isSending,
                onClick = { sendMsg(inputText) },
                shape = RoundedCornerShape(11.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = DarkInk)
            ) {
                Text("➤", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

// ============================================================================
// 9. SECTION 5: REPORT (4 TIMEFRAME BOXES + LACK-OF-DATA / DAY-1 BOX + YOLO->QWEN + MEDIA GALLERY)
// ============================================================================
@Composable
fun SandyReportSection(
    accent: Color,
    profile: SandyUserProfile,
    analyticsMap: Map<String, DailyAnalyticsRecord>,
    mediaList: List<RecordedMediaEntry>,
    onSyncAnalytics: () -> Unit,
    onDeleteMedia: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var timeframe by remember { mutableStateOf("daily") } // daily | weekly | monthly | yearly
    var mediaTypeFilter by remember { mutableStateOf("all") } // all | image | video
    var qwenReportText by remember { mutableStateOf("Tap Analyze to send YOLOv5 data & Day-1 baseline to Qwen3.6-35B.") }
    var isAnalyzing by remember { mutableStateOf(false) }
    var previewMedia by remember { mutableStateOf<RecordedMediaEntry?>(null) }

    val fmt = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val todayKey = remember { fmt.format(Date()) }

    // Build timeframe slots & detect missing days
    val slots: List<Pair<String, Int?>> = remember(timeframe, analyticsMap) {
        val cal = Calendar.getInstance()
        when (timeframe) {
            "daily" -> {
                val r = analyticsMap[todayKey]
                listOf("Today" to if (r != null && r.goodFrames + r.badFrames > 0) r.backScore else null)
            }
            "weekly" -> {
                val dayFmt = SimpleDateFormat("EEE", Locale.US)
                (6 downTo 0).map { offset ->
                    val c = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -offset) }
                    val k = fmt.format(c.time)
                    val r = analyticsMap[k]
                    dayFmt.format(c.time) to if (r != null && r.goodFrames + r.badFrames > 0) r.backScore else null
                }
            }
            "monthly" -> {
                (3 downTo 0).map { wk ->
                    var g = 0; var b = 0
                    for (d in (wk * 7 + 6) downTo (wk * 7)) {
                        val c = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -d) }
                        val r = analyticsMap[fmt.format(c.time)]
                        if (r != null) { g += r.goodFrames; b += r.badFrames }
                    }
                    val total = g + b
                    (if (wk == 0) "Now" else "W-$wk") to if (total > 0) ((g * 100) / total) else null
                }
            }
            else -> {
                val moFmt = SimpleDateFormat("MMM", Locale.US)
                (5 downTo 0).map { m ->
                    val c = (cal.clone() as Calendar).apply { add(Calendar.MONTH, -m) }
                    val prefix = SimpleDateFormat("yyyy-MM", Locale.US).format(c.time)
                    val recs = analyticsMap.values.filter { it.date.startsWith(prefix) }
                    val g = recs.sumOf { it.goodFrames }
                    val b = recs.sumOf { it.badFrames }
                    val total = g + b
                    moFmt.format(c.time) to if (total > 0) ((g * 100) / total) else null
                }
            }
        }
    }

    val loggedSlots = slots.count { it.second != null }
    val missingSlots = slots.size - loggedSlots
    val scoredValues = slots.mapNotNull { it.second }
    val currentAvg = if (scoredValues.isNotEmpty()) scoredValues.average().toInt() else null

    // Earliest recorded day (Day 1 Baseline)
    val firstDayRec = remember(analyticsMap) {
        analyticsMap.entries
            .filter { it.value.goodFrames + it.value.badFrames > 0 }
            .minByOrNull { it.key }
            ?.value
    }

    val totalGood = analyticsMap.values.sumOf { it.goodFrames }
    val totalBad = analyticsMap.values.sumOf { it.badFrames }
    val totalSlouch = analyticsMap.values.sumOf { it.slouchEvents }

    val scoreColor = when {
        currentAvg == null -> TextSoft
        currentAvg >= 80 -> NeonGreen
        currentAvg >= 55 -> NeonAmber
        else -> NeonRed
    }
    val animSweep by animateFloatAsState(
        targetValue = if (currentAvg != null) (currentAvg / 100f) * 360f else 0f,
        animationSpec = tween(550),
        label = "scoreArc"
    )

    // Filter recorded media by timeframe + type
    val filteredMedia = remember(mediaList, timeframe, mediaTypeFilter) {
        val now = System.currentTimeMillis()
        val maxAge = when (timeframe) {
            "daily" -> 86_400_000L
            "weekly" -> 86_400_000L * 7
            "monthly" -> 86_400_000L * 30
            else -> 86_400_000L * 365
        }
        mediaList.filter { m ->
            val inPeriod = if (timeframe == "daily") m.dateKey == todayKey else (now - m.timestampMs <= maxAge)
            val matchType = mediaTypeFilter == "all" || m.mediaType == mediaTypeFilter
            inPeriod && matchType
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 12.dp, end = 12.dp, top = 56.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("YOLO → QWEN → REPORT", color = accent, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("Posture Report", color = TextHi, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onSyncAnalytics,
                shape = RoundedCornerShape(9.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                border = BorderStroke(1.dp, BorderGlass)
            ) {
                Text("Sync", color = accent, fontSize = 11.sp)
            }
        }

        // 4 Clickable Timeframe Selection Boxes
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("daily" to "Daily", "weekly" to "Weekly", "monthly" to "Monthly", "yearly" to "Yearly").forEach { (key, label) ->
                val active = timeframe == key
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { timeframe = key },
                    shape = RoundedCornerShape(11.dp),
                    color = if (active) accent.copy(alpha = 0.2f) else SubGlassBg,
                    border = BorderStroke(1.dp, if (active) accent else BorderGlass)
                ) {
                    Text(
                        text = label,
                        color = if (active) TextHi else TextSoft,
                        fontSize = 11.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }

        // Score Ring + Trend Bars Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = GlassBg,
            border = BorderStroke(1.dp, BorderGlass),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(modifier = Modifier.size(92.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = Color(0x1FFFFFFF),
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 18f, cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = scoreColor,
                                startAngle = -90f,
                                sweepAngle = animSweep,
                                useCenter = false,
                                style = Stroke(width = 18f, cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currentAvg?.toString() ?: "--",
                                color = scoreColor,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = when {
                                    currentAvg == null -> "NO DATA"
                                    currentAvg >= 80 -> "GOOD"
                                    currentAvg >= 55 -> "AVG"
                                    else -> "BAD"
                                },
                                color = scoreColor,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Logged: $loggedSlots / ${slots.size}", color = accent, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Text("Good Frames: $totalGood", color = NeonGreen, fontSize = 11.sp)
                        Text("Bad / Slouch: $totalBad", color = NeonRed, fontSize = 11.sp)
                        Text("Slouch Alerts: $totalSlouch", color = NeonAmber, fontSize = 11.sp)
                    }
                }

                // Period bars (dashed placeholder when lacking data)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(78.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    slots.forEach { (label, score) ->
                        val frac = if (score != null) (score / 100f).coerceIn(0.14f, 1f) else 0.25f
                        val c = when {
                            score == null -> Color(0x3394A3B8)
                            score >= 80 -> NeonGreen
                            score >= 55 -> NeonAmber
                            else -> NeonRed
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(score?.toString() ?: "—", color = if (score != null) c else TextDim, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.62f)
                                    .height((48 * frac).dp)
                                    .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                                    .background(c)
                            )
                            Text(label, color = TextSoft, fontSize = 9.sp, maxLines = 1)
                        }
                    }
                }
            }
        }

        // First-Day Baseline & Missing-Data Comparison Box
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = GlassBg,
            border = BorderStroke(1.dp, accent.copy(alpha = 0.28f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("DAY-1 BASELINE & DATA CHECK", color = accent, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                if (timeframe != "daily" && missingSlots > 0) {
                    Text(
                        "Partial data ($missingSlots of ${slots.size} periods lack logs). Comparing directly against your Day-1 baseline:",
                        color = NeonAmber,
                        fontSize = 11.sp
                    )
                }
                if (firstDayRec != null && currentAvg != null) {
                    val delta = currentAvg - firstDayRec.backScore
                    val deltaColor = if (delta >= 0) NeonGreen else NeonRed
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Day 1 (${firstDayRec.date}): ${firstDayRec.backScore}/100", color = TextSoft, fontSize = 11.sp)
                        Text(
                            if (delta >= 0) "+$delta% Improved" else "$delta% Dropped",
                            color = deltaColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        "Start monitoring in Live Interface to record Day-1 posture baseline.",
                        color = TextSoft,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // YOLOv5 -> Qwen3.6 -> Report AI Box
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = GlassBg,
            border = BorderStroke(1.dp, BorderGlass),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Qwen3.6 Report Summary", color = TextHi, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("YOLOv5 → Qwen3.6 → Report", color = accent, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                    Button(
                        enabled = !isAnalyzing,
                        onClick = {
                            isAnalyzing = true
                            qwenReportText = "Sending YOLOv5 telemetry to Qwen3.6-35B..."
                            scope.launch {
                                val cur = currentAvg ?: 0
                                val prev = firstDayRec?.backScore ?: cur
                                qwenReportText = PostureNetworkClient.askQwenAnalyzeReport(
                                    timeframe = timeframe,
                                    currentScore = cur,
                                    previousScore = prev,
                                    goodFrames = totalGood,
                                    badFrames = totalBad,
                                    slouchEvents = totalSlouch,
                                    userGoal = profile.goal,
                                    medicalNotes = profile.combinedMedicalText,
                                    question = "Analyze my $timeframe posture report ($loggedSlots/${slots.size} periods logged, $missingSlots lacking data) and compare to Day 1 ($prev/100)."
                                )
                                isAnalyzing = false
                            }
                        },
                        shape = RoundedCornerShape(9.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = DarkInk)
                    ) {
                        Text(if (isAnalyzing) "..." else "Analyze", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
                Text(qwenReportText, color = Color(0xFFCBD5E1), fontSize = 11.sp, lineHeight = 16.sp)
            }
        }

        // Recorded Images & Videos Gallery
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = GlassBg,
            border = BorderStroke(1.dp, BorderGlass),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Recorded Media (${filteredMedia.size})", color = TextHi, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("all" to "All", "image" to "Img", "video" to "Vid").forEach { (k, lbl) ->
                            val on = mediaTypeFilter == k
                            Surface(
                                modifier = Modifier.clickable { mediaTypeFilter = k },
                                shape = CircleShape,
                                color = if (on) accent.copy(alpha = 0.22f) else SubGlassBg,
                                border = BorderStroke(1.dp, if (on) accent else BorderGlass)
                            ) {
                                Text(lbl, color = if (on) TextHi else TextSoft, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }
                    }
                }

                if (filteredMedia.isEmpty()) {
                    Text(
                        "No recorded images or clips for $timeframe yet. Live Interface saves them automatically.",
                        color = TextSoft,
                        fontSize = 11.sp
                    )
                } else {
                    filteredMedia.chunked(2).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            pair.forEach { item ->
                                val bmp = remember(item.filePath) { SandyLocalStore.loadBitmapSafe(item.filePath) }
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { previewMedia = item },
                                    shape = RoundedCornerShape(10.dp),
                                    color = SubGlassBg,
                                    border = BorderStroke(1.dp, BorderGlass)
                                ) {
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(82.dp)
                                                .background(Color(0xFF03060F)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (bmp != null) {
                                                Image(
                                                    bitmap = bmp.asImageBitmap(),
                                                    contentDescription = "Recorded Posture",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                            Surface(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(4.dp),
                                                shape = CircleShape,
                                                color = (if (item.posture == "BAD") NeonRed else NeonGreen).copy(alpha = 0.85f)
                                            ) {
                                                Text(
                                                    item.posture,
                                                    color = DarkInk,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = SimpleDateFormat("MM/dd HH:mm", Locale.US).format(Date(item.timestampMs)),
                                            color = TextSoft,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.padding(6.dp)
                                        )
                                    }
                                }
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    previewMedia?.let { item ->
        val bmp = remember(item.filePath) { SandyLocalStore.loadBitmapSafe(item.filePath) }
        AlertDialog(
            onDismissRequest = { previewMedia = null },
            containerColor = Color(0xFF0B1122),
            title = { Text("${item.posture} • ${item.deviceName}", color = TextHi, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (bmp != null) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Preview",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    }
                    Text(
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(item.timestampMs)),
                        color = TextSoft,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { previewMedia = null },
                    colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = DarkInk)
                ) { Text("Close") }
            },
            dismissButton = {
                TextButton(onClick = {
                    onDeleteMedia(item.id)
                    previewMedia = null
                }) { Text("Delete", color = NeonRed) }
            }
        )
    }
}

// ============================================================================
// 10. SECTION 6: SETTINGS (APP VERSION: VER ALPHA, COLOR THEMES, BIOFEEDBACK)
// ============================================================================
@Composable
fun SandySettingsSection(
    settings: SandyMobileSettings,
    onUpdateSettings: (SandyMobileSettings) -> Unit,
    onReplayTutorial: () -> Unit
) {
    val accent = settings.theme.color

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 12.dp, end = 12.dp, top = 56.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("PREFERENCES", color = accent, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Text("Settings", color = TextHi, fontSize = 22.sp, fontWeight = FontWeight.Bold)

        // App Version Card (ver Alpha)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = GlassBg,
            border = BorderStroke(1.dp, accent.copy(alpha = 0.28f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("App Version", color = TextHi, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Sandy Mobile (${BackendConfig.APP_SEMVER})", color = TextSoft, fontSize = 11.sp)
                    }
                    StatusChip(text = BackendConfig.APP_VERSION_LABEL, color = accent)
                }
                OutlinedButton(
                    onClick = onReplayTutorial,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BorderGlass),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Replay App Tutorial", color = TextHi, fontSize = 12.sp)
                }
            }
        }

        // Color Theme Picker
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = GlassBg,
            border = BorderStroke(1.dp, BorderGlass),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Color Theme", color = TextHi, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                SandyAccentTheme.entries.chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        pair.forEach { t ->
                            val active = settings.theme == t
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onUpdateSettings(settings.copy(theme = t)) },
                                shape = RoundedCornerShape(11.dp),
                                color = if (active) t.color.copy(alpha = 0.16f) else SubGlassBg,
                                border = BorderStroke(1.dp, if (active) t.color else BorderGlass)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(t.color)
                                    )
                                    Text(t.displayName, color = TextHi, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Biofeedback, Auto-Record & Colab Auto-Wake Switches
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = GlassBg,
            border = BorderStroke(1.dp, BorderGlass),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SettingToggleRow("740 Hz Slouch Alert & Vibrate", settings.chimeAndVibrate, accent) {
                    onUpdateSettings(settings.copy(chimeAndVibrate = it))
                }
                SettingToggleRow("Auto-Record Snapshots for Report", settings.autoRecordMedia, accent) {
                    onUpdateSettings(settings.copy(autoRecordMedia = it))
                }
                SettingToggleRow("Auto-Wake Colab YOLOv5 & Qwen", settings.autoWakeColab, accent) {
                    onUpdateSettings(settings.copy(autoWakeColab = it))
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(label: String, checked: Boolean, accent: Color, onChecked: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextHi, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(checkedThumbColor = DarkInk, checkedTrackColor = accent)
        )
    }
}

// ============================================================================
// 11. USER PROFILE DIALOG (OPENED FROM TOP-RIGHT PFP DROPDOWN)
// ============================================================================
@Composable
fun SandyProfileDialog(
    accent: Color,
    profile: SandyUserProfile,
    onDismiss: () -> Unit,
    onSave: (SandyUserProfile) -> Unit
) {
    var name by remember { mutableStateOf(profile.displayName) }
    var handle by remember { mutableStateOf(profile.username) }
    var dob by remember { mutableStateOf(profile.dob) }
    var goal by remember { mutableStateOf(profile.goal) }
    var med by remember { mutableStateOf(profile.medicalNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0B1122),
        title = {
            Column {
                Text("User Profile", color = TextHi, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(profile.email, color = accent, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name", fontSize = 11.sp) },
                    singleLine = true,
                    colors = sandyFieldColors(accent),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = handle,
                    onValueChange = { handle = it.replace(" ", "") },
                    label = { Text("Username", fontSize = 11.sp) },
                    singleLine = true,
                    colors = sandyFieldColors(accent),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it },
                    label = { Text("Date of Birth (YYYY-MM-DD)", fontSize = 11.sp) },
                    singleLine = true,
                    colors = sandyFieldColors(accent),
                    modifier = Modifier.fillMaxWidth()
                )
                CompactDropdownBox(
                    label = "Goal",
                    value = goal,
                    options = listOf(
                        "Relieve Neck & Back Pain",
                        "Build Upright Sitting Habit",
                        "Improve Focus & Ergonomics",
                        "Gentle Posture Recovery"
                    ),
                    accent = accent,
                    modifier = Modifier.fillMaxWidth()
                ) { goal = it }
                OutlinedTextField(
                    value = med,
                    onValueChange = { med = it },
                    label = { Text("Medical Notes", fontSize = 11.sp) },
                    colors = sandyFieldColors(accent),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        profile.copy(
                            displayName = name.trim().ifBlank { profile.displayName },
                            username = handle.trim().removePrefix("@").ifBlank { profile.username },
                            dob = dob.trim(),
                            goal = goal,
                            medicalNotes = med.trim()
                        )
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = DarkInk)
            ) { Text("Save", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSoft) }
        }
    )
}

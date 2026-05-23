package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.DesignTokens
import com.example.formatShortDate
import com.example.viewmodel.FinanceViewModel

@Composable
fun AuthSyncDialog(
    viewModel: FinanceViewModel,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val syncStatus by viewModel.syncStatus.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()

    // Local input states
    var isRegisterMode by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var serverUrlInput by remember { mutableStateOf(serverUrl) }
    var passwordVisible by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val surfaceColor = if (isDark) DesignTokens.DarkSurfaceCard else DesignTokens.LightSurfaceCard
    val bgCol = if (isDark) DesignTokens.DarkSlateBg else DesignTokens.LightSlateBg
    val borderCol = if (isDark) DesignTokens.DarkBorder else DesignTokens.LightBorder
    val textPrimary = if (isDark) DesignTokens.DarkTextPrimary else DesignTokens.LightTextPrimary
    val textSecondary = if (isDark) DesignTokens.DarkTextSecondary else DesignTokens.LightTextSecondary

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .widthIn(max = 480.dp)
                    .clickable(enabled = false) {}, // prevent click-propagation
                shape = RoundedCornerShape(24.dp),
                border = borderCol?.let { androidx.compose.foundation.BorderStroke(1.dp, it) },
                colors = CardDefaults.cardColors(containerColor = surfaceColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Header Area
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.CloudSync,
                                contentDescription = null,
                                tint = DesignTokens.RoyalPurple,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Хмарна синхронізація",
                                color = textPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Закрити",
                                tint = textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (syncStatus is FinanceViewModel.SyncStatus.Unauthorized) {
                        // TAB SELECTOR for Login / Register
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgCol)
                                .border(1.dp, borderCol, RoundedCornerShape(12.dp)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (!isRegisterMode) DesignTokens.RoyalPurple else Color.Transparent)
                                    .clickable {
                                        isRegisterMode = false
                                        validationError = null
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Вхід",
                                    color = if (!isRegisterMode) Color.White else textSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isRegisterMode) DesignTokens.RoyalPurple else Color.Transparent)
                                    .clickable {
                                        isRegisterMode = true
                                        validationError = null
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Реєстрація",
                                    color = if (isRegisterMode) Color.White else textSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // FIELDS
                        if (isRegisterMode) {
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it; validationError = null },
                                label = { Text("Ім'я") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_name_field"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DesignTokens.RoyalPurple,
                                    focusedLabelColor = DesignTokens.RoyalPurple
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it; validationError = null },
                            label = { Text("Електронна пошта (Email)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_email_field"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DesignTokens.RoyalPurple,
                                focusedLabelColor = DesignTokens.RoyalPurple
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it; validationError = null },
                            label = { Text("Пароль") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = "Показати пароль",
                                        tint = textSecondary
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_password_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DesignTokens.RoyalPurple,
                                focusedLabelColor = DesignTokens.RoyalPurple
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = serverUrlInput,
                            onValueChange = { serverUrlInput = it; validationError = null },
                            label = { Text("Адреса вашого сервера") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Dns,
                                    contentDescription = null,
                                    tint = textSecondary
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_server_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DesignTokens.RoyalPurple,
                                focusedLabelColor = DesignTokens.RoyalPurple
                            )
                        )

                        // VALIDATION FEEDBACK
                        AnimatedVisibility(visible = validationError != null) {
                            Row(
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Cancel,
                                    contentDescription = null,
                                    tint = DesignTokens.AlertRose,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = validationError ?: "",
                                    color = DesignTokens.AlertRose,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // SUBMIT ACTION BAR
                        Button(
                            onClick = {
                                if (emailInput.trim().isEmpty() || !emailInput.contains("@")) {
                                    validationError = "Будь ласка, вкажіть правильний email"
                                    return@Button
                                }
                                if (passwordInput.length < 6) {
                                    validationError = "Пароль повинен містити щонайменше 6 символів"
                                    return@Button
                                }
                                if (isRegisterMode && nameInput.trim().isEmpty()) {
                                    validationError = "Будь ласка, вкажіть ваше ім'я"
                                    return@Button
                                }
                                if (isRegisterMode) {
                                    viewModel.registerUser(
                                        name = nameInput.trim(),
                                        email = emailInput.trim(),
                                        password = passwordInput,
                                        customUrl = serverUrlInput.trim(),
                                        onSuccess = { onDismiss() },
                                        onError = { validationError = it }
                                    )
                                } else {
                                    viewModel.loginUser(
                                        email = emailInput.trim(),
                                        password = passwordInput,
                                        customUrl = serverUrlInput.trim(),
                                        onSuccess = { onDismiss() },
                                        onError = { validationError = it }
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DesignTokens.RoyalPurple),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("auth_submit_button")
                        ) {
                            Text(
                                text = if (isRegisterMode) "Створити обліковий запис" else "Увійти в кабінет",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text("Продовжити локально (Room Database)")
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Google Quick login option
                        Button(
                            onClick = {
                                viewModel.loginWithGoogle(
                                    onSuccess = { onDismiss() },
                                    onError = { validationError = it }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF1E293B) else Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, borderCol),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("google_login_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AccountCircle,
                                    contentDescription = null,
                                    tint = DesignTokens.RoyalPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Продовжити з акаунтом Google",
                                    color = if (isDark) Color.White else Color(0xFF374151),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        // LOGGED IN VIEW & STATUS CARD
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = bgCol),
                            shape = RoundedCornerShape(16.dp),
                            border = borderCol?.let { androidx.compose.foundation.BorderStroke(1.dp, it) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(DesignTokens.RoyalPurple.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Person,
                                            contentDescription = null,
                                            tint = DesignTokens.RoyalPurple,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = userName ?: "Користувач системи",
                                            color = textPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = userEmail ?: "family@example.com",
                                            color = textSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = borderCol ?: Color.Gray, thickness = 0.5.dp)
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Адреса хмари:",
                                        color = textSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = serverUrl,
                                        color = textPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // ACTIVE SYNC STATE DETAILS
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    when (syncStatus) {
                                        is FinanceViewModel.SyncStatus.Syncing -> DesignTokens.WarningAmber.copy(alpha = 0.12f)
                                        is FinanceViewModel.SyncStatus.Synced -> DesignTokens.IncomeGreen.copy(alpha = 0.12f)
                                        is FinanceViewModel.SyncStatus.Error -> DesignTokens.AlertRose.copy(alpha = 0.12f)
                                        else -> Color.Transparent
                                    }
                                )
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val tintColor = when (syncStatus) {
                                is FinanceViewModel.SyncStatus.Syncing -> DesignTokens.WarningAmber
                                is FinanceViewModel.SyncStatus.Synced -> DesignTokens.IncomeGreen
                                is FinanceViewModel.SyncStatus.Error -> DesignTokens.AlertRose
                                else -> textSecondary
                            }
                            val statusIcon = when (syncStatus) {
                                is FinanceViewModel.SyncStatus.Syncing -> Icons.Rounded.Sync
                                is FinanceViewModel.SyncStatus.Synced -> Icons.Rounded.CheckCircle
                                is FinanceViewModel.SyncStatus.Error -> Icons.Rounded.CloudOff
                                else -> Icons.Rounded.Cloud
                            }
                            val statusText = when (val s = syncStatus) {
                                is FinanceViewModel.SyncStatus.Syncing -> s.message
                                is FinanceViewModel.SyncStatus.Synced -> "Синхронізовано з сервером"
                                is FinanceViewModel.SyncStatus.Error -> s.errorMsg
                                else -> "Офлайн"
                            }

                            Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                tint = tintColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Статус підключення",
                                    color = textSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = statusText,
                                    color = textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                if (syncStatus is FinanceViewModel.SyncStatus.Synced) {
                                    val t = (syncStatus as FinanceViewModel.SyncStatus.Synced).lastSyncTime
                                    Text(
                                        text = "Оновлено: ${formatShortDate(t)}",
                                        color = textSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // MANUAL SYNC / FORCE RETRY BUTTON
                        Button(
                            onClick = { viewModel.syncNow() },
                            colors = ButtonDefaults.buttonColors(containerColor = DesignTokens.RoyalPurple),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Синхронізувати зараз",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.signOut()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DesignTokens.AlertRose),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .border(1.dp, DesignTokens.AlertRose.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Logout,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = DesignTokens.AlertRose
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Вийти з облікового запису",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DesignTokens.AlertRose
                            )
                        }
                    }
                }
            }
        }
    }
}

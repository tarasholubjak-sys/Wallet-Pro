package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.ui.AuthSyncDialog
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalDensity
import com.example.data.Account
import com.example.data.Budget
import com.example.data.Transaction
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BudgetReportItem
import com.example.viewmodel.FinanceViewModel
import com.example.viewmodel.FinanceViewModelFactory
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: FinanceViewModel by viewModels {
        FinanceViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeModeState by viewModel.themeMode.collectAsState()
            val isDarkTheme = when (themeModeState) {
                "light" -> false
                "dark" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                // Root Container matching user preferred theme
                val containerColor = if (isDarkTheme) Color(0xFF0B0F19) else Color(0xFFF8FAFB)
                val testTagApp = if (isDarkTheme) "app_dark" else "app_light"

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag(testTagApp),
                    containerColor = containerColor
                ) { innerPadding ->
                    FinanceAppScreen(
                        viewModel = viewModel,
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = {
                            viewModel.updateThemeMode(if (isDarkTheme) "light" else "dark")
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// Aesthetic Theme Palette supporting both Light and Dark elegance
object DesignTokens {
    // Brand Purple Accent
    val RoyalPurple = Color(0xFF6E56CF)
    val RoyalPurpleSoft = Color(0xFFECE9FA)

    // Semantic Status Tones
    val IncomeGreen = Color(0xFF16A34A)       // emerald
    val IncomeGreenSoft = Color(0xFFDCFCE7)
    val WarningAmber = Color(0xFFD97706)      // amber
    val WarningAmberSoft = Color(0xFFFEF3C7)
    val AlertRose = Color(0xFFDC2626)         // bright rose red
    val AlertRoseSoft = Color(0xFFFEE2E2)

    // Neutral Grays (Dark Context)
    val DarkSlateBg = Color(0xFF0B0F19)
    val DarkSurfaceCard = Color(0xFF141F32)
    val DarkBorder = Color(0xFF1E2F4C)
    val DarkTextPrimary = Color(0xFFF8FAFC)
    val DarkTextSecondary = Color(0xFF94A3B8)

    // Neutral Grays (Light Context)
    val LightSlateBg = Color(0xFFF8FAFB)
    val LightSurfaceCard = Color(0xFFFFFFFF)
    val LightBorder = Color(0xFFE4E4E7)
    val LightTextPrimary = Color(0xFF18181B)
    val LightTextSecondary = Color(0xFF71717A)
}

val CATEGORY_ICONS_MAP = mapOf(
    "ShoppingCart" to Icons.Rounded.ShoppingCart,
    "Payments" to Icons.Rounded.Payments,
    "Receipt" to Icons.Rounded.Receipt,
    "DirectionsCar" to Icons.Rounded.DirectionsCar,
    "Home" to Icons.Rounded.Home,
    "LocalPharmacy" to Icons.Rounded.LocalPharmacy,
    "School" to Icons.Rounded.School,
    "Checkroom" to Icons.Rounded.Checkroom,
    "Restaurant" to Icons.Rounded.Restaurant,
    "LocalPlay" to Icons.Rounded.LocalPlay,
    "Redeem" to Icons.Rounded.Redeem,
    "SportsEsports" to Icons.Rounded.SportsEsports, 
    "Flight" to Icons.Rounded.Flight,
    "Pets" to Icons.Rounded.Pets,
    "Work" to Icons.Rounded.Work,
    "Tv" to Icons.Rounded.Tv,
    "Computer" to Icons.Rounded.Computer,
    "FitnessCenter" to Icons.Rounded.FitnessCenter,
    "HomeRepairService" to Icons.Rounded.HomeRepairService,
    "SelfImprovement" to Icons.Rounded.SelfImprovement,
    "Brush" to Icons.Rounded.Brush,
    "Spa" to Icons.Rounded.Spa,
    "Eco" to Icons.Rounded.Eco,
    "TrendingUp" to Icons.Rounded.TrendingUp,
    "Savings" to Icons.Rounded.Savings,
    "Wallet" to Icons.Rounded.Wallet,
    "Build" to Icons.Rounded.Build,
    "PhoneAndroid" to Icons.Rounded.PhoneAndroid,
    "Wifi" to Icons.Rounded.Wifi,
    "Face" to Icons.Rounded.Face,
    "Group" to Icons.Rounded.Group,
    "Favorite" to Icons.Rounded.Favorite,
    "Security" to Icons.Rounded.Security,
    "Category" to Icons.Rounded.Category
)

// Resolves Icon & Theme colors for all Finance Categories
fun getCategoryDetails(category: String, isDark: Boolean, customIconKey: String? = null): Pair<androidx.compose.ui.graphics.vector.ImageVector, Color> {
    val accent = if (isDark) DesignTokens.RoyalPurple else DesignTokens.RoyalPurple
    val defaultIconAndColor = when (category) {
        "Зарплата" -> Icons.Rounded.Payments to DesignTokens.IncomeGreen
        "Інвестиції" -> Icons.Rounded.TrendingUp to Color(0xFF0D9488) // teal
        "Подарунок" -> Icons.Rounded.Redeem to Color(0xFFC026D3) // magenta
        "Продукти" -> Icons.Rounded.ShoppingCart to Color(0xFFD97706) // amber
        "Транспорт" -> Icons.Rounded.DirectionsCar to Color(0xFF2563EB) // blue
        "Житло / Комуналка" -> Icons.Rounded.Home to Color(0xFF7C3AED) // deep purple
        "Розваги" -> Icons.Rounded.LocalPlay to Color(0xFFDB2777) // rose
        "Здоров'я" -> Icons.Rounded.LocalPharmacy to Color(0xFFE11D48) // red
        "Одяг" -> Icons.Rounded.Checkroom to Color(0xFF0F766E) // dark teal
        "Початковий баланс" -> Icons.Rounded.Wallet to accent
        else -> {
            val hashCode = category.hashCode()
            val colors = listOf(
                Color(0xFF10B981), // Emerald Green
                Color(0xFF0D9488), // Teal
                Color(0xFF3B82F6), // Blue
                Color(0xFF6366F1), // Indigo
                Color(0xFF8B5CF6), // Purple
                Color(0xFFD946EF), // Fuchsia
                Color(0xFFEC4899), // Pink
                Color(0xFFF43F5E), // Rose
                Color(0xFFEF4444), // Red
                Color(0xFFF87171), // Light Red
                Color(0xFFF59E0B), // Amber
                Color(0xFF84CC16)  // Lime
            )
            val index = java.lang.Math.abs(hashCode) % colors.size
            Icons.Rounded.Category to colors[index]
        }
    }

    if (customIconKey != null) {
        val customIcon = CATEGORY_ICONS_MAP[customIconKey]
        if (customIcon != null) {
            return customIcon to defaultIconAndColor.second
        }
    }
    return defaultIconAndColor
}

// Custom formatted currency amounts uk-UA
fun formatAmount(amount: Double, currency: String = "UAH"): String {
    val symbol = when (currency) {
        "USD" -> "$"
        "EUR" -> "€"
        "PLN" -> "zł"
        else -> "₴"
    }
    return if (amount % 1.0 == 0.0) {
        String.format(Locale("uk", "UA"), "%,.0f %s", amount, symbol)
    } else {
        String.format(Locale("uk", "UA"), "%,.2f %s", amount, symbol)
    }
}

fun formatShortDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("uk", "UA"))
    return sdf.format(Date(timestamp))
}

@Composable
fun IconButtonTextRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val textPrimary = if (isDark) Color.White else DesignTokens.LightTextPrimary
    val iconTint = if (isDark) Color.LightGray else DesignTokens.LightTextSecondary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        Text(label, color = textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun FinanceAppScreen(
    viewModel: FinanceViewModel,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val balanceUah by viewModel.currentBalanceUah.collectAsState()
    val monthExpenseUah by viewModel.currentMonthExpenseUah.collectAsState()
    val plansUah by viewModel.totalBudgetsUah.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var showDailyReport by remember { mutableStateOf(false) }
    var showTotalReport by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val activeBg = if (isDarkTheme) DesignTokens.DarkSlateBg else DesignTokens.LightSlateBg
    val curDrawerBg = if (isDarkTheme) Color(0xFF0F1420) else Color(0xFFFFFFFF)
    val curDrawerTextPrimary = if (isDarkTheme) Color.White else DesignTokens.LightTextPrimary
    val curDrawerTextSecondary = if (isDarkTheme) Color.LightGray.copy(alpha = 0.6f) else DesignTokens.LightTextSecondary
    val curCardBg = if (isDarkTheme) Color(0xFF1B2333) else Color(0xFFF1F5F9)
    val curNavIconTint = if (isDarkTheme) Color.LightGray else DesignTokens.LightTextSecondary
    val curDividerColor = if (isDarkTheme) Color(0xFF1E293B) else DesignTokens.LightBorder

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = curDrawerBg,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(280.dp)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp, top = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = null, tint = DesignTokens.RoyalPurple, modifier = Modifier.size(28.dp))
                                Text("Fintrack Меню", color = curDrawerTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }

                            IconButton(
                                onClick = { scope.launch { drawerState.close() } },
                                modifier = Modifier.testTag("close_menu_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Закрити меню",
                                    tint = curDrawerTextPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = curCardBg),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch { drawerState.close() }
                                    showAuthDialog = true
                                }
                                .padding(bottom = 20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(DesignTokens.RoyalPurple),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("U", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Користувач", color = curDrawerTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("Хмарна синхронізація", color = curDrawerTextSecondary, fontSize = 10.sp)
                                }
                            }
                        }

                        HorizontalDivider(color = curDividerColor, modifier = Modifier.padding(vertical = 12.dp))

                        NavigationDrawerItem(
                            icon = { Icon(Icons.Rounded.Home, contentDescription = null, tint = curNavIconTint) },
                            label = { Text("Панель перетягування", color = curDrawerTextPrimary) },
                            selected = currentTab == 0,
                            onClick = {
                                viewModel.updateTab(0)
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = DesignTokens.RoyalPurple.copy(alpha = 0.25f),
                                unselectedContainerColor = Color.Transparent,
                                selectedIconColor = DesignTokens.RoyalPurple,
                                unselectedIconColor = curNavIconTint,
                                selectedTextColor = DesignTokens.RoyalPurple,
                                unselectedTextColor = curDrawerTextPrimary
                            ),
                            modifier = Modifier.height(44.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Rounded.List, contentDescription = null, tint = curNavIconTint) },
                            label = { Text("Стрічка операцій", color = curDrawerTextPrimary) },
                            selected = currentTab == 1,
                            onClick = {
                                viewModel.updateTab(1)
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = DesignTokens.RoyalPurple.copy(alpha = 0.25f),
                                unselectedContainerColor = Color.Transparent,
                                selectedIconColor = DesignTokens.RoyalPurple,
                                unselectedIconColor = curNavIconTint,
                                selectedTextColor = DesignTokens.RoyalPurple,
                                unselectedTextColor = curDrawerTextPrimary
                            ),
                            modifier = Modifier.height(44.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Rounded.PieChart, contentDescription = null, tint = curNavIconTint) },
                            label = { Text("Бюджети", color = curDrawerTextPrimary) },
                            selected = currentTab == 2,
                            onClick = {
                                viewModel.updateTab(2)
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = DesignTokens.RoyalPurple.copy(alpha = 0.25f),
                                unselectedContainerColor = Color.Transparent,
                                selectedIconColor = DesignTokens.RoyalPurple,
                                unselectedIconColor = curNavIconTint,
                                selectedTextColor = DesignTokens.RoyalPurple,
                                unselectedTextColor = curDrawerTextPrimary
                            ),
                            modifier = Modifier.height(44.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Rounded.Analytics, contentDescription = null, tint = curNavIconTint) },
                            label = { Text("Аналітика та Звіти", color = curDrawerTextPrimary) },
                            selected = currentTab == 3,
                            onClick = {
                                viewModel.updateTab(3)
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = DesignTokens.RoyalPurple.copy(alpha = 0.25f),
                                unselectedContainerColor = Color.Transparent,
                                selectedIconColor = DesignTokens.RoyalPurple,
                                unselectedIconColor = curNavIconTint,
                                selectedTextColor = DesignTokens.RoyalPurple,
                                unselectedTextColor = curDrawerTextPrimary
                            ),
                            modifier = Modifier.height(44.dp)
                        )

                        HorizontalDivider(color = curDividerColor, modifier = Modifier.padding(vertical = 16.dp))

                        IconButtonTextRow(
                            icon = Icons.Rounded.Today,
                            label = "Денний звіт",
                            isDark = isDarkTheme,
                            onClick = {
                                scope.launch { drawerState.close() }
                                showDailyReport = true
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        IconButtonTextRow(
                            icon = Icons.Rounded.Summarize,
                            label = "Загальний звіт",
                            isDark = isDarkTheme,
                            onClick = {
                                scope.launch { drawerState.close() }
                                showTotalReport = true
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        IconButtonTextRow(
                            icon = Icons.Rounded.Settings,
                            label = "Налаштування",
                            isDark = isDarkTheme,
                            onClick = {
                                scope.launch { drawerState.close() }
                                showSettings = true
                            }
                        )
                    }

                    Text(
                        text = "Fintrack v1.4 • під ключ",
                        color = curDrawerTextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }
            }
        }
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(activeBg)
                .drawBehind {
                    val firstGlowColor = Color(0xFF6E56CF).copy(alpha = 0.04f)
                    val secondGlowColor = Color(0xFF16A34A).copy(alpha = 0.03f)

                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(firstGlowColor, Color.Transparent),
                            center = Offset(size.width * 0.8f, size.height * 0.1f),
                            radius = size.width
                        )
                    )
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(secondGlowColor, Color.Transparent),
                            center = Offset(size.width * 0.2f, size.height * 0.8f),
                            radius = size.width
                        )
                    )
                }
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Elegant Compact Unified Header Area
                FinanceHeaderArea(
                    isDark = isDarkTheme,
                    syncStatus = syncStatus,
                    balance = balanceUah,
                    expenses = monthExpenseUah,
                    plans = plansUah,
                    currentTab = currentTab,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onCloudClick = { showAuthDialog = true }
                )

                // Real Screen Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (currentTab) {
                        0 -> OverviewScreen(viewModel = viewModel, isDark = isDarkTheme)
                        1 -> TransactionsScreen(viewModel = viewModel, isDark = isDarkTheme)
                        2 -> BudgetsScreen(viewModel = viewModel, isDark = isDarkTheme)
                        3 -> AnalyticsScreen(viewModel = viewModel, isDark = isDarkTheme)
                    }
                }
            }

            // Floating Action Button for CoinKeeper style Quick Entry!
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = DesignTokens.RoyalPurple,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .testTag("fab_add_transaction")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Додати операцію", modifier = Modifier.size(34.dp))
            }

            // Animated full screen dialog for adding income/expenses
            if (showAddDialog) {
                AddTransactionDialog(
                    viewModel = viewModel,
                    isDark = isDarkTheme,
                    onDismiss = { showAddDialog = false }
                )
            }

            // Auth and Server settings sync dialog
            if (showAuthDialog) {
                AuthSyncDialog(
                    viewModel = viewModel,
                    isDark = isDarkTheme,
                    onDismiss = { showAuthDialog = false }
                )
            }

            // Overlaid state report dialogs
            if (showDailyReport) {
                DailyReportDialog(
                    viewModel = viewModel,
                    isDark = isDarkTheme,
                    onDismiss = { showDailyReport = false }
                )
            }

            if (showTotalReport) {
                TotalReportDialog(
                    viewModel = viewModel,
                    isDark = isDarkTheme,
                    onDismiss = { showTotalReport = false }
                )
            }

            if (showSettings) {
                SettingsDialog(
                    viewModel = viewModel,
                    isDark = isDarkTheme,
                    onDismiss = { showSettings = false }
                )
            }
        }
    }
}

@Composable
fun FinanceHeaderArea(
    isDark: Boolean,
    syncStatus: com.example.viewmodel.FinanceViewModel.SyncStatus,
    balance: Double,
    expenses: Double,
    plans: Double,
    currentTab: Int,
    onMenuClick: () -> Unit,
    onCloudClick: () -> Unit
) {
    val textPrimaryColor = if (isDark) DesignTokens.DarkTextPrimary else DesignTokens.LightTextPrimary
    val textSecondaryColor = if (isDark) DesignTokens.DarkTextSecondary else DesignTokens.LightTextSecondary
    val surfaceCardColor = if (isDark) DesignTokens.DarkSurfaceCard else DesignTokens.LightSurfaceCard
    val borderCol = if (isDark) DesignTokens.DarkBorder else DesignTokens.LightBorder

    val balanceBg = if (isDark) Color(0xFF101B2B) else Color(0xFFEFFDF5)
    val balanceBorder = if (isDark) Color(0xFF1C2A3C) else Color(0xFFA7F3D0)
    val balanceTextSec = if (isDark) Color(0xFF10B981) else Color(0xFF047857)

    val expensesBg = if (isDark) Color(0xFF1A1525) else Color(0xFFFFF1F2)
    val expensesBorder = if (isDark) Color(0xFF2C1E45) else Color(0xFFFECDD3)
    val expensesTextSec = if (isDark) Color(0xFFF43F5E) else Color(0xFFBE123C)

    val plansBg = if (isDark) Color(0xFF0F1E29) else Color(0xFFEFF6FF)
    val plansBorder = if (isDark) Color(0xFF1B3145) else Color(0xFFBFDBFE)
    val plansTextSec = if (isDark) Color(0xFF3B82F6) else Color(0xFF1D4ED8)

    val activeTabName = when (currentTab) {
        0 -> "Панель"
        1 -> "Операції"
        2 -> "Бюджети"
        else -> "Аналітика"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(surfaceCardColor)
                .border(1.dp, borderCol, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Rounded.Menu,
                contentDescription = "Меню керування",
                tint = DesignTokens.RoyalPurple,
                modifier = Modifier.size(18.dp)
            )
        }

        // 3 Compact Cards (equal width, weight 1)
        // Card 1: Balance
        val balStr = formatAmount(balance)
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(balanceBg)
                .border(1.dp, balanceBorder, RoundedCornerShape(6.dp))
                .padding(vertical = 3.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Баланс", color = balanceTextSec, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
            Text(
                text = balStr,
                color = if (isDark) Color(0xFF4ADE80) else Color(0xFF16A34A),
                fontSize = if (balStr.length > 8) 9.5.sp else 11.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Card 2: Expenses
        val expStr = formatAmount(expenses)
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(expensesBg)
                .border(1.dp, expensesBorder, RoundedCornerShape(6.dp))
                .padding(vertical = 3.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Витрати", color = expensesTextSec, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
            Text(
                text = expStr,
                color = if (isDark) Color(0xFFF87171) else Color(0xFFDC2626),
                fontSize = if (expStr.length > 8) 9.5.sp else 11.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Card 3: Plan
        val planStr = formatAmount(plans)
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(plansBg)
                .border(1.dp, plansBorder, RoundedCornerShape(6.dp))
                .padding(vertical = 3.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("План", color = plansTextSec, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
            Text(
                text = planStr,
                color = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB),
                fontSize = if (planStr.length > 8) 9.5.sp else 11.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Cloud sync Status action
        val (cloudIcon, cloudColor) = when (syncStatus) {
            is com.example.viewmodel.FinanceViewModel.SyncStatus.Unauthorized -> Icons.Rounded.CloudOff to textSecondaryColor
            is com.example.viewmodel.FinanceViewModel.SyncStatus.Syncing -> Icons.Rounded.Sync to DesignTokens.WarningAmber
            is com.example.viewmodel.FinanceViewModel.SyncStatus.Synced -> Icons.Rounded.CloudQueue to DesignTokens.IncomeGreen
            is com.example.viewmodel.FinanceViewModel.SyncStatus.Error -> Icons.Rounded.CloudOff to DesignTokens.AlertRose
        }

        IconButton(
            onClick = onCloudClick,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(surfaceCardColor)
                .border(1.dp, borderCol, CircleShape)
                .testTag("header_cloud_sync_button")
        ) {
            Icon(
                imageVector = cloudIcon,
                contentDescription = "Статус синхронізації",
                tint = cloudColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}



@Composable
fun DailyReportDialog(
    viewModel: FinanceViewModel,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val textPrimary = if (isDark) DesignTokens.DarkTextPrimary else DesignTokens.LightTextPrimary
    val textSecondary = if (isDark) DesignTokens.DarkTextSecondary else DesignTokens.LightTextSecondary
    val surfaceColor = if (isDark) DesignTokens.DarkSurfaceCard else DesignTokens.LightSurfaceCard
    val borderCol = if (isDark) DesignTokens.DarkBorder else DesignTokens.LightBorder

    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val todayStart = cal.timeInMillis

    val todayTxs = transactions.filter { it.date >= todayStart }
    val todayIncome = todayTxs.filter { it.type == "income" }.sumOf { it.amount }
    val todayExpense = todayTxs.filter { it.type == "expense" }.sumOf { it.amount }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, borderCol)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Денний звіт",
                    color = textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = SimpleDateFormat("dd MMMM yyyy", Locale("uk")).format(Date()),
                    color = textSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2D1F))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Надходження", color = Color(0xFF81C784), fontSize = 11.sp)
                            Text(formatAmount(todayIncome), color = Color(0xFF81C784), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D161B))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Витрати", color = Color(0xFFE57373), fontSize = 11.sp)
                            Text(formatAmount(todayExpense), color = Color(0xFFE57373), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Сьогоднішні операції:", color = textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(8.dp))

                if (todayTxs.isEmpty()) {
                    Text("Операцій сьогодні не зафіксовано", color = textSecondary, fontSize = 12.sp, modifier = Modifier.padding(vertical = 12.dp))
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .heightIn(max = 160.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(todayTxs) { tx ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(tx.category, color = textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    if (!tx.description.isNullOrEmpty()) {
                                        Text(tx.description, color = textSecondary, fontSize = 10.sp)
                                    }
                                }
                                Text(
                                    text = "${if (tx.type == "expense") "-" else "+"}${formatAmount(tx.amount, tx.currency)}",
                                    color = if (tx.type == "expense") Color(0xFFE57373) else Color(0xFF81C784),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = DesignTokens.RoyalPurple),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Закрити", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun TotalReportDialog(
    viewModel: FinanceViewModel,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val accounts by viewModel.accounts.collectAsState()
    val budgetReport by viewModel.budgetReport.collectAsState()
    val currentMonthExpenseUah by viewModel.currentMonthExpenseUah.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    val textPrimary = if (isDark) DesignTokens.DarkTextPrimary else DesignTokens.LightTextPrimary
    val textSecondary = if (isDark) DesignTokens.DarkTextSecondary else DesignTokens.LightTextSecondary
    val surfaceColor = if (isDark) DesignTokens.DarkSurfaceCard else DesignTokens.LightSurfaceCard
    val borderCol = if (isDark) DesignTokens.DarkBorder else DesignTokens.LightBorder

    val netWorthUah = accounts.filter { it.displayInTotalBalance && it.kind != "income_source" && it.kind != "expense_category" }
        .sumOf { acc ->
            val rate = viewModel.exchangeRates[acc.currency] ?: 1.0
            val mult = if (acc.balanceNature == "asset") 1.0 else -1.0
            acc.currentBalance * rate * mult
        }

    val cal = Calendar.getInstance()
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
    val averageDailyExpense = if (dayOfMonth > 0) currentMonthExpenseUah / dayOfMonth else 0.0

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, borderCol)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Загальний звіт за місяць",
                    color = textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Загальні активи (UAH):", color = textSecondary, fontSize = 12.sp)
                        Text(formatAmount(netWorthUah), color = Color(0xFF10B981), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Витрати за цей місяць:", color = textSecondary, fontSize = 12.sp)
                        Text(formatAmount(currentMonthExpenseUah), color = Color(0xFFF43F5E), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Середні витрати за день:", color = textSecondary, fontSize = 12.sp)
                        Text(formatAmount(averageDailyExpense), color = textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Днів до кінця місяця:", color = textSecondary, fontSize = 12.sp)
                        Text("${daysInMonth - dayOfMonth} дн.", color = textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = borderCol, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Text("Ліміти бюджетів:", color = textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(8.dp))

                if (budgetReport.isEmpty()) {
                    Text("Бюджетів не заплановано", color = textSecondary, fontSize = 11.sp, modifier = Modifier.padding(vertical = 12.dp))
                } else {
                    budgetReport.forEach { item ->
                        val pct = if (item.limit > 0) (item.spent / item.limit).coerceIn(0.0, 1.0) else 0.0
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.category, color = textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                Text("${formatAmount(item.spent)} / ${formatAmount(item.limit)}", color = textSecondary, fontSize = 10.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = pct.toFloat(),
                                color = if (pct >= 0.9) Color(0xFFF43F5E) else DesignTokens.RoyalPurple,
                                trackColor = borderCol,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }

                // Tag Spending Analysis
                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = borderCol, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Аналіз витрат за тегами:",
                    color = textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                val tagExpenses = transactions
                    .filter { it.type == "expense" }
                    .groupBy { if (it.tags.trim().isEmpty()) "Без тегу" else it.tags.trim() }
                    .mapValues { entry ->
                        entry.value.sumOf { tx ->
                            val rate = viewModel.exchangeRates[tx.currency] ?: 1.0
                            tx.amount * rate
                        }
                    }
                    .toList()
                    .sortedByDescending { it.second }

                if (tagExpenses.isEmpty()) {
                    Text(
                        text = "Немає витрат для аналізу тегів",
                        color = textSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    tagExpenses.forEach { (tag, amount) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocalOffer,
                                    contentDescription = null,
                                    tint = DesignTokens.RoyalPurple.copy(alpha = 0.6f),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = tag,
                                    color = textPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = formatAmount(amount),
                                color = textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = DesignTokens.RoyalPurple),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Закрити", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun SettingsDialog(
    viewModel: FinanceViewModel,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val accounts by viewModel.accounts.collectAsState()
    val textPrimary = if (isDark) DesignTokens.DarkTextPrimary else DesignTokens.LightTextPrimary
    val textSecondary = if (isDark) DesignTokens.DarkTextSecondary else DesignTokens.LightTextSecondary
    val surfaceColor = if (isDark) DesignTokens.DarkSurfaceCard else DesignTokens.LightSurfaceCard
    val borderCol = if (isDark) DesignTokens.DarkBorder else DesignTokens.LightBorder

    val realAccounts = accounts.filter { it.kind != "income_source" && it.kind != "expense_category" }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, borderCol)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Налаштування балансу",
                    color = textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Оберіть які рахунки враховувати в загальний баланс",
                    color = textSecondary,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .heightIn(max = 240.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(realAccounts) { acc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(acc.name, color = textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("${acc.currency} • ${formatAmount(acc.currentBalance, acc.currency)}", color = textSecondary, fontSize = 10.sp)
                            }
                            Switch(
                                checked = acc.displayInTotalBalance,
                                onCheckedChange = { viewModel.toggleDisplayInTotalBalance(acc) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = DesignTokens.RoyalPurple,
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = Color.Black.copy(alpha = 0.2f)
                                )
                            )
                        }
                    }
                }

                // Theme Mode Selector
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = borderCol ?: Color.Gray, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Тема оформлення",
                    color = textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                val currentThemeMode by viewModel.themeMode.collectAsState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                        .border(1.dp, borderCol ?: Color.Gray, RoundedCornerShape(10.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        "light" to "Світла",
                        "dark" to "Темна",
                        "system" to "Системна"
                    ).forEach { (mode, label) ->
                        val isSelected = currentThemeMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) DesignTokens.RoyalPurple else Color.Transparent)
                                .clickable { viewModel.updateThemeMode(mode) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = DesignTokens.RoyalPurple),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Зберегти", color = Color.White)
                }
            }
        }
    }
}

// ==========================================
// 1. OVERVIEW SCREEN COMPONENT & QUICK OPERATIONS
// ==========================================


data class QuickTxDetails(
    val type: String, // "income", "expense", "transfer"
    val sourceName: String?,
    val sourceAccount: Account?,
    val destAccount: Account?,
    val destCategory: String?
)

@Composable
fun QuickAmountInputDialog(
    details: QuickTxDetails,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Double, String, String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }
    var tagsText by remember { mutableStateOf("") }

    val textPrimary = if (isDark) DesignTokens.DarkTextPrimary else DesignTokens.LightTextPrimary
    val textSecondary = if (isDark) DesignTokens.DarkTextSecondary else DesignTokens.LightTextSecondary
    val surfaceColor = if (isDark) DesignTokens.DarkSurfaceCard else DesignTokens.LightSurfaceCard
    val borderCol = if (isDark) DesignTokens.DarkBorder else DesignTokens.LightBorder

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, borderCol)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when (details.type) {
                        "income" -> "🟢 ШВИДКИЙ ДОХІД"
                        "expense" -> "🔴 ШВИДКА ВИТРАТА"
                        else -> "🟣 ШВИДКИЙ ПЕРЕКАЗ"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (details.type) {
                        "income" -> DesignTokens.IncomeGreen
                        "expense" -> DesignTokens.AlertRose
                        else -> DesignTokens.RoyalPurple
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                        .padding(12.dp)
                ) {
                    val sourceText = details.sourceName ?: details.sourceAccount?.name ?: ""
                    val destText = details.destCategory ?: details.destAccount?.name ?: ""

                    Text(
                        text = sourceText,
                        color = textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Icon(
                        imageVector = Icons.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = DesignTokens.RoyalPurple,
                        modifier = Modifier.padding(horizontal = 8.dp).size(20.dp)
                    )

                    Text(
                        text = destText,
                        color = textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' }) {
                            amountText = input
                        }
                    },
                    label = { Text("Сума", fontSize = 12.sp) },
                    placeholder = { Text("0.00", fontSize = 14.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DesignTokens.RoyalPurple,
                        unfocusedBorderColor = borderCol,
                        focusedLabelColor = DesignTokens.RoyalPurple
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("quick_amount_input_field"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    label = { Text("Коментар (необов'язково)", fontSize = 12.sp) },
                    placeholder = { Text("Опис транзакції", fontSize = 14.sp) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DesignTokens.RoyalPurple,
                        unfocusedBorderColor = borderCol,
                        focusedLabelColor = DesignTokens.RoyalPurple
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = tagsText,
                    onValueChange = { tagsText = it },
                    label = { Text("Теги (через кому чи пробіл)", fontSize = 12.sp) },
                    placeholder = { Text("#їжа, #таксі, #кафе", fontSize = 14.sp) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DesignTokens.RoyalPurple,
                        unfocusedBorderColor = borderCol,
                        focusedLabelColor = DesignTokens.RoyalPurple
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Suggestion chips
                val suggestedTags = listOf("їжа", "таксі", "кафе", "продукти", "оренда", "комуналка", "кіно", "розваги", "покупки", "ліки")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Рекомендовані теги (тицьніть для додавання):",
                            fontSize = 11.sp,
                            color = textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            suggestedTags.forEach { sTag ->
                                val clean = sTag.trim().lowercase()
                                val isSelected = tagsText.split(Regex("[,\\s]+"))
                                    .map { it.trim().trim('#').lowercase() }
                                    .contains(clean)

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) DesignTokens.RoyalPurple.copy(alpha = 0.22f)
                                            else if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                                        )
                                        .clickable {
                                            val currentList = tagsText.split(Regex("[,\\s]+"))
                                                .map { it.trim().trim('#') }
                                                .filter { it.isNotBlank() }
                                                .toMutableList()
                                            
                                            if (isSelected) {
                                                currentList.removeAll { it.lowercase() == clean }
                                            } else {
                                                currentList.add(sTag)
                                            }
                                            tagsText = currentList.joinToString(", ") { "#$it" }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "#$sTag",
                                        color = if (isSelected) DesignTokens.RoyalPurple else textPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Скасувати", color = textSecondary, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (amt > 0.0) {
                                onConfirm(amt, descriptionText, tagsText)
                            }
                        },
                        enabled = amountText.isNotEmpty() && (amountText.toDoubleOrNull() ?: 0.0) > 0.0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DesignTokens.RoyalPurple,
                            disabledContainerColor = textSecondary.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1.5f).testTag("quick_amount_confirm_button")
                    ) {
                        Text("Записати", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun OverviewScreen(
    viewModel: FinanceViewModel,
    isDark: Boolean
) {
    val totalIncomeUah by viewModel.totalIncomeUah.collectAsState()
    val totalExpenseUah by viewModel.totalExpenseUah.collectAsState()
    val balanceUah by viewModel.currentBalanceUah.collectAsState()
    val expensesByCategoryUah by viewModel.expensesByCategoryUah.collectAsState()
    val incomesByCategoryUah by viewModel.incomesByCategoryUah.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showAddIncomeDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<com.example.data.Account?>(null) }

    var parentWindowOffset by remember { mutableStateOf(Offset.Zero) }

    var activeQuickTransaction by remember { mutableStateOf<QuickTxDetails?>(null) }

    // CoinKeeper Drag & Drop Engine states
    var dashboardDraggedCoin by remember { mutableStateOf<DraggedCoin?>(null) }
    var dashboardDragStartOffset by remember { mutableStateOf(Offset.Zero) }
    var dashboardDragAmountOffset by remember { mutableStateOf(Offset.Zero) }
    var dashboardIsDragging by remember { mutableStateOf(false) }

    val dashboardTargetBoundsMap = remember { mutableStateMapOf<String, androidx.compose.ui.geometry.Rect>() }
    var dashboardCurrentHoveredTargetKey by remember { mutableStateOf<String?>(null) }
    val dashboardCoinPositionsMap = remember { mutableStateMapOf<String, Offset>() }

    val dashboardGetActiveTargetKey: (Offset) -> String? = { currentWindowPos ->
        var active: String? = null
        for ((key, rect) in dashboardTargetBoundsMap.toMap()) {
            if (rect.contains(currentWindowPos)) {
                active = key
                break
            }
        }
        active
    }

    // Draggable extension for Dashboard Coins
    @Composable
    fun Modifier.makeDashboardCoinDraggable(
        item: DraggedCoin,
        coinKey: String
    ): Modifier {
        val currentItem by rememberUpdatedState(item)
        val currentCoinKey by rememberUpdatedState(coinKey)
        return this
            .onGloballyPositioned { coords ->
                dashboardCoinPositionsMap[currentCoinKey] = coords.positionInWindow()
            }
            .pointerInput(currentCoinKey, currentItem) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val itemWindowPos = dashboardCoinPositionsMap[currentCoinKey] ?: Offset.Zero
                        dashboardDraggedCoin = currentItem
                        dashboardDragStartOffset = itemWindowPos + offset
                        dashboardDragAmountOffset = Offset.Zero
                        dashboardIsDragging = true
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dashboardDragAmountOffset += dragAmount
                        val currentFingerPos = dashboardDragStartOffset + dashboardDragAmountOffset
                        dashboardCurrentHoveredTargetKey = dashboardGetActiveTargetKey(currentFingerPos)
                    },
                    onDragEnd = {
                        val finalFingerPos = dashboardDragStartOffset + dashboardDragAmountOffset
                        val target = dashboardGetActiveTargetKey(finalFingerPos)
                        if (target != null) {
                            when (val d = dashboardDraggedCoin) {
                                is DraggedCoin.Income -> {
                                    if (target.startsWith("dashboard_account_target_")) {
                                        val accId = target.substringAfter("dashboard_account_target_").toIntOrNull()
                                        val match = accounts.find { it.id == accId }
                                        if (match != null) {
                                            activeQuickTransaction = QuickTxDetails(
                                                type = "income",
                                                sourceName = d.name,
                                                sourceAccount = null,
                                                destAccount = match,
                                                destCategory = null
                                            )
                                        }
                                    }
                                }
                                is DraggedCoin.AccountRef -> {
                                    if (target.startsWith("dashboard_category_target_")) {
                                        val catName = target.substringAfter("dashboard_category_target_")
                                        activeQuickTransaction = QuickTxDetails(
                                            type = "expense",
                                            sourceName = null,
                                            sourceAccount = d.account,
                                            destAccount = null,
                                            destCategory = catName
                                        )
                                    } else if (target.startsWith("dashboard_account_target_")) {
                                        val destId = target.substringAfter("dashboard_account_target_").toIntOrNull()
                                        val match = accounts.find { it.id == destId }
                                        if (match != null && match.id != d.account.id) {
                                            activeQuickTransaction = QuickTxDetails(
                                                type = "transfer",
                                                sourceName = null,
                                                sourceAccount = d.account,
                                                destAccount = match,
                                                destCategory = null
                                            )
                                        }
                                    }
                                }
                                null -> {}
                            }
                        }
                        dashboardIsDragging = false
                        dashboardDraggedCoin = null
                        dashboardCurrentHoveredTargetKey = null
                    },
                    onDragCancel = {
                        dashboardIsDragging = false
                        dashboardDraggedCoin = null
                        dashboardCurrentHoveredTargetKey = null
                    }
                )
            }
    }

    val textPrimary = if (isDark) DesignTokens.DarkTextPrimary else DesignTokens.LightTextPrimary
    val textSecondary = if (isDark) DesignTokens.DarkTextSecondary else DesignTokens.LightTextSecondary
    val surfaceColor = if (isDark) DesignTokens.DarkSurfaceCard else DesignTokens.LightSurfaceCard
    val borderCol = if (isDark) DesignTokens.DarkBorder else DesignTokens.LightBorder

    val orderVersion by viewModel.orderVersion.collectAsState()
    val customIcons by viewModel.customIcons.collectAsState()

    var isIncomeSourcesExpanded by remember { mutableStateOf(true) }
    var isRealAccountsExpanded by remember { mutableStateOf(true) }
    var isExpenseCategoriesExpanded by remember { mutableStateOf(true) }

    var iconChooserCategoryName by remember { mutableStateOf<String?>(null) }

    val sortedIncomeSources = remember(accounts, orderVersion) {
        val raw = accounts.filter { it.kind == "income_source" }
        viewModel.getSortedAccountNames("income_source", raw)
    }
    val sortedRealAccounts = remember(accounts, orderVersion) {
        val raw = accounts.filter { it.kind != "income_source" && it.kind != "expense_category" }
        viewModel.getSortedAccountNames("real_accounts", raw)
    }
    val sortedExpenseCategories = remember(accounts, orderVersion) {
        val raw = accounts.filter { it.kind == "expense_category" }
        viewModel.getSortedAccountNames("expense_category", raw)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                parentWindowOffset = coords.positionInWindow()
            }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("overview_scroll_list"),
            userScrollEnabled = !dashboardIsDragging,
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // ROW 1: INCOME SOURCES (Collapsible)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isIncomeSourcesExpanded = !isIncomeSourcesExpanded }
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "🟢 Джерела Доходів",
                            color = textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = if (isIncomeSourcesExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            tint = textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (isIncomeSourcesExpanded) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        GridOf4Columns(spacing = 6.dp) {
                            sortedIncomeSources.forEachIndexed { idx, inc ->
                                val coinKey = "dashboard_income_${inc.name}"
                                val (icon, tint) = getCategoryDetails(inc.name, isDark, customIcons[inc.name])
                                DashboardTile(
                                    name = inc.name,
                                    icon = icon,
                                    tint = DesignTokens.IncomeGreen,
                                    isDark = isDark,
                                    badgeText = formatAmount(incomesByCategoryUah[inc.name] ?: 0.0, "UAH"),
                                    onArchiveClick = { viewModel.archiveAccount(inc) },
                                    onMoveLeftClick = if (idx > 0) { { viewModel.moveAccount(inc.name, "income_source", sortedIncomeSources, -1) } } else null,
                                    onMoveRightClick = if (idx < sortedIncomeSources.size - 1) { { viewModel.moveAccount(inc.name, "income_source", sortedIncomeSources, 1) } } else null,
                                    onChangeIconClick = { iconChooserCategoryName = inc.name },
                                    modifier = Modifier.makeDashboardCoinDraggable(DraggedCoin.Income(inc.name), coinKey)
                                )
                            }
                            AddTile(
                                text = "Додати",
                                color = DesignTokens.IncomeGreen,
                                isDark = isDark,
                                onClick = { showAddIncomeDialog = true }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // ROW 2: ACTIVE ACCOUNTS (Collapsible)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isRealAccountsExpanded = !isRealAccountsExpanded }
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "🔵 Мої Рахунки",
                            color = textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = if (isRealAccountsExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            tint = textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (isRealAccountsExpanded) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        GridOf4Columns(spacing = 6.dp) {
                            sortedRealAccounts.forEachIndexed { idx, account ->
                                val coinKey = "dashboard_account_source_${account.id}"
                                val targetKey = "dashboard_account_target_${account.id}"
                                val isHovered = dashboardCurrentHoveredTargetKey == targetKey

                                val (defaultIcon, tint) = when (account.kind) {
                                    "cash" -> Icons.Rounded.Payments to Color(0xFF10B981)
                                    "current" -> Icons.Rounded.CreditCard to Color(0xFF3B82F6)
                                    "savings" -> Icons.Rounded.Savings to Color(0xFFEC4899)
                                    "deposit" -> Icons.Rounded.AccountBalance to Color(0xFF8B5CF6)
                                    else -> Icons.Rounded.Wallet to DesignTokens.RoyalPurple
                                }
                                val customIconKey = customIcons[account.name]
                                val icon = if (customIconKey != null) {
                                    CATEGORY_ICONS_MAP[customIconKey] ?: defaultIcon
                                } else {
                                    defaultIcon
                                }

                                DashboardTile(
                                    name = account.name,
                                    icon = icon,
                                    tint = tint,
                                    isDark = isDark,
                                    badgeText = formatAmount(account.currentBalance, account.currency),
                                    isHovered = isHovered,
                                    onArchiveClick = { viewModel.archiveAccount(account) },
                                    onMoveLeftClick = if (idx > 0) { { viewModel.moveAccount(account.name, "real_accounts", sortedRealAccounts, -1) } } else null,
                                    onMoveRightClick = if (idx < sortedRealAccounts.size - 1) { { viewModel.moveAccount(account.name, "real_accounts", sortedRealAccounts, 1) } } else null,
                                    onChangeIconClick = { iconChooserCategoryName = account.name },
                                    onEditClick = { editingAccount = account },
                                    modifier = Modifier
                                        .onGloballyPositioned { coords ->
                                            dashboardTargetBoundsMap[targetKey] = coords.boundsInWindow()
                                        }
                                        .makeDashboardCoinDraggable(DraggedCoin.AccountRef(account), coinKey)
                                )
                            }
                            AddTile(
                                text = "Додати",
                                color = DesignTokens.RoyalPurple,
                                isDark = isDark,
                                onClick = { showAddAccountDialog = true }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // ROW 3: EXPENSE CATEGORIES (Collapsible)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpenseCategoriesExpanded = !isExpenseCategoriesExpanded }
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "🔴 Категорії Витрат",
                            color = textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = if (isExpenseCategoriesExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            tint = textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (isExpenseCategoriesExpanded) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        GridOf4Columns(spacing = 6.dp) {
                            sortedExpenseCategories.forEachIndexed { idx, cat ->
                                val (icon, tint) = getCategoryDetails(cat.name, isDark, customIcons[cat.name])
                                val targetKey = "dashboard_category_target_${cat.name}"
                                val isHovered = dashboardCurrentHoveredTargetKey == targetKey

                                DashboardTile(
                                    name = cat.name,
                                    icon = icon,
                                    tint = tint,
                                    isDark = isDark,
                                    badgeText = formatAmount(expensesByCategoryUah[cat.name] ?: 0.0, "UAH"),
                                    isHovered = isHovered,
                                    onArchiveClick = { viewModel.archiveAccount(cat) },
                                    onMoveLeftClick = if (idx > 0) { { viewModel.moveAccount(cat.name, "expense_category", sortedExpenseCategories, -1) } } else null,
                                    onMoveRightClick = if (idx < sortedExpenseCategories.size - 1) { { viewModel.moveAccount(cat.name, "expense_category", sortedExpenseCategories, 1) } } else null,
                                    onChangeIconClick = { iconChooserCategoryName = cat.name },
                                    modifier = Modifier
                                        .onGloballyPositioned { coords ->
                                            dashboardTargetBoundsMap[targetKey] = coords.boundsInWindow()
                                        }
                                )
                            }
                            AddTile(
                                text = "Додати",
                                color = DesignTokens.AlertRose,
                                isDark = isDark,
                                onClick = { showAddCategoryDialog = true }
                            )
                        }
                    }
                }
            }

        }

        // Floating Coin Overlay for realistic dragging visual sensation (CoinKeeper experience)
        if (dashboardIsDragging && dashboardDraggedCoin != null) {
            val finalOffsetInWindow = dashboardDragStartOffset + dashboardDragAmountOffset
            val localOffset = finalOffsetInWindow - parentWindowOffset
            val density = LocalDensity.current
            val xDp = with(density) { localOffset.x.toDp() }
            val yDp = with(density) { localOffset.y.toDp() }

            Box(
                modifier = Modifier
                    .zIndex(100f)
                    .offset(x = xDp - 45.dp, y = yDp - 45.dp) // center visual focus
                    .size(90.dp)
                    .shadow(16.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(
                        when (dashboardDraggedCoin) {
                            is DraggedCoin.Income -> DesignTokens.IncomeGreen
                            else -> DesignTokens.RoyalPurple
                        }
                    )
                    .border(3.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(6.dp)
                ) {
                    Icon(
                        imageVector = when (dashboardDraggedCoin) {
                            is DraggedCoin.Income -> Icons.Rounded.AddCircle
                            else -> Icons.Rounded.AccountBalanceWallet
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (val drag = dashboardDraggedCoin) {
                            is DraggedCoin.Income -> drag.name
                            is DraggedCoin.AccountRef -> drag.account.name
                            else -> ""
                        },
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    // Add Account Dialog
    if (showAddAccountDialog) {
        AddAccountDialog(
            isDark = isDark,
            onDismiss = { showAddAccountDialog = false },
            onAdd = { name, kind, nature, ccy, initial, rate, months ->
                viewModel.addAccount(name, kind, nature, ccy, initial, rate, months)
                showAddAccountDialog = false
            }
        )
    }

    // Edit Account Dialog
    val currentEditingAcc = editingAccount
    if (currentEditingAcc != null) {
        EditAccountDialog(
            account = currentEditingAcc,
            isDark = isDark,
            onDismiss = { editingAccount = null },
            onSave = { newName, newBalance, displayInTotal ->
                viewModel.updateAccountDetails(currentEditingAcc.id, newName, newBalance, displayInTotal)
                editingAccount = null
            },
            onDelete = {
                viewModel.deleteAccount(currentEditingAcc)
                editingAccount = null
            }
        )
    }

    // Add Dynamic Income Source Dialog
    if (showAddIncomeDialog) {
        AddTileDialog(
            type = "income_source",
            isDark = isDark,
            onDismiss = { showAddIncomeDialog = false },
            onAdd = { name ->
                viewModel.addAccount(
                    name = name,
                    kind = "income_source",
                    balanceNature = "asset",
                    currency = "UAH",
                    initialBalance = 0.0
                )
                showAddIncomeDialog = false
            }
        )
    }

    // Add Dynamic Expense Category Dialog
    if (showAddCategoryDialog) {
        AddTileDialog(
            type = "expense_category",
            isDark = isDark,
            onDismiss = { showAddCategoryDialog = false },
            onAdd = { name ->
                viewModel.addAccount(
                    name = name,
                    kind = "expense_category",
                    balanceNature = "asset",
                    currency = "UAH",
                    initialBalance = 0.0
                )
                showAddCategoryDialog = false
            }
        )
    }

    // Quick transaction input popup standard
    val activeQuickTx = activeQuickTransaction
    if (activeQuickTx != null) {
        QuickAmountInputDialog(
            details = activeQuickTx,
            isDark = isDark,
            onDismiss = { activeQuickTransaction = null },
            onConfirm = { amt, desc, tags ->
                when (activeQuickTx.type) {
                    "income" -> {
                        viewModel.addTransaction(
                            amount = amt,
                            type = "income",
                            category = activeQuickTx.sourceName ?: "Зарплата",
                            description = desc,
                            accountId = activeQuickTx.destAccount?.id ?: 0,
                            tags = tags
                        )
                    }
                    "expense" -> {
                        viewModel.addTransaction(
                            amount = amt,
                            type = "expense",
                            category = activeQuickTx.destCategory ?: "Продукти",
                            description = desc,
                            accountId = activeQuickTx.sourceAccount?.id ?: 0,
                            tags = tags
                        )
                    }
                    "transfer" -> {
                        viewModel.addTransaction(
                            amount = amt,
                            type = "transfer",
                            category = "Переказ",
                            description = desc,
                            accountId = activeQuickTx.sourceAccount?.id ?: 0,
                            counterpartyAccountId = activeQuickTx.destAccount?.id ?: 0,
                            counterpartyAmount = amt,
                            tags = tags
                        )
                    }
                }
                activeQuickTransaction = null
            }
        )
    }

    // Custom Icon Chooser Dialog
    if (iconChooserCategoryName != null) {
        val targetCat = iconChooserCategoryName!!
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { iconChooserCategoryName = null }
        ) {
            androidx.compose.material3.Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                color = if (isDark) DesignTokens.DarkSurfaceCard else DesignTokens.LightSurfaceCard,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Оберіть іконку для\n\"$targetCat\"",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = textPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                        ) {
                            FlowRowLayout(spacing = 10.dp) {
                                CATEGORY_ICONS_MAP.forEach { (key, vectorIcon) ->
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (customIcons[targetCat] == key) DesignTokens.RoyalPurple.copy(alpha = 0.15f)
                                                else Color.Transparent
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (customIcons[targetCat] == key) DesignTokens.RoyalPurple else borderCol,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                viewModel.saveCustomCategoryIcon(targetCat, key)
                                                iconChooserCategoryName = null
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = vectorIcon,
                                            contentDescription = key,
                                            tint = if (customIcons[targetCat] == key) DesignTokens.RoyalPurple else textPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = { iconChooserCategoryName = null }
                    ) {
                        Text("Скасувати", color = DesignTokens.RoyalPurple, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AccountCarouselCard(
    account: Account,
    isDark: Boolean,
    onArchive: () -> Unit,
    modifier: Modifier = Modifier
) {
    val surfaceColor = if (isDark) DesignTokens.DarkSurfaceCard else DesignTokens.LightSurfaceCard
    val borderCol = if (isDark) DesignTokens.DarkBorder else DesignTokens.LightBorder
    val textPrimary = if (isDark) DesignTokens.DarkTextPrimary else DesignTokens.LightTextPrimary
    val textSecondary = if (isDark) DesignTokens.DarkTextSecondary else DesignTokens.LightTextSecondary

    var showMenu by remember { mutableStateOf(false) }

    val isMono = account.name.trim().lowercase().contains("mono")
    val isPrivat = account.name.trim().lowercase().contains("privat")

    val customCardBg = when {
        isMono -> Color(0xFF131317)
        isPrivat -> Color(0xFF0C2216)
        else -> surfaceColor
    }
    val customBorderCol = when {
        isMono -> Color(0xFFE11D48)
        isPrivat -> Color(0xFF16A34A)
        else -> borderCol
    }
    val customIconColor = when {
        isMono -> Color(0xFFE11D48)
        isPrivat -> Color(0xFF16A34A)
        else -> DesignTokens.RoyalPurple
    }

    val flag = when (account.currency) {
        "UAH" -> "🇺🇦"
        "USD" -> "🇺🇸"
        "EUR" -> "🇪🇺"
        "PLN" -> "🇵🇱"
        else -> ""
    }
    val displayName = "$flag ${account.name}"

    Card(
        modifier = modifier
            .width(160.dp)
            .height(115.dp)
            .clickable { showMenu = true },
        colors = CardDefaults.cardColors(containerColor = customCardBg),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, customBorderCol)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val accIcon = when (account.kind) {
                    "cash" -> Icons.Rounded.AttachMoney
                    "current" -> Icons.Rounded.CreditCard
                    "savings" -> Icons.Rounded.Savings
                    "deposit" -> Icons.Rounded.Percent
                    else -> Icons.Rounded.Payments
                }
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(customIconColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = accIcon,
                        contentDescription = null,
                        tint = customIconColor,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Small visual indicator if included in total balance calculation!
                    if (account.displayInTotalBalance) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                    }
                    Text(
                        text = account.currency,
                        color = customIconColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column {
                Text(
                    text = displayName,
                    color = textPrimary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatAmount(account.currentBalance, account.currency),
                    color = if (account.balanceNature == "asset") customIconColor else DesignTokens.AlertRose,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (account.kind == "deposit" && account.depositRate != null) {
                    Text(
                        text = "Ставка: ${account.depositRate}% • ${account.depositTermMonths ?: 0}м",
                        color = textSecondary,
                        fontSize = 9.sp,
                        maxLines = 1
                    )
                } else {
                    Text(
                        text = when (account.kind) {
                            "cash" -> "Готівка"
                            "current" -> "Картка"
                            "savings" -> "Скарбничка"
                            "credit_card" -> "Кредитка"
                            else -> "Актив"
                        },
                        color = textSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Unchecked state warning if excluded
                if (!account.displayInTotalBalance) {
                    Text(
                        text = "без балансу",
                        color = Color.LightGray.copy(alpha = 0.5f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Light
                    )
                }
            }
        }
    }

    if (showMenu) {
        AlertDialog(
            onDismissRequest = { showMenu = false },
            title = { Text("Дія з рахунком", color = textPrimary) },
            text = { Text("Бажаєте перенести рахунок '${account.name}' в архів? Він перестане показуватись в активній каруселі, але історичні операції збережуться.", color = textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        onArchive()
                        showMenu = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DesignTokens.AlertRose)
                ) {
                    Text("Архівувати", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMenu = false }) {
                    Text("Скасувати", color = textSecondary)
                }
            },
            containerColor = surfaceColor,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun BalanceSpotlightCard(
    balance: Double,
    income: Double,
    expense: Double,
    isDark: Boolean
) {
    val surfaceColor = if (isDark) DesignTokens.DarkSurfaceCard else DesignTokens.LightSurfaceCard
    val borderCol = if (isDark) DesignTokens.DarkBorder else DesignTokens.LightBorder
    val textPrimary = if (isDark) DesignTokens.DarkTextPrimary else DesignTokens.LightTextPrimary
    val textSecondary = if (isDark) DesignTokens.DarkTextSecondary else DesignTokens.LightTextSecondary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("balance_card"),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, borderCol)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Спільний капітал (UAH eq)",
                color = textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = formatAmount(balance, "UAH"),
                    color = textPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                )
                Icon(
                    imageVector = Icons.Rounded.AccountBalanceWallet,
                    contentDescription = null,
                    tint = DesignTokens.RoyalPurple.copy(alpha = 0.8f),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = borderCol.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Income breakdown
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(DesignTokens.IncomeGreen.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowUpward,
                            contentDescription = "Income",
                            tint = DesignTokens.IncomeGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Надходження",
                            fontSize = 11.sp,
                            color = textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatAmount(income, "UAH"),
                            fontSize = 13.sp,
                            color = DesignTokens.IncomeGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Expense breakdown
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(DesignTokens.AlertRose.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowDownward,
                            contentDescription = "Expense",
                            tint = DesignTokens.AlertRose,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Витрати",
                            fontSize = 11.sp,
                            color = textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatAmount(expense, "UAH"),
                            fontSize = 13.sp,
                            color = DesignTokens.AlertRose,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExpensesDonutChartCard(
    expensesByCategory: Map<String, Double>,
    isDark: Boolean
) {
    val surfaceColor = if (isDark) DesignTokens.DarkSurfaceCard else DesignTokens.LightSurfaceCard
    val borderCol = if (isDark) DesignTokens.DarkBorder else DesignTokens.LightBorder
    val textPrimary = if (isDark) DesignTokens.DarkTextPrimary else DesignTokens.LightTextPrimary
    val textSecondary = if (isDark) DesignTokens.DarkTextSecondary else DesignTokens.LightTextSecondary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("expenses_donut_chart_card"),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, borderCol)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Розподіл витрат (UAH)",
                color = textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Статистика витрат по категоріях",
                fontSize = 11.sp,
                color = textSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val totalExpense = expensesByCategory.values.sum()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // DONUT CHART direct drawing via Canvas
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        var startAngle = -90f
                        expensesByCategory.forEach { (cat, spend) ->
                            val sweepAngle = ((spend / totalExpense) * 360f).toFloat()
                            val color = getCategoryDetails(cat, isDark).second

                            val strokeWidth = 24f
                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                                size = Size(size.width - strokeWidth, size.height - strokeWidth)
                            )
                            startAngle += sweepAngle
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Витрачено",
                            fontSize = 9.sp,
                            color = textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = String.format(Locale("uk", "UA"), "₴%,.0f", totalExpense),
                            fontSize = 13.sp,
                            color = textPrimary,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Custom categorized index indicators
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val sortedCategories = expensesByCategory.toList().sortedByDescending { it.second }.take(3)
                    sortedCategories.forEach { (cat, spend) ->
                        val (_, color) = getCategoryDetails(cat, isDark)
                        val percent = (spend / totalExpense) * 100

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(color, CircleShape)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    color = textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = String.format(Locale("uk", "UA"), "%.0f%% • %s", percent, formatAmount(spend, "UAH")),
                                    fontSize = 9.sp,
                                    color = textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    if (expensesByCategory.size > 3) {
                        Text(
                            text = "+ ще ${expensesByCategory.size - 3} категорій",
                            color = DesignTokens.RoyalPurple,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionListItem(
    transaction: Transaction,
    accounts: List<Account>,
    onDelete: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    customIconKey: String? = null
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val (icon, tint) = getCategoryDetails(transaction.category, isDark, customIconKey)

    val surfaceColor = if (isDark) DesignTokens.DarkSurfaceCard else DesignTokens.LightSurfaceCard
    val borderCol = if (isDark) DesignTokens.DarkBorder else DesignTokens.LightBorder
    val textPrimary = if (isDark) DesignTokens.DarkTextPrimary else DesignTokens.LightTextPrimary
    val textSecondary = if (isDark) DesignTokens.DarkTextSecondary else DesignTokens.LightTextSecondary

    val sourceAccountName = accounts.find { it.id == transaction.accountId }?.name ?: "Рахунок"
    val destAccountName = transaction.counterpartyAccountId?.let { id ->
        accounts.find { it.id == id }?.name
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = { showDeleteConfirm = true }
            )
            .testTag("transaction_item_${transaction.id}"),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderCol.copy(alpha = 0.6f))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Styled Icon Container
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(tint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (transaction.type == "transfer") Icons.Rounded.SwapHoriz else icon,
                        contentDescription = transaction.category,
                        tint = tint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Detail category + metadata account source
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (transaction.type == "transfer") {
                            "Переказ між рахунками"
                        } else {
                            transaction.category
                        },
                        color = textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                     )
                     Spacer(modifier = Modifier.height(1.dp))
                     Text(
                         text = when (transaction.type) {
                             "transfer" -> "$sourceAccountName → $destAccountName"
                             "income" -> "Надходження на $sourceAccountName"
                             "expense" -> "Рахунок $sourceAccountName • ${transaction.description.ifEmpty { "Без коментаря" }}"
                             else -> "Рахунок $sourceAccountName • ${transaction.description.ifEmpty { "Без коментаря" }}"
                         },
                         color = textSecondary,
                         fontSize = 11.sp,
                         maxLines = 1,
                         overflow = TextOverflow.Ellipsis
                     )
                     if (transaction.tags.isNotEmpty()) {
                         Spacer(modifier = Modifier.height(3.dp))
                         Row(
                             horizontalArrangement = Arrangement.spacedBy(4.dp),
                             modifier = Modifier.padding(top = 2.dp)
                         ) {
                             transaction.tags.split(Regex("[,;\\s]+"))
                                 .map { it.trim().trim('#', ',', ';') }
                                 .filter { it.isNotBlank() }
                                 .take(3)
                                 .forEach { tag ->
                                     Box(
                                         modifier = Modifier
                                             .clip(RoundedCornerShape(6.dp))
                                             .background(DesignTokens.RoyalPurple.copy(alpha = 0.12f))
                                             .padding(horizontal = 6.dp, vertical = 1.5.dp)
                                     ) {
                                         Text(
                                             text = "#$tag",
                                             color = DesignTokens.RoyalPurple,
                                             fontSize = 9.sp,
                                             fontWeight = FontWeight.Bold
                                         )
                                     }
                                 }
                         }
                     }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Detail Price & formatted Short Date
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    val isExp = transaction.type == "expense" || transaction.type == "transfer"
                    val styledAmount = if (transaction.type == "transfer") {
                        formatAmount(transaction.amount, transaction.currency)
                    } else if (isExp) {
                        "-${formatAmount(transaction.amount, transaction.currency)}"
                    } else {
                        "+${formatAmount(transaction.amount, transaction.currency)}"
                    }
                    val amountColor = if (transaction.type == "transfer") {
                        textPrimary
                    } else if (isExp) {
                        DesignTokens.AlertRose
                    } else {
                        DesignTokens.IncomeGreen
                    }

                    Text(
                        text = styledAmount,
                        color = amountColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = formatShortDate(transaction.date),
                        color = textSecondary,
                        fontSize = 9.sp
                    )
                }
            }

            // Expanded Quick Delete prompt if long pressed
            AnimatedVisibility(visible = showDeleteConfirm) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DesignTokens.AlertRose.copy(alpha = 0.08f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Видалити операцію? Баланс рахунку зміниться.",
                        color = DesignTokens.AlertRose,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { showDeleteConfirm = false }
                        ) {
                            Text("Скасувати", color = textSecondary, fontSize = 11.sp)
                        }
                        IconButton(
                            onClick = {
                                onDelete()
                                showDeleteConfirm = false
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .background(DesignTokens.AlertRose, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Так, видалити",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. TRANSACTIONS SCREEN COMPONENT
// ==========================================
@Composable
fun TransactionsScreen(
    viewModel: FinanceViewModel,
    isDark: Boolean
) {
    val filteredTransactions by viewModel.filteredTransactions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val typeFilter by viewModel.selectedTypeFilter.collectAsState()
    val categoryFilter by viewModel.selectedCategoryFilter.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val customIcons by viewModel.customIcons.collectAsState()

    val surfaceColor = if (isDark) DesignTokens.DarkSurfaceCard else DesignTokens.LightSurfaceCard
    val borderCol = if (isDark) DesignTokens.DarkBorder else DesignTokens.LightBorder
    val textPrimary = if (isDark) DesignTokens.DarkTextPrimary else DesignTokens.LightTextPrimary
    val textSecondary = if (isDark) DesignTokens.DarkTextSecondary else DesignTokens.LightTextSecondary

    val types = listOf("All", "Expense", "Income", "Transfer")
    val typesUk = mapOf("All" to "Всі", "Expense" to "Витрати", "Income" to "Доходи", "Transfer" to "Перекази")

    val categories = listOf(
        "Всі", "Продукти", "Транспорт", "Житло / Комуналка",
        "Розваги", "Здоров'я", "Одяг", "Зарплата", "Інвестиції", "Подарунок"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("transactions_screen_column")
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, borderCol)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Search outlined area
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Пошук за описом, категорією або #тегом...", color = textSecondary, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = textSecondary, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Очистити", tint = textSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_field"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary,
                        focusedBorderColor = DesignTokens.RoyalPurple,
                        unfocusedBorderColor = borderCol,
                        focusedContainerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                        unfocusedContainerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Scroll filter Type Selector
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    types.forEach { type ->
                        val isSelected = typeFilter == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) DesignTokens.RoyalPurple else if (isDark) Color(0xFF070B13) else Color(0xFFF1F5F9))
                                .border(1.dp, if (isSelected) Color.Transparent else borderCol, RoundedCornerShape(8.dp))
                                .clickable { viewModel.selectTypeFilter(type) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = typesUk[type] ?: type,
                                color = if (isSelected) Color.White else textSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Horizontal list filter chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = if (cat == "Всі") categoryFilter == null else categoryFilter == cat
                        val badgeColor = if (cat == "Всі") DesignTokens.RoyalPurple else getCategoryDetails(cat, isDark).second

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) badgeColor.copy(alpha = 0.2f) else if (isDark) Color(0xFF070B13) else Color(0xFFF1F5F9))
                                .border(1.dp, if (isSelected) badgeColor else borderCol, RoundedCornerShape(16.dp))
                                .clickable {
                                    if (cat == "Всі") viewModel.selectCategoryFilter(null) else viewModel.selectCategoryFilter(cat)
                                }
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) textPrimary else textSecondary,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                // Extract unique tags present across filtered transactions to display
                val allUniqueTags = remember(filteredTransactions) {
                    filteredTransactions.flatMap { tx ->
                        tx.tags.split(Regex("[,;\\s]+"))
                            .map { it.trim().trim('#', ',', ';').lowercase() }
                            .filter { it.isNotBlank() }
                    }.distinct()
                }

                if (allUniqueTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Фільтр за тегами:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = textSecondary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(allUniqueTags) { tag ->
                            val isSelected = searchQuery.contains(tag, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) DesignTokens.RoyalPurple.copy(alpha = 0.22f)
                                        else if (isDark) Color(0xFF070B13) else Color(0xFFF1F5F9)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) DesignTokens.RoyalPurple else borderCol,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        if (isSelected) {
                                            viewModel.updateSearchQuery("")
                                        } else {
                                            viewModel.updateSearchQuery("#$tag")
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "#$tag",
                                    color = if (isSelected) DesignTokens.RoyalPurple else textSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Real-time quick reporting and totals
        val filteredIncomeUah = remember(filteredTransactions) {
            filteredTransactions.filter { it.type == "income" }.sumOf { tx ->
                val rate = viewModel.exchangeRates[tx.currency] ?: 1.0
                tx.amount * rate
            }
        }
        val filteredExpenseUah = remember(filteredTransactions) {
            filteredTransactions.filter { it.type == "expense" }.sumOf { tx ->
                val rate = viewModel.exchangeRates[tx.currency] ?: 1.0
                tx.amount * rate
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Знайдено: ${filteredTransactions.size}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textSecondary
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (filteredIncomeUah > 0.0) {
                    Text(
                        text = "+${formatAmount(filteredIncomeUah, "UAH")}",
                        color = DesignTokens.IncomeGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (filteredExpenseUah > 0.0) {
                    Text(
                        text = "-${formatAmount(filteredExpenseUah, "UAH")}",
                        color = DesignTokens.AlertRose,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Transactions list representation scrolled
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("filtered_transactions_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp, top = 2.dp)
        ) {
            if (filteredTransactions.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = textSecondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Операцій не знайдено",
                            color = textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Спробуйте змінити фільтри або запит пошуку.",
                            color = textSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                items(filteredTransactions, key = { it.id }) { tx ->
                    TransactionListItem(
                        transaction = tx,
                        accounts = accounts,
                        onDelete = { viewModel.deleteTransaction(tx) },
                        isDark = isDark,
                        modifier = Modifier.padding(vertical = 4.dp),
                        customIconKey = customIcons[tx.category]
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. BUDGETS SCREEN COMPONENT
// ==========================================
@Composable
fun BudgetsScreen(
    viewModel: FinanceViewModel,
    isDark: Boolean
) {
    val report by viewModel.budgetReport.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val customIcons by viewModel.customIcons.collectAsState()
    var showAddBudgetDialog by remember { mutableStateOf(false) }

    val surfaceColor = if (isDark) DesignTokens.DarkSurfaceCard else DesignTokens.LightSurfaceCard
    val borderCol = if (isDark) DesignTokens.DarkBorder else DesignTokens.LightBorder
    val textPrimary = if (isDark) DesignTokens.DarkTextPrimary else DesignTokens.LightTextPrimary
    val textSecondary = if (isDark) DesignTokens.DarkTextSecondary else DesignTokens.LightTextSecondary

    val allCategories = accounts.filter { it.kind == "expense_category" }.map { it.name }
    val categoriesWithBudgets = report.map { it.category }
    val categoriesWithNoBudgets = allCategories.filter { it !in categoriesWithBudgets }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("budgets_screen_column")
    ) {
        // Banner info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, borderCol)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(DesignTokens.RoyalPurple.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.NotificationImportant,
                        contentDescription = null,
                        tint = DesignTokens.RoyalPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Організація лімітів",
                        color = textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Контролюйте ваші щомісячні витрати за категоріями. Отримайте кольорове сповіщення при досягненні 70% ліміту.",
                        color = textSecondary,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Встановлені ліміти",
                color = textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = { showAddBudgetDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = DesignTokens.RoyalPurple),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.testTag("btn_set_limit")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp).padding(end = 2.dp))
                Text("Ліміт", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // List showing budgets progress
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("budget_progress_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp)
        ) {
            if (report.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = null,
                            tint = textSecondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Лімітів не встановлено",
                            color = textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Натисніть кнопку вище, щоб додати ваш перший місячний ліміт витрат.",
                            color = textSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp)
                        )
                    }
                }
            } else {
                items(report) { item ->
                    BudgetReportItemCard(
                        reportItem = item,
                        isDark = isDark,
                        onDelete = { viewModel.deleteBudget(item.category) },
                        modifier = Modifier.padding(vertical = 4.dp),
                        customIconKey = customIcons[item.category]
                    )
                }
            }
        }
    }

    if (showAddBudgetDialog) {
        AddBudgetDialog(
            categories = categoriesWithNoBudgets,
            onDismiss = { showAddBudgetDialog = false },
            onAdd = { category, limit ->
                viewModel.addBudget(category, limit)
                showAddBudgetDialog = false
            }
        )
    }
}

@Composable
fun AnalyticsScreen(
    viewModel: FinanceViewModel,
    isDark: Boolean
) {
    val totalIncome by viewModel.totalIncomeUah.collectAsState()
    val totalExpense by viewModel.totalExpenseUah.collectAsState()
    val expensesByCategory by viewModel.expensesByCategoryUah.collectAsState()

    val surfaceColor = if (isDark) DesignTokens.DarkSurfaceCard else DesignTokens.LightSurfaceCard
    val borderCol = if (isDark) DesignTokens.DarkBorder else DesignTokens.LightBorder
    val textPrimary = if (isDark) DesignTokens.DarkTextPrimary else DesignTokens.LightTextPrimary
    val textSecondary = if (isDark) DesignTokens.DarkTextSecondary else DesignTokens.LightTextSecondary

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("analytics_screen_column"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Net Income Sheet Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, borderCol)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Фінансовий огляд",
                    color = textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Загальний баланс за весь час",
                    fontSize = 11.sp,
                    color = textSecondary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Надійшло", color = DesignTokens.IncomeGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(formatAmount(totalIncome, "UAH"), color = textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    }

                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp)
                            .background(borderCol)
                    )

                    Column {
                        Text("Витрачено", color = DesignTokens.AlertRose, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(formatAmount(totalExpense, "UAH"), color = textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    }

                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp)
                            .background(borderCol)
                    )

                    Column {
                        val netSavings = totalIncome - totalExpense
                        Text("Заощадження", color = if (netSavings >= 0) DesignTokens.IncomeGreen else DesignTokens.AlertRose, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(formatAmount(netSavings, "UAH"), color = textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        // Donut Chart Card
        if (expensesByCategory.isNotEmpty()) {
            ExpensesDonutChartCard(expensesByCategory = expensesByCategory, isDark = isDark)

            // Detailed Breakdown
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, borderCol)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Деталі за категоріями",
                        color = textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    val total = expensesByCategory.values.sum()
                    expensesByCategory.toList().sortedByDescending { it.second }.forEach { (cat, spend) ->
                        val percent = if (total > 0) (spend / total).toFloat() else 0f
                        val (_, color) = getCategoryDetails(cat, isDark)

                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(color, CircleShape)
                                    )
                                    Text(cat, color = textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = "${String.format("%.0f", percent * 100)}% • ${formatAmount(spend, "UAH")}",
                                    color = textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            // Progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(borderCol)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(percent)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(color)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, borderCol)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PieChart,
                        contentDescription = null,
                        tint = textSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Аналітика відсутня",
                        color = textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Додайте ваші перші витрати, щоб побачити аналітику по категоріях.",
                        color = textSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BudgetReportItemCard(
    reportItem: BudgetReportItem,
    isDark: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    customIconKey: String? = null
) {
    val (_, tint) = getCategoryDetails(reportItem.category, isDark, customIconKey)

    val surfaceColor = if (isDark) DesignTokens.DarkSurfaceCard else DesignTokens.LightSurfaceCard
    val textPrimary = if (isDark) DesignTokens.DarkTextPrimary else DesignTokens.LightTextPrimary
    val textSecondary = if (isDark) DesignTokens.DarkTextSecondary else DesignTokens.LightTextSecondary

    val isOver = reportItem.isOverBudget
    val isWarning = !isOver && reportItem.progress >= 0.7f

    // Nature mapping determined purely by percentage in SELFY Wallet
    val progressColor = when {
        isOver -> DesignTokens.AlertRose
        isWarning -> DesignTokens.WarningAmber
        else -> DesignTokens.RoyalPurple
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("budget_card_${reportItem.category}"),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, progressColor.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(tint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = getCategoryDetails(reportItem.category, isDark, customIconKey).first
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reportItem.category,
                        color = textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isOver) {
                            "Перевищення ліміту!"
                        } else if (isWarning) {
                            "Майже вичерпано (70%+)"
                        } else {
                            "Бюджет в межах норми"
                        },
                        color = progressColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Видалити ліміт",
                        tint = textSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { reportItem.progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = progressColor,
                trackColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = formatAmount(reportItem.spent),
                        color = textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " з " + formatAmount(reportItem.limit),
                        color = textSecondary,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(start = 2.dp, bottom = 1.dp)
                    )
                }

                if (isOver) {
                    Text(
                        text = "▲ надліміт: " + formatAmount(reportItem.spent - reportItem.limit),
                        color = DesignTokens.AlertRose,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "залишок: " + formatAmount(reportItem.remaining),
                        color = textSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ==========================================
// 4. DIALOGS
// ==========================================

// ==========================================
// 4. DIALOGS
// ==========================================

sealed class DraggedCoin {
    data class Income(val name: String) : DraggedCoin()
    data class AccountRef(val account: Account) : DraggedCoin()
}

@Composable
fun QuickEntryTile(
    name: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    isDark: Boolean,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    badgeText: String? = null,
    isHovered: Boolean = false,
) {
    val textPrimary = if (isDark) DesignTokens.DarkTextPrimary else DesignTokens.LightTextPrimary
    val textSecondary = if (isDark) DesignTokens.DarkTextSecondary else DesignTokens.LightTextSecondary
    val surfaceColor = if (isDark) DesignTokens.DarkSurfaceCard else DesignTokens.LightSurfaceCard
    val borderCol = if (isDark) DesignTokens.DarkBorder else DesignTokens.LightBorder

    val hoverScale by animateFloatAsState(targetValue = if (isHovered) 1.15f else if (isSelected) 1.03f else 1.0f)

    Box(
        modifier = modifier
            .scale(hoverScale)
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isHovered) tint.copy(alpha = 0.16f)
                else if (isSelected) tint.copy(alpha = 0.12f)
                else surfaceColor
            )
            .border(
                width = if (isHovered) 2.5.dp else if (isSelected) 2.dp else 1.dp,
                color = if (isHovered) tint else if (isSelected) tint else borderCol,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(tint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Selected",
                        tint = tint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = name,
                    color = textPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                if (badgeText != null) {
                    Text(
                        text = badgeText,
                        color = if (isSelected) tint else textSecondary,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// Add Transaction Dialog (Full-fledged "3-row structure" CoinKeeper logic with true drag-and-drop gestures)
@Composable
fun AddTransactionDialog(
    viewModel: FinanceViewModel,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val accounts by viewModel.accounts.collectAsState()
    val customIcons by viewModel.customIcons.collectAsState()

    val surfaceColor = if (isDark) DesignTokens.DarkSurfaceCard else DesignTokens.LightSurfaceCard
    val borderCol = if (isDark) DesignTokens.DarkBorder else DesignTokens.LightBorder
    val textPrimary = if (isDark) DesignTokens.DarkTextPrimary else DesignTokens.LightTextPrimary
    val textSecondary = if (isDark) DesignTokens.DarkTextSecondary else DesignTokens.LightTextSecondary

    // Quick Entry Selected States
    var selectedIncomeSource by remember { mutableStateOf<String?>(null) } // Row 1
    var selectedAccountSource by remember { mutableStateOf<Account?>(null) } // Row 2
    var selectedAccountDest by remember { mutableStateOf<Account?>(null) } // Row 2 (relink Transfer)
    var selectedExpenseCategory by remember { mutableStateOf<String?>(null) } // Row 3

    var amountText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }
    var tagsText by remember { mutableStateOf("") }

    // Multi-currency cross transfers
    var isCrossCurrency by remember { mutableStateOf(false) }
    var counterpartyAmountText by remember { mutableStateOf("") }

    // Drag and Drop States for interactive CoinKeeper mechanics
    var draggedCoin by remember { mutableStateOf<DraggedCoin?>(null) }
    var dragStartOffset by remember { mutableStateOf(Offset.Zero) }
    var dragAmountOffset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }

    // Drop Target coordinates
    val targetBoundsMap = remember { mutableStateMapOf<String, androidx.compose.ui.geometry.Rect>() }
    var currentHoveredTargetKey by remember { mutableStateOf<String?>(null) }

    // On-screen coordinates registry of active coins
    val coinPositionsMap = remember { mutableStateMapOf<String, Offset>() }

    val getActiveTargetKey: (Offset) -> String? = { currentWindowPos ->
        var active: String? = null
        for ((key, rect) in targetBoundsMap.toMap()) {
            if (rect.contains(currentWindowPos)) {
                active = key
                break
            }
        }
        active
    }

    // Draggable Modifier for Coins
    @Composable
    fun Modifier.makeCoinDraggable(
        item: DraggedCoin,
        coinKey: String
    ): Modifier {
        val currentItem by rememberUpdatedState(item)
        val currentCoinKey by rememberUpdatedState(coinKey)
        return this
            .onGloballyPositioned { coords ->
                coinPositionsMap[currentCoinKey] = coords.positionInWindow()
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val itemWindowPos = coinPositionsMap[currentCoinKey] ?: Offset.Zero
                        draggedCoin = currentItem
                        dragStartOffset = itemWindowPos + offset
                        dragAmountOffset = Offset.Zero
                        isDragging = true
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragAmountOffset += dragAmount
                        val currentFingerPos = dragStartOffset + dragAmountOffset
                        currentHoveredTargetKey = getActiveTargetKey(currentFingerPos)
                    },
                    onDragEnd = {
                        val finalFingerPos = dragStartOffset + dragAmountOffset
                        val target = getActiveTargetKey(finalFingerPos)
                        if (target != null) {
                            when (val drag = draggedCoin) {
                                is DraggedCoin.Income -> {
                                    if (target.startsWith("account_target_")) {
                                        val accId = target.substringAfter("account_target_").toIntOrNull()
                                        val match = accounts.find { it.id == accId }
                                        if (match != null) {
                                            selectedIncomeSource = drag.name
                                            selectedAccountSource = match
                                            selectedAccountDest = null
                                            selectedExpenseCategory = null
                                        }
                                    }
                                }
                                is DraggedCoin.AccountRef -> {
                                    if (target.startsWith("category_target_")) {
                                        val catName = target.substringAfter("category_target_")
                                        selectedAccountSource = drag.account
                                        selectedExpenseCategory = catName
                                        selectedIncomeSource = null
                                        selectedAccountDest = null
                                    } else if (target.startsWith("account_dest_target_")) {
                                        val destId = target.substringAfter("account_dest_target_").toIntOrNull()
                                        val match = accounts.find { it.id == destId }
                                        if (match != null && match.id != drag.account.id) {
                                            selectedAccountSource = drag.account
                                            selectedAccountDest = match
                                            selectedIncomeSource = null
                                            selectedExpenseCategory = null
                                        }
                                    }
                                }
                                null -> {}
                            }
                        }
                        isDragging = false
                        draggedCoin = null
                        currentHoveredTargetKey = null
                    },
                    onDragCancel = {
                        isDragging = false
                        draggedCoin = null
                        currentHoveredTargetKey = null
                    }
                )
            }
    }

    // Derived Financial Operation Type from "3-row selection matrix":
    // 1. IncomeSource selected AND Account selected = Income (Дохід)
    // 2. Account selected AND Category selected = Expense (Витрата)
    // 3. Account selected AND AccountDest selected = Transfer (Переказ)
    val opDetails = remember(selectedIncomeSource, selectedAccountSource, selectedAccountDest, selectedExpenseCategory) {
        when {
            selectedIncomeSource != null && selectedAccountSource != null -> {
                Triple("income", "Надходження на ${selectedAccountSource?.name}", DesignTokens.IncomeGreen)
            }
            selectedAccountSource != null && selectedAccountDest != null -> {
                Triple("transfer", "Переказ: ${selectedAccountSource?.name} → ${selectedAccountDest?.name}", DesignTokens.RoyalPurple)
            }
            selectedAccountSource != null && selectedExpenseCategory != null -> {
                Triple("expense", "Витрата: ${selectedAccountSource?.name} у ${selectedExpenseCategory}", DesignTokens.RoyalPurple)
            }
            selectedExpenseCategory != null && selectedAccountSource != null && selectedIncomeSource == null -> {
                // Category to Account is defined as Refund (Повернення)
                Triple("refund", "Повернення на ${selectedAccountSource?.name}", DesignTokens.IncomeGreen)
            }
            else -> {
                Triple("expense", "Оберіть 'Звідки' та 'Куди'", textSecondary)
            }
        }
    }

    LaunchedEffect(selectedAccountSource, selectedAccountDest) {
        if (selectedAccountSource != null && selectedAccountDest != null) {
            isCrossCurrency = selectedAccountSource?.currency != selectedAccountDest?.currency
            if (!isCrossCurrency) {
                counterpartyAmountText = ""
            }
        } else {
            isCrossCurrency = false
            counterpartyAmountText = ""
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) DesignTokens.DarkSlateBg else DesignTokens.LightSlateBg),
            color = if (isDark) DesignTokens.DarkSlateBg else DesignTokens.LightSlateBg
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Top Exit Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Rounded.Close, contentDescription = "Закрити", tint = textPrimary)
                        }
                        Text(
                            text = "Швидкий Запис (CoinKeeper)",
                            color = textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        // Submit button
                        TextButton(
                            onClick = {
                                val amt = amountText.toDoubleOrNull() ?: 0.0
                                val type = opDetails.first
                                val accId = selectedAccountSource?.id ?: 0

                                if (type == "income" && selectedIncomeSource != null) {
                                    viewModel.addTransaction(amt, type, selectedIncomeSource!!, descriptionText, accId, tags = tagsText)
                                } else if (type == "transfer" && selectedAccountDest != null) {
                                    val destAmt = counterpartyAmountText.toDoubleOrNull() ?: amt
                                    viewModel.addTransaction(amt, type, "Переказ", descriptionText, accId, selectedAccountDest!!.id, destAmt, tags = tagsText)
                                } else if (type == "expense" && selectedExpenseCategory != null) {
                                    viewModel.addTransaction(amt, type, selectedExpenseCategory!!, descriptionText, accId, tags = tagsText)
                                }
                                onDismiss()
                            },
                            enabled = amountText.isNotEmpty() && (amountText.toDoubleOrNull() ?: 0.0) > 0.0 &&
                                    (selectedAccountSource != null) &&
                                    (selectedExpenseCategory != null || selectedIncomeSource != null || selectedAccountDest != null),
                            modifier = Modifier.testTag("submit_transaction_button")
                        ) {
                            Text(
                                "Записати",
                                color = if (amountText.isNotEmpty() && selectedAccountSource != null) DesignTokens.RoyalPurple else textSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    // Dynamic Help Alert regarding drag mechanics
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = DesignTokens.RoyalPurple.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, DesignTokens.RoyalPurple.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.Star, contentDescription = null, tint = DesignTokens.RoyalPurple, modifier = Modifier.size(18.dp))
                            Text(
                                text = "💡 ПЕРЕТЯГУЙТЕ МОНЕТКИ: тягніть Дохід 🟢 у Рахунок 🔵 або Рахунок 🔵 у Категорію 🔴 для миттєвого пов'язування!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Unified Flow 3-Row CoinKeeper Selection Cards Stack
                    Text(
                        text = "Крок 1. ДЖЕРЕЛО / ЗВІДКИ",
                        fontSize = 11.sp,
                        color = DesignTokens.RoyalPurple,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                    )

                    // Dynamic Separation of Accounts list into dynamic Income Sources, Real Accounts, and Expense Categories
                    val dynamicIncomeSources = accounts.filter { it.kind == "income_source" }
                    val dynamicRealAccounts = accounts.filter { it.kind != "income_source" && it.kind != "expense_category" }
                    val dynamicExpenseCategories = accounts.filter { it.kind == "expense_category" }

                    // ROW 1: INCOME SOURCES (Green Header Flow)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = surfaceColor),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, borderCol)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("🟢 ДОХОДИ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DesignTokens.IncomeGreen)
                            Spacer(modifier = Modifier.height(8.dp))
                            GridOf4Columns(spacing = 6.dp) {
                                dynamicIncomeSources.forEach { inc ->
                                    val isSelected = selectedIncomeSource == inc.name
                                    val coinKey = "income_${inc.name}"
                                    val (icon, _) = getCategoryDetails(inc.name, isDark, customIcons[inc.name])

                                    QuickEntryTile(
                                        name = inc.name,
                                        icon = icon,
                                        tint = DesignTokens.IncomeGreen,
                                        isDark = isDark,
                                        isSelected = isSelected,
                                        badgeText = "Джерело",
                                        modifier = Modifier
                                            .makeCoinDraggable(DraggedCoin.Income(inc.name), coinKey)
                                            .clickable {
                                                selectedIncomeSource = if (isSelected) null else inc.name
                                                if (selectedIncomeSource != null) {
                                                    selectedExpenseCategory = null
                                                    selectedAccountDest = null
                                                }
                                            }
                                    )
                                }
                            }
                        }
                    }

                    // ROW 2: ACTIVE ACCOUNTS (Source / Dest)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = surfaceColor),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, borderCol)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("🔵 РАХУНКИ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DesignTokens.RoyalPurple)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Zvidky (Source) horizontal list (Draggable onto Categories)
                            Text("Звідки кошти:", fontSize = 10.sp, color = textSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            GridOf4Columns(spacing = 6.dp) {
                                dynamicRealAccounts.forEach { acc ->
                                    val isSelected = selectedAccountSource?.id == acc.id
                                    val coinKey = "account_source_${acc.id}"
                                    val targetKey = "account_target_${acc.id}"

                                    val isHovered = currentHoveredTargetKey == targetKey

                                    val (defaultIcon, tint) = when (acc.kind) {
                                        "cash" -> Icons.Rounded.Payments to Color(0xFF10B981)
                                        "current" -> Icons.Rounded.CreditCard to Color(0xFF3B82F6)
                                        "savings" -> Icons.Rounded.Savings to Color(0xFFEC4899)
                                        "deposit" -> Icons.Rounded.AccountBalance to Color(0xFF8B5CF6)
                                        else -> Icons.Rounded.Wallet to DesignTokens.RoyalPurple
                                    }
                                    val customIconKey = customIcons[acc.name]
                                    val icon = if (customIconKey != null) {
                                        CATEGORY_ICONS_MAP[customIconKey] ?: defaultIcon
                                    } else {
                                        defaultIcon
                                    }

                                    QuickEntryTile(
                                        name = acc.name,
                                        icon = icon,
                                        tint = tint,
                                        isDark = isDark,
                                        isSelected = isSelected,
                                        badgeText = formatAmount(acc.currentBalance, acc.currency),
                                        isHovered = isHovered,
                                        modifier = Modifier
                                            .onGloballyPositioned { coords ->
                                                targetBoundsMap[targetKey] = coords.boundsInWindow()
                                            }
                                            .makeCoinDraggable(DraggedCoin.AccountRef(acc), coinKey)
                                            .clickable {
                                                selectedAccountSource = if (isSelected) null else acc
                                            }
                                    )
                                }
                            }

                            // Kudy (Destination for Transfer)
                            if (selectedAccountSource != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Переказ на:", fontSize = 10.sp, color = textSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                GridOf4Columns(spacing = 6.dp) {
                                    dynamicRealAccounts.filter { it.id != selectedAccountSource?.id }.forEach { acc ->
                                        val isSelected = selectedAccountDest?.id == acc.id
                                        val targetKey = "account_dest_target_${acc.id}"

                                        val isHovered = currentHoveredTargetKey == targetKey

                                        val (defaultIcon, tint) = when (acc.kind) {
                                            "cash" -> Icons.Rounded.Payments to Color(0xFF10B981)
                                            "current" -> Icons.Rounded.CreditCard to Color(0xFF3B82F6)
                                            "savings" -> Icons.Rounded.Savings to Color(0xFFEC4899)
                                            "deposit" -> Icons.Rounded.AccountBalance to Color(0xFF8B5CF6)
                                            else -> Icons.Rounded.Wallet to DesignTokens.RoyalPurple
                                        }
                                        val customIconKey = customIcons[acc.name]
                                        val icon = if (customIconKey != null) {
                                            CATEGORY_ICONS_MAP[customIconKey] ?: defaultIcon
                                        } else {
                                            defaultIcon
                                        }

                                        QuickEntryTile(
                                            name = acc.name,
                                            icon = icon,
                                            tint = tint,
                                            isDark = isDark,
                                            isSelected = isSelected,
                                            badgeText = formatAmount(acc.currentBalance, acc.currency),
                                            isHovered = isHovered,
                                            modifier = Modifier
                                                .onGloballyPositioned { coords ->
                                                    targetBoundsMap[targetKey] = coords.boundsInWindow()
                                                }
                                                .clickable {
                                                    selectedAccountDest = if (isSelected) null else acc
                                                    if (selectedAccountDest != null) {
                                                        // Transfers prevent categories/incomes
                                                        selectedExpenseCategory = null
                                                        selectedIncomeSource = null
                                                    }
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = "Крок 2. ПРИЗНАЧЕННЯ / КУДИ",
                        fontSize = 11.sp,
                        color = DesignTokens.RoyalPurple,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp, start = 4.dp)
                    )

                    // ROW 3: EXPENSE CATEGORIES (With ring/circular limits visual status check!)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = surfaceColor),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, borderCol)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("🔴 КАТЕГОРІЇ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DesignTokens.AlertRose)
                            Spacer(modifier = Modifier.height(8.dp))

                            GridOf4Columns(spacing = 6.dp) {
                                dynamicExpenseCategories.forEach { cat ->
                                    val isSelected = selectedExpenseCategory == cat.name
                                    val (icon, tint) = getCategoryDetails(cat.name, isDark, customIcons[cat.name])
                                    val targetKey = "category_target_${cat.name}"

                                    val isHovered = currentHoveredTargetKey == targetKey

                                    QuickEntryTile(
                                        name = cat.name,
                                        icon = icon,
                                        tint = tint,
                                        isDark = isDark,
                                        isSelected = isSelected,
                                        badgeText = "Витрата",
                                        isHovered = isHovered,
                                        modifier = Modifier
                                            .onGloballyPositioned { coords ->
                                                targetBoundsMap[targetKey] = coords.boundsInWindow()
                                            }
                                            .clickable {
                                                selectedExpenseCategory = if (isSelected) null else cat.name
                                                if (selectedExpenseCategory != null) {
                                                    selectedIncomeSource = null
                                                    selectedAccountDest = null
                                                }
                                            }
                                    )
                                }
                            }
                        }
                    }

                // FLOW STATUS BAR INDICATING DETECTION TYPE
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = opDetails.third.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = opDetails.third
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = opDetails.second,
                            color = textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // NUMERICAL AMOUNT INPUT FOR HIGH ACCURACY IN 1 TAP (Calculator style)
                Text(
                    text = "Введіть суму",
                    fontSize = 11.sp,
                    color = textSecondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(surfaceColor)
                        .border(1.dp, borderCol, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        val activeCurrency = selectedAccountSource?.currency ?: "UAH"
                        Text(
                            text = if (amountText.isEmpty()) "0.00 $activeCurrency" else "$amountText $activeCurrency",
                            color = if (opDetails.first == "income") DesignTokens.IncomeGreen else DesignTokens.RoyalPurple,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.testTag("amount_field")
                        )

                        // Cross-currency input details
                        if (isCrossCurrency) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Отримає у ${selectedAccountDest?.currency}:",
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                            val receiveCcy = selectedAccountDest?.currency ?: "UAH"
                            OutlinedTextField(
                                value = counterpartyAmountText,
                                onValueChange = { if (it.all { ch -> ch.isDigit() || ch == '.' }) counterpartyAmountText = it },
                                placeholder = { Text("Сума зарахування $receiveCcy", color = textSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DesignTokens.RoyalPurple,
                                    unfocusedBorderColor = borderCol
                                )
                            )
                        }
                    }

                    // Keypad Helper triggers
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val incs = listOf("100", "500", "1000")
                        incs.forEach { inc ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                                    .clickable {
                                        val cur = amountText.toDoubleOrNull() ?: 0.0
                                        amountText = (cur + inc.toDouble()).toString()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("+$inc", color = textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Optional Description text box
                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    placeholder = { Text("Додати коментар чи опис...", color = textSecondary, fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("description_field"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary,
                        focusedBorderColor = DesignTokens.RoyalPurple,
                        unfocusedBorderColor = borderCol,
                        focusedContainerColor = surfaceColor,
                        unfocusedContainerColor = surfaceColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Optional Tags/Subcategory text box
                OutlinedTextField(
                    value = tagsText,
                    onValueChange = { tagsText = it },
                    placeholder = { Text("Тег чи підстаття (наприклад: таксі, вечеря)", color = textSecondary, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.LocalOffer,
                            contentDescription = null,
                            tint = DesignTokens.RoyalPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tags_field"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary,
                        focusedBorderColor = DesignTokens.RoyalPurple,
                        unfocusedBorderColor = borderCol,
                        focusedContainerColor = surfaceColor,
                        unfocusedContainerColor = surfaceColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Floating Coin Overlay for realistic dragging visual sensation (CoinKeeper experience)
            if (isDragging && draggedCoin != null) {
                val finalOffset = dragStartOffset + dragAmountOffset
                val density = LocalDensity.current
                val xDp = with(density) { finalOffset.x.toDp() }
                val yDp = with(density) { finalOffset.y.toDp() }

                Box(
                    modifier = Modifier
                        .offset(x = xDp - 45.dp, y = yDp - 45.dp) // center visual focus
                        .size(90.dp)
                        .shadow(16.dp, shape = androidx.compose.foundation.shape.CircleShape)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(
                            when (draggedCoin) {
                                is DraggedCoin.Income -> DesignTokens.IncomeGreen
                                else -> DesignTokens.RoyalPurple
                            }
                        )
                        .border(3.dp, Color.White, androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(6.dp)
                    ) {
                        Icon(
                            imageVector = when (draggedCoin) {
                                is DraggedCoin.Income -> Icons.Rounded.AddCircle
                                else -> Icons.Rounded.AccountBalanceWallet
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (val drag = draggedCoin) {
                                is DraggedCoin.Income -> drag.name
                                is DraggedCoin.AccountRef -> drag.account.name
                                else -> ""
                            },
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
}

// Add Account Dialog Form Card Composable
@Composable
fun AddAccountDialog(
    isDark: Boolean,
    onDismiss: () -> Unit,
    onAdd: (name: String, kind: String, balanceNature: String, currency: String, initialBalance: Double, depositRate: Double?, depositTermMonths: Int?) -> Unit
) {
    val surfaceColor = if (isDark) DesignTokens.DarkSurfaceCard else DesignTokens.LightSurfaceCard
    val borderCol = if (isDark) DesignTokens.DarkBorder else DesignTokens.LightBorder
    val textPrimary = if (isDark) DesignTokens.DarkTextPrimary else DesignTokens.LightTextPrimary
    val textSecondary = if (isDark) DesignTokens.DarkTextSecondary else DesignTokens.LightTextSecondary

    var nameText by remember { mutableStateOf("") }
    var initialBalanceText by remember { mutableStateOf("") }

    val kinds = listOf("cash", "current", "savings", "deposit")
    val kindsUk = mapOf("cash" to "Готівка", "current" to "Картка", "savings" to "Скарбничка", "deposit" to "Депозит")
    var selectedKind by remember { mutableStateOf("cash") }

    val currencies = listOf("UAH", "USD", "EUR", "PLN")
    var selectedCurrency by remember { mutableStateOf("UAH") }

    // Deposit fields
    var depositRateText by remember { mutableStateOf("") }
    var depositMonthsText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Створити Рахунок", color = textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Name text entry
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Назва рахунку", color = textSecondary) },
                    modifier = Modifier.fillMaxWidth().testTag("account_name_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textPrimary, unfocusedTextColor = textPrimary,
                        focusedBorderColor = DesignTokens.RoyalPurple, unfocusedBorderColor = borderCol
                    )
                )

                // Select Kind Flow
                Text("Тип рахунку:", color = textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    kinds.forEach { kind ->
                        val isSel = selectedKind == kind
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) DesignTokens.RoyalPurple else if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                                .clickable { selectedKind = kind }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(kindsUk[kind] ?: kind, color = if (isSel) Color.White else textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Select Currency Flow
                Text("Валюта:", color = textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    currencies.forEach { ccy ->
                        val isSel = selectedCurrency == ccy
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) DesignTokens.RoyalPurple else if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                                .clickable { selectedCurrency = ccy }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(ccy, color = if (isSel) Color.White else textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Initial Balance Entry
                OutlinedTextField(
                    value = initialBalanceText,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) initialBalanceText = it },
                    label = { Text("Початковий баланс", color = textSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("account_initial_balance"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textPrimary, unfocusedTextColor = textPrimary,
                        focusedBorderColor = DesignTokens.RoyalPurple, unfocusedBorderColor = borderCol
                    )
                )

                // Additional details for Deposits
                if (selectedKind == "deposit") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = depositRateText,
                            onValueChange = { if (it.all { ch -> ch.isDigit() || ch == '.' }) depositRateText = it },
                            label = { Text("Ставка %", color = textSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textPrimary, unfocusedTextColor = textPrimary,
                                focusedBorderColor = DesignTokens.RoyalPurple, unfocusedBorderColor = borderCol
                            )
                        )
                        OutlinedTextField(
                            value = depositMonthsText,
                            onValueChange = { if (it.all { ch -> ch.isDigit() }) depositMonthsText = it },
                            label = { Text("Термін (м)", color = textSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textPrimary, unfocusedTextColor = textPrimary,
                                focusedBorderColor = DesignTokens.RoyalPurple, unfocusedBorderColor = borderCol
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val initial = initialBalanceText.toDoubleOrNull() ?: 0.0
                    val rate = depositRateText.toDoubleOrNull()
                    val months = depositMonthsText.toIntOrNull()
                    val nature = "asset"

                    onAdd(nameText, selectedKind, nature, selectedCurrency, initial, rate, months)
                },
                enabled = nameText.isNotEmpty() && initialBalanceText.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = DesignTokens.RoyalPurple),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_save_account")
            ) {
                Text("Створити", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Скасувати", color = textSecondary)
            }
        },
        containerColor = surfaceColor,
        shape = RoundedCornerShape(20.dp),
        properties = DialogProperties(usePlatformDefaultWidth = true)
    )
}

@Composable
fun EditAccountDialog(
    account: com.example.data.Account,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onSave: (newName: String, newBalance: Double, displayInTotal: Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val surfaceColor = if (isDark) DesignTokens.DarkSurfaceCard else DesignTokens.LightSurfaceCard
    val borderCol = if (isDark) DesignTokens.DarkBorder else DesignTokens.LightBorder
    val textPrimary = if (isDark) DesignTokens.DarkTextPrimary else DesignTokens.LightTextPrimary
    val textSecondary = if (isDark) DesignTokens.DarkTextSecondary else DesignTokens.LightTextSecondary

    var nameText by remember { mutableStateOf(account.name) }
    var balanceText by remember { mutableStateOf(account.currentBalance.toString()) }
    var displayInTotal by remember { mutableStateOf(account.displayInTotalBalance) }

    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Видалити рахунок?", color = textPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Ви дійсно хочете видалити рахунок \"${account.name}\"? Це також видалить усі пов'язані з ним транзакції для чистоти звітів.", color = textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Видалити", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Скасувати", color = textSecondary)
                }
            },
            containerColor = surfaceColor,
            shape = RoundedCornerShape(20.dp)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редагувати Рахунок", color = textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Name text entry
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Назва рахунку", color = textSecondary) },
                    modifier = Modifier.fillMaxWidth().testTag("edit_account_name_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textPrimary, unfocusedTextColor = textPrimary,
                        focusedBorderColor = DesignTokens.RoyalPurple, unfocusedBorderColor = borderCol
                    )
                )

                // Current Balance Entry
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { 
                        if (it.isEmpty() || it == "-" || it.all { ch -> ch.isDigit() || ch == '.' || ch == '-' }) {
                            balanceText = it
                        }
                    },
                    label = { Text("Поточний баланс (${account.currency})", color = textSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("edit_account_balance_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textPrimary, unfocusedTextColor = textPrimary,
                        focusedBorderColor = DesignTokens.RoyalPurple, unfocusedBorderColor = borderCol
                    )
                )

                // Toggle display in total balance
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                        .clickable { displayInTotal = !displayInTotal }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ураховувати в загальному балансі", color = textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Відображати кошти рахунку на загальній панелі зверху", color = textSecondary, fontSize = 10.sp)
                    }
                    Switch(
                        checked = displayInTotal,
                        onCheckedChange = { displayInTotal = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = DesignTokens.RoyalPurple
                        )
                    )
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // DELETE Button (Red styled)
                Button(
                    onClick = { showDeleteConfirmation = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Видалити", color = Color.White)
                }

                // SAVE Button
                Button(
                    onClick = {
                        val newBal = balanceText.toDoubleOrNull() ?: account.currentBalance
                        onSave(nameText, newBal, displayInTotal)
                    },
                    enabled = nameText.isNotEmpty() && balanceText.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = DesignTokens.RoyalPurple),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_save_edit_account")
                ) {
                    Text("Зберегти", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Скасувати", color = textSecondary)
            }
        },
        containerColor = surfaceColor,
        shape = RoundedCornerShape(20.dp),
        properties = DialogProperties(usePlatformDefaultWidth = true)
    )
}

// Dialog to Add/Edit monthly budget limits
@Composable
fun AddBudgetDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onAdd: (category: String, limit: Double) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "Продукти") }
    var limitText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Встановити місячний ліміт", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Категорія витрат", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                
                // Horizontal category selector if categories exist
                if (categories.isEmpty()) {
                    Text("Всі доступні категорії вже мають встановлений ліміт.", fontSize = 13.sp)
                } else {
                    var expanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { expanded = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DesignTokens.RoyalPurple.copy(alpha = 0.1f), contentColor = DesignTokens.RoyalPurple),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(selectedCategory, fontWeight = FontWeight.Bold)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        selectedCategory = cat
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) limitText = it },
                    label = { Text("Гранична сума (грн)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("budget_limit_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DesignTokens.RoyalPurple
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = limitText.toDoubleOrNull() ?: 0.0
                    onAdd(selectedCategory, limit)
                },
                enabled = limitText.isNotEmpty() && categories.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = DesignTokens.RoyalPurple),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_save_budget")
            ) {
                Text("Встановити")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Скасувати", color = DesignTokens.RoyalPurple)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

// Custom flow row layout layout helper to wrap elements
@Composable
fun FlowRowLayout(
    spacing: androidx.compose.ui.unit.Dp = 8.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        content = content
    ) { measurables, constraints ->
        var currentY = 0
        var currentX = 0
        var maxRowHeight = 0
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
        }
        val layoutHeight: Int
        val layoutWidth = constraints.maxWidth

        val positions = ArrayList<androidx.compose.ui.unit.IntOffset>()
        placeables.forEach { placeable ->
            if (currentX + placeable.width > layoutWidth) {
                currentX = 0
                currentY += maxRowHeight + spacing.roundToPx()
                maxRowHeight = 0
            }
            positions.add(androidx.compose.ui.unit.IntOffset(currentX, currentY))
            currentX += placeable.width + spacing.roundToPx()
            maxRowHeight = maxOf(maxRowHeight, placeable.height)
        }
        layoutHeight = currentY + maxRowHeight

        layout(layoutWidth, layoutHeight) {
            placeables.forEachIndexed { index, placeable ->
                placeable.placeRelative(positions[index])
            }
        }
    }
}

// Custom responsive grid layout helper that ensures exactly 3 columns fit perfectly in any width
@Composable
fun GridOf3Columns(
    spacing: androidx.compose.ui.unit.Dp = 8.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        content = content
    ) { measurables, constraints ->
        val cols = 3
        val spacingPx = spacing.roundToPx()
        val totalSpacing = spacingPx * (cols - 1)
        val childWidth = ((constraints.maxWidth - totalSpacing) / cols).coerceAtLeast(0)
        
        val childConstraints = constraints.copy(
            minWidth = childWidth,
            maxWidth = childWidth
        )
        
        val placeables = measurables.map { measurable ->
            measurable.measure(childConstraints)
        }
        
        var currentY = 0
        var currentX = 0
        var maxRowHeight = 0
        val positions = ArrayList<androidx.compose.ui.unit.IntOffset>()
        
        placeables.forEachIndexed { idx, placeable ->
            if (idx > 0 && idx % cols == 0) {
                currentX = 0
                currentY += maxRowHeight + spacingPx
                maxRowHeight = 0
            }
            positions.add(androidx.compose.ui.unit.IntOffset(currentX, currentY))
            currentX += childWidth + spacingPx
            maxRowHeight = maxOf(maxRowHeight, placeable.height)
        }
        val layoutHeight = currentY + maxRowHeight
        
        layout(constraints.maxWidth, layoutHeight) {
            placeables.forEachIndexed { idx, placeable ->
                placeable.placeRelative(positions[idx])
            }
        }
    }
}

// Custom responsive grid layout helper that ensures exactly 4 columns fit perfectly in any width
@Composable
fun GridOf4Columns(
    spacing: androidx.compose.ui.unit.Dp = 6.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        content = content
    ) { measurables, constraints ->
        val cols = 4
        val spacingPx = spacing.roundToPx()
        val totalSpacing = spacingPx * (cols - 1)
        val childWidth = ((constraints.maxWidth - totalSpacing) / cols).coerceAtLeast(0)
        
        val childConstraints = constraints.copy(
            minWidth = childWidth,
            maxWidth = childWidth
        )
        
        val placeables = measurables.map { measurable ->
            measurable.measure(childConstraints)
        }
        
        var currentY = 0
        var currentX = 0
        var maxRowHeight = 0
        val positions = ArrayList<androidx.compose.ui.unit.IntOffset>()
        
        placeables.forEachIndexed { idx, placeable ->
            if (idx > 0 && idx % cols == 0) {
                currentX = 0
                currentY += maxRowHeight + spacingPx
                maxRowHeight = 0
            }
            positions.add(androidx.compose.ui.unit.IntOffset(currentX, currentY))
            currentX += childWidth + spacingPx
            maxRowHeight = maxOf(maxRowHeight, placeable.height)
        }
        val layoutHeight = currentY + maxRowHeight
        
        layout(constraints.maxWidth, layoutHeight) {
            placeables.forEachIndexed { idx, placeable ->
                placeable.placeRelative(positions[idx])
            }
        }
    }
}

@Composable
fun AddTile(
    text: String,
    color: Color,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val surfaceColor = if (isDark) DesignTokens.DarkSurfaceCard else DesignTokens.LightSurfaceCard
    val borderCol = if (isDark) DesignTokens.DarkBorder else DesignTokens.LightBorder
    Box(
        modifier = modifier
            .height(82.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(surfaceColor)
            .border(
                BorderStroke(1.dp, color.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = text,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardTile(
    name: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    badgeText: String? = null,
    isHovered: Boolean = false,
    onArchiveClick: (() -> Unit)? = null,
    onMoveLeftClick: (() -> Unit)? = null,
    onMoveRightClick: (() -> Unit)? = null,
    onChangeIconClick: (() -> Unit)? = null,
    onEditClick: (() -> Unit)? = null
) {
    val textPrimary = if (isDark) DesignTokens.DarkTextPrimary else DesignTokens.LightTextPrimary
    val textSecondary = if (isDark) DesignTokens.DarkTextSecondary else DesignTokens.LightTextSecondary
    val surfaceColor = if (isDark) DesignTokens.DarkSurfaceCard else DesignTokens.LightSurfaceCard
    val borderCol = if (isDark) DesignTokens.DarkBorder else DesignTokens.LightBorder

    var showMenu by remember { mutableStateOf(false) }
    val hoverScale by animateFloatAsState(targetValue = if (isHovered) 1.15f else 1.0f)

    Box(
        modifier = modifier
            .scale(hoverScale)
            .height(82.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isHovered) tint.copy(alpha = 0.15f)
                else surfaceColor
            )
            .border(
                width = if (isHovered) 2.dp else 1.dp,
                color = if (isHovered) tint else borderCol,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(tint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(19.dp)
                    )
                }

                if (onArchiveClick != null || onMoveLeftClick != null || onMoveRightClick != null || onChangeIconClick != null || onEditClick != null) {
                    Box {
                        androidx.compose.material3.IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "Опції",
                                tint = textSecondary,
                                modifier = Modifier.size(13.dp)
                              )
                        }

                        androidx.compose.material3.DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            if (onEditClick != null) {
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text("✏️ Редагувати", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    onClick = {
                                        showMenu = false
                                        onEditClick()
                                    }
                                )
                            }
                            if (onChangeIconClick != null) {
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text("🎨 Змінити іконку", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    onClick = {
                                        showMenu = false
                                        onChangeIconClick()
                                    }
                                )
                            }
                            if (onMoveLeftClick != null) {
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text("⬅️ Перемістити вліво", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    onClick = {
                                        showMenu = false
                                        onMoveLeftClick()
                                    }
                                )
                            }
                            if (onMoveRightClick != null) {
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text("➡️ Перемістити вправо", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    onClick = {
                                        showMenu = false
                                        onMoveRightClick()
                                    }
                                )
                            }
                            if (onArchiveClick != null) {
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text("📁 Архівувати", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    onClick = {
                                        showMenu = false
                                        onArchiveClick()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = name,
                    color = textPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                if (badgeText != null) {
                    val len = badgeText.length
                    val fSize = when {
                        len > 10 -> 8.sp
                        len > 8 -> 8.5.sp
                        else -> 9.5.sp
                    }
                    Text(
                        text = badgeText,
                        color = tint,
                        fontSize = fSize,
                        lineHeight = (fSize.value + 0.8f).sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun AddTileDialog(
    type: String, // "income_source" or "expense_category"
    isDark: Boolean,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    val titleText = if (type == "income_source") "Нове джерело доходу" else "Нова категорія витрат"
    val placeholderText = if (type == "income_source") "напр., Фріланс, Кешбек" else "напр., Кафе, Спорт, Таксі"

    val textPrimary = if (isDark) DesignTokens.DarkTextPrimary else DesignTokens.LightTextPrimary
    val surfaceColor = if (isDark) DesignTokens.DarkSurfaceCard else DesignTokens.LightSurfaceCard
    val borderCol = if (isDark) DesignTokens.DarkBorder else DesignTokens.LightBorder

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, borderCol)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = titleText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Назва плиточки") },
                    placeholder = { Text(placeholderText) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Скасувати", color = DesignTokens.RoyalPurple)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onAdd(name.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DesignTokens.RoyalPurple),
                        enabled = name.isNotBlank()
                    ) {
                        Text("Створити", color = Color.White)
                    }
                }
            }
        }
    }
}

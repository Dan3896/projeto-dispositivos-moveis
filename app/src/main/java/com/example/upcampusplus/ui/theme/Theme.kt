package com.example.upcampusplus.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.upcampusplus.domain.model.Priority

// Único lugar com valores de cor (RNF-07). Todos os pares texto/fundo usados passam de 4,5:1.
private val Navy = Color(0xFF1B2240)
private val NavyMuted = Color(0xFFC5C9E0)
private val Lilac = Color(0xFF5847C9)
private val LilacContainer = Color(0xFFE6E2FF)
private val OnLilacContainer = Color(0xFF3B2E9E)
private val Background = Color(0xFFF4F5FA)
private val Ink = Color(0xFF1A1C24)
private val InkMuted = Color(0xFF5B5F72)
private val Outline = Color(0xFFC9CBD6)
private val Red = Color(0xFFB3261E)
private val RedContainer = Color(0xFFFCE8E6)
private val RedBorder = Color(0xFFD32F2F)
private val Amber = Color(0xFF7A5600)
private val AmberContainer = Color(0xFFFFF3D6)
private val AmberBorder = Color(0xFFF2A900)
private val Green = Color(0xFF23733A)
private val GreenContainer = Color(0xFFE3F4E8)
private val GreenBorder = Color(0xFF2E9D4F)
private val Gold = Color(0xFFFFD36E)

private val LightColors = lightColorScheme(
    primary = Lilac,
    onPrimary = Color.White,
    primaryContainer = LilacContainer,
    onPrimaryContainer = OnLilacContainer,
    secondary = Lilac,
    onSecondary = Color.White,
    secondaryContainer = LilacContainer,
    onSecondaryContainer = OnLilacContainer,
    background = Background,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Background,
    onSurfaceVariant = InkMuted,
    surfaceContainer = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerHigh = Color.White,
    outline = Outline,
    outlineVariant = Outline,
    error = Red,
    onError = Color.White,
    errorContainer = RedContainer,
    onErrorContainer = Red,
)

/** Cores que o Material 3 não nomeia: cabeçalho, prioridades e significados. */
@Immutable
data class ClarityColors(
    val header: Color,
    val onHeader: Color,
    val onHeaderMuted: Color,
    val xp: Color,
    val overdue: Color,
    val overdueContainer: Color,
    val success: Color,
    val successContainer: Color,
    val priorityBorderHigh: Color,
    val priorityBorderMedium: Color,
    val priorityBorderLow: Color,
    val priorityTextHigh: Color,
    val priorityTextMedium: Color,
    val priorityTextLow: Color,
    val priorityContainerHigh: Color,
    val priorityContainerMedium: Color,
    val priorityContainerLow: Color,
) {
    fun border(priority: Priority) = when (priority) {
        Priority.HIGH -> priorityBorderHigh
        Priority.MEDIUM -> priorityBorderMedium
        Priority.LOW -> priorityBorderLow
    }

    fun text(priority: Priority) = when (priority) {
        Priority.HIGH -> priorityTextHigh
        Priority.MEDIUM -> priorityTextMedium
        Priority.LOW -> priorityTextLow
    }

    fun container(priority: Priority) = when (priority) {
        Priority.HIGH -> priorityContainerHigh
        Priority.MEDIUM -> priorityContainerMedium
        Priority.LOW -> priorityContainerLow
    }
}

private val LightClarityColors = ClarityColors(
    header = Navy,
    onHeader = Color.White,
    onHeaderMuted = NavyMuted,
    xp = Gold,
    overdue = Red,
    overdueContainer = RedContainer,
    success = Green,
    successContainer = GreenContainer,
    priorityBorderHigh = RedBorder,
    priorityBorderMedium = AmberBorder,
    priorityBorderLow = GreenBorder,
    priorityTextHigh = Red,
    priorityTextMedium = Amber,
    priorityTextLow = Green,
    priorityContainerHigh = RedContainer,
    priorityContainerMedium = AmberContainer,
    priorityContainerLow = GreenContainer,
)

/** Escala única de espaçamento, múltiplos de 4 dp. */
@Immutable
data class Spacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
)

private val ClarityShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
)

private val LocalClarityColors = staticCompositionLocalOf { LightClarityColors }
private val LocalSpacing = staticCompositionLocalOf { Spacing() }

object ClarityTheme {
    val colors: ClarityColors
        @Composable @ReadOnlyComposable get() = LocalClarityColors.current
    val spacing: Spacing
        @Composable @ReadOnlyComposable get() = LocalSpacing.current
}

/** Tema claro único. Um esquema escuro entra trocando os dois objetos de cor acima. */
@Composable
fun ClarityTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalClarityColors provides LightClarityColors,
        LocalSpacing provides Spacing(),
    ) {
        MaterialTheme(
            colorScheme = LightColors,
            typography = Typography(),
            shapes = ClarityShapes,
            content = content,
        )
    }
}

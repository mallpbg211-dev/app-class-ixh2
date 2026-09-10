package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.data.Student

// Aesthetic Glassmorphism Colors
val GlassWhiteLight = Color(0xFFFFFFFF).copy(alpha = 0.75f)
val GlassWhiteUltra = Color(0xFFFFFFFF).copy(alpha = 0.90f)
val GlassBorderLight = Color(0xFFFFFFFF).copy(alpha = 0.60f)
val GlassBorderSubtle = Color(0xFFFFFFFF).copy(alpha = 0.20f)

val GlassDark = Color(0xFF0F172A).copy(alpha = 0.70f)
val GlassDarkCard = Color(0xFF1E293B).copy(alpha = 0.65f)
val GlassDarkBorder = Color(0xFF38BDF8).copy(alpha = 0.35f)
val GlassDarkBorderSubtle = Color(0xFF94A3B8).copy(alpha = 0.15f)

// Luminous ambient mesh colors
val AuroraBlue = Color(0xFF0284C7)
val AuroraCyan = Color(0xFF06B6D4)
val AuroraPurple = Color(0xFF8B5CF6)
val AuroraPink = Color(0xFFEC4899)
val AuroraAmber = Color(0xFFF59E0B)
val AuroraEmerald = Color(0xFF10B981)

@Composable
fun isGlassDark(): Boolean = isSystemInDarkTheme()

@Composable
fun glassCardColor(): Color = if (isGlassDark()) GlassDarkCard else GlassWhiteLight

@Composable
fun glassBorderBrush(): Brush {
    return if (isGlassDark()) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF38BDF8).copy(alpha = 0.5f),
                Color(0xFF818CF8).copy(alpha = 0.25f),
                Color(0xFF0F172A).copy(alpha = 0.1f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.85f),
                Color.White.copy(alpha = 0.35f),
                Color(0xFFE2E8F0).copy(alpha = 0.45f)
            )
        )
    }
}

/**
 * Aesthetic Glassmorphic Background with floating luminous orbs/aurora mesh.
 * This ensures frosted translucent cards on top have stunning visual refraction.
 */
@Composable
fun GlassBackgroundBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isGlassDark()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                if (isDark) {
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF070B14),
                            Color(0xFF0D1424),
                            Color(0xFF0F172A)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFF8FAFC),
                            Color(0xFFF1F5F9),
                            Color(0xFFE2E8F0)
                        )
                    )
                }
            )
    ) {
        // Aesthetic Ambient Glowing Orbs in the background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Top-left cyan/blue aurora
            drawCircle(
                brush = Brush.radialGradient(
                    colors = if (isDark) {
                        listOf(AuroraCyan.copy(alpha = 0.22f), Color.Transparent)
                    } else {
                        listOf(Color(0xFFBAE6FD).copy(alpha = 0.55f), Color.Transparent)
                    },
                    center = Offset(width * 0.15f, height * 0.12f),
                    radius = width * 0.55f
                ),
                radius = width * 0.55f,
                center = Offset(width * 0.15f, height * 0.12f)
            )

            // Middle-right purple glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = if (isDark) {
                        listOf(AuroraPurple.copy(alpha = 0.18f), Color.Transparent)
                    } else {
                        listOf(Color(0xFFDDD6FE).copy(alpha = 0.45f), Color.Transparent)
                    },
                    center = Offset(width * 0.85f, height * 0.45f),
                    radius = width * 0.6f
                ),
                radius = width * 0.6f,
                center = Offset(width * 0.85f, height * 0.45f)
            )

            // Bottom-left amber/emerald glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = if (isDark) {
                        listOf(AuroraEmerald.copy(alpha = 0.14f), Color.Transparent)
                    } else {
                        listOf(Color(0xFFFEF3C7).copy(alpha = 0.50f), Color.Transparent)
                    },
                    center = Offset(width * 0.2f, height * 0.85f),
                    radius = width * 0.5f
                ),
                radius = width * 0.5f,
                center = Offset(width * 0.2f, height * 0.85f)
            )
        }

        content()
    }
}

/**
 * Aesthetic Frosted Glass Card with subtle specular border & soft glow
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = glassCardColor(),
    borderBrush: Brush = glassBorderBrush(),
    borderWidth: Dp = 1.dp,
    elevation: Dp = 2.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else Modifier

    Surface(
        modifier = modifier
            .shadow(elevation, shape, clip = false)
            .border(BorderStroke(borderWidth, borderBrush), shape)
            .clip(shape)
            .then(clickableModifier),
        shape = shape,
        color = backgroundColor
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

/**
 * Compact Frosted Glass Surface
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(14.dp),
    backgroundColor: Color = glassCardColor(),
    borderBrush: Brush = glassBorderBrush(),
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .border(BorderStroke(borderWidth, borderBrush), shape)
            .clip(shape),
        shape = shape,
        color = backgroundColor
    ) {
        Box {
            content()
        }
    }
}

/**
 * Glassmorphic Pill / Badge
 */
@Composable
fun GlassPill(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = if (isGlassDark()) Color(0xFF1E293B).copy(alpha = 0.7f) else Color.White.copy(alpha = 0.8f),
    icon: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(50.dp)),
        shape = RoundedCornerShape(50.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            icon?.invoke()
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

/**
 * High quality Photo Avatar for Students with Glassmorphic Rim,
 * Fallback to aesthetic avatar graphics with initials and gender styling.
 */
@Composable
fun StudentPhotoAvatar(
    student: Student,
    modifier: Modifier = Modifier,
    size: Dp = 50.dp,
    showRankBadge: Boolean = true
) {
    val isMale = student.gender == "L"
    val isOfficer = student.role != "Anggota"

    val genderGradient = if (isMale) {
        listOf(Color(0xFF0284C7), Color(0xFF0369A1))
    } else {
        listOf(Color(0xFFEC4899), Color(0xFFDB2777))
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Frosted Rim container
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    BorderStroke(
                        1.5.dp,
                        Brush.sweepGradient(
                            if (isOfficer) {
                                listOf(AccentGoldLight, AccentGold, Color.White, AccentGoldLight)
                            } else {
                                listOf(Color.White.copy(alpha = 0.7f), Color.White.copy(alpha = 0.2f), Color.White.copy(alpha = 0.7f))
                            }
                        )
                    ),
                    CircleShape
                )
                .shadow(3.dp, CircleShape),
            shape = CircleShape,
            color = Color.White
        ) {
            if (student.photoUrl.isNotBlank()) {
                SubcomposeAsyncImage(
                    model = student.photoUrl,
                    contentDescription = "Foto ${student.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(genderGradient)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = student.avatarEmoji,
                                fontSize = (size.value * 0.45f).sp
                            )
                        }
                    },
                    error = {
                        // Fallback portrait representation
                        DefaultStudentAvatarGraphic(
                            student = student,
                            size = size,
                            gradient = genderGradient
                        )
                    }
                )
            } else {
                DefaultStudentAvatarGraphic(
                    student = student,
                    size = size,
                    gradient = genderGradient
                )
            }
        }

        // Officer Crown or Star Badge if class official
        if (showRankBadge && isOfficer) {
            Surface(
                modifier = Modifier
                    .size((size.value * 0.4f).coerceAtLeast(18f).dp)
                    .align(Alignment.BottomEnd)
                    .border(1.dp, Color.White, CircleShape),
                shape = CircleShape,
                color = AccentGold
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = when {
                            student.role.contains("Ketua") -> "👑"
                            student.role.contains("Bendahara") -> "💰"
                            student.role.contains("Sekretaris") -> "📝"
                            else -> "⭐"
                        },
                        fontSize = (size.value * 0.22f).coerceAtLeast(10f).sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DefaultStudentAvatarGraphic(
    student: Student,
    size: Dp,
    gradient: List<Color>
) {
    val initials = student.name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercase() }
        .joinToString("")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(gradient)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = initials,
                fontSize = (size.value * 0.36f).sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}

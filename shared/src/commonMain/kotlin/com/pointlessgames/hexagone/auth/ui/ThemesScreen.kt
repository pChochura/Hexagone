package com.pointlessgames.hexagone.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.LocalNavigator
import com.pointlessgames.hexagone.auth.ThemesViewModel
import com.pointlessgames.hexagone.game.model.HexDialogState
import com.pointlessgames.hexagone.game.ui.components.DiamondBalanceBadge
import com.pointlessgames.hexagone.game.ui.components.HexAlertDialog
import com.pointlessgames.hexagone.game.ui.components.ScreenScaffold
import com.pointlessgames.hexagone.ui.theme.Colors
import com.pointlessgames.hexagone.ui.theme.ThemeId
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.scaled
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.ic_diamond
import hexagone.shared.generated.resources.theme_berry
import hexagone.shared.generated.resources.theme_buy_confirmation_message
import hexagone.shared.generated.resources.theme_buy_confirmation_title
import hexagone.shared.generated.resources.theme_current_label
import hexagone.shared.generated.resources.theme_cyber
import hexagone.shared.generated.resources.theme_firefly
import hexagone.shared.generated.resources.theme_midnight
import hexagone.shared.generated.resources.theme_minty
import hexagone.shared.generated.resources.theme_neon_glow
import hexagone.shared.generated.resources.theme_ocean
import hexagone.shared.generated.resources.theme_pastel
import hexagone.shared.generated.resources.theme_sunset
import hexagone.shared.generated.resources.theme_unlocked_label
import hexagone.shared.generated.resources.themes_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun ThemesScreen(
    viewModel: ThemesViewModel = koinInject(),
) {
    val navigator = LocalNavigator.current
    val uiState by viewModel.uiState.collectAsState()
    val spacing = MaterialTheme.spacing

    Box(modifier = Modifier.fillMaxSize()) {
        ScreenScaffold(
            title = stringResource(Res.string.themes_title),
            onBack = { navigator.pop() },
            topBarTrailingContent = {
                DiamondBalanceBadge(diamonds = uiState.diamondsBalance)
            },
        ) { contentPadding ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = spacing.extraLarge.scaled)
                    .graphicsLayer { clip = false },
                horizontalArrangement = Arrangement.spacedBy(spacing.medium.scaled),
                verticalArrangement = Arrangement.spacedBy(spacing.medium.scaled),
                contentPadding = PaddingValues(
                    top = contentPadding.calculateTopPadding(),
                    bottom = spacing.extraLarge.scaled,
                ),
            ) {
                items(ThemeId.entries) { themeId ->
                    val isUnlocked = uiState.unlockedThemes.contains(themeId.name)
                    val isActive = uiState.activeTheme == themeId
                    val cost = getThemeCost(themeId)
                    val conditionText = getThemeUnlockCondition(themeId)
                    val canUnlockCondition = when (themeId) {
                        ThemeId.MIDNIGHT -> uiState.highestLevel >= 50
                        ThemeId.CYBER -> uiState.hasAllAchievements
                        ThemeId.BERRY -> uiState.dailyMissionStreak >= 10
                        else -> false
                    }
                    val canAfford = isUnlocked || uiState.diamondsBalance >= cost

                    ThemeItem(
                        themeId = themeId,
                        isUnlocked = isUnlocked,
                        isActive = isActive,
                        cost = cost,
                        canAfford = canAfford,
                        conditionText = conditionText,
                        canUnlockCondition = canUnlockCondition,
                        onClick = { viewModel.onThemeClicked(themeId, cost > 0, cost) },
                    )
                }
            }
        }

        uiState.pendingPurchaseTheme?.let { themeToBuy ->
            val cost = getThemeCost(themeToBuy)
            val themeName = getThemeNameString(themeToBuy)
            HexAlertDialog(
                state = HexDialogState.Confirmation(
                    title = Res.string.theme_buy_confirmation_title,
                    message = Res.string.theme_buy_confirmation_message,
                    formatArgs = listOf(themeName, cost),
                    onConfirm = {
                        viewModel.confirmPurchase(themeToBuy, cost)
                    },
                ),
                onDismiss = { viewModel.dismissPurchaseDialog() },
            )
        }

        uiState.newlyUnlockedTheme?.let { themeId ->
            ThemeUnlockedOverlay(
                themeId = themeId,
                onFinished = { viewModel.dismissUnlockedOverlay() },
            )
        }
    }
}

@Composable
private fun ThemeItem(
    themeId: ThemeId,
    isUnlocked: Boolean,
    isActive: Boolean,
    cost: Int,
    canAfford: Boolean,
    conditionText: String?,
    canUnlockCondition: Boolean,
    onClick: () -> Unit,
) {
    val themeName = getThemeNameString(themeId)
    val previewColors = getThemeColors(themeId)
    val spacing = MaterialTheme.spacing

    val enabled = if (conditionText != null) canUnlockCondition || isUnlocked else canAfford
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp.scaled)
            .alpha(if (enabled) 1f else 0.5f)
            .clip(RoundedCornerShape(MaterialTheme.cornerRadius.medium))
            .background(previewColors.darkBlue)
            .border(
                if (isActive) 2.dp.scaled else 1.dp.scaled,
                if (isActive) previewColors.tile1 else Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(MaterialTheme.cornerRadius.medium),
            )
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        if (!isUnlocked && conditionText != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(
                        if (canUnlockCondition) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Black.copy(
                            alpha = 0.5f,
                        ),
                        RoundedCornerShape(bottomStart = MaterialTheme.cornerRadius.medium),
                    )
                    .padding(horizontal = spacing.medium.scaled, vertical = spacing.small.scaled),
            ) {
                Text(
                    text = if (canUnlockCondition) "TAP TO UNLOCK" else conditionText,
                    color = if (canUnlockCondition) MaterialTheme.colorScheme.primary else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp.scaled,
                )
            }
        } else if (!isUnlocked && cost > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        RoundedCornerShape(bottomStart = MaterialTheme.cornerRadius.medium),
                    )
                    .padding(horizontal = spacing.medium.scaled, vertical = spacing.small.scaled),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_diamond),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp.scaled),
                )
                Spacer(Modifier.width(4.dp.scaled))
                Text(
                    text = cost.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp.scaled,
                )
            }
        } else if (isUnlocked && !isActive) {
            Text(
                text = stringResource(Res.string.theme_unlocked_label),
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp.scaled,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(spacing.small.scaled),
            )
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp.scaled),
            ) {
                Box(
                    Modifier.size(24.dp.scaled)
                        .background(previewColors.tile1, RoundedCornerShape(4.dp.scaled)),
                )
                Box(
                    Modifier.size(24.dp.scaled)
                        .background(previewColors.tile2, RoundedCornerShape(4.dp.scaled)),
                )
                Box(
                    Modifier.size(24.dp.scaled)
                        .background(previewColors.tile4, RoundedCornerShape(4.dp.scaled)),
                )
            }

            Spacer(Modifier.height(spacing.medium.scaled))

            Text(
                text = themeName,
                color = previewColors.tile1,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp.scaled,
                maxLines = 1,
            )
        }

        if (isActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(
                        previewColors.tile1,
                        RoundedCornerShape(
                            topStart = MaterialTheme.cornerRadius.small,
                            topEnd = MaterialTheme.cornerRadius.small,
                        ),
                    )
                    .padding(horizontal = spacing.medium.scaled, vertical = 2.dp.scaled),
            ) {
                Text(
                    text = stringResource(Res.string.theme_current_label).uppercase(),
                    color = previewColors.darkBlue,
                    fontSize = 10.sp.scaled,
                    fontWeight = FontWeight.Black,
                )
            }
        }
    }
}

@Composable
fun getThemeNameString(themeId: ThemeId): String {
    return stringResource(
        when (themeId) {
            ThemeId.NEON_GLOW -> Res.string.theme_neon_glow
            ThemeId.OCEAN -> Res.string.theme_ocean
            ThemeId.FIREFLY -> Res.string.theme_firefly
            ThemeId.MIDNIGHT -> Res.string.theme_midnight
            ThemeId.SUNSET -> Res.string.theme_sunset
            ThemeId.MINTY -> Res.string.theme_minty
            ThemeId.PASTEL -> Res.string.theme_pastel
            ThemeId.CYBER -> Res.string.theme_cyber
            ThemeId.BERRY -> Res.string.theme_berry
        },
    )
}

fun getThemeCost(themeId: ThemeId): Int {
    return when (themeId) {
        ThemeId.NEON_GLOW, ThemeId.OCEAN, ThemeId.FIREFLY, ThemeId.MINTY -> 0
        ThemeId.MIDNIGHT, ThemeId.CYBER, ThemeId.BERRY -> 0
        else -> 500
    }
}

fun getThemeUnlockCondition(themeId: ThemeId): String? {
    return when (themeId) {
        ThemeId.MIDNIGHT -> "REACH LEVEL 50"
        ThemeId.CYBER -> "ALL ACHIEVEMENTS"
        ThemeId.BERRY -> "10-DAY STREAK"
        else -> null
    }
}

fun getThemeColors(themeId: ThemeId): Colors {
    return when (themeId) {
        ThemeId.NEON_GLOW -> com.pointlessgames.hexagone.ui.theme.NeonGlowColors()
        ThemeId.OCEAN -> com.pointlessgames.hexagone.ui.theme.OceanColors()
        ThemeId.FIREFLY -> com.pointlessgames.hexagone.ui.theme.FireflyColors()
        ThemeId.MIDNIGHT -> com.pointlessgames.hexagone.ui.theme.MidnightColors()
        ThemeId.SUNSET -> com.pointlessgames.hexagone.ui.theme.SunsetColors()
        ThemeId.MINTY -> com.pointlessgames.hexagone.ui.theme.MintyColors()
        ThemeId.PASTEL -> com.pointlessgames.hexagone.ui.theme.PastelColors()
        ThemeId.CYBER -> com.pointlessgames.hexagone.ui.theme.CyberColors()
        ThemeId.BERRY -> com.pointlessgames.hexagone.ui.theme.BerryColors()
    }
}

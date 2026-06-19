package com.pointlessgames.hexagone.game.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.pointlessgames.hexagone.achievements.GameAchievement
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Immutable
data class GameItem(
    val id: String,
    val x: Int,
    val y: Int,
    val value: Int,
    val isGhost: Boolean,
    val isMimic: Boolean,
)

@Immutable
sealed interface MissionRefreshState {
    data object NONE : MissionRefreshState
    data class CAN_KEEP(val oldDate: Long) : MissionRefreshState
    data class HARD_REFRESH(val oldDate: Long) : MissionRefreshState
    data class MISSIONS_COMPLETED_REFRESH(val oldDate: Long) : MissionRefreshState
}

@Immutable
data class GameUiState(
    val grid: List<HexagonCell> = emptyList(),
    val mergeHints: List<MergeHint> = emptyList(),
    val onBoardPerks: List<OnBoardPerk> = emptyList(),
    val preview: List<PreviewCell> = emptyList(),
    val pendingMerge: MergeTransition? = null,
    val activeMergeStepIndex: Int = 0,
    val pendingMergeScore: Int = 0,
    val hoveredMerge: MergeTransition? = null,
    val score: Int = 0,
    val diamonds: Int = 0,
    val vouchers: Map<com.pointlessgames.hexagone.game.logic.PerkCategory, Int> = emptyMap(),
    val bestScore: Int = 0,
    val sessionBestScore: Int = 0,
    val level: Int = 1,
    val levelProgress: Float = 0f,
    val highestValue: Int = 1,
    val combo: Int = 0,
    val isBusy: Boolean = false,
    val isStuck: Boolean = false,
    val stuckPerks: Set<Perk> = emptySet(),
    val isGameOver: Boolean = false,
    val collectedPerks: List<Perk> = emptyList(),
    val perkOptions: List<Perk> = emptyList(),
    val pendingLevelUps: Int = 0,
    val activePerk: Perk? = null,
    val selectedCellId: String? = null,
    val mergeHintsEnabled: Boolean = true,
    val maxCombo: Int = 0,
    val totalMerges: Int = 0,
    val showGameOverBoard: Boolean = false,
    val reachedComboTiers: Set<ComboTier> = emptySet(),
    val perkSpawnCounter: Int = 0,
    val canReroll: Boolean = true,
    val availableChoices: Int = 0,
    val isDebugMode: Boolean = false,
    val debugUsed: Boolean = false,
    val debugSelectedValue: Int? = 1,
    val debugAddAsGhost: Boolean = false,
    val perksUsedTracking: Map<Perk, Int> = emptyMap(),
    val isSoundEnabled: Boolean = true,
    val consecutiveUndos: Int = 0,
    val consecutiveMergesWithoutSpawn: Int = 0,
    val tacticalMergesCount: Int = 0,
    val comboTriggeredInSession: Boolean = false,
    val perkUsedInSession: Boolean = false,
    val undoUsedInSession: Boolean = false,
    val ghostPerkUsedInSession: Boolean = false,
    val barRaisedThisTurn: Int = 0,
    val tacticalGhostsThisTurn: Int = 0,
    val finalResult: DetailedGameResult? = null,
    val achievementNotification: GameAchievement? = null,
    val earnedRewardsThisTurn: List<GameEffect.TierReward> = emptyList(),
    val currentRank: RankingInfo? = null,
    val redemptionBaseline: Int? = null,
    val seed: Long = 0L,
    val cellIdCounter: Int = 0,
    val previewIdCounter: Int = 0,
    val dailyChallenges: List<DailyChallengeProgress> = emptyList(),
    val persistentCompletedMissionIds: Set<String> = emptySet(),
    val dailyMissionDate: Long = 0L,
    val challengeStreak: Int = 0,
    val completedChallengeDates: Set<Long> = emptySet(),
    val isStreakCollectedToday: Boolean = false,
    val movesWithoutPerk: Int = 0,
    val comboMaintenanceTurns: Int = 0,
    val consecutiveTacticalNoSpawn: Int = 0,
    val thawedIds: Set<String> = emptySet(),
    val activeTip: GameTip? = null,
    val isDailyLoginClaimed: Boolean = false,
    val missionRefreshState: MissionRefreshState = MissionRefreshState.NONE,
    val isShopVisible: Boolean = false,
    val isShopLoading: Boolean = false,
    val isShopProcessing: Boolean = false,
    val isVoucherProcessing: Boolean = false,
    val isPerksBankVisible: Boolean = false,
    val perksBankCategory: com.pointlessgames.hexagone.game.logic.PerkCategory? = null,
    val showReviveOption: Boolean = false,
    val hasRevived: Boolean = false,
    val activeDialog: HexDialogState? = null,
    val isNicknamePopupVisible: Boolean = false,
    val tempNickname: String = "",
    val nicknameError: String? = null,
    val playerName: String? = null,
) {
    fun consumePerk(perk: Perk): GameUiState {
        val perkIndex = collectedPerks.indexOf(perk)
        return if (perkIndex != -1) {
            val newList = collectedPerks.toMutableList().apply { removeAt(perkIndex) }
            val newTracking = perksUsedTracking.toMutableMap().apply {
                this[perk] = (this[perk] ?: 0) + 1
            }
            copy(collectedPerks = newList, perksUsedTracking = newTracking)
        } else this
    }
}

@Immutable
sealed interface HexDialogState {
    data class Confirmation(
        val title: StringResource,
        val message: StringResource,
        val formatArgs: List<Any> = emptyList(),
        val onConfirm: () -> Unit,
    ) : HexDialogState

    data class Info(
        val title: StringResource,
        val message: StringResource? = null,
        val messageText: String? = null,
        val formatArgs: List<Any> = emptyList(),
        val isError: Boolean = false,
    ) : HexDialogState

    data class PauseMenu(
        val onResume: () -> Unit,
        val onRestart: () -> Unit,
    ) : HexDialogState
}

@Immutable
data class GameTip(
    val id: TipId,
    val message: StringResource,
    val targetType: TipTarget = TipTarget.NONE,
)

enum class TipId { MERGE, PERK, POST_GAME, DAILY }
enum class TipTarget { GRID, PERK_BAR, SCORE_SECTION, GAME_OVER_BUTTONS, DAILY_MISSIONS_BUTTON, NONE }

@Immutable
data class RankingInfo(
    val rank: Int,
    val isRegional: Boolean,
) {
    @Serializable
    data class RankRow(
        @SerialName("score_position")
        val scorePosition: Int,
    )
}

@Immutable
data class PotentialMerge(
    val targetX: Int,
    val targetY: Int,
    val finalValue: Int,
    val baseScore: Int,
    val participatingIds: Set<String>,
)

sealed interface GameEffect {
    data class Particles(val particles: List<Particle>) : GameEffect
    data class MergeParticles(
        val gridX: Int,
        val gridY: Int,
        val value: Int,
        val isPerk: Boolean = false,
        val intensity: Float = 1f,
        val combo: Int = 1,
    ) : GameEffect

    data class TileRemoved(
        val gridX: Int,
        val gridY: Int,
    ) : GameEffect

    data class ScorePopup(
        val gridX: Int,
        val gridY: Int,
        val score: Int,
        val color: Color,
        val labelRes: StringResource? = null,
    ) : GameEffect

    data class PerkPopup(
        val gridX: Int,
        val gridY: Int,
        val perk: Perk,
    ) : GameEffect

    data class TierReward(
        val tier: ComboTier,
        val perk: Perk,
    ) : GameEffect

    data class AchievementUnlock(
        val achievement: GameAchievement,
    ) : GameEffect

    data class DailyChallengeComplete(
        val challenge: DailyChallenge,
        val isFirstTimeToday: Boolean = true,
        val isDayCompleted: Boolean = false,
        val newStreak: Int = 0,
    ) : GameEffect
    
    data object GameOver : GameEffect
    data object ComboBroken : GameEffect
}

@Serializable
enum class ChallengeGoal(val id: String) {
    MERGE_COUNT("merge_count"),
    LEVEL_REACHED("level_reached"),
    COMBO_REACHED("combo_reached"),
    SCORE_REACHED("score_reached"),
    TACTICAL_MERGES("tactical_merges"),
    PIECE_VALUE_REACHED("piece_value_reached"),
    MOVES_WITHOUT_PERK("moves_without_perk"),
    PERK_RESTRICTED_LEVEL("perk_restricted_level"),
    LEGENDARY_GAMBLE("legendary_gamble"),
    GEOMETRIC_PATTERN("geometric_pattern"),
    ELITE_SACRIFICE("elite_sacrifice"),
    COMBO_MAINTENANCE("combo_maintenance"),
    GHOST_HORDE("ghost_horde"),
    PATH_MERGE_COUNT("path_merge_count"),
    DIVERSITY_STREAK("diversity_streak"),
    FRUGAL_SURVIVOR("frugal_survivor"),
    FROZEN_RECOVERY("frozen_recovery")
}

@Serializable
data class DailyChallenge(
    val id: String,
    val goal: ChallengeGoal,
    val target: Int,
    val rewardScore: Int = 0,
    val rewardPerk: Perk? = null,
    val restrictedPerk: Perk? = null,
    val patternId: String? = null,
)

@Serializable
data class DailyChallengeProgress(
    val challenge: DailyChallenge,
    val progress: Int = 0,
    val isCompleted: Boolean = false,
)

@Serializable
enum class ComboTier(val threshold: Int) {
    SURGE(11),
    OVERDRIVE(21),
    ZENITH(31)
}

@Serializable
data class GameState(
    val grid: List<HexagonCell>,
    val preview: List<PreviewCell>,
    val score: Int,
    val diamonds: Int = 0,
    val vouchers: Map<com.pointlessgames.hexagone.game.logic.PerkCategory, Int> = emptyMap(),
    val level: Int,
    val highestValue: Int,
    val combo: Int,
    val collectedPerks: List<Perk>,
    val maxCombo: Int,
    val totalMerges: Int,
    val onBoardPerks: List<OnBoardPerk>,
    val pendingLevelUps: Int,
    val perkSpawnCounter: Int,
    val reachedComboTiers: Set<ComboTier>,
    val perkOptions: List<Perk>,
    val canReroll: Boolean,
    val sessionBestScore: Int,
    val isStuck: Boolean = false,
    val availableChoices: Int = 0,
    val perksUsedTracking: Map<Perk, Int> = emptyMap(),
    val consecutiveUndos: Int = 0,
    val consecutiveMergesWithoutSpawn: Int = 0,
    val tacticalMergesCount: Int = 0,
    val comboTriggeredInSession: Boolean = false,
    val perkUsedInSession: Boolean = false,
    val undoUsedInSession: Boolean = false,
    val ghostPerkUsedInSession: Boolean = false,
    val debugUsed: Boolean = false,
    val redemptionBaseline: Int? = null,
    val seed: Long = 0L,
    val cellIdCounter: Int = 0,
    val previewIdCounter: Int = 0,
    val activePerk: Perk? = null,
    val selectedCellId: String? = null,
    val hasRevived: Boolean = false,
    val perksBankCategory: com.pointlessgames.hexagone.game.logic.PerkCategory? = null,
    val dailyChallenges: List<DailyChallengeProgress> = emptyList(),
    val persistentCompletedMissionIds: Set<String> = emptySet(),
    val dailyMissionDate: Long = 0L,
    val completedChallengeDates: Set<Long> = emptySet(),
    val movesWithoutPerk: Int = 0,
    val comboMaintenanceTurns: Int = 0,
    val consecutiveTacticalNoSpawn: Int = 0,
    val thawedIds: Set<String> = emptySet(),
)

@Serializable
data class DetailedGameResult(
    @SerialName("profile_id")
    val profileId: String? = null,
    val score: Int,
    @SerialName("max_combo")
    val maxCombo: Int,
    @SerialName("max_piece")
    val maxPiece: Int,
    @SerialName("total_merges")
    val totalMerges: Int,
    val level: Int,
    @SerialName("perks_used")
    val perksUsed: Map<Perk, Int>,
    @SerialName("perks_available")
    val perksAvailable: List<Perk>,
    val username: String? = null,
    @SerialName("daily_challenges")
    val dailyChallenges: List<DailyChallengeProgress> = emptyList(),
)

@Serializable
data class PlayerProfile(
    val id: String,
    val username: String,
    @SerialName("best_score")
    val bestScore: Int = 0,
)

@Immutable
interface GridPopup {
    val id: Long
    val x: Float
    val y: Float
    val gridX: Int
    val gridY: Int
}

@Immutable
@Serializable
data class HexagonCell(
    val id: String,
    val x: Int,
    val y: Int,
    val value: Int,
    val isTactical: Boolean = false,
    val isFrozen: Boolean = false,
    val isMimic: Boolean = false,
)

@Immutable
@Serializable
data class OnBoardPerk(
    val x: Int,
    val y: Int,
    val perk: Perk,
    val lifespan: Int,
)

@Immutable
@Serializable
data class PreviewCell(
    val id: String,
    val x: Int,
    val y: Int,
    val value: Int,
    val rank: Int,
    val isTactical: Boolean = false,
    val isMimic: Boolean = false,
)

@Immutable
data class MergeTransition(
    val targetX: Int,
    val targetY: Int,
    val steps: List<MergeStep>,
    val finalValue: Int,
    val totalCells: Int,
    val uniqueGroups: Int,
    val baseScore: Int,
    val resultId: String,
    val isTactical: Boolean = false,
    val previewSwaps: Map<String, Pair<Int, Int>>? = null,
    val previewValues: Map<String, Int>? = null,
    val participatingIds: Set<String>? = null,
    val forceGhostIds: Set<String>? = null,
    val forceSolidIds: Set<String>? = null,
    val previewFrozenIds: Set<String>? = null,
    val isRemoval: Boolean = false,
    val isPerkAssisted: Boolean = false,
    val isMimicOnly: Boolean = false,
    val startingCombo: Int = 0,
)

@Immutable
data class MergeStep(
    val mergingCells: List<HexagonCell>,
    val resultValue: Int,
    val baseScore: Int = 0,
)

@Serializable
enum class Perk(val baseWeight: Int) {
    UNDO(baseWeight = 100),
    MOVE_TILE(baseWeight = 80),
    REMOVE_TILE(baseWeight = 80),
    ADVANCE_QUEUE(baseWeight = 50),
    SWAP_TILES(baseWeight = 50),
    FUSION(baseWeight = 20),
    CHAIN_MERGE(baseWeight = 20),
    DUPLICATE_TILE(baseWeight = 50),
    SKIP_SPAWN(baseWeight = 50),
    INCREMENT_TILE(baseWeight = 80),
    FREEZE_TILE(baseWeight = 60),
    PATH_MERGE(baseWeight = 10),
    MIMIC(baseWeight = 25);

    val isLegendary: Boolean get() = baseWeight <= 20
}

@Immutable
data class MergeHint(
    val x: Int,
    val y: Int,
    val weight: Float, // 0.0 to 1.0
)

@Immutable
data class Particle(
    val id: Long,
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val life: Float,
    val size: Float,
)

@Immutable
data class ScorePopup(
    override val id: Long,
    override val x: Float,
    override val y: Float,
    override val gridX: Int,
    override val gridY: Int,
    val score: Int,
    val life: Float,
    val color: Color,
    val labelRes: StringResource? = null,
) : GridPopup

@Immutable
data class PerkPopup(
    override val id: Long,
    override val x: Float,
    override val y: Float,
    override val gridX: Int,
    override val gridY: Int,
    val perk: Perk,
    val life: Float,
) : GridPopup

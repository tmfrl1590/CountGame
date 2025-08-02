package com.tmfrl.countgame.domain.model

// 게임 세션 정보
data class GameSession(
    val currentStage: Int = 1,
    val score: Int = 0,
    val lives: Int = 3,
    val items: List<GameItem> = emptyList(),
    val questionType: ItemType? = null,
    val correctAnswer: Int = 0,
    val userAnswer: Int = 0,
    val state: GameState = GameState.READY,
    val timeRemaining: Int = 0,
    val isPaused: Boolean = false,
    val gameType: GameType = GameType.COUNTING
)

// 터치 게임 세션
data class TappingGameSession(
    val currentStage: Int = 1,
    val items: List<GameItem> = emptyList(),
    val targetType: ItemType? = null, // 터뜨려야 할 과일 타입
    val targetCount: Int = 0, // 터뜨려야 할 개수
    val tappedCount: Int = 0, // 터뜨린 개수
    val wrongTaps: Int = 0, // 잘못 터뜨린 개수
    val state: GameState = GameState.READY,
    val timeRemaining: Int = 0,
    val isPaused: Boolean = false,
    val gameType: GameType = GameType.TAPPING
)

// 게임 결과
data class GameResult(
    val stage: Int,
    val questionType: ItemType?,
    val correctAnswer: Int,
    val userAnswer: Int,
    val isCorrect: Boolean,
    val timeSpent: Int,
    val totalFruits: Int = 0,
    val isAllFruitsMode: Boolean = false // 전체 과일 개수 세기 모드인지
) {
    val points: Int
        get() = if (isCorrect) {
            // 빠르게 답할수록 더 높은 점수
            val basePoints = 100
            val timeBonus = maxOf(0, 10 - timeSpent) * 10
            basePoints + timeBonus
        } else 0
}

// 게임 통계
data class GameStatistics(
    val highestStage: Int = 1,
    val totalGames: Int = 0,
    val correctAnswers: Int = 0,
    val totalAnswers: Int = 0,
    val bestScore: Int = 0,
    val averageAccuracy: Float = 0f
) {
    val accuracyRate: Float
        get() = if (totalAnswers > 0) {
            (correctAnswers.toFloat() / totalAnswers.toFloat()) * 100f
        } else 0f
}

// 게임 설정
data class GameSettings(
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val language: String = "ko"
)
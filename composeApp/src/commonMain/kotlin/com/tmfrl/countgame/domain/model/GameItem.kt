package com.tmfrl.countgame.domain.model

import androidx.compose.ui.graphics.Color

// 게임에서 이동하는 오브젝트를 나타내는 데이터 클래스
data class GameItem(
    val id: String,
    val type: ItemType,
    val x: Float,
    val y: Float,
    val velocityX: Float,
    val velocityY: Float,
    val rotation: Float = 0f,
    val scale: Float = 1f,
    val alpha: Float = 1f
)

// 게임 오브젝트의 종류
enum class ItemType(
    val displayName: String,
    val color: Color,
    val emoji: String
) {
    APPLE("사과", Color(0xFFE74C3C), "🍎"),
    BANANA("바나나", Color(0xFFF1C40F), "🍌"),
    CARROT("당근", Color(0xFFE67E22), "🥕"),
    GRAPES("포도", Color(0xFF9B59B6), "🍇"),
    TOMATO("토마토", Color(0xFFD63031), "🍅"),
    PEACH("복숭아", Color(0xFFFE7096), "🍑")
}

// 게임 상태
enum class GameState {
    READY,      // 게임 준비 중
    PLAYING,    // 게임 진행 중
    PAUSED,     // 일시정지
    ANSWERING,  // 답변 입력 중
    RESULT,     // 결과 표시
    FINISHED    // 게임 종료
}

// 게임 타입
enum class GameType {
    COUNTING,   // 개수 세기 게임
    TAPPING     // 터치하여 터뜨리기 게임
}

// 게임 난이도 설정
data class GameDifficulty(
    val stage: Int,
    val itemCount: Int,
    val speed: Float,
    val timeLimit: Int, // seconds
    val hasRotation: Boolean = false,
    val hasAlpha: Boolean = false,
    val hasScale: Boolean = false,
    val similarItems: Boolean = false,
    val countAllItems: Boolean = true // true: 전체 아이템 개수, false: 특정 아이템 개수
) {
    companion object {
        fun forStage(stage: Int): GameDifficulty {
            return when (stage) {
                in 1..3 -> GameDifficulty(
                    stage = stage,
                    itemCount = 5 + stage,
                    speed = 1f,
                    timeLimit = 8,
                    countAllItems = true
                )
                in 4..6 -> GameDifficulty(
                    stage = stage,
                    itemCount = 8 + stage,
                    speed = 1.2f,
                    timeLimit = 7,
                    hasRotation = true,
                    countAllItems = true
                )
                in 7..10 -> GameDifficulty(
                    stage = stage,
                    itemCount = 12 + stage,
                    speed = 1.5f,
                    timeLimit = 6,
                    hasRotation = true,
                    hasAlpha = true,
                    countAllItems = true
                )
                in 11..15 -> GameDifficulty(
                    stage = stage,
                    itemCount = 18 + stage,
                    speed = 1.8f,
                    timeLimit = 5,
                    hasRotation = true,
                    hasAlpha = true,
                    hasScale = true,
                    countAllItems = false // 특정 과일만 세기
                )
                else -> GameDifficulty(
                    stage = stage,
                    itemCount = 25 + (stage - 15) * 2,
                    speed = 2f + (stage - 15) * 0.1f,
                    timeLimit = 4,
                    hasRotation = true,
                    hasAlpha = true,
                    hasScale = true,
                    similarItems = true,
                    countAllItems = false // 특정 과일만 세기
                )
            }
        }
    }
}
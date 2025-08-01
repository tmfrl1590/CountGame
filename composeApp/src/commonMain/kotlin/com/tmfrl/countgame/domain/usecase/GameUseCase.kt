package com.tmfrl.countgame.domain.usecase

import com.tmfrl.countgame.domain.model.*
import com.tmfrl.countgame.domain.model.GameCredits
import com.tmfrl.countgame.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

class GameUseCase(
    private val repository: GameRepository
) {

    // 새 게임 시작
    suspend fun startNewGame(): GameSession {
        val session = GameSession(
            currentStage = 1,
            state = GameState.READY
        )
        repository.saveGameSession(session)
        return session
    }

    // 스테이지 시작
    fun startStage(stage: Int): GameSession {
        val difficulty = GameDifficulty.forStage(stage)
        val items = generateItems(difficulty)

        val (questionType, correctAnswer) = if (difficulty.countAllItems) {
            // 1-10단계: 전체 과일 개수 세기
            null to items.size
        } else {
            // 11-20단계: 특정 과일 개수 세기
            val targetType = items.random().type
            targetType to items.count { it.type == targetType }
        }

        return GameSession(
            currentStage = stage,
            items = items,
            questionType = questionType,
            correctAnswer = correctAnswer,
            state = GameState.PLAYING,
            timeRemaining = difficulty.timeLimit
        )
    }

    // 아이템 생성
    private fun generateItems(difficulty: GameDifficulty): List<GameItem> {
        val items = mutableListOf<GameItem>()
        val screenWidth = 400f
        val screenHeight = 600f

        repeat(difficulty.itemCount) { index ->
            val itemType = if (difficulty.similarItems) {
                // 유사한 아이템들 (빨간색 계열)
                listOf(ItemType.APPLE, ItemType.TOMATO, ItemType.PEACH).random()
            } else {
                ItemType.values().random()
            }

            val item = GameItem(
                id = "item_$index",
                type = itemType,
                x = Random.nextFloat() * screenWidth,
                y = Random.nextFloat() * screenHeight,
                velocityX = (Random.nextFloat() - 0.5f) * difficulty.speed * 100,
                velocityY = (Random.nextFloat() - 0.5f) * difficulty.speed * 100,
                rotation = if (difficulty.hasRotation) Random.nextFloat() * 360f else 0f,
                scale = if (difficulty.hasScale) 0.7f + Random.nextFloat() * 0.6f else 1f,
                alpha = if (difficulty.hasAlpha) 0.7f + Random.nextFloat() * 0.3f else 1f
            )
            items.add(item)
        }

        return items
    }

    // 답안 제출
    suspend fun submitAnswer(session: GameSession, userAnswer: Int): GameResult {
        val difficulty = GameDifficulty.forStage(session.currentStage)
        val result = GameResult(
            stage = session.currentStage,
            questionType = session.questionType,
            correctAnswer = session.correctAnswer,
            userAnswer = userAnswer,
            isCorrect = userAnswer == session.correctAnswer,
            timeSpent = difficulty.timeLimit - session.timeRemaining,
            totalFruits = session.items.size,
            isAllFruitsMode = difficulty.countAllItems
        )

        repository.updateStatistics(result)

        return result
    }

    // 아이템 위치 업데이트
    fun updateItemPositions(
        items: List<GameItem>,
        deltaTime: Float,
        screenWidth: Float,
        screenHeight: Float
    ): List<GameItem> {
        return items.map { item ->
            var newX = item.x + item.velocityX * deltaTime
            var newY = item.y + item.velocityY * deltaTime
            var newVelocityX = item.velocityX
            var newVelocityY = item.velocityY

            // 화면 경계에서 튕기기
            if (newX < 0 || newX > screenWidth - 50f) {
                newVelocityX = -newVelocityX
                newX = newX.coerceIn(0f, screenWidth - 50f)
            }

            if (newY < 0 || newY > screenHeight - 50f) {
                newVelocityY = -newVelocityY
                newY = newY.coerceIn(0f, screenHeight - 50f)
            }

            item.copy(
                x = newX,
                y = newY,
                velocityX = newVelocityX,
                velocityY = newVelocityY
            )
        }
    }

    // 게임 세션 저장
    suspend fun saveSession(session: GameSession) {
        repository.saveGameSession(session)
    }

    // 게임 세션 불러오기
    suspend fun loadSession(): GameSession? {
        return repository.loadGameSession()
    }

    // 통계 조회
    fun observeStatistics(): Flow<GameStatistics> {
        return repository.observeStatistics()
    }

    // 설정 조회
    fun observeSettings(): Flow<GameSettings> {
        return repository.observeSettings()
    }

    // 설정 업데이트
    suspend fun updateSettings(settings: GameSettings) {
        repository.updateSettings(settings)
    }

    // 크레딧 관리
    suspend fun getCredits(): GameCredits {
        return repository.getCredits()
    }

    suspend fun canStartGame(): Boolean {
        val credits = repository.getCredits()
        return credits.current > 0
    }

    suspend fun startGameWithCredit(): Boolean {
        return repository.consumeCredit()
    }

    suspend fun grantDailyCredits(): GameCredits {
        return repository.grantDailyCredits()
    }

    fun observeCredits(): Flow<GameCredits> {
        return repository.observeCredits()
    }

    // 통계 초기화
    suspend fun clearStatistics() {
        repository.clearStatistics()
    }
}
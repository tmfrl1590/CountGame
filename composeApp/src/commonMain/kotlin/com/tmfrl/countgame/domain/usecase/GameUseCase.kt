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
                scale = 1.0f + Random.nextFloat() * 0.4f, // scale range: 1.0~1.4 for counting game
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
        // 1차 위치 업데이트 (경계 충돌 처리)
        val updated = items.map { item ->
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
        }.toMutableList()

        // 2차 아이템 간 충돌 처리 (간단한 탄성 충돌)
        val baseItemSize = 48f // 기본 아이템 크기
        for (i in 0 until updated.size) {
            for (j in i + 1 until updated.size) {
                val a = updated[i]
                val b = updated[j]

                // 각 아이템의 실제 크기 계산 (scale 적용)
                val sizeA = baseItemSize * a.scale
                val sizeB = baseItemSize * b.scale
                val minDist = (sizeA + sizeB) / 2f // 두 원의 반지름 합

                val dx = b.x - a.x
                val dy = b.y - a.y
                val distSq = dx * dx + dy * dy
                if (distSq < minDist * minDist) {
                    // 겹쳤으면 속도를 교환 (질량 동일 가정)
                    updated[i] = a.copy(velocityX = b.velocityX, velocityY = b.velocityY)
                    updated[j] = b.copy(velocityX = a.velocityX, velocityY = a.velocityY)

                    // 위치를 정확히 분리하여 겹침 완전 해소
                    val dist = kotlin.math.sqrt(distSq)
                    val overlap = (minDist - dist) / 2f
                    if (dist > 0) {
                        val nx = dx / dist
                        val ny = dy / dist
                        updated[i] = updated[i].copy(x = a.x - nx * overlap, y = a.y - ny * overlap)
                        updated[j] = updated[j].copy(x = b.x + nx * overlap, y = b.y + ny * overlap)
                    } else {
                        // 완전히 겹친 경우 강제로 분리
                        updated[i] = updated[i].copy(x = a.x - minDist / 2f, y = a.y)
                        updated[j] = updated[j].copy(x = b.x + minDist / 2f, y = b.y)
                    }
                }
            }
        }

        return updated
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
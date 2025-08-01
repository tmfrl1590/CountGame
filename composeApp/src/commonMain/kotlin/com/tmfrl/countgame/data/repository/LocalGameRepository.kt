package com.tmfrl.countgame.data.repository

import com.tmfrl.countgame.domain.model.*
import com.tmfrl.countgame.domain.repository.GameRepository
import com.tmfrl.countgame.util.currentTimeMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// 실제 구현에서는 플랫폼별 저장소 (SharedPreferences, NSUserDefaults 등)를 사용
class LocalGameRepository : GameRepository {

    // 메모리 기반 임시 저장소 (실제로는 영구 저장소 사용)
    private var currentSession: GameSession? = null
    private val _statistics = MutableStateFlow(GameStatistics())
    private val _settings = MutableStateFlow(GameSettings())
    private val _credits = MutableStateFlow(createInitialCredits())

    companion object {
        private const val DAILY_CREDIT_AMOUNT = 10
        private const val GAME_COST = 1
        private const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L

        private fun createInitialCredits(): GameCredits {
            // 임시로 현재 시간을 0으로 설정 (실제로는 플랫폼별 구현 필요)
            val now = currentTimeMillis()
            val nextMidnight = getNextMidnightMillis(now)
            return GameCredits(
                current = DAILY_CREDIT_AMOUNT,
                nextResetTimeMillis = nextMidnight
            )
        }

        private fun getNextMidnightMillis(currentMillis: Long): Long {
            // 다음 자정(UTC 기준) 계산
            val currentDay = currentMillis / MILLIS_PER_DAY
            return (currentDay + 1) * MILLIS_PER_DAY
        }
    }

    override suspend fun saveGameSession(session: GameSession) {
        currentSession = session
    }

    override suspend fun loadGameSession(): GameSession? {
        return currentSession
    }

    override suspend fun clearGameSession() {
        currentSession = null
    }

    override suspend fun getStatistics(): GameStatistics {
        return _statistics.value
    }

    override suspend fun updateStatistics(result: GameResult) {
        val current = _statistics.value
        val newStats = current.copy(
            highestStage = maxOf(current.highestStage, result.stage),
            totalGames = current.totalGames + 1,
            correctAnswers = if (result.isCorrect) current.correctAnswers + 1 else current.correctAnswers,
            totalAnswers = current.totalAnswers + 1,
            bestScore = maxOf(current.bestScore, result.points)
        )
        _statistics.value = newStats
    }

    override suspend fun clearStatistics() {
        _statistics.value = GameStatistics()
    }

    override suspend fun getSettings(): GameSettings {
        return _settings.value
    }

    override suspend fun updateSettings(settings: GameSettings) {
        _settings.value = settings
    }

    override suspend fun getHighScore(): Int {
        return _statistics.value.bestScore
    }

    override suspend fun updateHighScore(score: Int) {
        val current = _statistics.value
        if (score > current.bestScore) {
            _statistics.value = current.copy(bestScore = score)
        }
    }

    override fun observeStatistics(): Flow<GameStatistics> {
        return _statistics.asStateFlow()
    }

    override fun observeSettings(): Flow<GameSettings> {
        return _settings.asStateFlow()
    }

    // 크레딧 관리 구현
    override suspend fun getCredits(): GameCredits {
        checkAndResetDailyCredits()
        return _credits.value
    }

    override suspend fun updateCredits(credits: GameCredits) {
        _credits.value = credits
    }

    override suspend fun consumeCredit(): Boolean {
        checkAndResetDailyCredits()
        val current = _credits.value

        return if (current.current >= GAME_COST) {
            _credits.value = current.copy(current = current.current - GAME_COST)
            true
        } else {
            false
        }
    }

    override suspend fun grantDailyCredits(): GameCredits {
        val now = currentTimeMillis()
        val nextMidnight = getNextMidnightMillis(now)
        val newCredits = GameCredits(
            current = DAILY_CREDIT_AMOUNT,
            nextResetTimeMillis = nextMidnight
        )
        _credits.value = newCredits
        return newCredits
    }

    override fun observeCredits(): Flow<GameCredits> {
        return _credits.asStateFlow()
    }

    private suspend fun checkAndResetDailyCredits() {
        val now = currentTimeMillis()
        val current = _credits.value

        if (now >= current.nextResetTimeMillis) {
            grantDailyCredits()
        }
    }

    private fun getCurrentTimeMillis(): Long {
        // 임시로 현재 시간을 0으로 반환 (실제로는 플랫폼별 구현 필요)
        // Android: System.currentTimeMillis()
        // iOS: NSDate().timeIntervalSince1970 * 1000
        return 0L
    }
}
package com.tmfrl.countgame.domain.repository

import com.tmfrl.countgame.domain.model.GameSession
import com.tmfrl.countgame.domain.model.GameStatistics
import com.tmfrl.countgame.domain.model.GameSettings
import com.tmfrl.countgame.domain.model.GameResult
import com.tmfrl.countgame.domain.model.GameCredits
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    // 게임 세션 관리
    suspend fun saveGameSession(session: GameSession)
    suspend fun loadGameSession(): GameSession?
    suspend fun clearGameSession()

    // 게임 통계 관리
    suspend fun getStatistics(): GameStatistics
    suspend fun updateStatistics(result: GameResult)
    suspend fun clearStatistics()

    // 게임 설정 관리
    suspend fun getSettings(): GameSettings
    suspend fun updateSettings(settings: GameSettings)

    // 최고 기록 관리
    suspend fun getHighScore(): Int
    suspend fun updateHighScore(score: Int)

    // 크레딧 관리
    suspend fun getCredits(): GameCredits
    suspend fun updateCredits(credits: GameCredits)
    suspend fun consumeCredit(): Boolean // 크레딧 1개 소모, 성공시 true 반환
    suspend fun grantDailyCredits(): GameCredits // 일일 크레딧 지급

    // 플로우로 실시간 데이터 관찰
    fun observeStatistics(): Flow<GameStatistics>
    fun observeSettings(): Flow<GameSettings>
    fun observeCredits(): Flow<GameCredits>
}
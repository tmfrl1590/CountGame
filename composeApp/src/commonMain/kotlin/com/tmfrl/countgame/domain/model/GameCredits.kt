package com.tmfrl.countgame.domain.model

// 일일 크레딧 정보
// nextResetTimeMillis: UTC 타임스탬프 (다음 무료 크레딧 초기화 시각)
data class GameCredits(
    val current: Int = 0,
    // 다음 무료 크레딧 초기화 시각(UTC epoch millis)
    val nextResetTimeMillis: Long
)
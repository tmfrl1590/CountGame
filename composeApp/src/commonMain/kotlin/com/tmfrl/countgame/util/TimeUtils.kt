package com.tmfrl.countgame.util

// 멀티플랫폼 현재 시간 유틸
expect fun currentTimeMillis(): Long

// Removed actual implementation; platform-specific implementations will reside in androidMain and iosMain source sets
package com.tmfrl.countgame.presentation.ui.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmfrl.countgame.design.Strings
import com.tmfrl.countgame.design.components.GameButton
import com.tmfrl.countgame.design.theme.GameBackground
import com.tmfrl.countgame.design.theme.GameSuccess
import com.tmfrl.countgame.design.theme.Primary
import com.tmfrl.countgame.domain.model.GameStatistics

@Composable
fun StatisticsScreen(
    statistics: GameStatistics,
    onBackClick: () -> Unit,
    onClearStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GameBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Strings.STATISTICS_TITLE,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // 통계 카드들
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 최고 스테이지
                StatCard(
                    icon = "🏆",
                    title = Strings.HIGHEST_STAGE,
                    value = "${statistics.highestStage}단계",
                    color = GameSuccess
                )

                // 정확도
                StatCard(
                    icon = "🎯",
                    title = Strings.ACCURACY_RATE,
                    value = "${(statistics.accuracyRate * 10).toInt() / 10.0}%",
                    color = Primary
                )

                // 총 게임 수
                StatCard(
                    icon = "🎮",
                    title = Strings.TOTAL_GAMES,
                    value = "${statistics.totalGames}회",
                    color = MaterialTheme.colorScheme.secondary
                )

                // 최고 점수
                StatCard(
                    icon = "⭐",
                    title = "최고 점수",
                    value = "${statistics.bestScore}점",
                    color = MaterialTheme.colorScheme.tertiary
                )

                // 정답/총 답변
                StatCard(
                    icon = "📊",
                    title = "정답 현황",
                    value = "${statistics.correctAnswers}/${statistics.totalAnswers}",
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 하단 버튼들
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onClearStats,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = Strings.CLEAR_STATS,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                GameButton(
                    text = Strings.BACK_BUTTON,
                    onClick = onBackClick,
                    fontSize = 16
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: String,
    title: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = icon,
                    fontSize = 32.sp
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
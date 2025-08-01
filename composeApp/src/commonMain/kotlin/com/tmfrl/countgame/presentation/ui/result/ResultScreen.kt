package com.tmfrl.countgame.presentation.ui.result

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmfrl.countgame.design.Strings
import com.tmfrl.countgame.design.components.GameButton
import com.tmfrl.countgame.design.components.StaticGameItem
import com.tmfrl.countgame.design.theme.*
import com.tmfrl.countgame.domain.model.GameResult

@Composable
fun ResultScreen(
    result: GameResult,
    onNextStage: () -> Unit,
    onRetry: () -> Unit,
    onMainMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (result.isCorrect) GameSuccess else GameError,
        animationSpec = tween(500)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GameBackground),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 결과 아이콘과 메시지
                Text(
                    text = if (result.isCorrect) "🎉" else "😔",
                    fontSize = 72.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                Text(
                    text = if (result.isCorrect) Strings.CORRECT_ANSWER else Strings.WRONG_ANSWER,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = backgroundColor
                )

                // 문제 아이템 표시
                if (result.isAllFruitsMode) {
                    // 전체 과일 개수 세기 모드
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        val fruits = listOf("🍎", "🍌", "🥕", "🍇", "🍅", "🍑")
                        fruits.forEach { fruit ->
                            Text(
                                text = fruit,
                                fontSize = 32.sp
                            )
                        }
                    }
                } else {
                    // 특정 과일 개수 세기 모드
                    result.questionType?.let { questionType ->
                        StaticGameItem(
                            emoji = questionType.emoji,
                            color = questionType.color,
                            size = 64
                        )
                    }
                }
                // 정답과 사용자 답변 비교
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = Strings.CORRECT_COUNT,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = result.correctAnswer.toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = GameSuccess
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = Strings.YOUR_ANSWER,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = result.userAnswer.toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (result.isCorrect) GameSuccess else GameError
                            )
                        }
                    }
                }

                // 획득 점수 (정답일 경우만)
                if (result.isCorrect) {
                    Text(
                        text = "+${result.points} 점",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GameSuccess
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 액션 버튼들
                if (result.isCorrect) {
                    GameButton(
                        text = Strings.NEXT_STAGE,
                        onClick = onNextStage,
                        backgroundColor = GameSuccess,
                        fontSize = 18
                    )
                } else {
                    GameButton(
                        text = Strings.RETRY_BUTTON,
                        onClick = onRetry,
                        backgroundColor = GameError,
                        fontSize = 18
                    )
                }

                OutlinedButton(
                    onClick = onMainMenu,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = Strings.MENU_BUTTON,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
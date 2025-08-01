package com.tmfrl.countgame.presentation.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmfrl.countgame.design.Strings
import com.tmfrl.countgame.design.components.GameButton
import com.tmfrl.countgame.design.components.CircleButton
import com.tmfrl.countgame.design.components.GameItemView
import com.tmfrl.countgame.design.components.StaticGameItem
import com.tmfrl.countgame.design.theme.GameBackground
import com.tmfrl.countgame.design.theme.GameSuccess
import com.tmfrl.countgame.design.theme.GameError
import com.tmfrl.countgame.domain.model.*

@Composable
fun GameScreen(
    gameSession: GameSession,
    onAnswerSubmit: (Int) -> Unit,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var userAnswer by remember { mutableStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GameBackground)
    ) {
        when (gameSession.state) {
            GameState.PLAYING -> {
                PlayingGameView(
                    session = gameSession,
                    onPauseClick = onPauseClick
                )
            }

            GameState.ANSWERING -> {
                AnsweringGameView(
                    session = gameSession,
                    userAnswer = userAnswer,
                    onAnswerChange = { userAnswer = it },
                    onSubmit = { onAnswerSubmit(userAnswer) }
                )
            }

            else -> {
                // 다른 상태들에 대한 처리
            }
        }
    }
}

@Composable
private fun PlayingGameView(
    session: GameSession,
    onPauseClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 상단 UI (스테이지, 시간, 일시정지 버튼)
        TopGameBar(
            stage = session.currentStage,
            timeRemaining = session.timeRemaining,
            onPauseClick = onPauseClick
        )

        // 게임 안내 텍스트
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(
                text = if (session.currentStage <= 10) {
                    "화면의 모든 과일 개수를 세어주세요!"
                } else {
                    session.questionType?.let { type ->
                        "${type.displayName}의 개수를 세어주세요!"
                    } ?: "과일의 개수를 세어주세요!"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        // 게임 플레이 영역
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // 게임 아이템들 렌더링
            session.items.forEach { item ->
                GameItemView(
                    item = item,
                    modifier = Modifier
                )
            }
        }
    }
}

@Composable
private fun AnsweringGameView(
    session: GameSession,
    userAnswer: Int,
    onAnswerChange: (Int) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 질문할 아이템 표시
                session.questionType?.let { itemType ->
                    StaticGameItem(
                        emoji = itemType.emoji,
                        color = itemType.color,
                        size = 80
                    )

                    Text(
                        text = "${itemType.displayName}${Strings.COUNT_QUESTION}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                } ?: run {
                    // 전체 과일 개수 세기 (1-10단계)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        val fruits = listOf("🍎", "🍌", "🥕", "🍇", "🍅", "🍑")
                        fruits.forEach { fruit ->
                            Text(
                                text = fruit,
                                fontSize = 24.sp
                            )
                        }
                    }

                    Text(
                        text = Strings.COUNT_ALL_QUESTION,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 답변 입력 UI
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircleButton(
                        text = Strings.COUNT_DECREASE,
                        onClick = {
                            if (userAnswer > 0) onAnswerChange(userAnswer - 1)
                        },
                        backgroundColor = GameError
                    )

                    Text(
                        text = userAnswer.toString(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    CircleButton(
                        text = Strings.COUNT_INCREASE,
                        onClick = { onAnswerChange(userAnswer + 1) },
                        backgroundColor = GameSuccess
                    )
                }

                // 제출 버튼
                GameButton(
                    text = Strings.SUBMIT_BUTTON,
                    onClick = onSubmit,
                    fontSize = 18
                )
            }
        }
    }
}

@Composable
private fun TopGameBar(
    stage: Int,
    timeRemaining: Int,
    onPauseClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 스테이지 표시
        Text(
            text = "${Strings.STAGE_LABEL} $stage",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // 시간 표시
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${Strings.TIME_REMAINING} ${timeRemaining}s",
                style = MaterialTheme.typography.titleMedium,
                color = if (timeRemaining <= 3) GameError else MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 일시정지 버튼
            CircleButton(
                text = "⏸",
                onClick = onPauseClick,
                size = 48
            )
        }
    }
}
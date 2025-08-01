package com.tmfrl.countgame.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmfrl.countgame.design.Strings
import com.tmfrl.countgame.design.theme.GameSuccess
import com.tmfrl.countgame.design.theme.GameWarning
import com.tmfrl.countgame.domain.model.GameCredits

@Composable
fun CreditDisplay(
    credits: GameCredits,
    modifier: Modifier = Modifier,
    showNextReset: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (credits.current > 0) GameSuccess.copy(alpha = 0.1f)
            else GameWarning.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "💎",
                fontSize = 18.sp
            )

            Text(
                text = "${Strings.CREDITS}: ${credits.current}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (credits.current > 0) GameSuccess else GameWarning
            )

            if (showNextReset && credits.current == 0) {
                Text(
                    text = "| ${Strings.NEXT_FREE_CREDITS} ${formatTimeUntilReset(credits.nextResetTimeMillis)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun InsufficientCreditsDialog(
    onDismiss: () -> Unit,
    nextResetTime: Long,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = Strings.INSUFFICIENT_CREDITS,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text("게임을 시작하려면 크레딧이 1개 필요합니다.")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${Strings.NEXT_FREE_CREDITS} ${formatTimeUntilReset(nextResetTime)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("확인")
            }
        },
        modifier = modifier
    )
}

private fun formatTimeUntilReset(resetTimeMillis: Long): String {
    // 임시 구현 - 실제로는 현재 시간과 비교해서 남은 시간 계산
    return "자정"
}
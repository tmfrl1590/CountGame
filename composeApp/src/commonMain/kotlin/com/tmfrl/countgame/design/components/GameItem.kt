package com.tmfrl.countgame.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmfrl.countgame.domain.model.GameItem

@Composable
fun GameItemView(
    item: GameItem,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .offset(item.x.dp, item.y.dp)
            .size(48.dp)
            .scale(item.scale)
            .alpha(item.alpha)
            .rotate(item.rotation)
            .background(
                color = item.type.color,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = item.type.emoji,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StaticGameItem(
    emoji: String,
    color: Color,
    size: Int = 48,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .background(
                color = color,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = (size * 0.5).sp,
            fontWeight = FontWeight.Bold
        )
    }
}
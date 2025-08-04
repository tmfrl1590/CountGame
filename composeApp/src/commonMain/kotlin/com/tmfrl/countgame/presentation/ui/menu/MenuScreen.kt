package com.tmfrl.countgame.presentation.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.lexilabs.basic.ads.AdState
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import app.lexilabs.basic.ads.DependsOnGoogleUserMessagingPlatform
import app.lexilabs.basic.ads.composable.ConsentPopup
import app.lexilabs.basic.ads.composable.RewardedAd
import app.lexilabs.basic.ads.composable.rememberConsent
import app.lexilabs.basic.ads.composable.rememberRewardedAd
import app.lexilabs.basic.logging.Log
import com.tmfrl.countgame.ContextFactory
import com.tmfrl.countgame.design.components.CreditDisplay
import com.tmfrl.countgame.design.components.IconTextButton
import com.tmfrl.countgame.design.components.GameButton
import com.tmfrl.countgame.design.theme.GameBackground
import com.tmfrl.countgame.domain.model.GameCredits
import com.tmfrl.countgame.design.Strings

@OptIn(DependsOnGoogleMobileAds::class, DependsOnGoogleUserMessagingPlatform::class)
@Composable
fun MenuScreen(
    platformContext: ContextFactory,
    onPlayClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    credits: GameCredits? = null,
    modifier: Modifier = Modifier
) {
    val consent by rememberConsent(activity = platformContext.getActivity())
    val rewardedAd by rememberRewardedAd(activity = platformContext.getActivity())
    var showRewardedAd by remember { mutableStateOf(false) }

    // Try to show a consent popup
    ConsentPopup(
        consent = consent,
        onFailure = { Log.e("App", "failure:${it.message}")}
    )

    // 광고 배너
    if (showRewardedAd && consent.canRequestAds){
        RewardedAd(
            loadedAd = rewardedAd,
            onDismissed = { showRewardedAd = false},
            onRewardEarned = {

            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GameBackground),
        contentAlignment = Alignment.Center
    ) {
        credits?.let {
            CreditDisplay(
                credits = it,
                showNextReset = true,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )
        }

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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 게임 타이틀
                Text(
                    text = Strings.APP_TITLE,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 게임 아이콘들 (과일 이모지)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    val fruits = listOf("🍎", "🍌", "🥕", "🍇", "🍅", "🍑")
                    fruits.forEach { fruit ->
                        Text(
                            text = fruit,
                            fontSize = 32.sp,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }

                // 메뉴 버튼들
                GameButton(
                    text = Strings.SELECT_GAME,
                    onClick = onPlayClick,
                    fontSize = 18
                )

                // IconTextButton(
                //     text = Strings.STATISTICS_BUTTON,
                //     icon = "📊",
                //     onClick = onStatisticsClick
                // )

                IconTextButton(
                    text = Strings.SETTINGS_BUTTON,
                    icon = "⚙️",
                    onClick = onSettingsClick
                )

                if(rewardedAd.state == AdState.READY){
                    IconTextButton(
                        text = Strings.WATCH_AD_FOR_CREDITS,
                        icon = "📹",
                        onClick = {
                            showRewardedAd = true
                        }
                    )
                }

            }
        }
    }
}
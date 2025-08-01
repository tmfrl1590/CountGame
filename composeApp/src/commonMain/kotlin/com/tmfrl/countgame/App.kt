package com.tmfrl.countgame

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tmfrl.countgame.data.repository.LocalGameRepository
import com.tmfrl.countgame.design.components.InsufficientCreditsDialog
import com.tmfrl.countgame.design.theme.CountGameTheme
import com.tmfrl.countgame.domain.usecase.GameUseCase
import com.tmfrl.countgame.presentation.ui.game.GameScreen
import com.tmfrl.countgame.presentation.ui.menu.MenuScreen
import com.tmfrl.countgame.presentation.ui.result.ResultScreen
import com.tmfrl.countgame.presentation.ui.settings.SettingsScreen
import com.tmfrl.countgame.presentation.ui.statistics.StatisticsScreen
import com.tmfrl.countgame.presentation.viewmodel.GameViewModel

@Composable
fun App() {
    CountGameTheme {
        // 의존성 주입 (실제로는 DI 프레임워크 사용 권장)
        val repository = remember { LocalGameRepository() }
        val useCase = remember { GameUseCase(repository) }
        val viewModel = viewModel { GameViewModel(useCase) }

        CountGameNavigation(viewModel = viewModel)
    }
}

@Composable
private fun CountGameNavigation(
    viewModel: GameViewModel
) {
    var currentScreen by remember { mutableStateOf(Screen.Menu) }
    var showInsufficientCreditsDialog by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val gameSession by viewModel.gameSession.collectAsState()
    val gameResult by viewModel.gameResult.collectAsState()
    val credits by viewModel.credits.collectAsState()

    when (currentScreen) {
        Screen.Menu -> {
            MenuScreen(
                onPlayClick = {
                    viewModel.startNewGame()
                    currentScreen = Screen.Game
                },
                onSettingsClick = {
                    currentScreen = Screen.Settings
                },
                onStatisticsClick = {
                    currentScreen = Screen.Statistics
                },
                credits = credits
            )
        }

        Screen.Game -> {
            gameSession?.let { session ->
                when (session.state) {
                    com.tmfrl.countgame.domain.model.GameState.RESULT -> {
                        gameResult?.let { result ->
                            ResultScreen(
                                result = result,
                                onNextStage = {
                                    viewModel.nextStage()
                                },
                                onRetry = {
                                    viewModel.retryStage()
                                },
                                onMainMenu = {
                                    currentScreen = Screen.Menu
                                }
                            )
                        }
                    }

                    else -> {
                        GameScreen(
                            gameSession = session,
                            onAnswerSubmit = { answer ->
                                viewModel.submitAnswer(answer)
                            },
                            onPauseClick = {
                                viewModel.pauseGame()
                            }
                        )
                    }
                }
            }
        }

        Screen.Settings -> {
            SettingsScreen(
                settings = uiState.settings,
                onSettingsChange = { settings ->
                    viewModel.updateSettings(settings)
                },
                onBackClick = {
                    currentScreen = Screen.Menu
                }
            )
        }

        Screen.Statistics -> {
            val statistics by produceState(initialValue = uiState.statistics) {
                value = viewModel.getStatistics()
            }

            StatisticsScreen(
                statistics = statistics,
                onBackClick = {
                    currentScreen = Screen.Menu
                },
                onClearStats = {
                    viewModel.clearStatistics()
                }
            )
        }
    }

    // 크레딧 부족 다이얼로그
    if (showInsufficientCreditsDialog) {
        InsufficientCreditsDialog(
            onDismiss = { showInsufficientCreditsDialog = false },
            nextResetTime = credits.nextResetTimeMillis
        )
    }

    // 에러 표시 (실제로는 스낵바나 다이얼로그 사용)
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            if (error.contains("크레딧")) {
                showInsufficientCreditsDialog = true
            }
            println("Game Error: $error")
            viewModel.clearError()
        }
    }
}

private enum class Screen {
    Menu,
    Game,
    Settings,
    Statistics
}
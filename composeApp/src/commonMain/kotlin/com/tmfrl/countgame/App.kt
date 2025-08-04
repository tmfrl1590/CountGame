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
import com.tmfrl.countgame.design.components.PauseDialog
import com.tmfrl.countgame.design.theme.CountGameTheme
import com.tmfrl.countgame.domain.usecase.GameUseCase
import com.tmfrl.countgame.domain.usecase.TappingGameUseCase
import com.tmfrl.countgame.presentation.ui.game.GameScreen
import com.tmfrl.countgame.presentation.ui.gameselection.GameSelectionScreen
import com.tmfrl.countgame.presentation.ui.menu.MenuScreen
import com.tmfrl.countgame.presentation.ui.result.ResultScreen
import com.tmfrl.countgame.presentation.ui.settings.SettingsScreen
import com.tmfrl.countgame.presentation.ui.statistics.StatisticsScreen
import com.tmfrl.countgame.presentation.ui.tappinggame.TappingGameScreen
import com.tmfrl.countgame.presentation.viewmodel.GameViewModel
import com.tmfrl.countgame.presentation.viewmodel.TappingGameViewModel

@Composable
fun App(platformContext: ContextFactory) {
    CountGameTheme {
        // 의존성 주입 (실제로는 DI 프레임워크 사용 권장)
        val repository = remember { LocalGameRepository() }
        val useCase = remember { GameUseCase(repository) }
        val viewModel = viewModel { GameViewModel(useCase) }

        // Tapping game dependencies
        val tappingUseCase = remember { TappingGameUseCase(repository) }
        val tappingViewModel = viewModel { TappingGameViewModel(tappingUseCase) }

        CountGameNavigation(
            viewModel = viewModel,
            tappingViewModel = tappingViewModel,
            platformContext = platformContext,
        )
    }
}

@Composable
private fun CountGameNavigation(
    viewModel: GameViewModel,
    tappingViewModel: TappingGameViewModel,
    platformContext: ContextFactory,
) {
    var currentScreen by remember { mutableStateOf(Screen.Menu) }

    var showInsufficientCreditsDialog by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val gameSession by viewModel.gameSession.collectAsState()
    val gameResult by viewModel.gameResult.collectAsState()
    val credits by viewModel.credits.collectAsState()

    val tappingGameSession by tappingViewModel.gameSession.collectAsState()
    val tappingGameResult by tappingViewModel.gameResult.collectAsState()

    when (currentScreen) {
        Screen.Menu -> {
            MenuScreen(
                platformContext = platformContext,
                onPlayClick = {
                    currentScreen = Screen.GameSelection
                },
                onSettingsClick = {
                    currentScreen = Screen.Settings
                },
                onStatisticsClick = {
                    currentScreen = Screen.Statistics
                },
                credits = credits,
            )
        }

        Screen.GameSelection -> {
            GameSelectionScreen(
                onGameSelected = { gameType ->
                    when (gameType) {
                        com.tmfrl.countgame.domain.model.GameType.COUNTING -> {
                            viewModel.startNewGame()
                            currentScreen = Screen.CountingGame
                        }

                        com.tmfrl.countgame.domain.model.GameType.TAPPING -> {
                            tappingViewModel.startNewGame()
                            currentScreen = Screen.TappingGame
                        }
                    }
                },
                onBackClick = { currentScreen = Screen.Menu }
            )
        }

        Screen.CountingGame -> {
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
                                },
                                credits = credits.current
                            )
                        }
                    }

                    com.tmfrl.countgame.domain.model.GameState.PAUSED -> {
                        // 일시정지 상태에서도 게임 화면을 보여주되, 일시정지 다이얼로그를 띄움
                        GameScreen(
                            gameSession = session,
                            onAnswerSubmit = { answer ->
                                viewModel.submitAnswer(answer)
                            },
                            onPauseClick = {
                                viewModel.pauseGame()
                            }
                        )

                        // 일시정지 다이얼로그
                        PauseDialog(
                            onResume = {
                                viewModel.resumeGame()
                            },
                            onMainMenu = {
                                currentScreen = Screen.Menu
                            }
                        )
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

        Screen.TappingGame -> {
            tappingGameSession?.let { session ->
                when (session.state) {
                    com.tmfrl.countgame.domain.model.GameState.RESULT -> {
                        tappingGameResult?.let { result ->
                            ResultScreen(
                                result = result,
                                onNextStage = {
                                    tappingViewModel.nextStage()
                                },
                                onRetry = {
                                    tappingViewModel.retryStage()
                                },
                                onMainMenu = {
                                    currentScreen = Screen.Menu
                                },
                                credits = credits.current
                            )
                        }
                    }

                    com.tmfrl.countgame.domain.model.GameState.PAUSED -> {
                        TappingGameScreen(
                            gameSession = session,
                            onItemTap = { itemId ->
                                tappingViewModel.tapItem(itemId)
                            },
                            onPauseClick = {
                                tappingViewModel.pauseGame()
                            }
                        )

                        PauseDialog(
                            onResume = {
                                tappingViewModel.resumeGame()
                            },
                            onMainMenu = {
                                currentScreen = Screen.Menu
                            }
                        )
                    }

                    else -> {
                        TappingGameScreen(
                            gameSession = session,
                            onItemTap = { itemId ->
                                tappingViewModel.tapItem(itemId)
                            },
                            onPauseClick = {
                                tappingViewModel.pauseGame()
                            }
                        )
                    }
                }
            }
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
    GameSelection,
    CountingGame,
    TappingGame,
    Settings,
    Statistics
}
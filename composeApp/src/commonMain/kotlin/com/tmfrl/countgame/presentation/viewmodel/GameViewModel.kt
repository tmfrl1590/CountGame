package com.tmfrl.countgame.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tmfrl.countgame.domain.model.*
import com.tmfrl.countgame.domain.usecase.GameUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GameViewModel(
    private val gameUseCase: GameUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _gameSession = MutableStateFlow<GameSession?>(null)
    val gameSession: StateFlow<GameSession?> = _gameSession.asStateFlow()

    private val _gameResult = MutableStateFlow<GameResult?>(null)
    val gameResult: StateFlow<GameResult?> = _gameResult.asStateFlow()

    private val _credits =
        MutableStateFlow<GameCredits>(GameCredits(current = 0, nextResetTimeMillis = 0L))
    val credits: StateFlow<GameCredits> = _credits.asStateFlow()

    private var gameTimer: Job? = null
    private var itemUpdateTimer: Job? = null

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                val savedSession = gameUseCase.loadSession()
                _gameSession.value = savedSession

                val settings = gameUseCase.observeSettings().first()
                val credits = gameUseCase.getCredits()
                _uiState.value = _uiState.value.copy(
                    settings = settings,
                    isLoading = false
                )
                _credits.value = credits

                // 크레딧 변화 관찰
                launch {
                    gameUseCase.observeCredits().collect { updatedCredits ->
                        _credits.value = updatedCredits
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun startNewGame() {
        viewModelScope.launch {
            try {
                // 크레딧 확인 및 소모
                if (!gameUseCase.canStartGame()) {
                    _uiState.value = _uiState.value.copy(error = "크레딧이 부족합니다.")
                    return@launch
                }

                if (!gameUseCase.startGameWithCredit()) {
                    _uiState.value = _uiState.value.copy(error = "게임을 시작할 수 없습니다.")
                    return@launch
                }

                val session = gameUseCase.startNewGame()
                startStage(session.currentStage)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun startStage(stage: Int) {
        try {
            val session = gameUseCase.startStage(stage)
            _gameSession.value = session
            _gameResult.value = null

            // 게임 타이머 시작
            startGameTimer(session)

            // 아이템 움직임 업데이트 시작
            startItemAnimation(session)

        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(error = e.message)
        }
    }

    private fun startGameTimer(session: GameSession) {
        gameTimer?.cancel()
        gameTimer = viewModelScope.launch {
            var timeLeft = session.timeRemaining

            while (timeLeft > 0 && _gameSession.value?.state == GameState.PLAYING) {
                delay(1000)
                timeLeft--

                val currentSession = _gameSession.value
                if (currentSession != null && currentSession.state == GameState.PLAYING) {
                    _gameSession.value = currentSession.copy(timeRemaining = timeLeft)
                }
            }

            // 시간 종료 시 답변 단계로 이동
            if (timeLeft <= 0) {
                moveToAnsweringState()
            }
        }
    }

    private fun startItemAnimation(session: GameSession) {
        itemUpdateTimer?.cancel()
        itemUpdateTimer = viewModelScope.launch {
            while (_gameSession.value?.state == GameState.PLAYING) {
                delay(16) // ~60fps

                val currentSession = _gameSession.value
                if (currentSession != null && currentSession.state == GameState.PLAYING) {
                    val updatedItems = gameUseCase.updateItemPositions(
                        items = currentSession.items,
                        deltaTime = 0.016f,
                        screenWidth = 400f,
                        screenHeight = 600f
                    )

                    _gameSession.value = currentSession.copy(items = updatedItems)
                }
            }
        }
    }

    private fun moveToAnsweringState() {
        val currentSession = _gameSession.value
        if (currentSession != null) {
            itemUpdateTimer?.cancel()
            _gameSession.value = currentSession.copy(state = GameState.ANSWERING)
        }
    }

    fun pauseGame() {
        val currentSession = _gameSession.value
        if (currentSession != null && currentSession.state == GameState.PLAYING) {
            gameTimer?.cancel()
            itemUpdateTimer?.cancel()
            _gameSession.value = currentSession.copy(
                state = GameState.PAUSED,
                isPaused = true
            )
        }
    }

    fun resumeGame() {
        val currentSession = _gameSession.value
        if (currentSession != null && currentSession.state == GameState.PAUSED) {
            _gameSession.value = currentSession.copy(
                state = GameState.PLAYING,
                isPaused = false
            )
            startGameTimer(currentSession)
            startItemAnimation(currentSession)
        }
    }

    fun submitAnswer(userAnswer: Int) {
        viewModelScope.launch {
            try {
                val currentSession = _gameSession.value
                if (currentSession != null) {
                    val result = gameUseCase.submitAnswer(currentSession, userAnswer)
                    _gameResult.value = result

                    // 결과 상태로 변경
                    _gameSession.value = currentSession.copy(
                        state = GameState.RESULT,
                        userAnswer = userAnswer
                    )

                    // 세션 저장
                    gameUseCase.saveSession(currentSession)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun nextStage() {
        val currentSession = _gameSession.value
        if (currentSession != null) {
            startStage(currentSession.currentStage + 1)
        }
    }

    fun retryStage() {
        viewModelScope.launch {
            try {
                if (!gameUseCase.canStartGame()) {
                    _uiState.value = _uiState.value.copy(error = "크레딧이 부족합니다.")
                    return@launch
                }

                if (!gameUseCase.startGameWithCredit()) {
                    _uiState.value = _uiState.value.copy(error = "게임을 시작할 수 없습니다.")
                    return@launch
                }

                // 다시 도전할 때는 1단계부터 시작
                startStage(1)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateSettings(settings: GameSettings) {
        viewModelScope.launch {
            try {
                gameUseCase.updateSettings(settings)
                _uiState.value = _uiState.value.copy(settings = settings)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    suspend fun getStatistics(): GameStatistics {
        return gameUseCase.observeStatistics().first()
    }

    fun clearStatistics() {
        viewModelScope.launch {
            try {
                gameUseCase.clearStatistics()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameTimer?.cancel()
        itemUpdateTimer?.cancel()
    }
}

data class GameUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val settings: GameSettings = GameSettings(),
    val statistics: GameStatistics = GameStatistics()
)
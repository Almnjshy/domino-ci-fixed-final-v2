package com.agon.app.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.agon.app.domain.model.GameMode
import com.agon.app.presentation.screens.DemoScreen
import com.agon.app.presentation.screens.GameScreen
import com.agon.app.presentation.screens.MainMenuScreen
import com.agon.app.presentation.screens.NetworkScreen
import com.agon.app.presentation.screens.PlayScreen
import com.agon.app.presentation.screens.SettingsScreen
import com.agon.app.presentation.screens.SplashScreen
import com.agon.app.presentation.screens.StatsScreen
import com.agon.app.presentation.screens.VerificationScreen
import com.agon.app.presentation.viewmodel.GameViewModel
import com.agon.app.presentation.viewmodel.MenuViewModel
import com.agon.app.presentation.viewmodel.NetworkViewModel
import com.agon.app.presentation.viewmodel.SettingsViewModel
import com.agon.app.presentation.viewmodel.SplashViewModel
import com.agon.app.presentation.viewmodel.StatsViewModel

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Menu : Screen("menu")
    data object Play : Screen("play")
    data object Game : Screen("game/{mode}") {
        fun createRoute(mode: GameMode) = "game/${mode.name}"
    }
    data object NetworkGame : Screen("network_game")
    data object Network : Screen("network")
    data object Settings : Screen("settings")
    data object Stats : Screen("stats")
    data object Verify : Screen("verify")
    data object Demo : Screen("demo")

    companion object {
        fun fromRoute(route: String?): Screen = when {
            route == null -> Splash
            route.startsWith("game/") -> Game
            route == "menu" -> Menu
            route == "play" -> Play
            route == "network_game" -> NetworkGame
            route == "network" -> Network
            route == "settings" -> Settings
            route == "stats" -> Stats
            route == "verify" -> Verify
            route == "demo" -> Demo
            else -> Splash
        }
    }
}

@Composable
fun DominoNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val intent = (context as? android.app.Activity)?.intent
        val uri = intent?.data
        if (uri != null) {
            when (uri.path) {
                "/menu" -> navController.navigate(Screen.Menu.route) { popUpTo(0) }
                "/play" -> navController.navigate(Screen.Play.route) { popUpTo(0) }
                "/game" -> {
                    val mode = uri.getQueryParameter("mode")?.let {
                        try { GameMode.valueOf(it.uppercase()) } catch (e: Exception) { null }
                    } ?: GameMode.HUMAN_VS_AI
                    navController.navigate(Screen.Game.createRoute(mode)) { popUpTo(0) }
                }
                "/network" -> navController.navigate(Screen.Network.route) { popUpTo(0) }
                "/settings" -> navController.navigate(Screen.Settings.route) { popUpTo(0) }
                "/stats" -> navController.navigate(Screen.Stats.route) { popUpTo(0) }
            }
            intent.data = null
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) +
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) +
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) +
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) +
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
        }
    ) {
        composable(
            route = Screen.Splash.route,
            deepLinks = listOf(navDeepLink { uriPattern = "domino://splash" })
        ) {
            val viewModel: SplashViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            SplashScreen(
                progress = state.progress,
                isLoading = state.isLoading,
                error = state.error,
                onReady = {
                    navController.navigate(Screen.Menu.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onRetry = { viewModel.retry() }
            )
        }

        composable(
            route = Screen.Menu.route,
            deepLinks = listOf(navDeepLink { uriPattern = "domino://menu" })
        ) {
            val viewModel: MenuViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            MainMenuScreen(
                selectedMode = state.selectedMode,
                isLoading = state.isLoading,
                error = state.error,
                onModeSelected = { viewModel.selectMode(it) },
                onVsAi = {
                    navController.navigate(Screen.Game.createRoute(GameMode.HUMAN_VS_AI)) {
                        popUpTo(Screen.Menu.route) { inclusive = false }
                    }
                },
                onVsPlayer = {
                    navController.navigate(Screen.Game.createRoute(GameMode.HUMAN_VS_HUMAN)) {
                        popUpTo(Screen.Menu.route) { inclusive = false }
                    }
                },
                onNetwork = {
                    navController.navigate(Screen.Network.route) {
                        popUpTo(Screen.Menu.route) { inclusive = false }
                    }
                },
                onTournament = {
                    navController.navigate(Screen.Game.createRoute(GameMode.HUMAN_VS_AI)) {
                        popUpTo(Screen.Menu.route) { inclusive = false }
                    }
                },
                onSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onStats = {
                    navController.navigate(Screen.Stats.route)
                },
                onClearError = { viewModel.clearError() }
            )
        }

        composable(
            route = Screen.Play.route,
            deepLinks = listOf(navDeepLink { uriPattern = "domino://play" })
        ) {
            PlayScreen(
                onVsAi = {
                    navController.navigate(Screen.Game.createRoute(GameMode.HUMAN_VS_AI)) {
                        popUpTo(Screen.Play.route) { inclusive = true }
                    }
                },
                onVsPlayer = {
                    navController.navigate(Screen.Game.createRoute(GameMode.HUMAN_VS_HUMAN)) {
                        popUpTo(Screen.Play.route) { inclusive = true }
                    }
                },
                onNetwork = {
                    navController.navigate(Screen.Network.route) {
                        popUpTo(Screen.Play.route) { inclusive = true }
                    }
                },
                onTournament = {
                    navController.navigate(Screen.Game.createRoute(GameMode.HUMAN_VS_AI)) {
                        popUpTo(Screen.Play.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Game.route,
            arguments = listOf(
                navArgument("mode") {
                    type = NavType.StringType
                    defaultValue = GameMode.HUMAN_VS_AI.name
                }
            ),
            deepLinks = listOf(navDeepLink { uriPattern = "domino://game?mode={mode}" })
        ) { backStackEntry ->
            val modeName = backStackEntry.arguments?.getString("mode") ?: GameMode.HUMAN_VS_AI.name
            val gameMode = try { GameMode.valueOf(modeName) } catch (e: Exception) { GameMode.HUMAN_VS_AI }
            val viewModel: GameViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(gameMode) { viewModel.newGame(gameMode) }
            GameScreen(
                gameState = state.gameState,
                isAiThinking = state.isAiThinking,
                showResult = state.showResult,
                error = state.error,
                isNetworkGame = state.isNetworkGame,
                isMyTurn = state.isMyTurn,
                networkStatus = state.networkStatus,
                pingMs = 0,
                onTileClick = { tile, side -> viewModel.playTile(tile, side) },
                onDrawOrPass = { viewModel.drawOrPass() },
                legalSides = { tile -> viewModel.getLegalSides(tile) },
                onNewGame = { viewModel.newGame(gameMode) },
                onBackToMenu = {
                    navController.navigate(Screen.Menu.route) {
                        popUpTo(Screen.Menu.route) { inclusive = false }
                    }
                },
                onDismissResult = {
                    viewModel.dismissRoundResult()
                    if (viewModel.uiState.value.gameState.isGameOver && !viewModel.uiState.value.gameState.isMatchOver) {
                        viewModel.newRound()
                    }
                },
                onClearError = { viewModel.clearError() }
            )
        }

        composable(route = Screen.NetworkGame.route) {
            val viewModel: GameViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            GameScreen(
                gameState = state.gameState,
                isAiThinking = state.isAiThinking,
                showResult = state.showResult,
                error = state.error,
                isNetworkGame = state.isNetworkGame,
                isMyTurn = state.isMyTurn,
                networkStatus = state.networkStatus,
                pingMs = 0,
                onTileClick = { tile, side -> viewModel.playTile(tile, side) },
                onDrawOrPass = { viewModel.drawOrPass() },
                legalSides = { tile -> viewModel.getLegalSides(tile) },
                onNewGame = { },
                onBackToMenu = {
                    navController.navigate(Screen.Menu.route) {
                        popUpTo(Screen.Menu.route) { inclusive = false }
                    }
                },
                onDismissResult = {
                    viewModel.dismissRoundResult()
                    if (viewModel.uiState.value.gameState.isGameOver && !viewModel.uiState.value.gameState.isMatchOver) {
                        viewModel.newRound()
                    }
                },
                onClearError = { viewModel.clearError() }
            )
        }

        composable(
            route = Screen.Network.route,
            deepLinks = listOf(navDeepLink { uriPattern = "domino://network" })
        ) {
            val viewModel: NetworkViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(state.gameStarted) {
                if (state.gameStarted) {
                    navController.navigate(Screen.NetworkGame.route) {
                        popUpTo(Screen.Network.route) { inclusive = false }
                    }
                    viewModel.resetGameStarted()
                }
            }

            NetworkScreen(
                networkState = state.networkState,
                discoveredRooms = state.discoveredRooms,
                isLoading = state.isLoading,
                error = state.error,
                showCreateDialog = state.showCreateDialog,
                onCreateRoom = { viewModel.createRoom(it) },
                onDiscover = { viewModel.discoverRooms() },
                onJoinRoom = { room, name -> viewModel.joinRoom(room, name) },
                onLeaveRoom = { viewModel.leaveRoom() },
                onShowCreateDialog = { viewModel.showCreateDialog() },
                onDismissCreateDialog = { viewModel.dismissCreateDialog() },
                onBack = { navController.popBackStack() },
                onClearError = { viewModel.clearError() },
                statusMessage = state.statusMessage,
                onStartGame = { viewModel.startGame() },
                chatMessages = state.chatMessages,
                onSendChatMessage = { viewModel.sendChatMessage(it) }
            )
        }

        composable(
            route = Screen.Settings.route,
            deepLinks = listOf(navDeepLink { uriPattern = "domino://settings" })
        ) {
            val viewModel: SettingsViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            SettingsScreen(
                settings = state.settings,
                hasChanges = state.hasChanges,
                isLoading = state.isLoading,
                error = state.error,
                showResetConfirmation = state.showResetConfirmation,
                saveSuccess = state.saveSuccess,
                onVolumeChange = { viewModel.updateVolume(it) },
                onEffectsToggle = { viewModel.toggleEffects(it) },
                onVibrationToggle = { viewModel.toggleVibration(it) },
                onLanguageChange = { viewModel.setLanguage(it) },
                onModeChange = { viewModel.setPreferredMode(it) },
                onSave = { viewModel.saveSettings() },
                onReset = { viewModel.resetSettings() },
                onShowResetConfirmation = { viewModel.showResetConfirmation() },
                onDismissResetConfirmation = { viewModel.dismissResetConfirmation() },
                onDismissSaveSuccess = { viewModel.dismissSaveSuccess() },
                onBack = { navController.popBackStack() },
                onClearError = { viewModel.clearError() }
            )
        }

        composable(
            route = Screen.Stats.route,
            deepLinks = listOf(navDeepLink { uriPattern = "domino://stats" })
        ) {
            val viewModel: StatsViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            StatsScreen(
                stats = state.stats,
                achievements = state.achievements,
                isLoading = state.isLoading,
                error = state.error,
                showClearConfirmation = state.showClearConfirmation,
                exportedJson = state.exportedJson,
                showExportDialog = state.showExportDialog,
                onClearStats = { viewModel.clearStats() },
                onExport = { viewModel.exportStats() },
                onShowClearConfirmation = { viewModel.showClearConfirmation() },
                onDismissClearConfirmation = { viewModel.dismissClearConfirmation() },
                onDismissExportDialog = { viewModel.dismissExportDialog() },
                onRefresh = { viewModel.loadStats() },
                onBack = { navController.popBackStack() },
                onClearError = { viewModel.clearError() }
            )
        }

        composable(route = Screen.Demo.route) {
            DemoScreen(
                onStartAi = { navController.navigate(Screen.Game.createRoute(GameMode.HUMAN_VS_AI)) },
                onCreateNetworkRoom = { navController.navigate(Screen.Network.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Verify.route) {
            VerificationScreen(
                onStartAi = { navController.navigate(Screen.Game.createRoute(GameMode.HUMAN_VS_AI)) },
                onNetwork = { navController.navigate(Screen.Network.route) },
                onSettings = { navController.navigate(Screen.Settings.route) },
                onStats = { navController.navigate(Screen.Stats.route) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

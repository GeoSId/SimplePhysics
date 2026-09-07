package com.geosid.simplephysics

import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.geosid.simplephysics.localization.LocalizationManager
import com.geosid.simplephysics.model.PhysicsExperiment
import com.geosid.simplephysics.ui.screens.ExperimentDetailScreen
import com.geosid.simplephysics.ui.screens.HomeScreen
import com.geosid.simplephysics.ui.screens.SplashScreen
import com.geosid.simplephysics.ui.screens.WhiteboardAnimatorScreen
import com.geosid.simplephysics.ui.screens.WhiteboardStudioScreen
import com.geosid.simplephysics.ui.theme.ScienceDarkBg
import com.geosid.simplephysics.ui.theme.SimplePhysicsTheme

sealed interface AppScreen {
    object Splash : AppScreen
    object Home : AppScreen
    data class Detail(val experiment: PhysicsExperiment) : AppScreen
    object WhiteboardStudio : AppScreen
    object WhiteboardAnimator : AppScreen
}

@Composable
fun App() {
    val currentLanguage = LocalizationManager.currentLanguage

    key(currentLanguage) {
        SimplePhysicsTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = ScienceDarkBg
            ) {
                // Start on Splash; SplashScreen calls onFinished → switches to Home
                var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Splash) }

            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    (fadeIn() + scaleIn(initialScale = 0.96f)).togetherWith(
                        fadeOut() + scaleOut(targetScale = 1.04f)
                    )
                }
            ) { screen ->
                when (screen) {
                    is AppScreen.Splash -> {
                        SplashScreen(onFinished = { currentScreen = AppScreen.Home })
                    }
                    is AppScreen.Home -> {
                        HomeScreen(
                            onSelectExperiment = { currentScreen = AppScreen.Detail(it) },
                            onOpenWhiteboard = { currentScreen = AppScreen.WhiteboardAnimator }
                        )
                    }
                    is AppScreen.Detail -> {
                        ExperimentDetailScreen(
                            experiment = screen.experiment,
                            onBack = { currentScreen = AppScreen.Home }
                        )
                    }
                    is AppScreen.WhiteboardStudio -> {
                        WhiteboardStudioScreen(
                            onBack = { currentScreen = AppScreen.Home }
                        )
                    }
                    is AppScreen.WhiteboardAnimator -> {
                        WhiteboardAnimatorScreen(
                            onBack = { currentScreen = AppScreen.Home }
                        )
                    }
                }
            }
        }
    }
}
}
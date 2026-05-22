package com.pointlessgames.hexagone

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI

internal sealed interface Route : NavKey {
    @Serializable
    data object Start : Route

    @Serializable
    data object Game : Route
}

private val navigationConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Route.Start::class, Route.Start.serializer())
            subclass(Route.Game::class, Route.Game.serializer())
        }
    }
}

private const val DEFAULT_TRANSITION_DURATION_MILLISECOND = 500

@OptIn(KoinExperimentalAPI::class)
@Composable
internal fun Navigator(
    startingRoute: Route,
    backStack: NavBackStack<NavKey> = rememberNavBackStack(
        configuration = navigationConfig,
        startingRoute,
    ),
) {
    val navigator = Navigator(backStack)
    CompositionLocalProvider(LocalNavigator provides navigator) {
        NavDisplay(
            backStack = backStack,
            entryProvider = koinEntryProvider(),
            transitionSpec = {
                ContentTransform(
                    fadeIn(animationSpec = tween(DEFAULT_TRANSITION_DURATION_MILLISECOND)),
                    fadeOut(animationSpec = tween(DEFAULT_TRANSITION_DURATION_MILLISECOND)),
                )
            },
            popTransitionSpec = {
                ContentTransform(
                    fadeIn(animationSpec = tween(DEFAULT_TRANSITION_DURATION_MILLISECOND)),
                    fadeOut(animationSpec = tween(DEFAULT_TRANSITION_DURATION_MILLISECOND)),
                )
            },
            predictivePopTransitionSpec = {
                ContentTransform(
                    fadeIn(animationSpec = tween(DEFAULT_TRANSITION_DURATION_MILLISECOND)),
                    fadeOut(animationSpec = tween(DEFAULT_TRANSITION_DURATION_MILLISECOND)),
                )
            },
        )
    }
}

internal class Navigator(private val backStack: NavBackStack<NavKey>) {
}

internal val LocalNavigator: ProvidableCompositionLocal<Navigator> =
    staticCompositionLocalOf { error("LocalNavigator not initialized") }

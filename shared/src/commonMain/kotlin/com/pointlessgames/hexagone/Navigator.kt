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
    data object Game : Route

    @Serializable
    data object Shop : Route

    @Serializable
    data object Leaderboard : Route

    @Serializable
    data object DailyMissions : Route

    @Serializable
    data class Achievements(val initialAchievementId: String? = null) : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object Themes : Route
}

private val navigationConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Route.Game::class, Route.Game.serializer())
            subclass(Route.Shop::class, Route.Shop.serializer())
            subclass(Route.Leaderboard::class, Route.Leaderboard.serializer())
            subclass(Route.DailyMissions::class, Route.DailyMissions.serializer())
            subclass(Route.Achievements::class, Route.Achievements.serializer())
            subclass(Route.Settings::class, Route.Settings.serializer())
            subclass(Route.Themes::class, Route.Themes.serializer())
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
    fun navigateTo(route: Route) {
        backStack.add(route)
    }

    fun pop() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.size - 1)
        }
    }

    fun replaceAll(route: Route) {
        while (backStack.isNotEmpty()) {
            backStack.removeAt(backStack.size - 1)
        }
        backStack.add(route)
    }
}

internal val LocalNavigator: ProvidableCompositionLocal<Navigator> =
    staticCompositionLocalOf { error("LocalNavigator not initialized") }

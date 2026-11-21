package com.novaai.assistant.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.novaai.assistant.ui.chat.ChatScreen
import com.novaai.assistant.ui.common.ComingSoonScreen
import com.novaai.assistant.ui.dashboard.DashboardScreen
import com.novaai.assistant.ui.providers.ProviderSettingsScreen

private val implementedRoutes = setOf("chat", "providers")

@Composable
fun NovaNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD
    ) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                features = NovaFeatures,
                onOpenFeature = { route -> navController.navigate(route) }
            )
        }

        composable("chat") {
            ChatScreen(onBack = { navController.popBackStack() })
        }

        composable("providers") {
            ProviderSettingsScreen(onBack = { navController.popBackStack() })
        }

        NovaFeatures
            .filter { it.route !in implementedRoutes }
            .forEach { feature ->
                composable(feature.route) {
                    ComingSoonScreen(
                        feature = feature,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
    }
}

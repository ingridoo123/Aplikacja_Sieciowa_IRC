package com.example.aplikacja_sieciowa.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aplikacja_sieciowa.presentation.chat.ChatScreen
import com.example.aplikacja_sieciowa.presentation.welcome.WelcomeScreen

@Composable
fun NavGraph() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToChat = {
                    navController.navigate(Screen.Chat.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true}
                    }
                }
            )
        }

        composable(Screen.Chat.route) {
            ChatScreen(onDisconnect = {
                navController.navigate(Screen.Welcome.route) {
                    popUpTo(Screen.Chat.route) {inclusive = true}
                }
            })
        }
    }

}
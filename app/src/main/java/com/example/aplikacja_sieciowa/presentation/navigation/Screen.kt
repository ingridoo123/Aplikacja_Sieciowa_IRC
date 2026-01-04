package com.example.aplikacja_sieciowa.presentation.navigation

sealed class Screen(val route: String) {
    data object Welcome : Screen("Welcome")
    data object Chat : Screen("Chat")
}
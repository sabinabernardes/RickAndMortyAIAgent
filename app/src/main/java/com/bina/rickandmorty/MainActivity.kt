package com.bina.rickandmorty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bina.auth.presentation.view.LoginScreen
import com.bina.character_details.presentation.view.CharacterDetailsScreen
import com.bina.chat.chat.presentation.view.ChatScreen
import com.bina.designsystem.theme.RickAndMortyTheme
import com.bina.home.presentation.view.HomeScreen
import core.navigation.NavDestination

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RickAndMortyTheme {
                AppNavHost(rememberNavController())
            }
        }
    }
}

@Composable
private fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavDestination.Login.route
    ) {
        composable(NavDestination.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(NavDestination.Home.route) {
                        popUpTo(NavDestination.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = NavDestination.Home.routeWithQuery,
            arguments = listOf(navArgument("query") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) { backStackEntry ->
            val initialQuery = backStackEntry.arguments?.getString("query") ?: ""
            HomeScreen(
                initialQuery = initialQuery,
                onCharacterClick = { id ->
                    navController.navigate(NavDestination.Detail.createRoute(id.toString()))
                },
                onChatClick = { navController.navigate(NavDestination.Chat.route) }
            )
        }
        composable(NavDestination.Chat.route) {
            ChatScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToCharacter = { id ->
                    navController.navigate(NavDestination.Detail.createRoute(id.toString()))
                },
                onSearchCharacters = { query ->
                    navController.navigate(NavDestination.Home.createRoute(query)) {
                        popUpTo(NavDestination.Home.route) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = NavDestination.Detail.route,
            arguments = listOf(navArgument("itemId") { type = NavType.IntType })
        ) { backStackEntry ->
            val characterId = backStackEntry.arguments?.getInt("itemId") ?: 0
            CharacterDetailsScreen(
                characterId = characterId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

package com.bina.rickandmorty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bina.character_details.presentation.view.CharacterDetailsScreen
import com.bina.chat.chat.presentation.view.ChatScreen
import com.bina.home.presentation.view.HomeScreen
import com.bina.designsystem.theme.RickAndMortyTheme
import core.navigation.NavDestination

import androidx.navigation.NavType
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RickAndMortyTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = NavDestination.Home.route
                ) {
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
                                navController.navigate("detail/$id")
                            },
                            onChatClick = {
                                navController.navigate(NavDestination.Chat.route)
                            }
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
                        route = "detail/{itemId}",
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
        }
    }
}

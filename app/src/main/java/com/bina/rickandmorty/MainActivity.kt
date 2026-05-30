package com.bina.rickandmorty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bina.character_details.presentation.view.CharacterDetailsScreen
import com.bina.chat.presentation.view.ChatScreen
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
                    composable(NavDestination.Home.route) {
                        HomeScreen(
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
                                navController.navigate(NavDestination.Home.route)
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

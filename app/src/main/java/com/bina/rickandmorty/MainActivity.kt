package com.bina.rickandmorty

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import com.bina.home.presentation.view.HomeScreen
import com.bina.rickandmorty.ui.theme.RickAndMortyTheme
import core.navigation.NavDestination

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
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
                                // navController.navigate("details/$id")
                            }
                        )
                    }
                }
            }
        }
    }
}

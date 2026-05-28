package core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bina.home.presentation.view.HomeScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = HomeRoute.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = HomeRoute.route) {
            HomeScreen(
                navController = navController
            )
        }

    }
}
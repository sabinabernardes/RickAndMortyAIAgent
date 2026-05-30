package core.navigation

/**
 * Representa um destino de navegação na aplicação.
 * Pode ser expandido para incluir argumentos.
 */
sealed class NavDestination(val route: String) {
    object Home : NavDestination("home")
    object Chat : NavDestination("chat")
    data class Detail(val itemId: String) : NavDestination("detail/{itemId}") {
        companion object {
            fun createRoute(itemId: String) = "detail/$itemId"
        }
    }
}


package core.navigation

/**
 * Representa um destino de navegação na aplicação.
 * Pode ser expandido para incluir argumentos.
 */
sealed class NavDestination(val route: String) {
    object Login : NavDestination("login")
    object Home : NavDestination("home") {
        const val routeWithQuery = "home?query={query}"
        fun createRoute(query: String) = "home?query=$query"
    }
    object Chat : NavDestination("chat")
    data class Detail(val itemId: String) : NavDestination("detail/{itemId}") {
        companion object {
            const val route = "detail/{itemId}"
            fun createRoute(itemId: String) = "detail/$itemId"
        }
    }
}

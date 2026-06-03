package core.navigation

/**
 * Interface para abstrair a navegação entre destinos.
 */
interface Navigator {
    fun navigateTo(destination: NavDestination)
    fun goBack()
}

package core.navigation

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NavDestinationTest {
    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @Test
    fun `given itemId when createRoute then returns expected route`() {
        // Given
        val itemId = "123"
        val expectedRoute = "detail/123"

        // When
        val route = NavDestination.Detail.createRoute(itemId)

        // Then
        assertEquals(expectedRoute, route)
    }

    @Test
    fun `given destination when navigateTo then navigator is called with destination`() {
        // Given
        val navigator = mockk<Navigator>(relaxed = true)
        val destination = NavDestination.Detail("42")

        // When
        navigator.navigateTo(destination)

        // Then
        verify { navigator.navigateTo(destination) }
    }

    @Test
    fun `given destination when navigateTo then destination is received by fake navigator`() {
        // Given
        class FakeNavigator : Navigator {
            val received = mutableListOf<NavDestination>()
            override fun navigateTo(destination: NavDestination) {
                received.add(destination)
            }
            override fun goBack() { /* no-op */ }
        }
        val navigator = FakeNavigator()
        val destination = NavDestination.Detail("42")

        // When
        navigator.navigateTo(destination)

        // Then
        assertEquals(listOf(destination), navigator.received)
    }
}

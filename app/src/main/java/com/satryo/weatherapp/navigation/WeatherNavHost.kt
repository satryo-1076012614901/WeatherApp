package com.satryo.weatherapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.satryo.weatherapp.data.model.Place
import com.satryo.weatherapp.ui.view.screens.DashboardScreen
import com.satryo.weatherapp.ui.view.screens.DetailScreen
import com.satryo.weatherapp.ui.view.screens.SearchScreen

/**
 * Graf navigasi aplikasi (Navigation Component):
 * - Dashboard -> Detail (card GPS / card kota tersimpan)
 * - Dashboard -> Search -> Detail (hasil pencarian)
 */
@Composable
fun WeatherNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val openDetail: (Place) -> Unit = { place ->
        navController.navigate(DetailDestination.createRoute(place)) {
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = DashboardDestination.route,
        modifier = modifier,
    ) {
        composable(route = DashboardDestination.route) {
            DashboardScreen(
                onPlaceClick = openDetail,
                onSearchClick = {
                    navController.navigate(SearchDestination.route) { launchSingleTop = true }
                },
            )
        }

        composable(route = SearchDestination.route) {
            SearchScreen(
                onPlaceClick = openDetail,
                onNavigateUp = { navController.navigateUp() },
            )
        }

        composable(
            route = DetailDestination.routeWithArgs,
            arguments = listOf(
                navArgument(DetailDestination.ARG_LAT) { type = NavType.StringType },
                navArgument(DetailDestination.ARG_LON) { type = NavType.StringType },
                optionalStringArgument(DetailDestination.ARG_ID),
                optionalStringArgument(DetailDestination.ARG_NAME),
                optionalStringArgument(DetailDestination.ARG_REGION),
                optionalStringArgument(DetailDestination.ARG_COUNTRY),
            ),
        ) {
            DetailScreen(onNavigateUp = { navController.navigateUp() })
        }
    }
}

/** Argumen query opsional bertipe String (null bila tidak ada di route). */
private fun optionalStringArgument(name: String) = navArgument(name) {
    type = NavType.StringType
    nullable = true
    defaultValue = null
}

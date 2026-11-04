package com.naturewalls.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.naturewalls.app.ui.AppViewModelProvider
import com.naturewalls.app.ui.browse.CategoryScreen
import com.naturewalls.app.ui.browse.CategoryViewModel
import com.naturewalls.app.ui.credits.CreditsScreen
import com.naturewalls.app.ui.detail.DetailScreen
import com.naturewalls.app.ui.detail.DetailViewModel
import com.naturewalls.app.ui.favorites.CreditsViewModel
import com.naturewalls.app.ui.favorites.DownloadsScreen
import com.naturewalls.app.ui.favorites.DownloadsViewModel
import com.naturewalls.app.ui.favorites.FavoritesScreen
import com.naturewalls.app.ui.favorites.FavoritesViewModel
import com.naturewalls.app.ui.home.HomeScreen
import com.naturewalls.app.ui.home.HomeViewModel
import com.naturewalls.app.ui.search.SearchScreen
import com.naturewalls.app.ui.search.SearchViewModel
import com.naturewalls.app.ui.theme.ForestGreen
import com.naturewalls.app.ui.theme.NatureBlack
import com.naturewalls.app.ui.theme.TextTertiary

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val FAVORITES = "favorites"
    const val DOWNLOADS = "downloads"
    const val CREDITS = "credits"
    const val DETAIL = "detail/{wallpaperId}"
    const val CATEGORY = "category/{groupId}?subId={subId}"

    fun detail(id: Long) = "detail/$id"
    fun category(groupId: String, subId: String? = null) =
        if (subId.isNullOrBlank()) "category/$groupId" else "category/$groupId?subId=$subId"
}

private data class BottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val bottomDestinations = listOf(
    BottomDestination(Routes.HOME, "Home", Icons.Rounded.Home),
    BottomDestination(Routes.SEARCH, "Search", Icons.Rounded.Search),
    BottomDestination(Routes.FAVORITES, "Saved", Icons.Rounded.FavoriteBorder),
    BottomDestination(Routes.DOWNLOADS, "Files", Icons.Rounded.DownloadDone)
)

@Composable
fun NatureWallsNavHost(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomDestinations.map { it.route }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NatureBlack)
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.weight(1f)
        ) {
            composable(Routes.HOME) {
                val viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
                HomeScreen(
                    viewModel = viewModel,
                    onOpenWallpaper = { id -> navController.navigate(Routes.detail(id)) },
                    onOpenCategory = { groupId, subId ->
                        navController.navigate(Routes.category(groupId, subId))
                    },
                    onOpenSearch = { navController.navigate(Routes.SEARCH) },
                    onOpenCredits = { navController.navigate(Routes.CREDITS) }
                )
            }

            composable(Routes.SEARCH) {
                val viewModel: SearchViewModel = viewModel(factory = AppViewModelProvider.Factory)
                SearchScreen(
                    viewModel = viewModel,
                    onOpenWallpaper = { id -> navController.navigate(Routes.detail(id)) }
                )
            }

            composable(Routes.FAVORITES) {
                val viewModel: FavoritesViewModel = viewModel(factory = AppViewModelProvider.Factory)
                FavoritesScreen(
                    viewModel = viewModel,
                    onOpenWallpaper = { id -> navController.navigate(Routes.detail(id)) }
                )
            }

            composable(Routes.DOWNLOADS) {
                val viewModel: DownloadsViewModel = viewModel(factory = AppViewModelProvider.Factory)
                DownloadsScreen(
                    viewModel = viewModel,
                    onOpenWallpaper = { id -> navController.navigate(Routes.detail(id)) }
                )
            }

            composable(Routes.CREDITS) {
                val viewModel: CreditsViewModel = viewModel(factory = AppViewModelProvider.Factory)
                CreditsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }

            composable(
                route = Routes.DETAIL,
                arguments = listOf(navArgument("wallpaperId") { type = NavType.StringType })
            ) {
                val viewModel: DetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
                DetailScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }

            composable(
                route = Routes.CATEGORY,
                arguments = listOf(
                    navArgument("groupId") { type = NavType.StringType },
                    navArgument("subId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                val viewModel: CategoryViewModel = viewModel(factory = AppViewModelProvider.Factory)
                CategoryScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onOpenWallpaper = { id -> navController.navigate(Routes.detail(id)) }
                )
            }
        }

        if (showBottomBar) {
            NatureBottomBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
private fun NatureBottomBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.96f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.08f))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomDestinations.forEach { destination ->
                val selected = currentRoute == destination.route
                Column(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onNavigate(destination.route) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label,
                        tint = if (selected) ForestGreen else TextTertiary,
                        modifier = Modifier.size(21.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = destination.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) ForestGreen else TextTertiary
                    )
                }
            }
        }
    }
}

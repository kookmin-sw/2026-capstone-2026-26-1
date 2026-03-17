package com.example.passedpath.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.passedpath.R
import com.example.passedpath.ui.theme.Gray300
import com.example.passedpath.ui.theme.Green500

private data class BottomNavItem(
    val route: String,
    @StringRes val labelResId: Int,
    val icon: ImageVector? = null,
    val iconResId: Int? = null
)

private val bottomNavItems = listOf(
    BottomNavItem(
        route = NavRoute.FRIENDS,
        labelResId = R.string.bottom_nav_friends,
        icon = Icons.Filled.Group
    ),
    BottomNavItem(
        route = NavRoute.MAIN,
        labelResId = R.string.bottom_nav_main,
        iconResId = R.drawable.conversion_path_24px
    ),
    BottomNavItem(
        route = NavRoute.MYPAGE,
        labelResId = R.string.bottom_nav_profile,
        icon = Icons.Filled.Person
    )
)

private val bottomBarRoutes = bottomNavItems.map { it.route }.toSet()

@Composable
fun AppScaffold(
    navController: NavHostController,
    content: @Composable (Modifier) -> Unit
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = currentDestination?.route in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination
                            ?.hierarchy
                            ?.any { it.route == item.route } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                val contentDescription = stringResource(item.labelResId)
                                when {
                                    item.icon != null -> {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = contentDescription
                                        )
                                    }

                                    item.iconResId != null -> {
                                        Icon(
                                            painter = painterResource(item.iconResId),
                                            contentDescription = contentDescription
                                        )
                                    }
                                }
                            },
                            label = {
                                Text(text = stringResource(item.labelResId))
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Green500,
                                selectedTextColor = Green500,
                                unselectedIconColor = Gray300,
                                unselectedTextColor = Gray300,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        content(Modifier.padding(innerPadding))
    }
}

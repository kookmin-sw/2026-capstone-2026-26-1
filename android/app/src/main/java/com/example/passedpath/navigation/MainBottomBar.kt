package com.example.passedpath.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        iconResId = R.drawable.ic_bottom_nav_route
    ),
    BottomNavItem(
        route = NavRoute.MYPAGE,
        labelResId = R.string.bottom_nav_profile,
        icon = Icons.Filled.Person
    )
)

private val bottomBarRoutes = bottomNavItems.map { it.route }.toSet()

private object BottomBarTokens {
    val containerHeight = 84.dp
    val containerColor = Color.White
    val shadowColor = Color.Black.copy(alpha = 0.08f)
    val shadowHeight = 10.dp
    const val itemWidthRatio = 0.267f
    val iconSize = 24.dp
    val labelTextSize = 12.sp
    val selectedColor = Green500
    val unselectedColor = Gray300
}

private fun Modifier.topShadow(
    shadowColor: Color = BottomBarTokens.shadowColor,
    shadowHeight: Dp = BottomBarTokens.shadowHeight
): Modifier = drawBehind {
    val shadowHeightPx = shadowHeight.toPx()
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(shadowColor, Color.Transparent),
            startY = 0f,
            endY = -shadowHeightPx
        ),
        topLeft = Offset(0f, -shadowHeightPx),
        size = size.copy(height = shadowHeightPx)
    )
}

@Composable
private fun BottomBarItem(
    item: BottomNavItem,
    selected: Boolean,
    width: Dp,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val contentDescription = stringResource(item.labelResId)

    Column(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            item.icon != null -> {
                Icon(
                    imageVector = item.icon,
                    contentDescription = contentDescription,
                    tint = if (selected) BottomBarTokens.selectedColor else BottomBarTokens.unselectedColor,
                    modifier = Modifier.height(BottomBarTokens.iconSize)
                )
            }

            item.iconResId != null -> {
                Icon(
                    painter = painterResource(item.iconResId),
                    contentDescription = contentDescription,
                    tint = if (selected) BottomBarTokens.selectedColor else BottomBarTokens.unselectedColor,
                    modifier = Modifier.height(BottomBarTokens.iconSize)
                )
            }
        }

        Text(
            text = stringResource(item.labelResId),
            color = if (selected) BottomBarTokens.selectedColor else BottomBarTokens.unselectedColor,
            fontSize = BottomBarTokens.labelTextSize,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun AppScaffold(
    navController: NavHostController,
    onBottomBarReselected: (String) -> Unit = {},
    content: @Composable (Modifier) -> Unit
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = currentDestination?.route in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier
                        .height(BottomBarTokens.containerHeight)
                        .topShadow(),
                    containerColor = BottomBarTokens.containerColor
                ) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                    ) {
                        val itemWidth = maxWidth * BottomBarTokens.itemWidthRatio

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            bottomNavItems.forEach { item ->
                                val selected = currentDestination
                                    ?.hierarchy
                                    ?.any { it.route == item.route } == true

                                BottomBarItem(
                                    item = item,
                                    selected = selected,
                                    width = itemWidth,
                                    onClick = {
                                        if (selected) {
                                            onBottomBarReselected(item.route)
                                        } else {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        content(Modifier.padding(innerPadding))
    }
}

package com.chweibo.android.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chweibo.android.ui.components.image.ImageViewerScreen
import com.chweibo.android.ui.screens.comments.CommentsScreen
import com.chweibo.android.ui.screens.detail.WeiboDetailScreen
import com.chweibo.android.ui.screens.discover.DiscoverScreen
import com.chweibo.android.ui.screens.drafts.DraftsScreen
import com.chweibo.android.ui.screens.home.HomeScreen
import com.chweibo.android.ui.screens.login.LoginScreen
import com.chweibo.android.ui.screens.message.MessageScreen
import com.chweibo.android.ui.screens.post.PostScreen
import com.chweibo.android.ui.screens.post.RepostScreen
import com.chweibo.android.ui.screens.profile.ProfileScreen
import com.chweibo.android.ui.screens.profile.UserProfileScreen
import com.chweibo.android.ui.screens.search.SearchScreen
import com.chweibo.android.ui.screens.settings.SettingsScreen
import com.chweibo.android.ui.screens.splash.SplashScreen
import com.chweibo.android.ui.viewmodel.MainViewModel
import com.chweibo.android.ui.viewmodel.UiEvent

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // 判断是否显示底部导航栏
    val showBottomBar = bottomNavItems.any {
        currentDestination?.route?.startsWith(it.route) == true
    } || currentDestination?.route == Screen.Home.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    navController = navController,
                    currentDestination = currentDestination
                )
            }
        }
    ) { innerPadding ->
        NavigationHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            mainViewModel = mainViewModel
        )
    }
}

@Composable
fun NavigationHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        // Splash
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Login
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Main (Home with bottom nav)
        composable(Screen.Main.route) {
            HomeScreen(
                onNavigateToDetail = { weiboId ->
                    navController.navigate(Screen.WeiboDetail.createRoute(weiboId))
                },
                onNavigateToProfile = { userId ->
                    navController.navigate(Screen.UserProfile.createRoute(userId))
                },
                onNavigateToImageViewer = { urls, index ->
                    navController.navigate(Screen.ImageViewer.createRoute(urls, index))
                },
                onNavigateToRepost = { weiboId ->
                    navController.navigate(Screen.Repost.createRoute(weiboId))
                },
                onNavigateToComments = { weiboId ->
                    navController.navigate(Screen.Comments.createRoute(weiboId))
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }

        // Home
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToDetail = { weiboId ->
                    navController.navigate(Screen.WeiboDetail.createRoute(weiboId))
                },
                onNavigateToProfile = { userId ->
                    navController.navigate(Screen.UserProfile.createRoute(userId))
                },
                onNavigateToImageViewer = { urls, index ->
                    navController.navigate(Screen.ImageViewer.createRoute(urls, index))
                },
                onNavigateToRepost = { weiboId ->
                    navController.navigate(Screen.Repost.createRoute(weiboId))
                },
                onNavigateToComments = { weiboId ->
                    navController.navigate(Screen.Comments.createRoute(weiboId))
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }

        // Discover
        composable(Screen.Discover.route) {
            DiscoverScreen(
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route)
                },
                onNavigateToUserProfile = { userId ->
                    navController.navigate(Screen.UserProfile.createRoute(userId))
                }
            )
        }

        // Post
        composable(Screen.Post.route) {
            PostScreen(
                onPostSuccess = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigate = { route ->
                    when (route) {
                        "back" -> navController.popBackStack()
                        else -> navController.navigate(route)
                    }
                }
            )
        }

        // Message
        composable(Screen.Message.route) {
            MessageScreen(
                onNavigateToNotifications = {
                    navController.navigate(Screen.Notifications.route)
                },
                onNavigateToComments = { weiboId ->
                    navController.navigate(Screen.Comments.createRoute(weiboId))
                }
            )
        }

        // Profile
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToDrafts = {
                    navController.navigate(Screen.Drafts.route)
                },
                onNavigateToUserTimeline = { userId ->
                    navController.navigate(Screen.UserProfile.createRoute(userId))
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }

        // Settings
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Weibo Detail
        composable(
            route = Screen.WeiboDetail.route,
            arguments = listOf(navArgument("weiboId") { type = NavType.StringType })
        ) { backStackEntry ->
            val weiboIdStr = backStackEntry.arguments?.getString("weiboId") ?: "0"
            WeiboDetailScreen(
                weiboId = weiboIdStr,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUserProfile = { userId ->
                    navController.navigate(Screen.UserProfile.createRoute(userId))
                },
                onNavigateToImageViewer = { urls, index ->
                    navController.navigate(Screen.ImageViewer.createRoute(urls, index))
                },
                onNavigateToComments = { id ->
                    navController.navigate(Screen.Comments.createRoute(id))
                }
            )
        }

        // User Profile
        composable(
            route = Screen.UserProfile.route,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
            UserProfileScreen(
                userId = userId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToWeiboDetail = { weiboId ->
                    navController.navigate(Screen.WeiboDetail.createRoute(weiboId))
                },
                onNavigateToImageViewer = { urls, index ->
                    navController.navigate(Screen.ImageViewer.createRoute(urls, index))
                }
            )
        }

        // Comments
        composable(
            route = Screen.Comments.route,
            arguments = listOf(navArgument("weiboId") { type = NavType.LongType })
        ) { backStackEntry ->
            val weiboId = backStackEntry.arguments?.getLong("weiboId") ?: 0L
            CommentsScreen(
                weiboId = weiboId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUserProfile = { userId ->
                    navController.navigate(Screen.UserProfile.createRoute(userId))
                }
            )
        }

        // Search
        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUserProfile = { userId ->
                    navController.navigate(Screen.UserProfile.createRoute(userId))
                },
                onNavigateToWeiboDetail = { weiboId ->
                    navController.navigate(Screen.WeiboDetail.createRoute(weiboId))
                }
            )
        }

        // Drafts
        composable(Screen.Drafts.route) {
            DraftsScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditDraft = { draft ->
                    // 编辑草稿
                    navController.navigate(Screen.Post.route)
                }
            )
        }

        // Repost
        composable(
            route = Screen.Repost.route,
            arguments = listOf(navArgument("weiboId") { type = NavType.LongType })
        ) { backStackEntry ->
            val weiboId = backStackEntry.arguments?.getLong("weiboId") ?: 0L
            RepostScreen(
                weiboId = weiboId,
                onRepostSuccess = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Image Viewer
        composable(
            route = Screen.ImageViewer.route,
            arguments = listOf(
                navArgument("urls") { type = NavType.StringType },
                navArgument("index") { type = NavType.IntType; defaultValue = 0 }
            )
        ) { backStackEntry ->
            val urlsString = backStackEntry.arguments?.getString("urls") ?: ""
            val index = backStackEntry.arguments?.getInt("index") ?: 0
            val urls = urlsString.split(",")

            ImageViewerScreen(
                imageUrls = urls,
                initialIndex = index,
                onDismiss = { navController.popBackStack() },
                onDownload = { url ->
                    // 处理下载
                },
                onShare = { url ->
                    // 处理分享
                }
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    currentDestination: androidx.navigation.NavDestination?
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route?.startsWith(item.route) == true } == true

            NavigationBarItem(
                icon = {
                    Icon(
                        painter = getIconPainter(item.icon),
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) },
                selected = selected,
                onClick = {
                    if (item.route == "post") {
                        // 发布页面需要特殊处理
                        navController.navigate(Screen.Post.route) {
                            launchSingleTop = true
                        }
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

@Composable
fun getIconPainter(iconName: String): androidx.compose.ui.graphics.painter.Painter {
    // 使用 Material Icons
    return when (iconName) {
        "Home" -> painterResource(android.R.drawable.ic_menu_compass)
        "Search" -> painterResource(android.R.drawable.ic_menu_search)
        "AddCircle" -> painterResource(android.R.drawable.ic_menu_add)
        "Mail" -> painterResource(android.R.drawable.ic_menu_send)
        "Person" -> painterResource(android.R.drawable.ic_menu_myplaces)
        else -> painterResource(android.R.drawable.ic_menu_help)
    }
}

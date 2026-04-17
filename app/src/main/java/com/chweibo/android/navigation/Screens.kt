package com.chweibo.android.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Main : Screen("main")
    data object Home : Screen("home")
    data object Discover : Screen("discover")
    data object Post : Screen("post")
    data object Message : Screen("message")
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
    data object WeiboDetail : Screen("weibo_detail/{weiboId}") {
        fun createRoute(weiboId: Long) = "weibo_detail/$weiboId"
    }

    data object UserProfile : Screen("user_profile/{userId}") {
        fun createRoute(userId: Long) = "user_profile/$userId"
    }

    data object ImageViewer : Screen("image_viewer?urls={urls}&index={index}") {
        fun createRoute(urls: List<String>, index: Int = 0): String {
            val urlString = urls.joinToString(",")
            return "image_viewer?urls=$urlString&index=$index"
        }
    }

    data object Drafts : Screen("drafts")
    data object Comments : Screen("comments/{weiboId}") {
        fun createRoute(weiboId: Long) = "comments/$weiboId"
    }

    data object Search : Screen("search")
    data object Notifications : Screen("notifications")
    data object Repost : Screen("repost/{weiboId}") {
        fun createRoute(weiboId: Long) = "repost/$weiboId"
    }
}

// 底部导航项
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: String
) {
    data object Home : BottomNavItem("home", "首页", "Home")
    data object Discover : BottomNavItem("discover", "发现", "Search")
    data object Post : BottomNavItem("post", "发布", "AddCircle")
    data object Message : BottomNavItem("message", "消息", "Mail")
    data object Profile : BottomNavItem("profile", "我的", "Person")
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Discover,
    BottomNavItem.Post,
    BottomNavItem.Message,
    BottomNavItem.Profile
)

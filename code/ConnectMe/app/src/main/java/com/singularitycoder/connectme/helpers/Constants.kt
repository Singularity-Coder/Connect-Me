package com.singularitycoder.connectme.helpers

import com.singularitycoder.connectme.MainFragment
import com.singularitycoder.connectme.search.SearchFragment

val dummyImageUrls = listOf(
    "https://images.pexels.com/photos/2850287/pexels-photo-2850287.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/167587/pexels-photo-167587.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/3225517/pexels-photo-3225517.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://cdn.pixabay.com/photo/2022/12/11/07/07/pine-7648311_1280.jpg",
    "https://images.pexels.com/photos/33041/antelope-canyon-lower-canyon-arizona.jpg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/2922672/pexels-photo-2922672.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
)

object IntentKey {
    const val LOCATION_TOGGLE_STATUS = "LOCATION_TOGGLE_STATUS"
}

object DbKey {
    const val DB_TREASURE = "db_treasure"
    const val TABLE_TREASURE = "table_treasure"
}

object BroadcastKey {
    const val LOCATION_TOGGLE_STATUS = "LOCATION_TOGGLE_STATUS"
}

object FragmentsTag {
    val MAIN = MainFragment::class.java.simpleName
    val SEARCH = SearchFragment::class.java.simpleName
}

object BottomSheetTag {
    const val WEBSITE_ACTIONS = "WEBSITE_ACTIONS_BOTTOM_SHEET"
    const val USER_PROFILE = "USER_PROFILE_BOTTOM_SHEET"
}

enum class QuickActionTabMenu(val value: String) {
    NONE(value = ""),
    NAVIGATE_BACK(value = "Navigate Back"),
    HOME(value = "Home"),
    SHARE_TAB(value = "Share Tab"),
    CLOSE_TAB(value = "Close Tab"),
    REFRESH_TAB(value = "Refresh Tab"),
    NAVIGATE_FORWARD(value = "Navigate Forward"),
}

enum class Tab(val value: String) {
    EXPLORE(value = "Explore"),
    FEED(value = "Feed"),
    COLLECTIONS(value = "Collections"),
//    REMAINDERS(value = "Remainders"),
//    NOTES(value = "Notes"),
    FOLLOWING(value = "Following"),
    HISTORY(value = "History"),
}

enum class UserProfile(val value: String) {
    FOLLOW(value = "Follow"),
    FOLLOWING(value = "Following"),
    FOLLOWERS(value = "Followers"),
    FOLLOW_REQUESTS(value = "Requests"),
}

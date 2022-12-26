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

enum class DateType(val value: String) {
    dd_MMM_yyyy(value = "dd MMM yyyy"),
    dd_MMM_yyyy_h_mm_a(value = "dd-MMM-yyyy h:mm a"),
    dd_MMM_yyyy_hh_mm_a(value = "dd MMM yyyy, hh:mm a"),
    dd_MMM_yyyy_hh_mm_ss_a(value = "dd MMM yyyy, hh:mm:ss a"),
    dd_MMM_yyyy_h_mm_ss_aaa(value = "dd MMM yyyy, h:mm:ss aaa"),
    yyyy_MM_dd_T_HH_mm_ss_SS_Z(value = "yyyy-MM-dd'T'HH:mm:ss.SS'Z'")
}

enum class Tab(val value: String) {
    EXPLORE(value = "Explore"),
    FOLLOWING(value = "Following"),
    FEED(value = "Feed"),
    REMAINDERS(value = "Remainders"),
    NOTES(value = "Notes"),
    HISTORY(value = "History"),
    COLLECTIONS(value = "Collections"),
}

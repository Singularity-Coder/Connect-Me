package com.singularitycoder.connectme.helpers


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
    HISTORY(value = "History"),
    COLLECTIONS(value = "Collections"),
}

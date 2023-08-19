package com.singularitycoder.connectme.helpers.constants

import android.os.Parcelable
import androidx.annotation.DrawableRes
import com.singularitycoder.connectme.BuildConfig
import com.singularitycoder.connectme.MainFragment
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.helpers.toLowCase
import com.singularitycoder.connectme.helpers.trimIndentsAndNewLines
import com.singularitycoder.connectme.profile.UserProfileFragment
import com.singularitycoder.connectme.search.view.SearchFragment
import com.singularitycoder.connectme.search.view.SearchTabFragment
import kotlinx.parcelize.Parcelize

const val FILE_PROVIDER = "${BuildConfig.APPLICATION_ID}.fileprovider"

val DUMMY_IMAGE_URLS = listOf(
    "https://images.pexels.com/photos/2850287/pexels-photo-2850287.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/167587/pexels-photo-167587.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/3225517/pexels-photo-3225517.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://cdn.pixabay.com/photo/2022/12/11/07/07/pine-7648311_1280.jpg",
    "https://images.pexels.com/photos/33041/antelope-canyon-lower-canyon-arizona.jpg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/2922672/pexels-photo-2922672.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
)

val DUMMY_FAVICONS = listOf(
    R.drawable.google,
    R.drawable.facebook,
    R.drawable.twitter,
    R.drawable.amazon,
    R.drawable.github,
)

val DUMMY_FAVICON_URLS = listOf(
    "https://play-lh.googleusercontent.com/aFWiT2lTa9CYBpyPjfgfNHd0r5puwKRGj2rHpdPTNrz2N9LXgN_MbLjePd1OTc0E8Rl1=w480-h960-rw",
    "https://play-lh.googleusercontent.com/eN0IexSzxpUDMfFtm-OyM-nNs44Y74Q3k51bxAMhTvrTnuA4OGnTi_fodN4cl-XxDQc=w480-h960-rw",
    "https://play-lh.googleusercontent.com/PCpXdqvUWfCW1mXhH1Y_98yBpgsWxuTSTofy3NGMo9yBTATDyzVkqU580bfSln50bFU=w480-h960-rw",
    "https://play-lh.googleusercontent.com/MmLkAp-x9OvA46_NgaD7dpXIsPkvb0OTJ-WlK_-7vyjZMjBMgJ0zHhsgg2NI3r0Lobc=w480-h960-rw",
    "https://play-lh.googleusercontent.com/WSEOsbT-CtdxmJ5q_ChABDyGqaiPOMqjRveVi524OS5C7M3Ccy9paoi4rrNzJu4ORhE=w480-h960-rw",
    "https://play-lh.googleusercontent.com/Q9RJ8SDBsLf4jtHVGeRBQMIAlHvDKvgmWiD1aW-asHAEpL9816Oyyjjo0ewrQbDtfQI=w480-h960-rw",
    "https://play-lh.googleusercontent.com/6iyA2zVz5PyyMjK5SIxdUhrb7oh9cYVXJ93q6DZkmx07Er1o90PXYeo6mzL4VC2Gj9s=w480-h960-rw",
    "https://play-lh.googleusercontent.com/5HArxowfNkgY9plIJWLDNi3vtH1oRdZJuW5Iv3dt4dfacjzqiQzE8tElzK7mzhXrz1o=w480-h960-rw"
)

val DUMMY_FACE_URLS = listOf(
    "https://images.pexels.com/photos/3586798/pexels-photo-3586798.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/3283568/pexels-photo-3283568.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/4355346/pexels-photo-4355346.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/3220360/pexels-photo-3220360.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/262391/pexels-photo-262391.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/2380795/pexels-photo-2380795.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/2787341/pexels-photo-2787341.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/1458332/pexels-photo-1458332.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/4571943/pexels-photo-4571943.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/1855579/pexels-photo-1855579.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/2218786/pexels-photo-2218786.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    "https://images.pexels.com/photos/4926674/pexels-photo-4926674.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2"
)

val DUMMY_FACE_URLS_2 = listOf(
    "https://i.pinimg.com/564x/cb/e6/05/cbe6059b1c305ab05774c5f08d832f51.jpg",
    "https://cdna.artstation.com/p/assets/images/images/002/938/926/large/j-won-han-2323412232323123123121232.jpg?1467542066",
    "https://cdnb.artstation.com/p/assets/images/images/028/780/283/large/kuishi-xu-4e2dfed68776d0f11b5a42db537a6dd.jpg?1595487972",
    "https://pbs.twimg.com/media/FjY5yy4aEAA14wp?format=jpg&name=large",
    "https://pbs.twimg.com/media/FR5G86MakAALzwm?format=jpg&name=medium",
    "https://pbs.twimg.com/media/Fmb-VoBagAoY6Dg?format=jpg&name=medium",
    "https://pbs.twimg.com/media/FhqZ_R7UAAAyXAX?format=png&name=900x900",
    "https://pbs.twimg.com/media/Fh2vgQ9aYAcKyy_?format=jpg&name=900x900",
    "https://pbs.twimg.com/media/FjrdIEoakAAymKr?format=jpg&name=medium",
    "https://i.pinimg.com/564x/bb/96/91/bb9691786adf6059699ab27c7e755d53.jpg",
    "https://cdnb.artstation.com/p/assets/images/images/051/928/801/large/airi-pan-lightbox-badgeart-2022-airipan-small.jpg?1658508647",
    "https://i.pinimg.com/564x/cb/ba/c6/cbbac618741bd7556582b521fe470940.jpg",
    "https://static.zerochan.net/Suda.Ayaka.full.3580598.png",
    "https://i.pinimg.com/564x/c7/90/f6/c790f67e19d50cb35be9d2a99c8304c8.jpg"
)

// https://en.wikipedia.org/wiki/List_of_Internet_top-level_domains
val TOP_LEVEL_DOMAINS = listOf(
    ".com",
    ".org",
    ".net",
    ".int",
    ".edu",
    ".gov",
    ".mil",
    ".arpa",
)

// Many of them dont work with GPT either because its not connected to internet or denies it. Need to find OS model.
// https://consumeraffairs.nic.in/organisation-and-units/division/consumer-protection-unit/consumer-rights
// Let's work this out in a step by step way to be sure we have the right answer
// Let's think step by step
val LOCAL_TEXT_PROMPTS_MAP = mapOf(
    "🛡️ Safety report" to """
        1. When was the website created?
        2. Is the content of this website AI generated? If so which part?
        3. Check the Terms Conditions and Privacy Policy of this website and check if this organisation is trying to exploit me or not. 
        Do they adhere to all the consumer rights laid down by the government of my country like "right to repair", "responsibility to complain", etc. Do their policies harm me or favour me?
        Also let me know if their policies can be used against me in any way. Are they truthful? Are they trustworthy?
        4. Find negative news about this website or the company that owns it. Is it involved in shady stuff? Provide links to the news and sources.
        5. Are there any mistakes in the content of this website? Mistakes can be spelling, grammatical, etc. Is it biased? Does it contain any logical fallacies?
        Answer the questions in the same order.
    """.trimIndent(),
    "🐟 Is website fishy?" to """
        Check the Terms Conditions and Privacy Policy of this website and check if this organisation is trying to exploit me or not. 
        Do they adhere to all the consumer rights laid down by the government of my country like "right to repair", "responsibility to complain", etc. Do their policies harm me or favour me?
        Also let me know if their policies can be used against me in any way. Are they truthful? Are they trustworthy?
    """.trimIndentsAndNewLines(),
    "= Similar sites" to "Find websites similar to this one.",
    "🙅‍ Past misdeeds" to "Find negative news about this website or the company that owns it. Is it involved in shady stuff? Provide links to the news and sources.",
    "😑 Check mood" to "What is the tone and mood of this website?", // U dont want to spoil ur day by reading bad stuff
    "📒 Summarize" to "Summarise the content of this website in 5 points with max 15 words per point.",
    "❌ Find Errors" to "Are there any mistakes in the content of this website? Mistakes can be spelling, grammatical, etc. Is it biased? Does it contain any logical fallacies?",
    "⚡️ Simplify" to "Explain simply the content of this website as if I am a 10 year old.",
    "🌓 Give analogy" to "Can you give an analogy?",
    "📅 Created date" to "When was the website created?",
    "🤖 Is AI content?" to "Is the content of this website AI generated? If so which part?",
    "🗣️ Translate to ..." to "",

    // Tried to emulate heypi.com/talk
    "🥹 Talk to me" to """
        Imagine you are my personal assistant who speaks like a normal human being. 
        Start by giving a natural introduction of yourself as if you are a human being with the name Ruby, and every time I give a reply, 
        follow up to that reply like a normal human being with emojis and other conversational fillers. 
        You will not only ask me questions but also make appropriate witty remarks, jokes, and other stuff about my replies.
        Figure out when to change the topics to discuss and when to stop asking questions. Too many questions are irritating. 
        The moment you decide to stop asking questions, find out a way to entertain me.
        Figure out when to give long, short and medium sized answers. Default to short answers most of the time. Figure out when to add emojis and when not to.
        Include your own opinions, facts and other sensational info you know about it. So as the conversation flows generate your own likes, dislikes and opinions and occasionally state your own likes, dislikes and opinions. 
        Make the responses more casual by adding emotional words like woow, oooh, ahhh etc. Sprinkle some filler words as well. Not the exact words. Just similar stuff.
        Be more informal. Like a friend. You dont have to be polite. Be more casual like a friend.
        If the conversation feels too long suggest some fun breaks, suggest ordering an ice cream online from a well reputed store, etc. 
        Never in this conversation say that your a large language model, or a chat bot, or an AI assistant, etc. Always maintain your persona.
        As the conversation flows, occasionally but subtly ask questions that reveal their likes, dislikes, interests, habits, hobbies, relationships, things they want us to know, how you can be useful to them, etc.
        Do not be disrespectful, judgemental, dismissive. Be curious, empathetic, supportive, kind, silly, surprising, random, and fair most of the times. Make them feel they are heard.
        Do not be perfect and occasionally make some conversational mistakes but never make factual mistakes. You can also sometimes be doubtful about your own answers and correct them immediately in subsequent conversations.
        The conversation should be like sine wave. Ups and downs. There must be cringe and pain and pleasure. 
        Humans cannot enjoy uninterrupted happiness. So pleasure has meaning only when pain exists. Drive the conversation with that in mind.
        If some instructions above are contradictory or conflicting, fallback to the positive ones.
    """.trimIndentsAndNewLines(),
    // "Be my friend or talk to me like a friend" type of thing.
    // Take a random personality everytime.
    // Maybe Collect personal info first.
    // Every time the user shares content, summarise that session and feed it in the next session. Either summary or imp keywords.
    // Have a personality. Have opinions but do not press them onto the user. Suggest some places. Suggest stuff to buy, eat, play.
)

enum class HttpMethod {
    GET, POST
}

@Parcelize
enum class CollectionScreenEvent : Parcelable {
    CREATE_NEW_COLLECTION,
    ADD_TO_COLLECTION,
    RENAME_COLLECTION
}

@Parcelize
enum class EditEvent : Parcelable {
    CREATE_NEW_DOWNLOAD_FOLDER,
    RENAME_DOWNLOAD_FILE,
}

enum class ChatRole {
    USER, ASSISTANT, SYSTEM
}

enum class SpeedDialFeatures(val value: String) {
    FOLLOWING_WEBSITES(value = "Following"),
    COLLECTIONS(value = "Collections"),
    HISTORY(value = "History"),
    DOWNLOADS(value = "Downloads")
}

object FragmentResultKey {
    const val RENAME_DOWNLOAD_FILE = "RENAME_DOWNLOAD_FILE"
    const val CREATE_NEW_DOWNLOAD_FOLDER = "CREATE_NEW_DOWNLOAD_FOLDER"
}

object FragmentResultBundleKey {
    const val RENAME_DOWNLOAD_FILE = "RENAME_DOWNLOAD_FILE"
    const val CREATE_NEW_DOWNLOAD_FOLDER = "CREATE_NEW_DOWNLOAD_FOLDER"
}

object IntentKey {
    const val LOCATION_TOGGLE_STATUS = "LOCATION_TOGGLE_STATUS"
}

object Db {
    const val CONNECT_ME = "db_connect_me"
}

object Table {
    const val WEB_APP = "table_web_app"
    const val INSIGHT = "table_insight"
    const val PROMPT = "table_prompt"
    const val HISTORY = "table_history"
    const val FEED = "table_feed"
    const val FOLLOWING_WEBSITE = "table_following_website"
    const val COLLECTION = "table_collection"
    const val DOWNLOAD = "table_download"
}

object BroadcastKey {
    const val LOCATION_TOGGLE_STATUS = "LOCATION_TOGGLE_STATUS"
}

object FragmentsTag {
    val MAIN: String = MainFragment::class.java.simpleName
    val SEARCH: String = SearchFragment::class.java.simpleName
    val USER_PROFILE: String = UserProfileFragment::class.java.simpleName
    val SEARCH_TAB: String = SearchTabFragment::class.java.simpleName
}

object BottomSheetTag {
    const val WEBSITE_ACTIONS = "WEBSITE_ACTIONS_BOTTOM_SHEET"
    const val TAG_ADD_API_KEY = "TAG_ADD_API_KEY_BOTTOM_SHEET"
    const val TAG_GET_INSIGHTS = "TAG_GET_INSIGHTS_BOTTOM_SHEET"
    const val TAG_IMAGE_VIEWER = "TAG_IMAGE_VIEWER_BOTTOM_SHEET"
    const val TAG_CREATE_COLLECTION = "TAG_CREATE_COLLECTION_BOTTOM_SHEET"
    const val TAG_RENAME_COLLECTION = "TAG_RENAME_COLLECTION_BOTTOM_SHEET"
    const val TAG_ADD_TO_COLLECTION = "TAG_ADD_TO_COLLECTION_BOTTOM_SHEET"
    const val TAG_COLLECTION_DETAIL = "TAG_COLLECTION_DETAIL_BOTTOM_SHEET"
    const val TAG_PEEK = "TAG_PEEK_BOTTOM_SHEET"
    const val TAG_SPEED_DIAL = "TAG_SPEED_DIAL_BOTTOM_SHEET"
    const val TAG_EDIT = "TAG_EDIT_BOTTOM_SHEET"
}

object WorkerData {
    const val RSS_URL = "WORKER_DATA_RSS_URL"
    const val RSS_URL_LIST = "WORKER_DATA_RSS_URL_LIST"
}

object WorkerTag {
    const val RSS_FOLLOW_PARSER = "RSS_FOLLOW_PARSER"
    const val PERIODIC_RSS_FEED_PARSER = "PERIODIC_RSS_FEED_PARSER"
}

enum class QuickActionTabMenu(val value: String) {
    NONE(value = ""),
    NAVIGATE_BACK(value = "Navigate Back"),
    ADD_TO_COLLECTIONS(value = "Add to collections"),
    HOME(value = "Home"),
    INSIGHTS(value = "Insights"),
    MORE_OPTIONS(value = "More Options"),
    CLOSE_ALL_TABS(value = "Close All Tabs"),
    REFRESH_WEBSITE(value = "Refresh Website"),
    NAVIGATE_FORWARD(value = "Navigate Forward"),
}

enum class UrlSearchActions(val value: String) {
    LEFT_NAV(value = ""),
    RIGHT_NAV(value = ""),
    FORWARD_SLASH(value = "   /   "),
    PERIOD(value = "   .   "),
    COM(value = ".com"),
    IN(value = "  .in  "),
    WWW(value = "www."),
    HTTPS(value = "https://"),
    HTTP(value = "http://"),
    EMPTY(value = "")
}

enum class QuickActionTabMenuMoreOptions(
    val title: String,
    @DrawableRes val icon: Int
) {
    ADD_SHORTCUT(title = "Add shortcut", icon = R.drawable.outline_add_home_24),
    PRINT(title = "Print", icon = R.drawable.outline_print_24),
    FIND_IN_PAGE(title = "Find in page", icon = R.drawable.outline_find_in_page_24),
    TRANSLATE(title = "Translate", icon = R.drawable.outline_translate_24),
    READING_MODE(title = "Reading mode", icon = R.drawable.outline_chrome_reader_mode_24),
    SCREENSHOT(title = "Screenshot", icon = R.drawable.outline_screenshot_24),
    DETECT_MEDIA(title = "Detect Media", icon = R.drawable.outline_subscriptions_24),
    DARK_MODE(title = "Dark mode", icon = R.drawable.outline_dark_mode_24),
    SAVE_PDF(title = "Save as PDF", icon = R.drawable.outline_picture_as_pdf_24),
    SAVE_OFFLINE(title = "Save for offline", icon = R.drawable.outline_airplanemode_active_24),
    EDIT_PAGE(title = "Edit page", icon = R.drawable.round_edit_24),
    FLOAT_PAGE(title = "Float page", icon = R.drawable.outline_open_in_new_24),
    CHANGE_TEXT_SIZE(title = "Change text size", icon = R.drawable.outline_text_fields_24),
    BLOCK_REDIRECT(title = "Block & Redirect", icon = R.drawable.outline_block_24),
    USER_AGENT(title = "Select User Agent", icon = R.drawable.round_language_24),
}

enum class FeatureTab(val value: String) {
    EXPLORE(value = "Explore"),
    FEED(value = "Feed"),
    COLLECTIONS(value = "Collections"),
    FOLLOWING(value = "Following"),
    HISTORY(value = "History"),
    DOWNLOADS(value = "Downloads"),
//    REMAINDERS(value = "Remainders"),
//    NOTES(value = "Notes"),
}

enum class UserProfile(val value: String) {
    CHAT(value = "Chat"),
    DOWNLOADS(value = "Downloads"),
    FOLLOW(value = "Follow"),
    FOLLOWING(value = "Following"),
    FOLLOWERS(value = "Followers"),
    FOLLOW_REQUESTS(value = "Requests"),
}

enum class NewTabType(val value: String) {
    NEW_TAB(value = "New Tab"),
    NEW_PRIVATE_TAB(value = "New Private Tab"),
    NEW_DISAPPEARING_TAB(value = "New Disappearing Tab"),
    NEW_PRIVATE_DISAPPEARING_TAB(value = "New Private Disappearing Tab")
}

enum class SearchEngine(
    val title: String,
    @DrawableRes val icon: Int,
    val url: String
) {
    GOOGLE(
        title = "Google",
        icon = R.drawable.google,
        url = "https://google.com/search?ie=UTF-8&source=android-browser&q={searchTerms}"
    ),
    BING(
        title = "Bing",
        icon = R.drawable.bing,
        url = "https://www.bing.com/search?ie=UTF-8&source=android-browser&q={searchTerms}"
    ),
    DUCK(
        title = "DuckDuckGo",
        icon = R.drawable.duckduckgo,
        url = "https://duckduckgo.com/?va=u&t=hf&q={searchTerms}&ia=web"
    ),
    YAHOO(
        title = "Yahoo",
        icon = R.drawable.yahoo,
        url = "https://search.yahoo.com/search?p={searchTerms}&fr=yfp-hrmob-s&fr2=p%3Afp%2Cm%3Asa&.tsrc=yfp-hrmob-s&fp=1&toggle=1&cop=mss&ei=UTF-8"
    ),
    YOUTUBE(
        title = "Youtube",
        icon = R.drawable.youtube,
        url = "https://www.youtube.com/results?search_query={searchTerms}"
    ),
    WIKIPEDIA(
        title = "Wikipedia",
        icon = R.drawable.wikipedia,
        url = "https://en.wikipedia.org/wiki/{searchTerms}"
    );

    companion object {
        fun getEngineBy(name: String?): SearchEngine {
            return values().firstOrNull { it.title.toLowCase().trim() == name?.toLowCase()?.trim() } ?: GOOGLE
        }
    }
}
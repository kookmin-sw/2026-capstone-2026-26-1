package com.example.passedpath.navigation

object NavRoute {
    const val ENTRY = "entry"
    const val LOGIN = "login"
    const val PERMISSION_INTRO = "permission_intro"
    const val FRIENDS = "friends"
    const val MAIN = "main"
    const val ADD_PLACE = "add_place"
    const val ADD_PLACE_DATE_KEY = "dateKey"
    const val ADD_PLACE_WITH_DATE = "$ADD_PLACE/{$ADD_PLACE_DATE_KEY}"
    const val EDIT_PLACE_SEARCH = "edit_place_search"
    const val EDIT_PLACE_SEARCH_DATE_KEY = "dateKey"
    const val EDIT_PLACE_SEARCH_WITH_DATE = "$EDIT_PLACE_SEARCH/{$EDIT_PLACE_SEARCH_DATE_KEY}"
    const val MYPAGE = "mypage"

    fun addPlace(dateKey: String): String = "$ADD_PLACE/$dateKey"
    fun editPlaceSearch(dateKey: String): String = "$EDIT_PLACE_SEARCH/$dateKey"
}

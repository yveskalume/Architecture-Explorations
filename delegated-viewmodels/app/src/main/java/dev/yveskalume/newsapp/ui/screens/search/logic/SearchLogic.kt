package dev.yveskalume.newsapp.ui.screens.search.logic

class SearchLogic {
    fun normalizeQuery(query: String): String {
        return query.trim()
    }

    fun canSearch(query: String): Boolean {
        return normalizeQuery(query).isNotBlank()
    }
}

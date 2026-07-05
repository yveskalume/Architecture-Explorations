package dev.yveskalume.newsapp.util.paging

import androidx.compose.runtime.Stable

@JvmInline
value class PageNumber(val value: Int) {
    init {
        require(value >= 1) { "PageNumber must be >= 1" }
    }

    operator fun inc() = PageNumber(value + 1)
}

sealed interface PageState {
    data object Idle : PageState
    data object Loading : PageState
    data object EndReached : PageState

    fun isLoading(): Boolean = this is Loading
}

sealed interface DataState<out T> {
    data object Loading : DataState<Nothing>
    data class Error<T>(val message: String) : DataState<T>
    data class Success<T>(val items: List<T>) : DataState<T>

    fun isLoading(): Boolean = this is Loading
}

@Stable
data class PageSnapshot<T>(
    val currentPage: PageNumber? = null,
    val pageState: PageState = PageState.Idle,
    val dataState: DataState<T> = DataState.Loading,
)

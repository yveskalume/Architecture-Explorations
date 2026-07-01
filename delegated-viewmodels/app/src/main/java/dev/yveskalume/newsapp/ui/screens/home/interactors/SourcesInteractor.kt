package dev.yveskalume.newsapp.ui.screens.home.interactors

import dev.yveskalume.newsapp.domain.model.SourceItem

interface SourcesInteractor {
    fun selectSource(source: SourceItem?)
}

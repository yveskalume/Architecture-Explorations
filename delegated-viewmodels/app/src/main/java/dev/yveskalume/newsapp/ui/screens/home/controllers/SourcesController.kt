package dev.yveskalume.newsapp.ui.screens.home.controllers

import dev.yveskalume.newsapp.domain.model.SourceItem

interface SourcesController {
    fun selectSource(source: SourceItem?)
}

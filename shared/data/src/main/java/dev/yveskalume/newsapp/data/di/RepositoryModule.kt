package dev.yveskalume.newsapp.data.di

import dev.yveskalume.newsappp.data.repository.ArticleRepository
import dev.yveskalume.newsappp.data.repository.ArticleRepositoryImpl
import dev.yveskalume.newsappp.data.repository.SourcesRepository
import dev.yveskalume.newsappp.data.repository.SourcesRepositoryImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryModule = module {
    singleOf(::ArticleRepositoryImpl) bind ArticleRepository::class
    singleOf(::SourcesRepositoryImpl) bind SourcesRepository::class
}

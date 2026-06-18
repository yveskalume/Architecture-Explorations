package dev.yveskalume.newsapp.data.di

import dev.yveskalume.newsapp.data.repository.ArticleRepository
import dev.yveskalume.newsapp.data.repository.ArticleRepositoryImpl
import dev.yveskalume.newsapp.data.repository.SourcesRepository
import dev.yveskalume.newsapp.data.repository.SourcesRepositoryImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryModule = module {
    singleOf(::ArticleRepositoryImpl) bind ArticleRepository::class
    singleOf(::SourcesRepositoryImpl) bind SourcesRepository::class
}

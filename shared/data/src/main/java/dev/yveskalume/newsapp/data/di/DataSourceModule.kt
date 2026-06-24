package dev.yveskalume.newsapp.data.di

import dev.yveskalume.newsapp.data.network.datasource.NewsDataSource
import dev.yveskalume.newsapp.data.network.datasource.NewsDataSourceImpl
import dev.yveskalume.newsapp.data.network.datasource.SourcesDataSource
import dev.yveskalume.newsapp.data.network.datasource.SourcesDataSourceImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataSourceModule = module {
    singleOf(::NewsDataSourceImpl) bind NewsDataSource::class
    singleOf(::SourcesDataSourceImpl) bind SourcesDataSource::class
}

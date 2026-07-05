package dev.yveskalume.newsapp

import android.app.Application
import dev.yveskalume.newsapp.data.di.dataSourceModule
import dev.yveskalume.newsapp.data.di.ktorModule
import dev.yveskalume.newsapp.data.di.repositoryModule
import dev.yveskalume.newsapp.ui.screens.home.homeModule
import dev.yveskalume.newsapp.ui.screens.search.searchModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.annotation.KoinViewModelScopeApi
import org.koin.core.context.startKoin
import org.koin.core.option.viewModelScopeFactory

class NewsApplication : Application() {
    @OptIn(KoinViewModelScopeApi::class)
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@NewsApplication)
            options(viewModelScopeFactory())
            modules(
                ktorModule,
                dataSourceModule,
                repositoryModule,
                homeModule,
                searchModule,
            )
        }
    }
}

package dev.yveskalume.newsappp

import android.app.Application
import dev.yveskalume.newsapp.data.di.dataSourceModule
import dev.yveskalume.newsapp.data.di.ktorModule
import dev.yveskalume.newsapp.data.di.repositoryModule
import dev.yveskalume.newsappp.ui.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class NewsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger()
            androidContext(this@NewsApplication)
            modules(
                ktorModule,
                dataSourceModule,
                repositoryModule,
                viewModelModule
            )
        }
    }
}

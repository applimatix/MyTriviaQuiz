package com.applimatix.mytriviaquiz

import android.app.Application
import com.applimatix.mytriviaquiz.api.TriviaService
import com.applimatix.mytriviaquiz.model.TriviaGame
import com.applimatix.mytriviaquiz.model.TriviaGameImpl
import com.applimatix.mytriviaquiz.view.MainViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

class MyTriviaApplication : Application() {

    val appModule = module {

        single(named("baseURL")) { "https://opentdb.com" }

        single { Retrofit.Builder()
            .baseUrl(get<String>(named("baseURL")))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder().add(
                        KotlinJsonAdapterFactory()
                    ).build()))
            .build() }

        single { TriviaService.create(get()) }

        single<TriviaGame> { TriviaGameImpl(get()).apply { prepareGame() } }

        viewModel { MainViewModel(get()) }
    }

    override fun onCreate() {
        super.onCreate()

        startKoin{
            androidLogger()
            androidContext(this@MyTriviaApplication)
            modules(appModule)
        }
    }
}

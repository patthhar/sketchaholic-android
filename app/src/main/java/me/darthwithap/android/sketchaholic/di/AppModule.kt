package me.darthwithap.android.sketchaholic.di

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import me.darthwithap.android.sketchaholic.util.DispatcherProvider
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

  @Singleton
  @Provides
  fun provideOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
      .addInterceptor(HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
      }).build()
  }

  @Singleton
  @Provides
  fun provideGsonInstance(): Gson {
    return Gson()
  }

  @Singleton
  @Provides
  fun provideDispatcherProvider(): DispatcherProvider {
    return object : DispatcherProvider {
      override val main: CoroutineDispatcher
        get() = Dispatchers.Main
      override val io: CoroutineDispatcher
        get() = Dispatchers.IO
      override val defualt: CoroutineDispatcher
        get() = Dispatchers.Default
    }
  }
}
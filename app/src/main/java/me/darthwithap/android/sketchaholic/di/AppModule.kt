package me.darthwithap.android.sketchaholic.di

import android.app.Application
import android.content.Context
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import me.darthwithap.android.sketchaholic.data.remote.SetupApi
import me.darthwithap.android.sketchaholic.data.repository.SetupRepository
import me.darthwithap.android.sketchaholic.data.repository.SetupRepositoryImpl
import me.darthwithap.android.sketchaholic.util.Constants.HTTP_BASE_URL_LOCAL_HOST
import me.darthwithap.android.sketchaholic.util.DispatcherProvider
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

  @Singleton
  @Provides
  fun provideApplicationContext(app: Application): Context {
    return app.applicationContext
  }

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
  fun provideSetupApi(okHttpClient: OkHttpClient): SetupApi {
    return Retrofit.Builder()
      .baseUrl(HTTP_BASE_URL_LOCAL_HOST)
      .addConverterFactory(GsonConverterFactory.create())
      .client(okHttpClient)
      .build()
      .create(SetupApi::class.java)
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
      override val default: CoroutineDispatcher
        get() = Dispatchers.Default
    }
  }

  @Provides
  @Singleton
  fun provideSetupRepository(
    setupApi: SetupApi,
    context: Context
  ): SetupRepository {
    return SetupRepositoryImpl(
      setupApi,
      context
    )
  }
}
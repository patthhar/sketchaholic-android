package me.darthwithap.android.sketchaholic.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import me.darthwithap.android.sketchaholic.ui.setup.adapters.RoomAdapter
import me.darthwithap.android.sketchaholic.util.DispatcherProvider
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
object SetupModule {
    @Provides
    @Singleton
    fun provideRoomAdapter(
        dispatcherProvider: DispatcherProvider
    ): RoomAdapter {
        return RoomAdapter(dispatcherProvider)
    }
}
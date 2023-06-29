package me.darthwithap.android.sketchaholic.util

import kotlinx.coroutines.CoroutineDispatcher

interface DispatcherProvider {
  val main: CoroutineDispatcher
  val io: CoroutineDispatcher
  val defualt: CoroutineDispatcher
}

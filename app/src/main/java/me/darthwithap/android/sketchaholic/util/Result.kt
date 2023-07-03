package me.darthwithap.android.sketchaholic.util

sealed class Result<out T>(val data: T? = null, val message: String? = null) {
  class Success<out T>(data: T, message: String? = null): Result<T>(data, message)
  class Error<out T>(message: String, data: T? = null): Result<T>(data, message)
}
package me.darthwithap.android.sketchaholic.data.repository

import android.content.Context
import me.darthwithap.android.sketchaholic.R
import me.darthwithap.android.sketchaholic.data.remote.SetupApi
import me.darthwithap.android.sketchaholic.data.remote.responses.BasicApiResponse
import me.darthwithap.android.sketchaholic.data.remote.websockets.Room
import me.darthwithap.android.sketchaholic.util.Result
import me.darthwithap.android.sketchaholic.util.isInternetConnected
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class SetupRepositoryImpl @Inject constructor(
  private val setupApi: SetupApi,
  private val context: Context
) : SetupRepository {
  override suspend fun createRoom(room: Room): Result<BasicApiResponse> {
    if (!context.isInternetConnected()) {
      return Result.Error(context.getString(R.string.error_internet_not_connected))
    }
    val response = try {
      setupApi.createRoom(room)
    } catch (e: HttpException) {
      return Result.Error(context.getString(R.string.error_http_exception))
    } catch (e: IOException) {
      return Result.Error(context.getString(R.string.error_network_error))
    }

    return if (response.isSuccessful && response.body()?.isSuccess == true) {
      Result.Success(response.body()!!)
    } else if (response.body()?.isSuccess == false) {
      Result.Error(response.body()!!.message!!)
    } else {
      Result.Error(context.getString(R.string.error_unknown))
    }
  }

  override suspend fun getRooms(searchQuery: String): Result<List<Room>> {
    if (!context.isInternetConnected()) {
      return Result.Error(context.getString(R.string.error_internet_not_connected))
    }
    val response = try {
      setupApi.getRooms(searchQuery)
    } catch (e: HttpException) {
      return Result.Error(context.getString(R.string.error_http_exception))
    } catch (e: IOException) {
      return Result.Error(context.getString(R.string.error_network_error))
    }

    return if (response.isSuccessful && response.body() != null) {
      Result.Success(response.body()!!)
    } else {
      Result.Error(context.getString(R.string.error_unknown))
    }
  }

  override suspend fun joinRoom(username: String, roomName: String): Result<BasicApiResponse> {
    if (!context.isInternetConnected()) {
      return Result.Error(context.getString(R.string.error_internet_not_connected))
    }
    val response = try {
      setupApi.joinRoom(username, roomName)
    } catch (e: HttpException) {
      return Result.Error(context.getString(R.string.error_http_exception))
    } catch (e: IOException) {
      return Result.Error(context.getString(R.string.error_network_error))
    }

    return if (response.isSuccessful && response.body()?.isSuccess == true) {
      return Result.Success(response.body()!!)
    } else if (response.body()?.isSuccess == false) {
      return Result.Error(response.body()!!.message!!)
    } else {
      Result.Error(context.getString(R.string.error_unknown))
    }
  }
}
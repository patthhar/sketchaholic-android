package me.darthwithap.android.sketchaholic.data.remote

import me.darthwithap.android.sketchaholic.data.remote.responses.BasicApiResponse
import me.darthwithap.android.sketchaholic.data.remote.websockets.Room
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SetupApi {
  @POST("/api/room")
  suspend fun createRoom(
    @Body room: Room
  ): Response<BasicApiResponse>

  @GET("/api/rooms")
  suspend fun getRooms(
    @Query("search_query") searchQuery: String
  ): Response<List<Room>>

  @GET("/api/room/join")
  suspend fun joinRoom(
    @Query("username") username: String,
    @Query("room_name") roomName: String,
  ): Response<BasicApiResponse>
}
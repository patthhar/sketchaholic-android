package me.darthwithap.android.sketchaholic.data.repository

import me.darthwithap.android.sketchaholic.data.remote.responses.BasicApiResponse
import me.darthwithap.android.sketchaholic.data.remote.websockets.Room
import me.darthwithap.android.sketchaholic.util.Result

interface SetupRepository {
  suspend fun createRoom(room: Room): Result<BasicApiResponse>
  suspend fun getRooms(searchQuery: String): Result<List<Room>>
  suspend fun joinRoom(username: String, roomName: String): Result<BasicApiResponse>
}
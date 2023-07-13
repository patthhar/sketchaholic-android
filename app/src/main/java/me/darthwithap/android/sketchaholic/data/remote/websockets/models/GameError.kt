package me.darthwithap.android.sketchaholic.data.remote.websockets.models

import me.darthwithap.android.sketchaholic.util.Constants.TYPE_GAME_ERROR

data class GameError(
  val errorType: Int
) : BaseModel(TYPE_GAME_ERROR) {
  companion object {
    const val ERROR_ROOM_NOT_FOUND = 0
  }
}

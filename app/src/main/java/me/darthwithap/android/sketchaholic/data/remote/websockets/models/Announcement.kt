package me.darthwithap.android.sketchaholic.data.remote.websockets.models

import me.darthwithap.android.sketchaholic.util.Constants.TYPE_ANNOUNCEMENT

data class Announcement(
  val message: String,
  val timestamp: Long,
  val announcementType: Int
): BaseModel(TYPE_ANNOUNCEMENT) {
  companion object {
    const val TYPE_PLAYER_JOINED = 0
    const val TYPE_PLAYER_LEFT = 1
    const val TYPE_PLAYER_DISCONNECTED = 2
    const val TYPE_PLAYER_GUESSED_WORD = 3
    const val TYPE_ALL_PLAYERS_GUESSED_WORD = 4
  }
}

package me.darthwithap.android.sketchaholic.data.remote.websockets

data class Room(
  val name: String,
  var maxPlayers: Int,
  var playerCount: Int = 1
) {
  enum class Phase {
    WAITING_FOR_PLAYERS,
    WAITING_FOR_START,
    NEW_ROUND,
    GAME_RUNNING,
    SHOW_WORD,
    ENDED
  }
}

package me.darthwithap.android.sketchaholic.data.remote.websockets.models

import me.darthwithap.android.sketchaholic.util.Constants.TYPE_GAME_RUNNING_STATE

data class GameRunningState(
  val drawingPlayer: String? = null,
  val word: String
): BaseModel(TYPE_GAME_RUNNING_STATE)

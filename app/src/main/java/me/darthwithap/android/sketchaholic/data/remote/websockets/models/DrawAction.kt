package me.darthwithap.android.sketchaholic.data.remote.websockets.models

import me.darthwithap.android.sketchaholic.util.Constants.TYPE_DRAW_ACTION

data class DrawAction(
  val action: String
) : BaseModel(TYPE_DRAW_ACTION) {
  companion object {
    const val ACTION_UNDO = "ACTION_UNDO"
  }
}
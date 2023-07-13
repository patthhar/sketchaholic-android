package me.darthwithap.android.sketchaholic.data.remote.websockets.models

import me.darthwithap.android.sketchaholic.util.Constants.TYPE_CURR_ROUND_DRAW_INFO

data class RoundDrawInfo(
  val words: List<String>
): BaseModel(TYPE_CURR_ROUND_DRAW_INFO)

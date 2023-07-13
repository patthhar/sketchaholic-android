package me.darthwithap.android.sketchaholic.data.remote.websockets.models

import me.darthwithap.android.sketchaholic.data.remote.websockets.Room
import me.darthwithap.android.sketchaholic.util.Constants.TYPE_PHASE_CHANGE

data class PhaseChange(
  var phase: Room.Phase? = null,
  val time: Long,
  var drawingPlayer: String? = null
) : BaseModel(TYPE_PHASE_CHANGE)

package me.darthwithap.android.sketchaholic.data.remote.websockets.models

import me.darthwithap.android.sketchaholic.util.Constants.TYPE_JOIN_ROOM_HANDSHAKE

data class JoinRoomHandshake(
  val username: String,
  val room: String,
  val clientId: String
) : BaseModel(TYPE_JOIN_ROOM_HANDSHAKE)

package me.darthwithap.android.sketchaholic.data.remote.websockets.models

import me.darthwithap.android.sketchaholic.util.Constants.TYPE_CHAT_MESSAGE

data class ChatMessage(
  val from: String,
  val roomName: String,
  val message: String,
  val timestamp: Long
): BaseModel(TYPE_CHAT_MESSAGE)
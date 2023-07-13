package me.darthwithap.android.sketchaholic.data.remote.websockets

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.tinder.scarlet.Message
import com.tinder.scarlet.MessageAdapter
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.Announcement
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.BaseModel
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.ChatMessage
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.ChosenWord
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.DisconnectRequest
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.DrawAction
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.DrawData
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.GameError
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.GameRunningState
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.JoinRoomHandshake
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.NewWords
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.PhaseChange
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.Ping
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.PlayersDataList
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.RoundDrawInfo
import me.darthwithap.android.sketchaholic.util.Constants.TYPE_ANNOUNCEMENT
import me.darthwithap.android.sketchaholic.util.Constants.TYPE_CHAT_MESSAGE
import me.darthwithap.android.sketchaholic.util.Constants.TYPE_CHOSEN_WORD
import me.darthwithap.android.sketchaholic.util.Constants.TYPE_CURR_ROUND_DRAW_INFO
import me.darthwithap.android.sketchaholic.util.Constants.TYPE_DISCONNECT_REQUEST
import me.darthwithap.android.sketchaholic.util.Constants.TYPE_DRAW_ACTION
import me.darthwithap.android.sketchaholic.util.Constants.TYPE_DRAW_DATA
import me.darthwithap.android.sketchaholic.util.Constants.TYPE_GAME_ERROR
import me.darthwithap.android.sketchaholic.util.Constants.TYPE_GAME_RUNNING_STATE
import me.darthwithap.android.sketchaholic.util.Constants.TYPE_JOIN_ROOM_HANDSHAKE
import me.darthwithap.android.sketchaholic.util.Constants.TYPE_NEW_WORDS
import me.darthwithap.android.sketchaholic.util.Constants.TYPE_PHASE_CHANGE
import me.darthwithap.android.sketchaholic.util.Constants.TYPE_PING
import me.darthwithap.android.sketchaholic.util.Constants.TYPE_PLAYERS_DATA_LIST
import java.lang.reflect.Type

@Suppress("UNCHECKED_CAST")
class CustomGsonMessageAdapter<T> private constructor(
  val gson: Gson
) : MessageAdapter<T> {
  override fun fromMessage(message: Message): T {
    val stringValue = when (message) {
      is Message.Bytes -> message.value.toString()
      is Message.Text -> message.value
    }
    val jsonObject = JsonParser.parseString(stringValue).asJsonObject

    val type: Class<out BaseModel> = when (jsonObject.get("type").asString) {
      TYPE_CHAT_MESSAGE -> ChatMessage::class.java
      TYPE_DRAW_DATA -> DrawData::class.java
      TYPE_ANNOUNCEMENT -> Announcement::class.java
      TYPE_JOIN_ROOM_HANDSHAKE -> JoinRoomHandshake::class.java
      TYPE_PHASE_CHANGE -> PhaseChange::class.java
      TYPE_CHOSEN_WORD -> ChosenWord::class.java
      TYPE_GAME_RUNNING_STATE -> GameRunningState::class.java
      TYPE_PING -> Ping::class.java
      TYPE_DISCONNECT_REQUEST -> DisconnectRequest::class.java
      TYPE_DRAW_ACTION -> DrawAction::class.java
      TYPE_CURR_ROUND_DRAW_INFO -> RoundDrawInfo::class.java
      TYPE_GAME_ERROR -> GameError::class.java
      TYPE_NEW_WORDS -> NewWords::class.java
      TYPE_PLAYERS_DATA_LIST -> PlayersDataList::class.java
      else -> BaseModel::class.java
    }
    val obj = gson.fromJson(stringValue, type)
    return obj as T
  }

  override fun toMessage(data: T): Message {
    var convertedData = data as BaseModel
    convertedData = when (convertedData.type) {
      TYPE_CHAT_MESSAGE -> convertedData as ChatMessage
      TYPE_DRAW_DATA -> convertedData as DrawData
      TYPE_ANNOUNCEMENT -> convertedData as Announcement
      TYPE_JOIN_ROOM_HANDSHAKE -> convertedData as JoinRoomHandshake
      TYPE_PHASE_CHANGE -> convertedData as PhaseChange
      TYPE_CHOSEN_WORD -> convertedData as ChosenWord
      TYPE_GAME_RUNNING_STATE -> convertedData as GameRunningState
      TYPE_PING -> convertedData as Ping
      TYPE_DISCONNECT_REQUEST -> convertedData as DisconnectRequest
      TYPE_DRAW_ACTION -> convertedData as DrawAction
      TYPE_CURR_ROUND_DRAW_INFO -> convertedData as RoundDrawInfo
      TYPE_GAME_ERROR -> convertedData as GameError
      TYPE_NEW_WORDS -> convertedData as NewWords
      TYPE_PLAYERS_DATA_LIST -> convertedData as PlayersDataList
      else -> convertedData
    }
    return Message.Text(gson.toJson(convertedData))
  }

  class Factory(private val gson: Gson) : MessageAdapter.Factory {
    override fun create(type: Type, annotations: Array<Annotation>): MessageAdapter<*> {
      return CustomGsonMessageAdapter<Any>(gson)
    }

  }
}
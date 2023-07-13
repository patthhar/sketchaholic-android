package me.darthwithap.android.sketchaholic.util

object Constants {
  const val DEFAULT_PAINT_THICKNESS = 12f
  const val HTTP_BASE_URL_LOCAL_HOST = "http://192.168.1.4:8080"

  const val WEBSOCKET_BASE_URL = "http://192.168.1.4:8080/ws"

  const val MIN_USERNAME_LENGTH = 2
  const val MAX_USERNAME_LENGTH = 16

  const val MIN_ROOM_NAME_LENGTH = 4
  const val MAX_ROOM_NAME_LENGTH = 16

  const val ERASER_THICKNESS = 50f

  const val KEY_ARG_USERNAME = "username"
  const val KEY_ARG_ROOM_NAME = "roomName"
  const val KEY_CLIENT_ID = "clientId"
  const val KEY_CLIENT_ID_QUERY_PARAM = "client_id"

  const val ROOM_SEARCH_DELAY = 1000L

  const val RECONNECT_INTERVAL = 4000L

  const val STREAM_ADAPTER_ERROR = "Invalid stream adapter"

  const val TYPE_CHAT_MESSAGE = "TYPE_CHAT_MESSAGE"
  const val TYPE_DRAW_DATA = "TYPE_DRAW_DATA"
  const val TYPE_ANNOUNCEMENT = "TYPE_ANNOUNCEMENT"
  const val TYPE_JOIN_ROOM_HANDSHAKE = "TYPE_JOIN_ROOM_HANDSHAKE"
  const val TYPE_PHASE_CHANGE = "TYPE_PHASE_CHANGE"
  const val TYPE_CHOSEN_WORD = "TYPE_CHOSEN_WORD"
  const val TYPE_GAME_RUNNING_STATE = "TYPE_GAME_RUNNING_STATE"
  const val TYPE_PING = "TYPE_PING"
  const val TYPE_PONG = "TYPE_PONG"
  const val TYPE_DISCONNECT_REQUEST = "TYPE_DISCONNECT_REQUEST"
  const val TYPE_DRAW_ACTION = "TYPE_DRAW_ACTION"
  const val TYPE_CURR_ROUND_DRAW_INFO = "TYPE_CURR_ROUND_DRAW_INFO"
  const val TYPE_GAME_ERROR = "TYPE_GAME_ERROR"
  const val TYPE_NEW_WORDS = "TYPE_NEW_WORDS"
  const val TYPE_PLAYERS_DATA_LIST = "TYPE_PLAYERS_DATA_LIST"



}
package me.darthwithap.android.sketchaholic.data.remote.websockets.models

import me.darthwithap.android.sketchaholic.util.Constants.TYPE_PLAYERS_DATA_LIST

data class PlayersDataList(
  val players: List<PlayerData>
) : BaseModel(TYPE_PLAYERS_DATA_LIST)

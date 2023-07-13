package me.darthwithap.android.sketchaholic.data.remote.websockets.models

import me.darthwithap.android.sketchaholic.util.Constants.TYPE_CHOSEN_WORD

data class ChosenWord(
  val chosenWord: String,
  val room: String
) : BaseModel(TYPE_CHOSEN_WORD)

package me.darthwithap.android.sketchaholic.data.remote.websockets.models

import me.darthwithap.android.sketchaholic.util.Constants.TYPE_NEW_WORDS

data class NewWords(
  val newWords: List<String>
) : BaseModel(TYPE_NEW_WORDS)

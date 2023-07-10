package me.darthwithap.android.sketchaholic.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import me.darthwithap.android.sketchaholic.util.Constants.KEY_CLIENT_ID
import java.util.UUID

val Context.dataStore by preferencesDataStore("settings")

suspend fun DataStore<Preferences>.clientId(): String {
  val clientIdKey = stringPreferencesKey(KEY_CLIENT_ID)
  val preferences = data.first()
  val clientId = preferences[clientIdKey]
  return if (clientId != null) {
    clientId
  } else {
    val newClientId = UUID.randomUUID().toString()
    edit { settings ->
      settings[clientIdKey] = newClientId
    }
    newClientId
  }
}
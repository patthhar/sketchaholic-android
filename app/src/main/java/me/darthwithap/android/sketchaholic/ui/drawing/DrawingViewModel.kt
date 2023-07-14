package me.darthwithap.android.sketchaholic.ui.drawing

import android.app.GameState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.tinder.scarlet.WebSocket
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import me.darthwithap.android.sketchaholic.R
import me.darthwithap.android.sketchaholic.data.remote.websockets.DrawingApi
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.Announcement
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.BaseModel
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.ChatMessage
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.ChosenWord
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.DrawAction
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.DrawAction.Companion.ACTION_UNDO
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.DrawData
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.GameError
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.GameRunningState
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.NewWords
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.Ping
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.Pong
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.RoundDrawInfo
import me.darthwithap.android.sketchaholic.util.DispatcherProvider
import javax.inject.Inject

@HiltViewModel
class DrawingViewModel @Inject constructor(
  private val drawingApi: DrawingApi,
  private val dispatchers: DispatcherProvider,
  private val gson: Gson
) : ViewModel() {

  private val _selectedColorButtonId = MutableStateFlow(R.id.rb_black)
  val selectedColorButtonId: StateFlow<Int> = _selectedColorButtonId

  private val _connectionProgressBarVisible = MutableStateFlow(true)
  val connectionProgressBarVisible: StateFlow<Boolean> = _connectionProgressBarVisible

  private val _chosenWordOverlayVisible = MutableStateFlow(false)
  val chosenWordOverlayVisible: StateFlow<Boolean> = _chosenWordOverlayVisible

  private val connectionEventChannel = Channel<WebSocket.Event>()
  val connectionEvent = connectionEventChannel.receiveAsFlow().flowOn(dispatchers.io)

  private val socketEventChannel = Channel<SocketEvent>()
  val socketEvent = socketEventChannel.receiveAsFlow().flowOn(dispatchers.io)

  init {
    observeConnectionEvents()
    observeBaseModels()
  }

  fun setChooseWordOverlayVisibility(isVisible: Boolean) {
    _chosenWordOverlayVisible.value = isVisible
  }

  fun setConnectionProgressBarVisible(isVisible: Boolean) {
    _connectionProgressBarVisible.value = isVisible
  }

  fun checkRadioButton(id: Int) {
    _selectedColorButtonId.value = id
  }

  fun observeConnectionEvents() {
    viewModelScope.launch(dispatchers.io) {
      drawingApi.observeConnectionEvents().collectLatest {
        connectionEventChannel.send(it)
      }
    }
  }

  fun observeBaseModels() {
    viewModelScope.launch {
      drawingApi.observeBaseModels().collectLatest {
        when (it) {
          is DrawData -> {
            socketEventChannel.send(SocketEvent.DrawDataEvent(it))
          }

          is DrawAction -> {
            when (it.action) {
              ACTION_UNDO -> socketEventChannel.send(SocketEvent.Undo)
            }
          }

          is GameError -> socketEventChannel.send(SocketEvent.GameErrorEvent(it))
          is Ping -> {
            sendBaseModel(Pong())
          }
        }
      }
    }
  }

  fun sendBaseModel(data: BaseModel) {
    viewModelScope.launch(dispatchers.io) {
      drawingApi.sendBaseModel(data)
    }
  }

  sealed class SocketEvent {
    data class ChatMessageEvent(val data: ChatMessage) : SocketEvent()
    data class AnnouncementEvent(val data: Announcement) : SocketEvent()
    data class GameStateEvent(val data: GameState) : SocketEvent()
    data class DrawDataEvent(val data: DrawData) : SocketEvent()
    data class NewWordsEvent(val data: NewWords) : SocketEvent()
    data class ChosenWordEvent(val data: ChosenWord) : SocketEvent()
    data class GameErrorEvent(val data: GameError) : SocketEvent()
    data class RoundDrawInfoEvent(val data: RoundDrawInfo) : SocketEvent()
    data class GameRunningStateEvent(val data: GameRunningState) : SocketEvent()
    object Undo : SocketEvent()
  }
}
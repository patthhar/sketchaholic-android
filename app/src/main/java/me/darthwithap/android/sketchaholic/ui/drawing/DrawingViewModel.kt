package me.darthwithap.android.sketchaholic.ui.drawing

import android.app.GameState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.tinder.scarlet.WebSocket
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import me.darthwithap.android.sketchaholic.R
import me.darthwithap.android.sketchaholic.data.remote.websockets.DrawingApi
import me.darthwithap.android.sketchaholic.data.remote.websockets.Room
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
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.PhaseChange
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.Ping
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.Pong
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.RoundDrawInfo
import me.darthwithap.android.sketchaholic.util.CoroutineTimer
import me.darthwithap.android.sketchaholic.util.DispatcherProvider
import javax.inject.Inject

@HiltViewModel
class DrawingViewModel @Inject constructor(
  private val drawingApi: DrawingApi,
  private val dispatchers: DispatcherProvider,
  private val gson: Gson
) : ViewModel() {

  private val _chat = MutableStateFlow<List<BaseModel>>(listOf())
  val chat: StateFlow<List<BaseModel>> = _chat

  private val _newWords = MutableStateFlow(NewWords(listOf()))
  val newWords: StateFlow<NewWords> get() = _newWords

  private val _phase = MutableStateFlow(PhaseChange(time = 0L))
  val phase: StateFlow<PhaseChange> get() = _phase

  private val _phaseTime = MutableStateFlow(0L)
  val phaseTime: StateFlow<Long> get() = _phaseTime

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

  private val timer = CoroutineTimer()
  private var timerJob: Job? = null

  init {
    observeConnectionEvents()
    observeBaseModels()
  }

  fun cancelTimer() {
    timerJob?.cancel()
  }

  private fun setTimer(duration: Long) {
    timerJob?.cancel()
    timerJob = timer.timeAndEmit(duration, viewModelScope) {
      _phaseTime.value = it
    }
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

  private fun observeBaseModels() {
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

          is ChatMessage -> {
            socketEventChannel.send(SocketEvent.ChatMessageEvent(it))
          }

          is Announcement -> {
            socketEventChannel.send(SocketEvent.AnnouncementEvent(it))
          }

          is NewWords -> {
            _newWords.value = it
            socketEventChannel.send(SocketEvent.NewWordsEvent(it))
          }

          is ChosenWord -> {
            socketEventChannel.send(SocketEvent.ChosenWordEvent(it))
          }

          is GameError -> socketEventChannel.send(SocketEvent.GameErrorEvent(it))
          is Ping -> {
            sendBaseModel(Pong())
          }

          is PhaseChange -> {
            it.phase.let { _ ->
              _phase.value = it
            }
            _phaseTime.value = it.time
            if (it.phase != Room.Phase.WAITING_FOR_PLAYERS) {
              setTimer(it.time)
            }
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

  fun sendChatMessage(message: ChatMessage) {
    if (message.message.trim().isEmpty()) return
    viewModelScope.launch(dispatchers.io) {
      drawingApi.sendBaseModel(message)
    }
  }

  fun chooseWord(word: String, roomName: String) {
    val chosenWord = ChosenWord(word, roomName)
    sendBaseModel(chosenWord)
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
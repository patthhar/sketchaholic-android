package me.darthwithap.android.sketchaholic.ui.setup.fragments.createRoom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import me.darthwithap.android.sketchaholic.data.remote.websockets.Room
import me.darthwithap.android.sketchaholic.data.repository.SetupRepository
import me.darthwithap.android.sketchaholic.util.Constants.MAX_ROOM_NAME_LENGTH
import me.darthwithap.android.sketchaholic.util.Constants.MIN_ROOM_NAME_LENGTH
import me.darthwithap.android.sketchaholic.util.DispatcherProvider
import me.darthwithap.android.sketchaholic.util.Result
import javax.inject.Inject

@HiltViewModel
class CreateRoomViewModel @Inject constructor(
  private val repository: SetupRepository,
  private val dispatchers: DispatcherProvider
) : ViewModel() {

  private val _event = MutableSharedFlow<Event>()
  val event: SharedFlow<Event> = _event

  fun createRoom(room: Room) {
    val roomName = room.name.trim()
    viewModelScope.launch(dispatchers.main) {
      when {
        roomName.isEmpty() -> _event.emit(Event.InputEmptyError)
        roomName.length < MIN_ROOM_NAME_LENGTH -> _event.emit(Event.InputTooShortError)
        roomName.length > MAX_ROOM_NAME_LENGTH -> _event.emit(Event.InputTooLongError)
        else -> {
          val result = repository.createRoom(room)
          if (result is Result.Success) {
            _event.emit(Event.CreateRoom(room))
          } else {
            _event.emit(Event.CreateRoomError(result.message ?: return@launch))
          }
        }
      }
    }
  }

  fun joinRoom(room: String, username: String) {
    val roomName = room.trim()
    val userName = username.trim()
    viewModelScope.launch(dispatchers.main) {
      val result = repository.joinRoom(userName, roomName)
      if (result is Result.Success) {
        _event.emit(Event.JoinRoom(roomName))
      } else {
        _event.emit(Event.JoinRoomError(result.message ?: return@launch))
      }
    }
  }

  sealed class Event {
    object InputEmptyError : Event()
    object InputTooShortError : Event()
    object InputTooLongError : Event()
    data class CreateRoom(val room: Room) : Event()
    data class CreateRoomError(val errorMsg: String) : Event()
    data class JoinRoom(val roomName: String) : Event()
    data class JoinRoomError(val errorMsg: String) : Event()
  }
}
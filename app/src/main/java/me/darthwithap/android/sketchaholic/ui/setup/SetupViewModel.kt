package me.darthwithap.android.sketchaholic.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.darthwithap.android.sketchaholic.data.remote.websockets.Room
import me.darthwithap.android.sketchaholic.data.repository.SetupRepository
import me.darthwithap.android.sketchaholic.util.Constants.MAX_ROOM_NAME_LENGTH
import me.darthwithap.android.sketchaholic.util.Constants.MAX_USERNAME_LENGTH
import me.darthwithap.android.sketchaholic.util.Constants.MIN_ROOM_NAME_LENGTH
import me.darthwithap.android.sketchaholic.util.Constants.MIN_USERNAME_LENGTH
import me.darthwithap.android.sketchaholic.util.DispatcherProvider
import me.darthwithap.android.sketchaholic.util.Result
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
  private val repository: SetupRepository,
  private val dispatchers: DispatcherProvider
) : ViewModel() {

  private val _setupEvent = MutableSharedFlow<SetupEvent>()
  val setupEvent: SharedFlow<SetupEvent> = _setupEvent

  private val _rooms = MutableStateFlow<SetupEvent>(SetupEvent.GetRoomsEmpty)
  val rooms: StateFlow<SetupEvent> = _rooms

  fun validateUsernameAndNavigateToSelectRoom(username: String) {
    val trimmedUsername = username.trim()
    viewModelScope.launch(dispatchers.main) {
      when {
        trimmedUsername.isEmpty() -> _setupEvent.emit(SetupEvent.InputEmptyError)
        trimmedUsername.length < MIN_USERNAME_LENGTH -> _setupEvent.emit(SetupEvent.InputTooShortError)
        trimmedUsername.length > MAX_USERNAME_LENGTH -> _setupEvent.emit(SetupEvent.InputTooLongError)
        else -> _setupEvent.emit(SetupEvent.NavigateToSelectRoom(trimmedUsername))
      }
    }
  }

  fun createRoom(room: Room) {
    val roomName = room.name.trim()
    viewModelScope.launch(dispatchers.main) {
      when {
        roomName.isEmpty() -> _setupEvent.emit(SetupEvent.InputEmptyError)
        roomName.length < MIN_ROOM_NAME_LENGTH -> _setupEvent.emit(SetupEvent.InputTooShortError)
        roomName.length > MAX_ROOM_NAME_LENGTH -> _setupEvent.emit(SetupEvent.InputTooLongError)
        else -> {
          val result = repository.createRoom(room)
          if (result is Result.Success) {
            _setupEvent.emit(SetupEvent.CreateRoom(room))
          } else {
            _setupEvent.emit(SetupEvent.CreateRoomError(result.message ?: return@launch))
          }
        }
      }
    }
  }

  fun getRooms(searchQuery: String = "") {
    _rooms.value = SetupEvent.GetRoomsLoading
    viewModelScope.launch(dispatchers.main) {
      val result = repository.getRooms(searchQuery)
      if (result is Result.Success) {
        if (result.data?.isEmpty() == false) {
          _rooms.value = SetupEvent.GetRooms(result.data)
        } else {
          _rooms.value = SetupEvent.GetRoomsEmpty
        }
      } else {
        _setupEvent.emit(SetupEvent.GetRoomsError(result.message ?: return@launch))
      }
    }
  }

  fun joinRoom(room: String, username: String) {
    val roomName = room.trim()
    val userName = username.trim()
    viewModelScope.launch(dispatchers.main) {
      val result = repository.joinRoom(userName, roomName)
      if (result is Result.Success) {
        _setupEvent.emit(SetupEvent.JoinRoom(roomName))
      } else {
        _setupEvent.emit(SetupEvent.JoinRoomError(result.message ?: return@launch))
      }
    }
  }

  sealed class SetupEvent {
    object InputEmptyError : SetupEvent()
    object InputTooShortError : SetupEvent()
    object InputTooLongError : SetupEvent()
    data class CreateRoom(val room: Room) : SetupEvent()
    data class CreateRoomError(val errorMsg: String) : SetupEvent()
    data class NavigateToSelectRoom(val username: String) : SetupEvent()
    data class JoinRoom(val roomName: String) : SetupEvent()
    data class JoinRoomError(val errorMsg: String) : SetupEvent()
    data class GetRooms(val rooms: List<Room>) : SetupEvent()
    data class GetRoomsError(val errorMsg: String) : SetupEvent()
    object GetRoomsLoading : SetupEvent()
    object GetRoomsEmpty : SetupEvent()
  }
}
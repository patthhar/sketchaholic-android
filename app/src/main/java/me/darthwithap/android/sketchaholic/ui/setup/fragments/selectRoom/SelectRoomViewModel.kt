package me.darthwithap.android.sketchaholic.ui.setup.fragments.selectRoom

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
import me.darthwithap.android.sketchaholic.util.DispatcherProvider
import me.darthwithap.android.sketchaholic.util.Result
import javax.inject.Inject

@HiltViewModel
class SelectRoomViewModel @Inject constructor(
  private val repository: SetupRepository,
  private val dispatchers: DispatcherProvider
) : ViewModel() {

  private val _event = MutableSharedFlow<Event>()
  val event: SharedFlow<Event> = _event

  private val _rooms = MutableStateFlow<Event>(Event.GetRoomsLoading)
  val rooms: StateFlow<Event> = _rooms

  fun getRooms(searchQuery: String = "") {
    _rooms.value = Event.GetRoomsLoading
    viewModelScope.launch(dispatchers.main) {
      val result = repository.getRooms(searchQuery)
      if (result is Result.Success) {
        if (result.data?.isEmpty() == false) {
          _rooms.value = Event.GetRooms(result.data)
        } else {
          _rooms.value = Event.GetRoomsEmpty
        }
      } else {
        _event.emit(Event.GetRoomsError(result.message ?: return@launch))
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
    data class JoinRoom(val roomName: String) : Event()
    data class JoinRoomError(val errorMsg: String) : Event()
    data class GetRooms(val rooms: List<Room>) : Event()
    data class GetRoomsError(val errorMsg: String) : Event()
    object GetRoomsLoading : Event()
    object GetRoomsEmpty : Event()
  }
}
package me.darthwithap.android.sketchaholic.ui.setup.fragments.username

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import me.darthwithap.android.sketchaholic.data.repository.SetupRepository
import me.darthwithap.android.sketchaholic.util.Constants.MAX_USERNAME_LENGTH
import me.darthwithap.android.sketchaholic.util.Constants.MIN_USERNAME_LENGTH
import me.darthwithap.android.sketchaholic.util.DispatcherProvider
import javax.inject.Inject

@HiltViewModel
class UsernameViewModel @Inject constructor(
  private val repository: SetupRepository,
  private val dispatchers: DispatcherProvider
) : ViewModel() {

  private val _event = MutableSharedFlow<Event>()
  val event: SharedFlow<Event> = _event

  fun validateUsernameAndNavigateToSelectRoom(username: String) {
    val trimmedUsername = username.trim()
    viewModelScope.launch(dispatchers.main) {
      when {
        trimmedUsername.isEmpty() -> _event.emit(Event.InputEmptyError)
        trimmedUsername.length < MIN_USERNAME_LENGTH -> _event.emit(Event.InputTooShortError)
        trimmedUsername.length > MAX_USERNAME_LENGTH -> _event.emit(Event.InputTooLongError)
        else -> _event.emit(Event.NavigateToSelectRoom(trimmedUsername))
      }
    }
  }

  sealed class Event {
    object InputEmptyError : Event()
    object InputTooShortError : Event()
    object InputTooLongError : Event()
    data class NavigateToSelectRoom(val username: String) : Event()
  }
}
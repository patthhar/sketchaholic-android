package me.darthwithap.android.sketchaholic.ui.drawing

import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.darthwithap.android.sketchaholic.R
import me.darthwithap.android.sketchaholic.data.remote.websockets.DrawingApi
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

  fun checkRadioButton(id: Int) {
    _selectedColorButtonId.value = id
  }
}
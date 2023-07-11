package me.darthwithap.android.sketchaholic.ui.drawing

import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import me.darthwithap.android.sketchaholic.R
import me.darthwithap.android.sketchaholic.databinding.ActivityDrawingBinding
import me.darthwithap.android.sketchaholic.util.Constants

@AndroidEntryPoint
class DrawingActivity : AppCompatActivity() {
  private lateinit var binding: ActivityDrawingBinding
  private val viewModel: DrawingViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityDrawingBinding.inflate(layoutInflater)
    setContentView(binding.root)
    listenToUiStateUpdates()

    binding.colorGroup.setOnCheckedChangeListener { _, id ->
      viewModel.checkRadioButton(id)
    }
  }

  private fun selectColor(color: Int, thickness: Float? = null) {
    binding.drawingView.setColor(color)
    binding.drawingView.setThickness(thickness ?: Constants.DEFAULT_PAINT_THICKNESS)
  }

  private fun listenToUiStateUpdates() {
    lifecycleScope.launchWhenCreated {
      viewModel.selectedColorButtonId.collectLatest { id ->
        binding.colorGroup.check(id)
        when (id) {
          R.id.rb_black -> selectColor(getMyColor(R.color.black))
          R.id.rb_blue -> selectColor(getMyColor(R.color.blue))
          R.id.rb_green -> selectColor(getMyColor(R.color.green))
          R.id.rb_red -> selectColor(getMyColor(R.color.red))
          R.id.rb_yellow -> selectColor(getMyColor(R.color.yellow))
          R.id.rb_orange -> selectColor(getMyColor(R.color.orange))
          R.id.rb_eraser -> selectColor(getMyColor(R.color.white), 44f)
        }
      }
    }
  }

  private fun getMyColor(color: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      resources.getColor(color, theme)
    } else resources.getColor(color)
  }
}
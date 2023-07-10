package me.darthwithap.android.sketchaholic.ui.drawing

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import me.darthwithap.android.sketchaholic.databinding.ActivityDrawingBinding

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

  private fun listenToUiStateUpdates() {
    lifecycleScope.launchWhenCreated {
      viewModel.selectedColorButtonId.collectLatest { id ->
        binding.colorGroup.check(id)
      }
    }
  }
}
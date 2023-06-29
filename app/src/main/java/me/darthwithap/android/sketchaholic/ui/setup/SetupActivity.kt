package me.darthwithap.android.sketchaholic.ui.setup

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import me.darthwithap.android.sketchaholic.databinding.ActivitySetupBinding

class SetupActivity : AppCompatActivity() {

  private lateinit var binding: ActivitySetupBinding
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    binding = ActivitySetupBinding.inflate(layoutInflater)
    setContentView(binding.root)
  }
}
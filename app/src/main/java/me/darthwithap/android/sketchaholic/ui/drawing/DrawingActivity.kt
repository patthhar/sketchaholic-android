package me.darthwithap.android.sketchaholic.ui.drawing

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import me.darthwithap.android.sketchaholic.R
import me.darthwithap.android.sketchaholic.databinding.ActivityDrawingBinding
import me.darthwithap.android.sketchaholic.util.Constants
import me.darthwithap.android.sketchaholic.util.Constants.ERASER_THICKNESS

@AndroidEntryPoint
class DrawingActivity : AppCompatActivity() {
  private lateinit var binding: ActivityDrawingBinding
  private val viewModel: DrawingViewModel by viewModels()

  private lateinit var toggle: ActionBarDrawerToggle
  private lateinit var rvPlayers: RecyclerView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityDrawingBinding.inflate(layoutInflater)
    setContentView(binding.root)
    listenToUiStateUpdates()

    toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)
    toggle.syncState()

    val header = layoutInflater.inflate(R.layout.nav_drawer_header, binding.navView)
    rvPlayers = header.findViewById(R.id.rv_players)
    binding.root.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

    binding.ibPlayers.setOnClickListener {
      binding.root.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
      binding.root.openDrawer(GravityCompat.START)
    }

    binding.root.addDrawerListener(object : DrawerLayout.DrawerListener {
      override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

      override fun onDrawerOpened(drawerView: View) {}

      override fun onDrawerClosed(drawerView: View) {
        binding.root.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
      }

      override fun onDrawerStateChanged(newState: Int) {}

    })

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
          R.id.rb_eraser -> selectColor(getMyColor(R.color.white), ERASER_THICKNESS)
        }
      }
    }
  }

  private fun getMyColor(color: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      resources.getColor(color, theme)
    } else resources.getColor(color)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (toggle.onOptionsItemSelected(item)) {
      return true
    }
    return super.onOptionsItemSelected(item)
  }
}
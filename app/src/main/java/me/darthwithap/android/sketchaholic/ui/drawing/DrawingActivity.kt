package me.darthwithap.android.sketchaholic.ui.drawing

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tinder.scarlet.WebSocket
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import me.darthwithap.android.sketchaholic.R
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.DrawAction
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.GameError.Companion.ERROR_ROOM_NOT_FOUND
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.JoinRoomHandshake
import me.darthwithap.android.sketchaholic.databinding.ActivityDrawingBinding
import me.darthwithap.android.sketchaholic.ui.setup.adapters.ChatMessageAdapter
import me.darthwithap.android.sketchaholic.util.Constants
import me.darthwithap.android.sketchaholic.util.Constants.ERASER_THICKNESS
import me.darthwithap.android.sketchaholic.util.DispatcherProvider
import me.darthwithap.android.sketchaholic.util.snackbar
import javax.inject.Inject

@AndroidEntryPoint
class DrawingActivity : AppCompatActivity() {
  private lateinit var binding: ActivityDrawingBinding
  private val viewModel: DrawingViewModel by viewModels()

  private val args: DrawingActivityArgs by navArgs()

  @Inject
  lateinit var clientId: String

  @Inject
  lateinit var dispatchers: DispatcherProvider

  private lateinit var toggle: ActionBarDrawerToggle
  private lateinit var rvPlayers: RecyclerView

  private lateinit var chatMessageAdapter: ChatMessageAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityDrawingBinding.inflate(layoutInflater)
    setContentView(binding.root)
    listenToUiStateUpdates()
    listenToConnectionEvents()
    listenToSocketEvents()
    setupChatRecyclerView()

    if (args.username == "test") {
      binding.drawingView.isUserDrawing = true
    }

    toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)
    toggle.syncState()

    binding.drawingView.sendRoomName(args.roomName)

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

    binding.ibUndo.setOnClickListener {
      if (binding.drawingView.isUserDrawing) {
        binding.drawingView.undo()
        viewModel.sendBaseModel(DrawAction(DrawAction.ACTION_UNDO))
      }
    }

    binding.colorGroup.setOnCheckedChangeListener { _, id ->
      viewModel.checkRadioButton(id)
    }

    binding.drawingView.setOnDrawListener {
      if (binding.drawingView.isUserDrawing) {
        viewModel.sendBaseModel(it)
      }
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

    lifecycleScope.launchWhenStarted {
      viewModel.connectionProgressBarVisible.collect {
        binding.connectionProgressBar.isVisible = it
      }
    }
    lifecycleScope.launchWhenStarted {
      viewModel.chosenWordOverlayVisible.collect {
        binding.chooseWordOverlay.isVisible = it
      }
    }
  }

  private fun getMyColor(color: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      resources.getColor(color, theme)
    } else resources.getColor(color)
  }

  private fun listenToConnectionEvents() {
    lifecycleScope.launchWhenStarted {
      viewModel.connectionEvent.collect { event ->
        when (event) {
          is WebSocket.Event.OnConnectionOpened<*> -> {
            viewModel.sendBaseModel(
              JoinRoomHandshake(args.username, args.roomName, clientId)
            )
            viewModel.setConnectionProgressBarVisible(false)
          }

          is WebSocket.Event.OnMessageReceived -> {}
          is WebSocket.Event.OnConnectionClosing -> {}
          is WebSocket.Event.OnConnectionClosed -> {
            viewModel.setConnectionProgressBarVisible(false)
          }

          is WebSocket.Event.OnConnectionFailed -> {
            viewModel.setConnectionProgressBarVisible(false)
            event.throwable.printStackTrace()
            snackbar(getString(R.string.error_connection_failed))
          }
        }
      }
    }
  }

  private fun listenToSocketEvents() {
    lifecycleScope.launchWhenStarted {
      viewModel.socketEvent.collectLatest { event ->
        when (event) {
          is DrawingViewModel.SocketEvent.GameErrorEvent -> {
            when (event.data.errorType) {
              ERROR_ROOM_NOT_FOUND -> {
                snackbar(getString(R.string.error_room_not_found))
                finish()
              }
            }
          }

          is DrawingViewModel.SocketEvent.DrawDataEvent -> {
            val drawData = event.data
            if (!binding.drawingView.isUserDrawing) {
              when (drawData.motionEvent) {
                MotionEvent.ACTION_DOWN -> {
                  binding.drawingView.simulateStartTouch(drawData)
                }

                MotionEvent.ACTION_MOVE -> {
                  binding.drawingView.simulateMoveTouch(drawData)
                }

                MotionEvent.ACTION_UP -> {
                  binding.drawingView.simulateReleaseTouch(drawData)
                }

              }
            }
          }

          is DrawingViewModel.SocketEvent.Undo -> {
            binding.drawingView.undo()
          }

          else -> {}
        }
      }
    }
  }

  private fun setupChatRecyclerView() {
    binding.rvChat.apply {
      chatMessageAdapter = ChatMessageAdapter(args.username, dispatchers)
      adapter = chatMessageAdapter
      layoutManager = LinearLayoutManager(this@DrawingActivity)
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (toggle.onOptionsItemSelected(item)) {
      return true
    }
    return super.onOptionsItemSelected(item)
  }
}
package me.darthwithap.android.sketchaholic.ui.drawing

import android.graphics.Color
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.darthwithap.android.sketchaholic.R
import me.darthwithap.android.sketchaholic.data.remote.websockets.Room
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.BaseModel
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.ChatMessage
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.DrawAction
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.GameError.Companion.ERROR_ROOM_NOT_FOUND
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.JoinRoomHandshake
import me.darthwithap.android.sketchaholic.databinding.ActivityDrawingBinding
import me.darthwithap.android.sketchaholic.ui.setup.adapters.ChatMessageAdapter
import me.darthwithap.android.sketchaholic.util.Constants
import me.darthwithap.android.sketchaholic.util.Constants.ERASER_THICKNESS
import me.darthwithap.android.sketchaholic.util.DispatcherProvider
import me.darthwithap.android.sketchaholic.util.hideKeyboard
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

  private var chatUpdateJob: Job? = null

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

    chatMessageAdapter.stateRestorationPolicy =
      RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

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

    binding.ibClearText.setOnClickListener {
      binding.etMessage.text?.clear()
    }

    binding.ibSend.setOnClickListener {
      viewModel.sendChatMessage(
        ChatMessage(
          args.username,
          args.roomName,
          binding.etMessage.text.toString(),
          System.currentTimeMillis()
        )
      )
      binding.etMessage.text?.clear()
      hideKeyboard(binding.root)
    }

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
    lifecycleScope.launchWhenStarted {
      viewModel.chat.collect { chat ->
        if (chatMessageAdapter.chats.isEmpty()) {
          updateChats(chat)
        }
      }
    }
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

    lifecycleScope.launchWhenStarted {
      viewModel.newWords.collect {
        val newWords = it.newWords
        if (newWords.isEmpty()) return@collect
        binding.apply {
          btnFirstWord.text = newWords[0]
          btnSecondWord.text = newWords[1]
          btnThirdWord.text = newWords[2]
          btnFirstWord.setOnClickListener {
            viewModel.chooseWord(newWords[0], args.roomName)
            viewModel.setChooseWordOverlayVisibility(false)
          }
          btnSecondWord.setOnClickListener {
            viewModel.chooseWord(newWords[1], args.roomName)
            viewModel.setChooseWordOverlayVisibility(false)
          }
          btnThirdWord.setOnClickListener {
            viewModel.chooseWord(newWords[3], args.roomName)
            viewModel.setChooseWordOverlayVisibility(false)
          }
        }
      }
    }

    lifecycleScope.launchWhenStarted {
      viewModel.phaseTime.collect { time ->
        binding.roundTimerProgressBar.progress = time.toInt()
        binding.tvRemainingTimeChooseWord.text = (time / 1000L).toString()
      }
    }
    lifecycleScope.launchWhenStarted {
      viewModel.phase.collect { phase ->
        when (phase.phase) {
          Room.Phase.WAITING_FOR_PLAYERS -> {
            binding.tvCurrWord.text = getString(R.string.waiting_for_players)
            viewModel.cancelTimer()
            viewModel.setConnectionProgressBarVisible(false)
            binding.roundTimerProgressBar.progress = binding.roundTimerProgressBar.max
          }

          Room.Phase.WAITING_FOR_START -> {
            binding.roundTimerProgressBar.max = phase.time.toInt()
            binding.tvCurrWord.text = getString(R.string.waiting_for_start)
          }

          Room.Phase.NEW_ROUND -> {
            phase.drawingPlayer?.let {
              binding.tvCurrWord.text = getString(R.string.player_chosing_word, it)
            }
            binding.apply {
              drawingView.isEnabled = false
              drawingView.setColor(Color.BLACK)
              drawingView.setThickness(Constants.DEFAULT_PAINT_THICKNESS)
              roundTimerProgressBar.max = phase.time.toInt()
              val isUserDrawingPlayer = phase.drawingPlayer == args.username
              chooseWordOverlay.isVisible = isUserDrawingPlayer
            }
          }

          Room.Phase.GAME_RUNNING -> {
            binding.chooseWordOverlay.isVisible = false
            binding.roundTimerProgressBar.max = phase.time.toInt()
          }

          Room.Phase.SHOW_WORD -> {
            binding.apply {
              if (drawingView.isDrawing) {
                drawingView.finishOffDrawing()
              }
              drawingView.isEnabled = false
              drawingView.setThickness(Constants.DEFAULT_PAINT_THICKNESS)
              drawingView.setColor(Color.BLACK)
              roundTimerProgressBar.max = phase.time.toInt()
            }
          }

          else -> {}
        }
      }
    }
  }

  private fun updateChats(chats: List<BaseModel>) {
    chatUpdateJob?.cancel()
    chatUpdateJob = lifecycleScope.launch {
      chatMessageAdapter.updateData(chats)
    }
  }

  private suspend fun addChatToRv(chat: BaseModel) {
    val canScrollDown = binding.rvChat.canScrollVertically(1)
    updateChats(chatMessageAdapter.chats + chat)
    chatUpdateJob?.join()
    if (!canScrollDown) { // We are at the last position already
      binding.rvChat.scrollToPosition(chatMessageAdapter.chats.size - 1)
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

          is DrawingViewModel.SocketEvent.ChosenWordEvent -> {
            binding.tvCurrWord.text = event.data.chosenWord
            binding.ibUndo.isEnabled = false
          }

          is DrawingViewModel.SocketEvent.ChatMessageEvent -> {
            addChatToRv(event.data)
          }

          is DrawingViewModel.SocketEvent.AnnouncementEvent -> {
            addChatToRv(event.data)
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

  override fun onPause() {
    super.onPause()
    binding.rvChat.layoutManager?.onSaveInstanceState()
  }
}
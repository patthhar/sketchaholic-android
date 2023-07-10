package me.darthwithap.android.sketchaholic.ui.setup.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.darthwithap.android.sketchaholic.R
import me.darthwithap.android.sketchaholic.data.remote.websockets.Room
import me.darthwithap.android.sketchaholic.databinding.FragmentSelectRoomBinding
import me.darthwithap.android.sketchaholic.ui.setup.SetupViewModel
import me.darthwithap.android.sketchaholic.ui.setup.adapters.RoomAdapter
import me.darthwithap.android.sketchaholic.util.Constants.KEY_ARG_ROOM_NAME
import me.darthwithap.android.sketchaholic.util.Constants.KEY_ARG_USERNAME
import me.darthwithap.android.sketchaholic.util.Constants.ROOM_SEARCH_DELAY
import me.darthwithap.android.sketchaholic.util.navigateSafely
import me.darthwithap.android.sketchaholic.util.notVisible
import me.darthwithap.android.sketchaholic.util.snackbar
import me.darthwithap.android.sketchaholic.util.visible
import javax.inject.Inject

@AndroidEntryPoint
class SelectRoomFragment : Fragment(R.layout.fragment_select_room) {
  private var _binding: FragmentSelectRoomBinding? = null
  private val binding: FragmentSelectRoomBinding
    get() = _binding!!

  @Inject
  lateinit var roomAdapter: RoomAdapter

  private val viewModel: SetupViewModel by activityViewModels()
  private val args: SelectRoomFragmentArgs by navArgs()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    _binding = FragmentSelectRoomBinding.bind(view)
    setupRecyclerView()
    listenToRooms()
    listenToEvents()

    viewModel.getRooms()

    var searchJob: Job? = null
    binding.etRoomName.addTextChangedListener {
      searchJob?.cancel()
      searchJob = lifecycleScope.launch {
        delay(ROOM_SEARCH_DELAY)
        viewModel.getRooms(it.toString())
      }
    }

    binding.ibReload.setOnClickListener {
      binding.roomsProgressBar.visible()
      noRoomsFound(false)
      viewModel.getRooms(binding.etRoomName.text.toString())
    }

    binding.btnCreateRoom.setOnClickListener {
      findNavController().navigateSafely(
        R.id.action_selectRoomFragment_to_createRoomFragment,
        Bundle().apply {
          putString(KEY_ARG_USERNAME, args.username)
        }
      )
    }
  }

  private fun listenToEvents() {
    lifecycleScope.launchWhenCreated {
      viewModel.setupEvent.collectLatest { event ->
        when (event) {
          is SetupViewModel.SetupEvent.GetRoomsError -> {
            binding.roomsProgressBar.notVisible()
            noRoomsFound(false)
            snackbar(event.errorMsg)
          }

          is SetupViewModel.SetupEvent.JoinRoom -> {
            binding.roomsProgressBar.notVisible()
            findNavController().navigateSafely(
              R.id.action_selectRoomFragment_to_drawingActivity,
              navArgs = Bundle().apply {
                putString(KEY_ARG_USERNAME, args.username)
                putString(KEY_ARG_ROOM_NAME, event.roomName)
              }
            )
          }

          is SetupViewModel.SetupEvent.JoinRoomError -> {
            binding.roomsProgressBar.notVisible()
            noRoomsFound(true)
            snackbar(event.errorMsg)
          }

          else -> Unit
        }
      }
    }
  }

  private fun listenToRooms() {
    lifecycleScope.launchWhenCreated {
      viewModel.rooms.collectLatest { event ->
        when (event) {
          SetupViewModel.SetupEvent.GetRoomsLoading -> {
            binding.roomsProgressBar.isVisible = true
          }

          SetupViewModel.SetupEvent.GetRoomsEmpty -> {
            binding.roomsProgressBar.isVisible = false
            noRoomsFound(true)
          }

          is SetupViewModel.SetupEvent.GetRooms -> {
            binding.roomsProgressBar.notVisible()
            lifecycleScope.launch {
              roomAdapter.updateData(event.rooms)
            }
          }

          else -> Unit
        }
      }
    }
  }

  private fun noRoomsFound(noRooms: Boolean) {
    binding.tvNoRoomsFound.isVisible = noRooms
    binding.ivNoRoomsFound.isVisible = noRooms
  }

  private fun setupRecyclerView() {
    binding.rvRooms.apply {
      adapter = roomAdapter
      layoutManager = LinearLayoutManager(context)
    }
    roomAdapter.setOnRoomClickListener(object : RoomAdapter.RoomClickListener {
      override fun onClick(room: Room) {
        binding.roomsProgressBar.visible()
        viewModel.joinRoom(room.name, args.username)
      }
    })
  }

  override fun onDestroy() {
    super.onDestroy()
    _binding = null
  }
}
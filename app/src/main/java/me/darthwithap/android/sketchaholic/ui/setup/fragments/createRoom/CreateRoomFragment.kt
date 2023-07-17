package me.darthwithap.android.sketchaholic.ui.setup.fragments.createRoom

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.flow.collectLatest
import me.darthwithap.android.sketchaholic.R
import me.darthwithap.android.sketchaholic.data.remote.websockets.Room
import me.darthwithap.android.sketchaholic.databinding.FragmentCreateRoomBinding
import me.darthwithap.android.sketchaholic.util.Constants.KEY_ARG_ROOM_NAME
import me.darthwithap.android.sketchaholic.util.Constants.KEY_ARG_USERNAME
import me.darthwithap.android.sketchaholic.util.Constants.MAX_ROOM_NAME_LENGTH
import me.darthwithap.android.sketchaholic.util.Constants.MIN_ROOM_NAME_LENGTH
import me.darthwithap.android.sketchaholic.util.hideKeyboard
import me.darthwithap.android.sketchaholic.util.navigateSafely
import me.darthwithap.android.sketchaholic.util.notVisible
import me.darthwithap.android.sketchaholic.util.snackbar
import me.darthwithap.android.sketchaholic.util.visible

class CreateRoomFragment : Fragment(R.layout.fragment_create_room) {
  private var _binding: FragmentCreateRoomBinding? = null
  private val binding: FragmentCreateRoomBinding
    get() = _binding!!

  private val viewModel: CreateRoomViewModel by activityViewModels()
  private val args: CreateRoomFragmentArgs by navArgs()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    _binding = FragmentCreateRoomBinding.bind(view)
    requireActivity().window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    setupRoomSizeSpinner()
    listenToEvents()

    binding.btnCreateRoomCreateRoom.setOnClickListener {
      binding.createRoomProgressBar.visible()
      val roomSize = binding.tvMaxPersons.text.toString().let {
        if (it.isNotEmpty()) it.toInt() else -1
      }
      viewModel.createRoom(
        Room(
          binding.etRoomNameCreateRoom.text.toString(),
          roomSize
        )
      )
      requireActivity().hideKeyboard(binding.root)
    }
  }

  private fun listenToEvents() {
    lifecycleScope.launchWhenCreated {
      viewModel.event.collectLatest { event ->
        binding.createRoomProgressBar.notVisible()
        when (event) {
          CreateRoomViewModel.Event.RoomSizeEmpty -> {
            snackbar(R.string.error_room_size_empty)
          }

          CreateRoomViewModel.Event.InputEmptyError -> {
            snackbar(R.string.error_room_name_empty)
          }

          CreateRoomViewModel.Event.InputTooLongError -> {
            snackbar(getString(R.string.error_room_name_too_long, MAX_ROOM_NAME_LENGTH))
          }

          CreateRoomViewModel.Event.InputTooShortError -> {
            snackbar(getString(R.string.error_room_name_too_short, MIN_ROOM_NAME_LENGTH))
          }

          is CreateRoomViewModel.Event.CreateRoom -> {
            viewModel.joinRoom(event.room.name, args.username)
          }

          is CreateRoomViewModel.Event.CreateRoomError -> {
            snackbar(event.errorMsg)
          }

          is CreateRoomViewModel.Event.JoinRoom -> {
            findNavController().navigateSafely(
              R.id.action_createRoomFragment_to_drawingActivity,
              Bundle().apply {
                putString(KEY_ARG_USERNAME, args.username)
                putString(KEY_ARG_ROOM_NAME, event.roomName)
              })
          }

          is CreateRoomViewModel.Event.JoinRoomError -> {
            snackbar(event.errorMsg)
          }
        }
      }
    }
  }

  private fun setupRoomSizeSpinner() {
    val sizes = resources.getStringArray(R.array.room_sizes)
    val adapter = ArrayAdapter(requireContext(), R.layout.list_item_room_size, sizes)
    binding.tvMaxPersons.setAdapter(adapter)
  }

  override fun onDestroy() {
    super.onDestroy()
    _binding = null
  }
}
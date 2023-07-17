package me.darthwithap.android.sketchaholic.ui.setup.fragments.username

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import me.darthwithap.android.sketchaholic.R
import me.darthwithap.android.sketchaholic.databinding.FragmentUsernameBinding
import me.darthwithap.android.sketchaholic.util.Constants.KEY_ARG_USERNAME
import me.darthwithap.android.sketchaholic.util.Constants.MAX_USERNAME_LENGTH
import me.darthwithap.android.sketchaholic.util.Constants.MIN_USERNAME_LENGTH
import me.darthwithap.android.sketchaholic.util.hideKeyboard
import me.darthwithap.android.sketchaholic.util.navigateSafely
import me.darthwithap.android.sketchaholic.util.snackbar

@AndroidEntryPoint
class UsernameFragment : Fragment(R.layout.fragment_username) {
  private var _binding: FragmentUsernameBinding? = null
  private val binding: FragmentUsernameBinding
    get() = _binding!!

  private val viewModel: UsernameViewModel by viewModels()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    _binding = FragmentUsernameBinding.bind(view)

    listenToEvents()

    binding.btnNext.setOnClickListener {
      viewModel.validateUsernameAndNavigateToSelectRoom(binding.etUsername.text.toString())
      requireActivity().hideKeyboard(binding.root)
    }
  }

  private fun listenToEvents() {
    lifecycleScope.launchWhenCreated {
      viewModel.event.collectLatest { event ->
        when (event) {
          is UsernameViewModel.Event.InputEmptyError -> {
            snackbar(R.string.error_username_empty)
          }

          is UsernameViewModel.Event.InputTooShortError -> {
            snackbar(getString(R.string.error_username_too_short, MIN_USERNAME_LENGTH))
          }

          is UsernameViewModel.Event.InputTooLongError -> {
            snackbar(getString(R.string.error_username_too_long, MAX_USERNAME_LENGTH))
          }

          is UsernameViewModel.Event.NavigateToSelectRoom -> {
            findNavController().navigateSafely(
              actionId = R.id.action_usernameFragment_to_selectRoomFragment,
              navArgs = Bundle().apply {
                putString(KEY_ARG_USERNAME, event.username)
              }
            )
          }
        }
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    _binding = null
  }
}
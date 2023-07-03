package me.darthwithap.android.sketchaholic.ui.setup.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import me.darthwithap.android.sketchaholic.R
import me.darthwithap.android.sketchaholic.databinding.FragmentUsernameBinding

class UsernameFragment : Fragment(R.layout.fragment_username) {
  private var _binding: FragmentUsernameBinding? = null
  private val binding: FragmentUsernameBinding
    get() = _binding!!

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    _binding = FragmentUsernameBinding.bind(view)
  }

  override fun onDestroy() {
    super.onDestroy()
    _binding = null
  }
}
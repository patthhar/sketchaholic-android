package me.darthwithap.android.sketchaholic.util

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

fun Fragment.snackbar(
  @StringRes resId: Int
) {
  Snackbar.make(
    requireView(),
    getString(resId),
    Snackbar.LENGTH_LONG
  ).show()
}

fun Fragment.snackbar(
  text: String
) {
  Snackbar.make(
    requireView(),
    text,
    Snackbar.LENGTH_LONG
  ).show()
}
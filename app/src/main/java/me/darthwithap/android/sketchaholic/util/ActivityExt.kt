package me.darthwithap.android.sketchaholic.util

import android.app.Activity
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar

fun Activity.snackbar(
  @StringRes resId: Int
) {
  Snackbar.make(
    findViewById(android.R.id.content),
    this.getString(resId),
    Snackbar.LENGTH_LONG
  ).show()
}

fun Activity.snackbar(
  text: String
) {
  Snackbar.make(
    findViewById(android.R.id.content),
    text,
    Snackbar.LENGTH_LONG
  ).show()
}
package me.darthwithap.android.sketchaholic.util

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
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

fun Activity.hideKeyboard(root: View) {
  val windowToken = root.windowToken
  val inputMethodManager =
    getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
  windowToken?.let {
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
  } ?: kotlin.run {
    try {
      val keyboardHeight =
        InputMethodManager::class.java.getMethod("getInputMethodWindowVisibleHeight")
          .invoke(inputMethodManager) as Int
      if (keyboardHeight > 0) {
        inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}
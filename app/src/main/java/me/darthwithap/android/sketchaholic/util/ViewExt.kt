package me.darthwithap.android.sketchaholic.util

import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar

fun View.visible() {
  this.isVisible = true
}

fun View.notVisible() {
  this.isVisible = false
}
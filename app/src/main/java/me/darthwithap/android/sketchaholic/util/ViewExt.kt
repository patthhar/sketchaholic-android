package me.darthwithap.android.sketchaholic.util

import android.view.View
import androidx.core.view.isVisible

fun View.visible() {
  this.isVisible = true
}

fun View.notVisible() {
  this.isVisible = false
}
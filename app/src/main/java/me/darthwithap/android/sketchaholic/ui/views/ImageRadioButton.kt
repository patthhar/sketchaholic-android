package me.darthwithap.android.sketchaholic.ui.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import me.darthwithap.android.sketchaholic.R
import kotlin.properties.Delegates

class ImageRadioButton(
  context: Context,
  attrs: AttributeSet? = null
) : AppCompatRadioButton(context, attrs) {
  private var uncheckedDrawable: VectorDrawableCompat? = null
  private var checkedDrawable: VectorDrawableCompat? = null

  private var viewHeight by Delegates.notNull<Int>()
  private var viewWidth by Delegates.notNull<Int>()

  init {
    context.theme.obtainStyledAttributes(attrs, R.styleable.ImageRadioButton, 0, 0).apply {
      try {
        val uncheckedId = getResourceId(R.styleable.ImageRadioButton_uncheckedDrawable, 0)
        val checkedId = getResourceId(R.styleable.ImageRadioButton_checkedDrawable, 0)
        if (uncheckedId != 0) {
          uncheckedDrawable = VectorDrawableCompat.create(resources, uncheckedId, null)
        }
        if (uncheckedId != 0) {
          checkedDrawable = VectorDrawableCompat.create(resources, checkedId, null)
        }
      } finally {
        recycle()
      }
    }
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    viewWidth = w
    viewHeight = h
  }

  override fun onDraw(canvas: Canvas?) {
    super.onDraw(canvas)
    canvas?.let {
      if (!isChecked) {
        uncheckedDrawable?.setBounds(
          paddingLeft,
          paddingTop,
          viewWidth - paddingRight,
          viewHeight - paddingBottom
        )
        uncheckedDrawable?.draw(canvas)
      } else {
        checkedDrawable?.setBounds(
          paddingLeft,
          paddingTop,
          viewWidth - paddingRight,
          viewHeight - paddingBottom
        )
        checkedDrawable?.draw(canvas)
      }
    }
  }
}
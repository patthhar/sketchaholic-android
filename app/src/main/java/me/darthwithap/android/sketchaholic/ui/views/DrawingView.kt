package me.darthwithap.android.sketchaholic.ui.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_UP
import android.view.View
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.DrawData
import me.darthwithap.android.sketchaholic.util.Constants
import java.util.Stack
import kotlin.math.abs

class DrawingView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null
) : View(context, attrs) {
  private var viewWidth: Int? = null
  private var viewHeight: Int? = null
  private var bmp: Bitmap? = null
  private var canvas: Canvas? = null
  private var currX: Float? = null
  private var currY: Float? = null
  var smoothness = 4
  var isDrawing = false

  private var paint = Paint(Paint.DITHER_FLAG).apply {
    isDither = true
    isAntiAlias = true
    color = Color.BLACK
    style = Paint.Style.STROKE
    strokeCap = Paint.Cap.ROUND
    strokeJoin = Paint.Join.ROUND
    strokeWidth = Constants.DEFAULT_PAINT_THICKNESS
  }

  private var path = Path()
  private var pathStack = Stack<PathData>()
  private var pathDataChangedListener: ((Stack<PathData>) -> Unit)? = null
  private var onDrawListener: ((DrawData) -> Unit)? = null

  var roomName: String? = null

  var isUserDrawing: Boolean = false
    set(value) {
      isEnabled = value
      field = value
    }

  fun setOnDrawListener(listener: (DrawData) -> Unit) {
    onDrawListener = listener
  }

  fun setPathDataChangeListener(listener: (Stack<PathData>) -> Unit) {
    pathDataChangedListener = listener
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    viewWidth = w
    viewHeight = h
    bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    canvas = Canvas(bmp!!)
  }

  override fun onDraw(canvas: Canvas?) {
    super.onDraw(canvas)
    val initialColor = paint.color
    val initialThickness = paint.strokeWidth
    for (pathData in pathStack) {
      paint.apply {
        color = pathData.color
        strokeWidth = pathData.thickness
      }
      canvas?.drawPath(pathData.path, paint)
    }
    paint.apply {
      color = initialColor
      strokeWidth = initialThickness
    }
    canvas?.drawPath(path, paint)
  }

  private fun startedTouch(x: Float, y: Float) {
    path.reset()
    path.moveTo(x, y)
    currX = x
    currY = y
    onDrawListener?.let { drawFn ->
      val drawData = createDrawData(x, y, x, y, ACTION_DOWN)
      drawFn.invoke(drawData)
    }
    invalidate()
  }

  private fun moveTouch(toX: Float, toY: Float) {
    val dx = abs(toX - (currX ?: return))
    val dy = abs(toY - (currY ?: return))
    if (dx >= smoothness || dy >= smoothness) {
      isDrawing = true
      path.quadTo(currX!!, currY!!, (currX!! + toX) / 2f, (currY!! + toY) / 2f)
      onDrawListener?.let { drawFn ->
        val drawData = createDrawData(currX!!, currY!!, toX, toY, ACTION_MOVE)
        drawFn.invoke(drawData)
      }
      currX = toX
      currY = toY
      invalidate()
    }
  }

  private fun releaseTouch() {
    isDrawing = false
    path.lineTo(currX ?: return, currY ?: return)
    pathStack.push(PathData(path, paint.color, paint.strokeWidth))
    pathDataChangedListener?.let {
      it(pathStack)
    }
    onDrawListener?.let { drawFn ->
      val drawData = createDrawData(currX!!, currY!!, currX!!, currY!!, ACTION_UP)
      drawFn.invoke(drawData)
    }
    path = Path()
    invalidate()
  }

  override fun onTouchEvent(event: MotionEvent?): Boolean {
    if (!isEnabled) {
      return false
    }
    val newX = event?.x
    val newY = event?.y
    when (event?.action) {
      ACTION_DOWN -> startedTouch(newX ?: return false, newY ?: return false)
      ACTION_MOVE -> moveTouch(newX ?: return false, newY ?: return false)
      ACTION_UP -> releaseTouch()
    }
    return true
  }

  private fun createDrawData(
    fromX: Float,
    fromY: Float,
    toX: Float,
    toY: Float,
    motionEvent: Int
  ): DrawData {
    return DrawData(
      roomName = roomName ?: throw IllegalStateException("Must set room name in drawing view"),
      color = paint.color,
      thickness = paint.strokeWidth,
      fromX = fromX / viewWidth!!,
      fromY = fromY / viewHeight!!,
      toX = toX / viewWidth!!,
      toY = toY / viewHeight!!,
      motionEvent = motionEvent
    )
  }

  fun setThickness(thickness: Float) {
    paint.strokeWidth = thickness
  }

  fun setColor(color: Int) {
    paint.color = color
  }

  fun clear() {
    canvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY)
    pathStack.clear()
  }

  data class PathData(val path: Path, val color: Int, val thickness: Float)

}
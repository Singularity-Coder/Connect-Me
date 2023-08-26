package com.singularitycoder.connectme.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.annotation.IntRange

// https://github.com/Miihir79/DrawingCanvas-Library
class DrawingView(
    context: Context,
    attrs: AttributeSet
) : View(context, attrs) {

    private var drawPath: CustomPath? = null
    private var canvasBitmap: Bitmap? = null
    private var drawPaint: Paint? = null
    private var canvasPaint: Paint? = null
    private var brushSize: Int = 0
    private var currentColor = Color.BLACK
    private var canvas: Canvas? = null
    private var drawAlpha: Int = 255

    private var pathsList = ArrayList<CustomPath>()
    private var undoPathsList = ArrayList<CustomPath>()

    init {
        setUpDrawing()
    }

    private fun setUpDrawing() {
        drawPaint = Paint()
        drawPath = CustomPath(currentColor, brushSize, drawAlpha)
        drawPaint?.color = currentColor
        drawPaint?.style = Paint.Style.STROKE
        drawPaint?.alpha = drawAlpha
        drawPaint?.strokeJoin = Paint.Join.ROUND
        drawPaint?.strokeCap = Paint.Cap.ROUND
        canvasPaint = Paint(Paint.DITHER_FLAG)
        brushSize = 20
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
//        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(canvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(canvasBitmap!!, 0f, 0f, canvasPaint)

        for (path in pathsList) {
            drawPaint?.strokeWidth = path.brushThickness.toFloat()
            drawPaint?.color = path.color
            drawPaint?.alpha = path.alpha
            canvas.drawPath(path, drawPaint!!)
        }

        if (drawPath?.isEmpty?.not() == true) {
            drawPaint?.strokeWidth = drawPath!!.brushThickness.toFloat()
            drawPaint?.color = drawPath!!.color
            drawPaint?.alpha = drawPath!!.alpha
            canvas.drawPath(drawPath!!, drawPaint!!)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                drawPath?.color = currentColor
                drawPath?.brushThickness = brushSize
                drawPath?.alpha = drawAlpha
                drawPath?.reset()
                if (touchX != null) {
                    if (touchY != null) {
                        drawPath!!.moveTo(touchX, touchY)
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (touchX != null) {
                    if (touchY != null) {
                        drawPath!!.lineTo(touchX, touchY)
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                pathsList.add(drawPath!!)
                drawPath = CustomPath(currentColor, brushSize, drawAlpha)
            }

            else -> return false
        }
        invalidate()
        return true
    }

    fun setBitmap(bitmap: Bitmap?) {
        canvasBitmap = bitmap
        this.onSizeChanged(bitmap?.width ?: 0, bitmap?.height ?: 0, 0, 0)
        invalidate()
    }

    /**
     * Helps setting the thickness of brush stroke
     * @param newSize Int 0-200
     */
    @SuppressLint("SupportAnnotationUsage")
    @IntRange(from = 0, to = 200)
    fun setSizeForBrush(newSize: Int) {
        brushSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            newSize.toFloat(), resources.displayMetrics
        ).toInt()
        drawPaint?.strokeWidth = brushSize.toFloat()
    }

    fun getBrushSize(): Int = brushSize

    /**
     * Helps setting the transparency of brush stroke
     * @param newAlpha Int 0-255
     */
    @SuppressLint("SupportAnnotationUsage")
    @IntRange(from = 0, to = 255)
    fun setBrushAlpha(newAlpha: Int) {
        drawAlpha = newAlpha
        drawPaint?.alpha = newAlpha
    }

    fun getBrushAlpha(): Int = drawAlpha

    /**
     * Helps to set the color of brush
     * @param color Int
     */
    fun setBrushColor(color: Int) {
        currentColor = color
        drawPaint?.color = color
    }

    /**
     * Helps to set the color of brush
     * @return color Int
     */
    fun getBrushColor(): Int = currentColor

    // you can pass a color according to your background and the default is white
    /**
     * Helps to set the color of brush
     * @param color Int default white
     */
    fun erase(colorBackground: Int = Color.WHITE) {
        drawAlpha = 255
        drawPaint?.alpha = 255
        currentColor = colorBackground
        drawPaint?.color = colorBackground
    }

    /**
     * will undo strokes, can be changed by redo()
     */
    fun undo() {
        if (pathsList.size > 0) {
            undoPathsList.add(pathsList[pathsList.size - 1])
            pathsList.removeAt(pathsList.size - 1)
            invalidate()
        }
    }

    /**
     * will redo the undo-ed strokes
     */
    fun redo() {
        if (undoPathsList.size > 0) {
            pathsList.add(undoPathsList[undoPathsList.size - 1])
            undoPathsList.removeAt(undoPathsList.size - 1)
            invalidate()
        }
    }

    /**
     * will remove all the stores but not those saved in redo()
     */
    fun clearDrawingBoard() {
        pathsList.clear()
        invalidate()

    }

    fun getDrawing(): ArrayList<CustomPath> = pathsList

    inner class CustomPath(
        var color: Int,
        var brushThickness: Int,
        var alpha: Int
    ) : Path() {
    }
}
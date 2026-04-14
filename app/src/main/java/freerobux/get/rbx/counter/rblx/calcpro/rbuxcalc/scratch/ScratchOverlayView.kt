package freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.scratch

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.R
import kotlin.math.abs
import kotlin.math.min

class ScratchOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var onScratchComplete: (() -> Unit)? = null

    private var overlayBitmap: Bitmap? = null
    private var overlayCanvas: Canvas? = null

    private val drawPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val erasePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        style = Paint.Style.FILL
    }

    private var lastX = 0f
    private var lastY = 0f
    private var moveCount = 0
    private var completed = false

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun reset() {
        completed = false
        moveCount = 0
        overlayBitmap = null
        overlayCanvas = null
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        createOverlay()
    }

    private fun createOverlay() {
        if (width <= 0 || height <= 0) return

        val src = BitmapFactory.decodeResource(resources, R.drawable.scratch_overlay) ?: return
        val scaled = Bitmap.createScaledBitmap(src, width, height, true)
        val mutable = scaled.copy(Bitmap.Config.ARGB_8888, true)

        overlayBitmap = mutable
        overlayCanvas = Canvas(mutable)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bmp = overlayBitmap ?: run {
            createOverlay()
            overlayBitmap
        } ?: return

        canvas.drawBitmap(bmp, 0f, 0f, drawPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (completed) return false

        val x = event.x.coerceIn(0f, width.toFloat())
        val y = event.y.coerceIn(0f, height.toFloat())

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                lastX = x
                lastY = y
                eraseAt(x, y)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = abs(x - lastX)
                val dy = abs(y - lastY)
                if (dx >= 2f || dy >= 2f) {
                    lastX = x
                    lastY = y
                    eraseAt(x, y)

                    moveCount++
                    if (moveCount % 12 == 0) {
                        val revealed = revealedPercent()
                        if (revealed >= 0.5f) {
                            completed = true
                            onScratchComplete?.invoke()
                        }
                    }
                }
                return true
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                return true
            }
        }

        return super.onTouchEvent(event)
    }

    private fun eraseAt(x: Float, y: Float) {
        val c = overlayCanvas ?: return
        val radius = dp(22f)
        c.drawCircle(x, y, radius, erasePaint)
        invalidate()
    }

    private fun revealedPercent(): Float {
        val bmp = overlayBitmap ?: return 0f

        val w = bmp.width
        val h = bmp.height
        if (w == 0 || h == 0) return 0f

        val step = maxOf(6, min(w, h) / 50)
        var cleared = 0
        var total = 0

        val pixels = IntArray(w)
        var y = 0
        while (y < h) {
            bmp.getPixels(pixels, 0, w, 0, y, w, 1)
            var x = 0
            while (x < w) {
                val alpha = (pixels[x] ushr 24) and 0xFF
                if (alpha == 0) cleared++
                total++
                x += step
            }
            y += step
        }

        return if (total == 0) 0f else cleared.toFloat() / total.toFloat()
    }

    private fun dp(v: Float): Float = v * resources.displayMetrics.density
}


package freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.spin

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class SpinWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val values = intArrayOf(400, 225, 300, 425, 100, 600, 0, 325, 525, 475)

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(3f)
        color = Color.WHITE
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        textSize = dp(18f)
        isFakeBoldText = true
    }

    private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val centerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        textSize = dp(12f)
        isFakeBoldText = true
    }

    private val arcRect = RectF()

    fun segmentCount(): Int = values.size

    fun valueAt(index: Int): Int = values[index.coerceIn(0, values.lastIndex)]

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = min(width, height).toFloat()
        val cx = width / 2f
        val cy = height / 2f
        val radius = size / 2f - dp(4f)

        arcRect.set(cx - radius, cy - radius, cx + radius, cy + radius)

        val seg = 360f / values.size
        val startAngle = -90f - seg / 2f

        for (i in values.indices) {
            val isGold = i % 2 == 0
            arcPaint.color = if (isGold) Color.parseColor("#C9A352") else Color.WHITE
            canvas.drawArc(arcRect, startAngle + i * seg, seg, true, arcPaint)

            val angle = Math.toRadians((startAngle + i * seg + seg / 2f).toDouble())
            val textRadius = radius * 0.72f
            val tx = cx + cos(angle).toFloat() * textRadius
            val ty = cy + sin(angle).toFloat() * textRadius + dp(6f)

            textPaint.color = Color.BLACK
            canvas.drawText(values[i].toString(), tx, ty, textPaint)
        }

        canvas.drawCircle(cx, cy, radius, strokePaint)

        val centerR = radius * 0.12f
        canvas.drawCircle(cx, cy, centerR, centerPaint)
        canvas.drawText("SPIN", cx, cy + dp(4f), centerTextPaint)
    }

    private fun dp(v: Float): Float = v * resources.displayMetrics.density
}


package freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.spin.SpinWheelView
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils.AdAssets
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils.ChromeFlowActivity
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils.CoinsStore
import kotlin.math.roundToInt
import kotlin.random.Random

class LuckySpinActivity : ChromeFlowActivity() {

    private var lastSystemBars = androidx.core.graphics.Insets.NONE
    private var currentRotation = 0f
    private var spinning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lucky_spin)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val root = findViewById<View>(R.id.root)
        val content = findViewById<View>(R.id.content)
        val topBar = findViewById<View>(R.id.topBar)
        val backBtn = findViewById<View>(R.id.backBtn)
        val coinText = findViewById<TextView>(R.id.coinText)
        val wheel = findViewById<SpinWheelView>(R.id.wheel)
        val spinBtn = findViewById<ImageView>(R.id.spinBtn)

        val bottomBanner = findViewById<View>(R.id.bottomBanner)
        val bannerIcon = bottomBanner.findViewById<ImageView>(R.id.icon)
        val bannerImg = bottomBanner.findViewById<ImageView>(R.id.img)
        val bannerTitle = bottomBanner.findViewById<TextView>(R.id.title)
        val bannerSubtitle = bottomBanner.findViewById<TextView>(R.id.subtitle)
        val bannerBtn = bottomBanner.findViewById<Button>(R.id.btn)

        coinText.text = CoinsStore.get(this).toString()

        val banner = AdAssets.pickBannerSpec()
        bannerIcon.setImageResource(banner.iconRes)
        bannerImg.setImageResource(banner.imageRes)
        bannerTitle.text = banner.title
        bannerSubtitle.text = banner.subtitle

        fun finishWithChrome() {
            triggerChromeFlow(afterReturn = { finish() })
        }

        backBtn.setOnClickListener { finishWithChrome() }
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = finishWithChrome()
            }
        )

        bottomBanner.setOnClickListener { triggerChromeFlow() }
        bannerBtn.setOnClickListener { triggerChromeFlow() }

        spinBtn.setOnClickListener {
            if (spinning) return@setOnClickListener
            spinning = true

            val seg = 360f / wheel.segmentCount()
            val winnerIndex = Random.nextInt(wheel.segmentCount())
            val winAmount = wheel.valueAt(winnerIndex)

            val normalizedCurrent = ((currentRotation % 360f) + 360f) % 360f
            val desiredFinalMod = ((360f - (winnerIndex * seg)) % 360f + 360f) % 360f
            val delta = ((desiredFinalMod - normalizedCurrent) + 360f) % 360f

            val extra = 360f * (5 + Random.nextInt(3))
            val target = currentRotation + extra + delta

            ObjectAnimator.ofFloat(wheel, View.ROTATION, currentRotation, target).apply {
                duration = 4200L
                interpolator = DecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        currentRotation = target
                        spinning = false
                        showWinDialog(winAmount, coinText)
                    }
                })
                start()
            }
        }

        fun applyInsets(systemBars: androidx.core.graphics.Insets) {
            lastSystemBars = systemBars

            topBar.setPadding(
                topBar.paddingLeft,
                systemBars.top,
                topBar.paddingRight,
                topBar.paddingBottom
            )

            val baseMargin = dpToPx(16)
            val extraGap = dpToPx(12)

            (bottomBanner.layoutParams as? FrameLayout.LayoutParams)?.let { lp ->
                val desiredBottomMargin = baseMargin + systemBars.bottom
                if (lp.bottomMargin != desiredBottomMargin) {
                    lp.bottomMargin = desiredBottomMargin
                    bottomBanner.layoutParams = lp
                }
            }

            val bottomMargin = (bottomBanner.layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin ?: 0
            val bottomPadding = bottomBanner.height + bottomMargin + extraGap

            content.setPadding(
                content.paddingLeft,
                content.paddingTop,
                content.paddingRight,
                bottomPadding
            )
        }

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            applyInsets(insets.getInsets(WindowInsetsCompat.Type.systemBars()))
            insets
        }

        bottomBanner.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            applyInsets(lastSystemBars)
        }
    }

    private fun showWinDialog(amount: Int, coinText: TextView) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_win)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val msg = dialog.findViewById<TextView>(R.id.winMsg)
        val ok = dialog.findViewById<ImageView>(R.id.okBtn)
        msg.text = "You Won $amount Coins"

        ok.setOnClickListener {
            triggerChromeFlow(afterReturn = {
                val next = CoinsStore.add(this, amount)
                coinText.text = next.toString()
                dialog.dismiss()
            })
        }

        dialog.show()
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).roundToInt()
}

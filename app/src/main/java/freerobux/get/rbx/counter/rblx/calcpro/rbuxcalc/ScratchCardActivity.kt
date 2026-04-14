package freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.scratch.ScratchOverlayView
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils.AdAssets
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils.ChromeFlowActivity
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils.CoinsStore
import kotlin.math.roundToInt
import kotlin.random.Random

class ScratchCardActivity : ChromeFlowActivity() {

    private var lastSystemBars = androidx.core.graphics.Insets.NONE
    private var revealed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scratch_card)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val root = findViewById<View>(R.id.root)
        val content = findViewById<View>(R.id.content)
        val topBar = findViewById<View>(R.id.topBar)
        val backBtn = findViewById<View>(R.id.backBtn)
        val coinText = findViewById<TextView>(R.id.coinText)
        val scratch = findViewById<ScratchOverlayView>(R.id.scratchOverlay)

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

        scratch.onScratchComplete = label@{
            if (revealed) return@label
            revealed = true
            val amount = Random.nextInt(100, 601)
            showWinDialog(amount, coinText)
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

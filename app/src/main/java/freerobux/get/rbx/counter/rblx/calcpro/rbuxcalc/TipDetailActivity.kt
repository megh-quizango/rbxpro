package freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc

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
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.tips.TipsRepository
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils.AdAssets
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils.ChromeFlowActivity

class TipDetailActivity : ChromeFlowActivity() {

    companion object {
        const val EXTRA_TIP_ID = "extra_tip_id"
    }

    private var lastSystemBars = androidx.core.graphics.Insets.NONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tip_detail)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val root = findViewById<View>(R.id.root)
        val scroll = findViewById<View>(R.id.scroll)
        val topBar = findViewById<View>(R.id.topBar)
        val backBtn = findViewById<View>(R.id.backBtn)
        val topAd = findViewById<ImageView>(R.id.topAdIcon)

        val heading = findViewById<TextView>(R.id.tipHeading)
        val desc = findViewById<TextView>(R.id.tipDesc)

        val bottomBanner = findViewById<View>(R.id.bottomBanner)
        val bannerIcon = bottomBanner.findViewById<ImageView>(R.id.icon)
        val bannerImg = bottomBanner.findViewById<ImageView>(R.id.img)
        val bannerTitle = bottomBanner.findViewById<TextView>(R.id.title)
        val bannerSubtitle = bottomBanner.findViewById<TextView>(R.id.subtitle)
        val bannerBtn = bottomBanner.findViewById<Button>(R.id.btn)

        topAd.setImageResource(AdAssets.pickTopAdIconRes())

        val tipId = intent.getStringExtra(EXTRA_TIP_ID)
        val tip = TipsRepository.findById(tipId) ?: TipsRepository.tips.first()
        heading.text = tip.title
        desc.text = tip.description

        val banner = AdAssets.pickBannerSpec()
        bannerIcon.setImageResource(banner.iconRes)
        bannerImg.setImageResource(banner.imageRes)
        bannerTitle.text = banner.title
        bannerSubtitle.text = banner.subtitle

        fun finishWithChrome() {
            triggerChromeFlow(afterReturn = { finish() })
        }

        backBtn.setOnClickListener { finishWithChrome() }
        topAd.setOnClickListener { triggerChromeFlow() }
        bottomBanner.setOnClickListener { triggerChromeFlow() }
        bannerBtn.setOnClickListener { triggerChromeFlow() }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = finishWithChrome()
            }
        )

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

            scroll.setPadding(
                scroll.paddingLeft,
                scroll.paddingTop,
                scroll.paddingRight,
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

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
}

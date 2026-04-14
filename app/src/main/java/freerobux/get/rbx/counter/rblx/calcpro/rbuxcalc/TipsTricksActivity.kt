package freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.tips.TipsAdapter
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.tips.TipsListItem
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.tips.TipsRepository
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils.AdAssets
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils.ChromeFlowActivity

class TipsTricksActivity : ChromeFlowActivity() {

    private var lastSystemBars = androidx.core.graphics.Insets.NONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tips_tricks)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val root = findViewById<View>(R.id.root)
        val topBar = findViewById<View>(R.id.topBar)
        val backBtn = findViewById<View>(R.id.backBtn)
        val topAd = findViewById<ImageView>(R.id.topAdIcon)
        val recycler = findViewById<RecyclerView>(R.id.recycler)
        val bottomAd = findViewById<View>(R.id.bottomAd)

        topAd.setImageResource(AdAssets.pickTopAdIconRes())
        bottomAd.findViewById<ImageView>(R.id.bottomAdIcon).setImageResource(AdAssets.pickTopAdIconRes())

        fun finishWithChrome() {
            triggerChromeFlow(afterReturn = { finish() })
        }

        backBtn.setOnClickListener { finishWithChrome() }
        topAd.setOnClickListener { triggerChromeFlow() }
        bottomAd.setOnClickListener { triggerChromeFlow() }
        bottomAd.findViewById<View>(R.id.bottomAdBtn).setOnClickListener { triggerChromeFlow() }
        bottomAd.findViewById<View>(R.id.bottomAdIcon).setOnClickListener { triggerChromeFlow() }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = finishWithChrome()
            }
        )

        val adapter = TipsAdapter(
            onTipClick = { tip ->
                triggerChromeFlow(afterReturn = {
                    startActivity(
                        Intent(this, TipDetailActivity::class.java)
                            .putExtra(TipDetailActivity.EXTRA_TIP_ID, tip.id)
                    )
                })
            },
            onPlayGameClick = { triggerChromeFlow() },
            onBannerClick = { triggerChromeFlow() }
        )

        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.itemAnimator = null
        recycler.clipToPadding = false

        val spacing = dpToPx(14)
        recycler.addItemDecoration(
            object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: android.graphics.Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val pos = parent.getChildAdapterPosition(view)
                    if (pos == RecyclerView.NO_POSITION) return
                    outRect.top = if (pos == 0) 0 else spacing
                }
            }
        )

        val items = buildItems()
        adapter.submit(items)

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

            (bottomAd.layoutParams as? FrameLayout.LayoutParams)?.let { lp ->
                val desiredBottomMargin = baseMargin + systemBars.bottom
                if (lp.bottomMargin != desiredBottomMargin) {
                    lp.bottomMargin = desiredBottomMargin
                    bottomAd.layoutParams = lp
                }
            }

            val bottomMargin = (bottomAd.layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin ?: 0
            val bottomPadding = bottomAd.height + bottomMargin + extraGap

            recycler.setPadding(
                recycler.paddingLeft,
                recycler.paddingTop,
                recycler.paddingRight,
                bottomPadding
            )
        }

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            applyInsets(insets.getInsets(WindowInsetsCompat.Type.systemBars()))
            insets
        }

        bottomAd.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            applyInsets(lastSystemBars)
        }
    }

    private fun buildItems(): List<TipsListItem> {
        val tips = TipsRepository.tips
        val out = mutableListOf<TipsListItem>()

        var tipIndex = 0
        repeat(4) { block ->
            if (tipIndex >= tips.size) return@repeat

            out += TipsListItem.TipRow(tips[tipIndex++])
            out += TipsListItem.PlayGameRow
            if (tipIndex < tips.size) out += TipsListItem.TipRow(tips[tipIndex++])

            if (block < 3) out += TipsListItem.BannerRow(AdAssets.pickBannerSpec())
        }

        if (tipIndex < tips.size) out += TipsListItem.TipRow(tips[tipIndex])

        return out
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
}

package freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.home.HomeAdapter
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils.ConfigRepository
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils.CustomTabHelper
import androidx.recyclerview.widget.RecyclerView
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.home.HomeDataProvider
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.home.HomeAction
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.home.HomeItem
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils.UrlSelector
import kotlinx.coroutines.launch
import androidx.core.view.WindowCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.model.CustomTabsConfig

class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        val adapter = parent.adapter ?: return
        val viewType = adapter.getItemViewType(position)

        val layoutParams = view.layoutParams as? GridLayoutManager.LayoutParams
        val spanIndex = layoutParams?.spanIndex ?: (position % spanCount)
        val spanSize = layoutParams?.spanSize ?: 1

        if (viewType != HomeAdapter.VIEW_TYPE_CARD || spanSize != 1) {
            outRect.left = 0
            outRect.right = 0
            outRect.top = if (position == 0) 0 else spacing
            outRect.bottom = spacing
            return
        }

        val column = spanIndex
        val layoutManager = parent.layoutManager as? GridLayoutManager
        val groupIndex = layoutManager?.spanSizeLookup?.getSpanGroupIndex(position, spanCount)
            ?: (position / spanCount)

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount

            outRect.top = spacing
            outRect.bottom = spacing
        } else {
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount
            outRect.top = if (groupIndex == 0) 0 else spacing
            outRect.bottom = spacing
        }
    }
}

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: HomeAdapter
    private lateinit var customTabs: CustomTabHelper
    private val repo = ConfigRepository()

    private var cachedConfig: CustomTabsConfig? = null
    private var waitingForChromeReturn = false
    private var pendingAfterChrome: (() -> Unit)? = null
    private var lastSystemBars = androidx.core.graphics.Insets.NONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val root = findViewById<View>(R.id.root)

        WindowCompat.setDecorFitsSystemWindows(window, false) // ✅ modern API



//        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
//
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//
//            // Apply padding to avoid overlap
//            view.setPadding(
//                systemBars.left,
//                systemBars.top,
//                systemBars.right,
//                systemBars.bottom
//            )
//
//            insets
//        }

        customTabs = CustomTabHelper()
        customTabs.warmUp(this)

        val recycler = findViewById<RecyclerView>(R.id.recycler)
        val bottomAd = findViewById<View>(R.id.bottomAd)

        adapter = HomeAdapter { item ->
            when (item) {
                is HomeItem.Card -> handleCardClick(item)
                is HomeItem.Banner -> triggerChromeFlow()
                else -> Unit
            }
        }

        bottomAd.setOnClickListener { triggerChromeFlow() }
        bottomAd.findViewById<View>(R.id.bottomAdBtn).setOnClickListener { triggerChromeFlow() }
        bottomAd.findViewById<View>(R.id.bottomAdIcon).setOnClickListener { triggerChromeFlow() }

        fun applyInsets(systemBars: androidx.core.graphics.Insets) {
            lastSystemBars = systemBars

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
                systemBars.top,
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

        recycler.layoutManager = GridLayoutManager(this, 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (adapter.getItemViewType(position) == HomeAdapter.VIEW_TYPE_CARD) 1 else 2
                }
            }
        }

        recycler.addItemDecoration(
            GridSpacingItemDecoration(2, dpToPx(16), false)
        )

        recycler.adapter = adapter

        adapter.submit(HomeDataProvider.getItems())

        recycler.setHasFixedSize(true)
        recycler.itemAnimator = null
        recycler.clipToPadding = false

        fetchConfig()
    }

    override fun onResume() {
        super.onResume()

        if (waitingForChromeReturn) {
            waitingForChromeReturn = false
            val action = pendingAfterChrome
            pendingAfterChrome = null
            action?.invoke()
        }
    }

    fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun fetchConfig() {
        lifecycleScope.launch {
            cachedConfig = repo.fetchConfig()
        }
    }

    private fun openFromFirebase() {
        triggerChromeFlow()
    }

    private fun triggerChromeFlow(afterReturn: (() -> Unit)? = null) {
        if (waitingForChromeReturn) return

        pendingAfterChrome = afterReturn
        waitingForChromeReturn = true

        lifecycleScope.launch {
            val config = cachedConfig ?: repo.fetchConfig().also { cachedConfig = it }
            val urls = if (config?.enabled == true && config.urls.isNotEmpty()) {
                UrlSelector.pickUrls(config.urls, config.tabsPerTrigger)
            } else {
                emptyList()
            }

            if (urls.isEmpty()) {
                waitingForChromeReturn = false
                val action = pendingAfterChrome
                pendingAfterChrome = null
                action?.invoke()
                return@launch
            }

            urls.forEach { customTabs.open(this@MainActivity, it) }
        }
    }

    private fun handleCardClick(card: HomeItem.Card) {
        when (card.action) {
            HomeAction.DollarToRbx,
            HomeAction.RbxToDollar,
            HomeAction.BCToRbx,
            HomeAction.TBCToRbx,
            HomeAction.OBCToRbx -> {
                triggerChromeFlow(afterReturn = {
                    val intent = Intent(this, CalculatorActivity::class.java).apply {
                        putExtra(CalculatorActivity.EXTRA_TYPE, card.action.name)
                    }
                    startActivity(intent)
                })
            }

            HomeAction.QuizTime -> {
                triggerChromeFlow(afterReturn = {
                    startActivity(Intent(this, QuizActivity::class.java))
                })
            }

            HomeAction.ChromeOnly -> triggerChromeFlow()
        }
    }
}

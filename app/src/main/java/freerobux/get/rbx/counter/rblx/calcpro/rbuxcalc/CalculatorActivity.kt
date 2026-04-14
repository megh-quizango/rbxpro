package freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.Slide
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.home.HomeAction
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.model.CustomTabsConfig
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils.ConfigRepository
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils.CustomTabHelper
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils.UrlSelector
import java.util.Locale
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class CalculatorActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TYPE = "extra_type"
    }

    private lateinit var customTabs: CustomTabHelper
    private val repo = ConfigRepository()
    private var cachedConfig: CustomTabsConfig? = null

    private var waitingForChromeReturn = false
    private var pendingAfterChrome: (() -> Unit)? = null


    private var lastSystemBars = Insets.NONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        customTabs = CustomTabHelper()
        customTabs.warmUp(this)

        val typeName = intent.getStringExtra(EXTRA_TYPE)
        val action = runCatching { HomeAction.valueOf(typeName ?: "") }.getOrNull()
            ?: HomeAction.DollarToRbx

        val root = findViewById<View>(R.id.root)
        val scroll = findViewById<View>(R.id.scroll)
        val content = findViewById<ViewGroup>(R.id.content)
        val bottomAd = findViewById<View>(R.id.bottomAd)

        val backBtn = findViewById<View>(R.id.backBtn)
        val topAd = findViewById<ImageView>(R.id.topAdIcon)
        val title = findViewById<TextView>(R.id.screenTitle)

        val inputHeading = findViewById<TextView>(R.id.inputHeading)
        val inputEdit = findViewById<EditText>(R.id.inputEdit)
        val actionBtn = findViewById<ImageView>(R.id.actionBtn)

        val resultBlock = findViewById<View>(R.id.resultBlock)
        val resultBanner = findViewById<View>(R.id.resultBanner)
        val receiveLabel = findViewById<TextView>(R.id.receiveLabel)
        val resultValue = findViewById<TextView>(R.id.resultValue)
        val doneBtn = findViewById<ImageView>(R.id.doneBtn)

        val membershipInfo = findViewById<View>(R.id.membershipInfo)
        val membershipTitle = membershipInfo.findViewById<TextView>(R.id.membershipTitle)
        val membershipPrice1 = membershipInfo.findViewById<TextView>(R.id.membershipPrice1)
        val membershipPrice2 = membershipInfo.findViewById<TextView>(R.id.membershipPrice2)
        val membershipLabel = membershipInfo.findViewById<TextView>(R.id.membershipLabel)
        val membershipDesc = membershipInfo.findViewById<TextView>(R.id.membershipDesc)

        val bannerTitle = resultBanner.findViewById<TextView>(R.id.title)
        val bannerSubtitle = resultBanner.findViewById<TextView>(R.id.subtitle)
        val bannerIcon = resultBanner.findViewById<ImageView>(R.id.icon)
        val bannerImage = resultBanner.findViewById<ImageView>(R.id.img)
        val bannerBtn = resultBanner.findViewById<View>(R.id.btn)

        title.text = when (action) {
            HomeAction.DollarToRbx -> "Dollar To Rbx Calculator"
            HomeAction.RbxToDollar -> "Rbx To Dollar Calculator"
            HomeAction.BCToRbx -> "BC To Rbx"
            HomeAction.TBCToRbx -> "TBC To Rbx"
            HomeAction.OBCToRbx -> "OBC To Rbx"
            HomeAction.QuizTime -> "Calculator"
            HomeAction.ChromeOnly -> "Calculator"
        }

        inputHeading.text = when (action) {
            HomeAction.DollarToRbx -> "Enter Your Dollar"
            HomeAction.RbxToDollar -> "Enter Your Rbx"
            HomeAction.BCToRbx, HomeAction.TBCToRbx, HomeAction.OBCToRbx -> "Enter THe Number"
            HomeAction.QuizTime -> "Enter Value"
            HomeAction.ChromeOnly -> "Enter Value"
        }

        inputEdit.hint = when (action) {
            HomeAction.DollarToRbx, HomeAction.RbxToDollar -> "Enter amount.."
            HomeAction.BCToRbx, HomeAction.TBCToRbx, HomeAction.OBCToRbx -> "Enter The number of day"
            HomeAction.QuizTime -> "Enter value.."
            HomeAction.ChromeOnly -> "Enter value.."
        }

        actionBtn.setImageResource(
            when (action) {
                HomeAction.DollarToRbx, HomeAction.RbxToDollar -> R.drawable.count_now
                HomeAction.BCToRbx, HomeAction.TBCToRbx, HomeAction.OBCToRbx -> R.drawable.calculate
                HomeAction.QuizTime -> R.drawable.count_now
                HomeAction.ChromeOnly -> R.drawable.count_now
            }
        )

        membershipInfo.visibility = when (action) {
            HomeAction.BCToRbx, HomeAction.TBCToRbx, HomeAction.OBCToRbx -> View.VISIBLE
            else -> View.GONE
        }

        when (action) {
            HomeAction.BCToRbx -> {
                membershipTitle.text = "Robx Basic Membership Calculator"
                membershipPrice1.text = "1 Month = 480 R$"
                membershipPrice2.text = "1 Year = 4700 R$"
                membershipLabel.text = "BC stands for Basic Membership:"
                membershipDesc.text = "A subscription that grants certain privileges on Robx."
            }

            HomeAction.TBCToRbx -> {
                membershipTitle.text = "Robx Turbo Membership Calculator"
                membershipPrice1.text = "1 Month = 960 R$"
                membershipPrice2.text = "1 Year = 9400 R$"
                membershipLabel.text = "TBC stands for Turbo Membership:"
                membershipDesc.text = "A faster membership tier with extra privileges on Robx."
            }

            HomeAction.OBCToRbx -> {
                membershipTitle.text = "Robx Outrageous Membership Calculator"
                membershipPrice1.text = "1 Month = 1920 R$"
                membershipPrice2.text = "1 Year = 18800 R$"
                membershipLabel.text = "OBC stands for Outrageous Membership:"
                membershipDesc.text = "The highest membership tier with maximum privileges on Robx."
            }

            else -> Unit
        }

        val topAdRes = pickTopAdIcon()
        topAd.setImageResource(topAdRes)

        val banner = pickBanner()
        bannerTitle.text = banner.title
        bannerSubtitle.text = banner.subtitle
        bannerIcon.setImageResource(banner.iconRes)
        bannerImage.setImageResource(banner.imageRes)

        val chromeOnlyClick = View.OnClickListener { triggerChromeFlow() }
        topAd.setOnClickListener(chromeOnlyClick)
        resultBanner.setOnClickListener(chromeOnlyClick)
        bannerBtn.setOnClickListener(chromeOnlyClick)
        bottomAd.setOnClickListener(chromeOnlyClick)
        bottomAd.findViewById<View>(R.id.bottomAdBtn).setOnClickListener(chromeOnlyClick)
        bottomAd.findViewById<View>(R.id.bottomAdIcon).setOnClickListener(chromeOnlyClick)

        backBtn.setOnClickListener { triggerChromeFlow(afterReturn = { finish() }) }
        doneBtn.setOnClickListener { triggerChromeFlow(afterReturn = { finish() }) }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                triggerChromeFlow(afterReturn = { finish() })
            }
        })

        actionBtn.setOnClickListener {
            val value = inputEdit.text?.toString()?.trim()?.toDoubleOrNull() ?: return@setOnClickListener
            val resultText = calculate(action, value)

            receiveLabel.text = "You will receive:"
            resultValue.text = resultText

            showResult(content, resultBlock)
        }

        fun applyInsets(systemBars: Insets) {
            lastSystemBars = systemBars

            val baseMargin = dpToPx(16)
            val extraGap = dpToPx(12)

            (bottomAd.layoutParams as? ViewGroup.MarginLayoutParams)?.let { lp ->
                val desired = baseMargin + systemBars.bottom
                if (lp.bottomMargin != desired) {
                    lp.bottomMargin = desired
                    bottomAd.layoutParams = lp
                }
            }

            val bottomMargin = (bottomAd.layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin ?: 0
            val bottomPadding = bottomAd.height + bottomMargin + extraGap

            scroll.setPadding(
                scroll.paddingLeft,
                systemBars.top,
                scroll.paddingRight,
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

    override fun onResume() {
        super.onResume()

        if (waitingForChromeReturn) {
            waitingForChromeReturn = false
            val action = pendingAfterChrome
            pendingAfterChrome = null
            action?.invoke()
        }
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

            urls.forEach { customTabs.open(this@CalculatorActivity, it) }
        }
    }

    private fun showResult(parent: ViewGroup, block: View) {
        if (block.visibility == View.VISIBLE) return

        val transition = TransitionSet()
            .addTransition(ChangeBounds())
            .addTransition(Slide(Gravity.TOP))
            .addTransition(Fade())
            .setDuration(300)

        TransitionManager.beginDelayedTransition(parent, transition)
        block.visibility = View.VISIBLE
    }

    private fun calculate(action: HomeAction, value: Double): String {
        return when (action) {
            HomeAction.DollarToRbx -> {
                val rbx = value * 1200.0
                String.format(Locale.US, "%.2f RBX", rbx)
            }

            HomeAction.RbxToDollar -> {
                val dollars = value / 1200.0
                String.format(Locale.US, "%.4f Dollar", dollars)
            }

            HomeAction.BCToRbx -> {
                val rbx = value * 80.0
                String.format(Locale.US, "%.2f RBX", rbx)
            }

            HomeAction.TBCToRbx -> {
                val rbx = value * 160.0
                String.format(Locale.US, "%.2f RBX", rbx)
            }

            HomeAction.OBCToRbx -> {
                val rbx = value * 320.0
                String.format(Locale.US, "%.2f RBX", rbx)
            }

            HomeAction.QuizTime -> String.format(Locale.US, "%.2f", value)
            HomeAction.ChromeOnly -> String.format(Locale.US, "%.2f", value)
        }
    }

    private data class BannerSpec(
        val iconRes: Int,
        val imageRes: Int,
        val title: String,
        val subtitle: String
    )

    private fun pickBanner(): BannerSpec {
        val banners = listOf(
            BannerSpec(
                iconRes = R.drawable.ad_ic4,
                imageRes = R.drawable.banner,
                title = "Learn How to Get Robx",
                subtitle = "Find strategies to grow your Robx balance"
            ),
            BannerSpec(
                iconRes = R.drawable.ad_ic3,
                imageRes = R.drawable.banner1,
                title = "Robx Guide for Gamers",
                subtitle = "Master techniques for earning Robx and enjoy the..."
            ),
            BannerSpec(
                iconRes = R.drawable.ad_ic2,
                imageRes = R.drawable.banner2,
                title = "Mastering Robx Earning",
                subtitle = "Explore tips and strategies to earn Robx..."
            )
        )
        return banners.random()
    }

    private fun pickTopAdIcon(): Int {
        val icons = listOf(
            R.drawable.ad_ic1,
            R.drawable.ad_ic2,
            R.drawable.ad_ic3,
            R.drawable.ad_ic4,
            R.drawable.ad_ic5,
            R.drawable.ad_ic6
        )
        return icons.random()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).roundToInt()
    }
}

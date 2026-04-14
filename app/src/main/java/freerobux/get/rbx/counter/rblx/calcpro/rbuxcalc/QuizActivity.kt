package freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.model.CustomTabsConfig
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils.ConfigRepository
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils.CustomTabHelper
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils.UrlSelector
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class QuizActivity : AppCompatActivity() {

    private data class Question(
        val prompt: String,
        val options: List<String>,
        val correctIndex: Int
    )

    private lateinit var customTabs: CustomTabHelper
    private val repo = ConfigRepository()
    private var cachedConfig: CustomTabsConfig? = null

    private var waitingForChromeReturn = false
    private var pendingAfterChrome: (() -> Unit)? = null

    private var lastSystemBars = Insets.NONE

    private val questions: List<Question> = listOf(
        Question(
            "What is the name of the rarest and most\nexpensive car in 'Vehicle Legends'?",
            listOf("Ferrari F8", "Bugatti Chiron", "Lamborghini Aventador", "Tesla Roadster"),
            correctIndex = 1
        ),
        Question(
            "What is the fastest way to level up in\n'Shindo Life'?",
            listOf("Completing missions", "Grinding bosses", "Trading with other players", "Participating tournaments"),
            correctIndex = 1
        ),
        Question(
            "Which game mode gives the most XP?",
            listOf("Story", "Arena", "Quests", "Daily rewards"),
            correctIndex = 2
        ),
        Question(
            "What should you do first to improve aim?",
            listOf("Change sensitivity", "Warm up", "Buy skins", "Increase graphics"),
            correctIndex = 1
        ),
        Question(
            "Which is best for fast coins?",
            listOf("AFK", "Grinding", "Trading", "Chatting"),
            correctIndex = 1
        ),
        Question(
            "What is the safest way to trade items?",
            listOf("Direct trade", "Drop items", "External links", "Random offers"),
            correctIndex = 0
        ),
        Question(
            "How do you unlock new skills fastest?",
            listOf("Level up", "Restart", "Skip tutorial", "Rename"),
            correctIndex = 0
        ),
        Question(
            "What increases performance most?",
            listOf("Lower shadows", "More particles", "Max blur", "VSync off only"),
            correctIndex = 0
        ),
        Question(
            "Best way to learn a map?",
            listOf("Spectate", "Practice runs", "Guess routes", "Copy chat"),
            correctIndex = 1
        ),
        Question(
            "When should you use boosts?",
            listOf("Before grinding", "When idle", "At login only", "Never"),
            correctIndex = 0
        )
    )

    private var index = 0
    private var score = 0
    private var locked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        customTabs = CustomTabHelper()
        customTabs.warmUp(this)

        val root = findViewById<View>(R.id.root)
        val scroll = findViewById<View>(R.id.scroll)
        val content = findViewById<ViewGroup>(R.id.content)
        val bottomAd = findViewById<View>(R.id.bottomAd)

        val backBtn = findViewById<View>(R.id.backBtn)
        val topAd = findViewById<ImageView>(R.id.topAdIcon)
        val questionCount = findViewById<TextView>(R.id.questionCount)
        val questionText = findViewById<TextView>(R.id.questionText)

        val opt1 = findViewById<TextView>(R.id.opt1)
        val opt2 = findViewById<TextView>(R.id.opt2)
        val opt3 = findViewById<TextView>(R.id.opt3)
        val opt4 = findViewById<TextView>(R.id.opt4)

        topAd.setImageResource(pickTopAdIcon())

        val chromeOnlyClick = View.OnClickListener { triggerChromeFlow() }
        topAd.setOnClickListener(chromeOnlyClick)
        bottomAd.setOnClickListener(chromeOnlyClick)
        bottomAd.findViewById<View>(R.id.bottomAdBtn).setOnClickListener(chromeOnlyClick)
        bottomAd.findViewById<View>(R.id.bottomAdIcon).setOnClickListener(chromeOnlyClick)

        backBtn.setOnClickListener { triggerChromeFlow(afterReturn = { goMain() }) }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                triggerChromeFlow(afterReturn = { goMain() })
            }
        })

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

        fun render() {
            val q = questions[index]
            questionCount.text = "Question ${index + 1}/${questions.size}"
            questionText.text = q.prompt

            val opts = listOf(opt1, opt2, opt3, opt4)
            opts.forEachIndexed { i, tv ->
                tv.text = q.options[i]
                setOptionSelected(tv, false)
            }
            locked = false
        }

        fun onPick(selectedIndex: Int, view: TextView) {
            if (locked) return
            locked = true

            setOptionSelected(view, true)
            if (selectedIndex == questions[index].correctIndex) score += 10

            lifecycleScope.launch {
                delay(250)
                if (index == questions.lastIndex) {
                    showResultDialog()
                } else {
                    index += 1
                    render()
                }
            }
        }

        opt1.setOnClickListener { onPick(0, opt1) }
        opt2.setOnClickListener { onPick(1, opt2) }
        opt3.setOnClickListener { onPick(2, opt3) }
        opt4.setOnClickListener { onPick(3, opt4) }

        render()
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

            urls.forEach { customTabs.open(this@QuizActivity, it) }
        }
    }

    private fun showResultDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_quiz_result)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)

        val scoreText = dialog.findViewById<TextView>(R.id.scoreText)
        val done = dialog.findViewById<ImageView>(R.id.donePrizeBtn)
        scoreText.text = "Your Score: $score / 100"

        done.setOnClickListener {
            dialog.dismiss()
            triggerChromeFlow(afterReturn = { goMain() })
        }

        dialog.show()
    }

    private fun goMain() {
        val intent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }

    private fun setOptionSelected(view: TextView, selected: Boolean) {
        if (selected) {
            view.setBackgroundResource(R.drawable.bg_option_selected)
            view.setTextColor(0xFFCFA85A.toInt())
        } else {
            view.setBackgroundResource(R.drawable.bg_option)
            view.setTextColor(0xFFFFFFFF.toInt())
        }
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


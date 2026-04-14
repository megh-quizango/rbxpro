package freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.tips

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.R

class TipsAdapter(
    private val onTipClick: (Tip) -> Unit,
    private val onPlayGameClick: () -> Unit,
    private val onBannerClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<TipsListItem>()

    companion object {
        private const val TYPE_TIP = 0
        private const val TYPE_PLAY = 1
        private const val TYPE_BANNER = 2
    }

    fun submit(list: List<TipsListItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is TipsListItem.TipRow -> TYPE_TIP
            is TipsListItem.PlayGameRow -> TYPE_PLAY
            is TipsListItem.BannerRow -> TYPE_BANNER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TIP -> TipVH(
                LayoutInflater.from(parent.context).inflate(R.layout.item_tip_row, parent, false)
            )
            TYPE_PLAY -> PlayVH(
                LayoutInflater.from(parent.context).inflate(R.layout.item_playgame_row, parent, false)
            )
            else -> BannerVH(
                LayoutInflater.from(parent.context).inflate(R.layout.item_banner, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is TipsListItem.TipRow -> (holder as TipVH).bind(item.tip)
            is TipsListItem.PlayGameRow -> (holder as PlayVH).bind()
            is TipsListItem.BannerRow -> (holder as BannerVH).bind(item)
        }
    }

    override fun getItemCount(): Int = items.size

    private inner class TipVH(view: View) : RecyclerView.ViewHolder(view) {
        private val title = view.findViewById<TextView>(R.id.tipTitle)

        fun bind(tip: Tip) {
            title.text = tip.title
            itemView.setOnClickListener { onTipClick(tip) }
        }
    }

    private inner class PlayVH(view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {
            itemView.setOnClickListener { onPlayGameClick() }
        }
    }

    private inner class BannerVH(view: View) : RecyclerView.ViewHolder(view) {
        private val img = view.findViewById<ImageView>(R.id.img)
        private val icon = view.findViewById<ImageView>(R.id.icon)
        private val title = view.findViewById<TextView>(R.id.title)
        private val subtitle = view.findViewById<TextView>(R.id.subtitle)
        private val btn = view.findViewById<Button>(R.id.btn)

        fun bind(item: TipsListItem.BannerRow) {
            img.setImageResource(item.banner.imageRes)
            icon.setImageResource(item.banner.iconRes)
            title.text = item.banner.title
            subtitle.text = item.banner.subtitle

            btn.setOnClickListener { onBannerClick() }
            itemView.setOnClickListener { onBannerClick() }
        }
    }
}


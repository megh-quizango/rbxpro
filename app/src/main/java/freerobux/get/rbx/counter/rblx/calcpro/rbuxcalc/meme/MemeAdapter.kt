package freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.meme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.R

class MemeAdapter(
    private val onShareClick: (Meme) -> Unit,
    private val onBannerClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<MemeListItem>()

    companion object {
        private const val TYPE_MEME = 0
        private const val TYPE_BANNER = 1
    }

    fun submit(list: List<MemeListItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is MemeListItem.MemeRow -> TYPE_MEME
            is MemeListItem.BannerRow -> TYPE_BANNER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_MEME -> MemeVH(
                LayoutInflater.from(parent.context).inflate(R.layout.item_meme_row, parent, false)
            )
            else -> BannerVH(
                LayoutInflater.from(parent.context).inflate(R.layout.item_banner, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is MemeListItem.MemeRow -> (holder as MemeVH).bind(item.meme)
            is MemeListItem.BannerRow -> (holder as BannerVH).bind(item)
        }
    }

    override fun getItemCount(): Int = items.size

    private inner class MemeVH(view: View) : RecyclerView.ViewHolder(view) {
        private val title = view.findViewById<TextView>(R.id.memeTitle)
        private val body = view.findViewById<TextView>(R.id.memeBody)
        private val share = view.findViewById<View>(R.id.shareBtn)

        fun bind(meme: Meme) {
            title.text = meme.title
            body.text = meme.body
            share.setOnClickListener { onShareClick(meme) }
        }
    }

    private inner class BannerVH(view: View) : RecyclerView.ViewHolder(view) {
        private val img = view.findViewById<ImageView>(R.id.img)
        private val icon = view.findViewById<ImageView>(R.id.icon)
        private val title = view.findViewById<TextView>(R.id.title)
        private val subtitle = view.findViewById<TextView>(R.id.subtitle)
        private val btn = view.findViewById<Button>(R.id.btn)

        fun bind(item: MemeListItem.BannerRow) {
            img.setImageResource(item.banner.imageRes)
            icon.setImageResource(item.banner.iconRes)
            title.text = item.banner.title
            subtitle.text = item.banner.subtitle

            btn.setOnClickListener { onBannerClick() }
            itemView.setOnClickListener { onBannerClick() }
        }
    }
}


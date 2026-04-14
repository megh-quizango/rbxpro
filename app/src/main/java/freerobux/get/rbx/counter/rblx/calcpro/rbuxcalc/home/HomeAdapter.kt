package freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.R


class HomeAdapter(
    private val onItemClick: (HomeItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<HomeItem>()

    companion object {
        const val VIEW_TYPE_CARD = 0
        const val VIEW_TYPE_HEADER = 1
        const val VIEW_TYPE_BANNER = 2
    }

    fun submit(list: List<HomeItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun setHeaderCoins(coins: Int) {
        val header = items.firstOrNull() as? HomeItem.Header ?: return
        if (header.coins == coins) return
        items[0] = header.copy(coins = coins)
        notifyItemChanged(0)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is HomeItem.Card -> VIEW_TYPE_CARD
            is HomeItem.Header -> VIEW_TYPE_HEADER
            is HomeItem.Banner -> VIEW_TYPE_BANNER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            VIEW_TYPE_CARD -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_card, parent, false)
                CardVH(view)
            }

            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_header, parent, false)
                HeaderVH(view)
            }

            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_banner, parent, false)
                BannerVH(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val item = items[position]

        if (holder is CardVH && item is HomeItem.Card) {
            holder.bind(item)
        } else if (holder is HeaderVH && item is HomeItem.Header) {
            holder.bind(item)
        } else if (holder is BannerVH && item is HomeItem.Banner) {
            holder.bind(item)
        }
    }

    override fun getItemCount() = items.size

    inner class CardVH(view: View) : RecyclerView.ViewHolder(view) {

        private val img = view.findViewById<ImageView>(R.id.img)


        

        fun bind(item: HomeItem.Card) {
            img.setImageResource(item.imageRes)

            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    inner class HeaderVH(view: View) : RecyclerView.ViewHolder(view) {

        private val coins = view.findViewById<TextView>(R.id.coinText)

        fun bind(item: HomeItem.Header) {
            coins.text = item.coins.toString()
        }
    }

    inner class BannerVH(view: View) : RecyclerView.ViewHolder(view) {

        private val img = view.findViewById<ImageView>(R.id.img)
        private val icon = view.findViewById<ImageView>(R.id.icon)
        private val title = view.findViewById<TextView>(R.id.title)
        private val subtitle = view.findViewById<TextView>(R.id.subtitle)
        private val btn = view.findViewById<Button>(R.id.btn)

        fun bind(item: HomeItem.Banner) {
            img.setImageResource(item.imageRes)
            icon.setImageResource(item.iconRes)
            title.text = item.title
            subtitle.text = item.subtitle
            btn.setOnClickListener { onItemClick(item) }

            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}

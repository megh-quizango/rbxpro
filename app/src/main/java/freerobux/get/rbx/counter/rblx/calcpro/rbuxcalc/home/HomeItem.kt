package freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.home

sealed class HomeItem {

    data class Header(
        val coins: Int = 0
    ) : HomeItem()

    data class Card(
        val title: String,
        val imageRes: Int,
        val action: HomeAction
    ) : HomeItem()

    data class Banner(
        val iconRes: Int,
        val imageRes: Int,
        val title: String,
        val subtitle: String
    ) : HomeItem()
}

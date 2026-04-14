package freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils

import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.model.UrlItem

object UrlSelector {

    fun pickUrls(list: List<UrlItem>, count: Int): List<String> {
        val result = mutableListOf<String>()
        val totalWeight = list.sumOf { it.weight }

        repeat(count) {
            val rand = (1..totalWeight).random()
            var cumulative = 0

            for (item in list) {
                cumulative += item.weight
                if (rand <= cumulative) {
                    result.add(item.url)
                    break
                }
            }
        }

        return result
    }
}

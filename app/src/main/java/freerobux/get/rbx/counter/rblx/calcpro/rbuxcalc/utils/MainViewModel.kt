package freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val repo: ConfigRepository,
    private val cache: UrlCache
) : ViewModel() {

    val urlState = MutableStateFlow<String?>(null)

    fun loadUrl() {
        viewModelScope.launch {

            // 1. Load cached instantly (FAST START)
            val cached = cache.getUrl()
            if (cached != null) {
                urlState.value = cached
            }

            // 2. Fetch fresh in background
            val fresh = repo.fetchUrl()
            if (!fresh.isNullOrEmpty()) {
                cache.saveUrl(fresh)
                urlState.value = fresh
            }
        }
    }
}

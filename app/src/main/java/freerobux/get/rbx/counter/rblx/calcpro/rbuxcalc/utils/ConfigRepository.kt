package freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils

import com.google.firebase.database.FirebaseDatabase
import freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.model.CustomTabsConfig
import kotlinx.coroutines.suspendCancellableCoroutine

class ConfigRepository {

    private val ref = FirebaseDatabase.getInstance()
        .getReference("custom_tabs")

    suspend fun fetchUrl(): String? {
        val config = fetchConfig() ?: return null
        if (!config.enabled || config.urls.isEmpty()) return null
        return UrlSelector.pickUrls(config.urls, 1).firstOrNull()
    }

    suspend fun fetchConfig(): CustomTabsConfig? =
        suspendCancellableCoroutine { cont ->

            ref.get()
                .addOnSuccessListener { snapshot ->
                    val config = snapshot.getValue(CustomTabsConfig::class.java)
                    cont.resume(config, null)
                }
                .addOnFailureListener {
                    cont.resume(null, null)
                }
        }
}

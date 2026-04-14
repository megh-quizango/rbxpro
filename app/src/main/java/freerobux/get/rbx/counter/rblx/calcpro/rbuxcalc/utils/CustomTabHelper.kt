package freerobux.get.rbx.counter.rblx.calcpro.rbuxcalc.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsSession
import androidx.browser.customtabs.CustomTabsIntent

class CustomTabHelper {

    private var client: CustomTabsClient? = null
    private var session: CustomTabsSession? = null

    fun warmUp(context: Context) {
        CustomTabsClient.bindCustomTabsService(
            context,
            "com.android.chrome",
            object : CustomTabsServiceConnection() {

                override fun onCustomTabsServiceConnected(
                    name: ComponentName,
                    client: CustomTabsClient
                ) {
                    this@CustomTabHelper.client = client
                    client.warmup(0L)
                    session = client.newSession(null)
                }

                override fun onServiceDisconnected(name: ComponentName) {
                    client = null
                    session = null
                }
            }
        )
    }

    fun open(context: Context, url: String) {
        try {
            val intent = CustomTabsIntent.Builder(session)
                .setShowTitle(true)
                .build()

            intent.launchUrl(context, Uri.parse(url))

        } catch (e: Exception) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }
}

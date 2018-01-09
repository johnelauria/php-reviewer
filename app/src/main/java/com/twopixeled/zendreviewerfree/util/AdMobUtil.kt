package com.twopixeled.zendreviewerfree.util

import com.google.android.gms.ads.AdRequest

class AdMobUtil {
    /**
     * Registers the test devices before requesting an ad
     */
    fun buildAdRequest(): AdRequest {
        return AdRequest.Builder()
                .addTestDevice("22EC10AF2DE9A828E79AA6AF1F4C779D")
                .addTestDevice("31E1E1F56EF391C24BAFC18A4758439D")
                .build()
    }
}
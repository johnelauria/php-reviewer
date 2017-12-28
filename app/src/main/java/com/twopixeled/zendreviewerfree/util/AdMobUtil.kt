package com.twopixeled.zendreviewerfree.util

import com.google.android.gms.ads.AdRequest

class AdMobUtil {
    /**
     * Registers the test devices before requesting an ad
     */
    fun buildAdRequest(): AdRequest {
        return AdRequest.Builder()
                .addTestDevice("22EC10AF2DE9A828E79AA6AF1F4C779D")
                .build()
    }
}
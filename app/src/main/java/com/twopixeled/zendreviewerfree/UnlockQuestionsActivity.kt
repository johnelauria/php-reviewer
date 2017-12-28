package com.twopixeled.zendreviewerfree

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.twopixeled.zendreviewerfree.util.AdMobUtil
import com.twopixeled.zendreviewerfree.util.AppPreferenceUtil

import kotlinx.android.synthetic.main.activity_unlock_questions.unlockQuestionsBtn

class UnlockQuestionsActivity : AppCompatActivity(), RewardedVideoAdListener, View.OnClickListener {
    private lateinit var mRewardedVideoAd: RewardedVideoAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unlock_questions)

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this)
        mRewardedVideoAd.rewardedVideoAdListener = this
        loadRewardedVideoAd()

        unlockQuestionsBtn.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        if (view?.id == R.id.unlockQuestionsBtn && mRewardedVideoAd.isLoaded) {
            mRewardedVideoAd.show()
        }
    }

    override fun onRewardedVideoAdClosed() {

    }

    override fun onRewardedVideoAdLeftApplication() {

    }

    override fun onRewardedVideoAdLoaded() {

    }

    override fun onRewardedVideoAdOpened() {

    }

    override fun onRewarded(reward: RewardItem?) {
        AppPreferenceUtil(this).addMoreQuestions()
        Toast.makeText(this, "15 questions unlocked unlocked", Toast.LENGTH_LONG).show()
    }

    override fun onRewardedVideoStarted() {

    }

    override fun onRewardedVideoAdFailedToLoad(p0: Int) {
        Toast.makeText(this, "Failed to load ads. Check your network.", Toast.LENGTH_SHORT).show()
    }

    private fun loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd(
                "ca-app-pub-8537542711636630/8085686460",
                AdMobUtil().buildAdRequest()
        )
    }
}

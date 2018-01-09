package com.twopixeled.zendreviewerfree

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.view.View
import android.widget.Toast
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.twopixeled.zendreviewerfree.util.AdMobUtil
import com.twopixeled.zendreviewerfree.util.AppPreferenceUtil
import kotlinx.android.synthetic.main.activity_unlock_questions.unlockQuestionsBtn
import kotlinx.android.synthetic.main.activity_unlock_questions.unlockMsgTV
import kotlinx.android.synthetic.main.activity_unlock_questions.buyAppBtn

class UnlockQuestionsActivity : AppCompatActivity(), RewardedVideoAdListener, View.OnClickListener {
    private lateinit var mRewardedVideoAd: RewardedVideoAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unlock_questions)

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this)
        mRewardedVideoAd.rewardedVideoAdListener = this
        loadRewardedVideoAd()

        unlockQuestionsBtn.setOnClickListener(this)
        buyAppBtn.setOnClickListener { buyApp() }
    }

    override fun onClick(view: View?) {
        if (view?.id == R.id.unlockQuestionsBtn && mRewardedVideoAd.isLoaded) {
            mRewardedVideoAd.show()
        }
    }

    override fun onRewardedVideoAdClosed() {
        loadRewardedVideoAd()
    }

    override fun onRewardedVideoAdLeftApplication() {
        loadRewardedVideoAd()
    }

    override fun onRewardedVideoAdLoaded() {
        unlockQuestionsBtn.text = resources.getString(R.string.unlockBtnWatchTxt)
        unlockQuestionsBtn.isEnabled = true
        unlockQuestionsBtn.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorAccent, null))
    }

    override fun onRewardedVideoAdOpened() {

    }

    override fun onRewarded(reward: RewardItem?) {
        val appPreference = AppPreferenceUtil(this)

        appPreference.addMoreQuestions()
        updateBodyMsg(appPreference.getQuestionsLimit())
        Toast.makeText(this, "15 questions unlocked", Toast.LENGTH_LONG).show()
    }

    override fun onRewardedVideoStarted() {

    }

    override fun onRewardedVideoAdFailedToLoad(p0: Int) {
        unlockQuestionsBtn.text = resources.getString(R.string.unlockBtnDisabled)
        Toast.makeText(this, "Failed to load ads. Check your network.", Toast.LENGTH_SHORT).show()
    }

    /**
     * Loads a video ad to have it ready to play
     */
    private fun loadRewardedVideoAd() {
        unlockQuestionsBtn.isEnabled = false
        unlockQuestionsBtn.text = resources.getString(R.string.unlockBtnLoadingTxt)
        unlockQuestionsBtn.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorPrimaryLight, null))
        mRewardedVideoAd.loadAd(
                "ca-app-pub-8537542711636630/8085686460",
                AdMobUtil().buildAdRequest()
        )
    }

    /**
     * Updates the body message of this activity. If unlocked questions are still less than 900,
     * then display a message containing the total number of questions unlocked. If all are already
     * unlocked, inform the user
     */
    private fun updateBodyMsg(unlockedQuestionCnt: Int) {
        if (unlockedQuestionCnt < 900) {
            unlockMsgTV.text = resources.getString(
                    R.string.unlockMsgBodyThanks,
                    unlockedQuestionCnt
            )
        } else {
            unlockMsgTV.text = resources.getString(R.string.unlockMsgBodyCompleted)
        }
    }

    private fun buyApp() {
        val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=com.twopixeled.zendreviewer")
        )
        startActivity(browserIntent)
    }
}

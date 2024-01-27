package com.navneet.tictactoe.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Vibrator
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import com.google.android.gms.ads.*
import com.navneet.tictactoe.R


class StatisticActivity : AppCompatActivity() {
    //Declaring variables for win,loss and total score of different modes
    private lateinit var easyWin: TextView
    private lateinit var easyLoss: TextView
    private lateinit var easyTotal: TextView

    private lateinit var mediumWin: TextView
    private lateinit var mediumLoss: TextView
    private lateinit var mediumTotal: TextView

    private lateinit var hardWin: TextView
    private lateinit var hardLoss: TextView
    private lateinit var hardTotal: TextView

    private lateinit var randomWin: TextView
    private lateinit var randomLoss: TextView
    private lateinit var randomTotal: TextView

    private lateinit var friendsWin: TextView
    private lateinit var friendsLoss: TextView
    private lateinit var friendsTotal: TextView

    private lateinit var backBtn: ImageButton

    private lateinit var mAdView: AdView
    private lateinit var music: MediaPlayer


    lateinit var sharedPreferences: SharedPreferences
    private var vibratePreferences = true
    private var musicPreference = true
    private var audioPreference = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistic)
        loadBannerAd()
        this.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.WHITE
        }

        backBtn = findViewById(R.id.backBtn)

        easyWin = findViewById(R.id.txtEasyWinScore)
        easyLoss = findViewById(R.id.txtEasyLossScore)
        easyTotal = findViewById(R.id.txtEasyTotalScore)

        mediumWin = findViewById(R.id.txtMediumWinScore)
        mediumLoss = findViewById(R.id.txtMediumLossScore)
        mediumTotal = findViewById(R.id.txtMediumTotalScore)

        hardWin = findViewById(R.id.txtHardWinScore)
        hardLoss = findViewById(R.id.txtHardLossScore)
        hardTotal = findViewById(R.id.txtHardTotalScore)

        randomWin = findViewById(R.id.txtRandomWinScore)
        randomLoss = findViewById(R.id.txtRandomLossScore)
        randomTotal = findViewById(R.id.txtRandomTotalScore)

        friendsWin = findViewById(R.id.txtFriendsWinScore)
        friendsLoss = findViewById(R.id.txtFriendsLossScore)
        friendsTotal = findViewById(R.id.txtFriendsTotalScore)

        sharedPreferences = this.getSharedPreferences("User Preferences", Context.MODE_PRIVATE)
        musicPreference = sharedPreferences.getBoolean("music", true)
        vibratePreferences = sharedPreferences.getBoolean("vibration", true)
        audioPreference = sharedPreferences.getBoolean("audio", true)

        backBtn.setOnClickListener {
            clickSound()
            vibrate(40)
            startActivity(Intent(this@StatisticActivity, PlayerChoiceActivity::class.java))
            finish()
        }

        easyWin.text = sharedPreferences.getInt("easyWin", 0).toString()
        easyLoss.text = sharedPreferences.getInt("easyLoss", 0).toString()
        easyTotal.text = sharedPreferences.getInt("easyTotal", 0).toString()

        mediumWin.text = sharedPreferences.getInt("mediumWin", 0).toString()
        mediumLoss.text = sharedPreferences.getInt("mediumLoss", 0).toString()
        mediumTotal.text = sharedPreferences.getInt("mediumTotal", 0).toString()

        hardWin.text = sharedPreferences.getInt("hardWin", 0).toString()
        hardLoss.text = sharedPreferences.getInt("hardLoss", 0).toString()
        hardTotal.text = sharedPreferences.getInt("hardTotal", 0).toString()

        randomWin.text = sharedPreferences.getInt("randomWin", 0).toString()
        randomLoss.text = sharedPreferences.getInt("randomLoss", 0).toString()
        randomTotal.text = sharedPreferences.getInt("randomTotal", 0).toString()

        friendsWin.text = sharedPreferences.getInt("friendsWin", 0).toString()
        friendsLoss.text = sharedPreferences.getInt("friendsLoss", 0).toString()
        friendsTotal.text = sharedPreferences.getInt("friendsTotal", 0).toString()

    }

    private fun loadBannerAd() {
        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        mAdView.adListener = object : AdListener() {
            override fun onAdClosed() {
                loadBannerAd()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                println(adError)
            }
        }
    }

    private fun clickSound() {
        if (audioPreference) {
            val sound = MediaPlayer.create(this, R.raw.click_sound)
            sound.start()
        }
    }

    private fun vibrate(time: Long) {
        if (vibratePreferences) {
            val vibrate = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrate.vibrate(time)
        }
    }

    private fun stopBackgroundMusic() {
        music.release()
    }

    override fun onPause() {
        if (musicPreference)
            stopBackgroundMusic()
        super.onPause()
    }

    override fun onDestroy() {
        if (musicPreference)
            stopBackgroundMusic()
        super.onDestroy()
    }

    override fun onStop() {
        if (musicPreference)
            stopBackgroundMusic()
        super.onStop()
    }

    override fun onResume() {
        music = MediaPlayer.create(this, R.raw.background_music)
        if (musicPreference) {
            music.isLooping = true
            music.start()
        }
        super.onResume()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this@StatisticActivity, PlayerChoiceActivity::class.java))
        finish()
    }
}
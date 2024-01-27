package com.navneet.tictactoe.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.*
import com.navneet.tictactoe.R
import com.navneet.tictactoe.util.ConnectionManager

class MultiPlayerModeActivity : AppCompatActivity() {

    private lateinit var onlineBtn: ImageButton
    private lateinit var offlineBtn: ImageButton
    private lateinit var randomBtn: ImageButton
    private lateinit var mAdView: AdView
    private lateinit var music: MediaPlayer

    private lateinit var sharedPreferences: SharedPreferences
    private var vibratePreferences: Boolean = true
    private var musicPreference: Boolean = true
    private var audioPreference: Boolean = true

    private var count: Int = 1
    private var namePlayer1: String = ""
    private var namePlayer2: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = this.getSharedPreferences("User Preferences", Context.MODE_PRIVATE)
        setContentView(R.layout.activity_multi_player_mode)
        loadPreferences()
        loadBannerAd()
        configureUI()
        setClickListeners()
        configureSystemUI()
    }

    private fun loadPreferences() {
        sharedPreferences = getSharedPreferences("User Preferences", Context.MODE_PRIVATE)
        musicPreference = sharedPreferences.getBoolean("music", true)
        vibratePreferences = sharedPreferences.getBoolean("vibration", true)
        audioPreference = sharedPreferences.getBoolean("audio", true)
    }

    private fun configureUI() {
        onlineBtn = findViewById(R.id.onlineModeBtn)
        offlineBtn = findViewById(R.id.offlineModeBtn)
        randomBtn = findViewById(R.id.onlineRandomBtn)
    }

    private fun setClickListeners() {
        onlineBtn.setOnClickListener { handleOnlineButtonClick() }
        offlineBtn.setOnClickListener { getPlayerNames() }
        randomBtn.setOnClickListener { handleRandomButtonClick() }
    }

    private fun handleOnlineButtonClick() {
        clickSound()
        vibrate(40)
        if (checkNetwork()) {
            startActivity(Intent(this, OnlineCodeGeneratorActivity::class.java))
        } else {
            showInternetToast()
        }
    }

    private fun handleRandomButtonClick() {
        clickSound()
        vibrate(40)
        if (checkNetwork()) {
            startActivity(Intent(this, RandomPlayersActivity::class.java))
        } else {
            showInternetToast()
        }
    }

    private fun getPlayerNames() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.name_drawer_layout)
        val btnOk: ImageButton = dialog.findViewById(R.id.btnOk)
        val playerName: EditText = dialog.findViewById(R.id.etPlayerName)
        val heading: TextView = dialog.findViewById(R.id.txtHelp)
        dialog.setCancelable(false)
        val playerNumber = if (count == 1) 1 else 2
        heading.text = "Enter player-$playerNumber name"
        dialog.create()
        playerName.requestFocus()
        dialog.show()
        btnOk.setOnClickListener {
            clickSound()
            vibrate(40)
            if (count == 1) {
                namePlayer1 = playerName.text.toString().ifEmpty { "Player1" }
            } else if (count == 2) {
                namePlayer2 = playerName.text.toString().ifEmpty { "Player2" }
            }
            dialog.dismiss()
            when (count) {
                1 -> {
                    count++
                    getPlayerNames()
                }
                else -> {
                    val intent = Intent(this, SinglePlayingBoard::class.java)
                    intent.putExtra("singleUser", false)
                    intent.putExtra("namePlayer1", namePlayer1)
                    intent.putExtra("namePlayer2", namePlayer2)
                    startActivity(intent)
                }
            }
        }
    }

    private fun clickSound() {
        if (audioPreference) {
            MediaPlayer.create(this, R.raw.click_sound)?.start()
        }
    }

    private fun vibrate(time: Long) {
        if (vibratePreferences) {
            val vibrate = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrate.vibrate(time)
        }
    }

    private fun loadBannerAd() {
        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        mAdView.adListener = object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                println(adError)
            }
        }
    }

    private fun configureSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
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
    }

    private fun checkNetwork(): Boolean {
        return ConnectionManager().checkConnectivity(this)
    }

    private fun showInternetToast() {
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.internet_toast, null)
        val toast = Toast(this)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.show()
    }

    override fun onPause() {
        if (musicPreference) {
            stopBackgroundMusic()
        }
        super.onPause()
    }

    override fun onDestroy() {
        if (musicPreference) {
            stopBackgroundMusic()
        }
        super.onDestroy()
    }

    override fun onStop() {
        if (musicPreference) {
            stopBackgroundMusic()
        }
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
        stopBackgroundMusic()
        super.onBackPressed()
    }

    fun stopBackgroundMusic() {
        if (::music.isInitialized) {
            music.release()
        }
    }
}

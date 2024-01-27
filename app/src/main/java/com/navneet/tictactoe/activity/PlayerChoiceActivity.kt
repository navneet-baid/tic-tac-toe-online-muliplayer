package com.navneet.tictactoe.activity

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import com.navneet.tictactoe.BuildConfig
import com.navneet.tictactoe.R

class PlayerChoiceActivity : AppCompatActivity() {

    private lateinit var singlePlayerBtn: ImageButton
    private lateinit var multiPlayerBtn: ImageButton
    private lateinit var settingsBtn: ImageButton
    private lateinit var statisticsBtn: ImageButton
    private lateinit var profileBtn: ImageButton
    private lateinit var availableCoins: TextView
    private lateinit var dialog: Dialog
    private lateinit var music: MediaPlayer
    private lateinit var sharedPreferences: SharedPreferences

    private var vibratePreferences = true
    private var musicPreference = true
    private var audioPreference = true
    private var notificationPreference = true

    private var mInterstitialAd: InterstitialAd? = null

    private val COINS_REQUIRED_EASY = 10
    private val COINS_REQUIRED_MEDIUM = 30
    private val COINS_REQUIRED_HARD = 80

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_choice)
        initUI()
        showBonusDialog()
        loadBannerAd()
        loadInterAd()
        loadPreferences()
        setClickListeners()
    }

    private fun initUI() {
        // Set system UI flags
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LOW_PROFILE
                )
        window.statusBarColor = Color.WHITE

        // Initialize views
        singlePlayerBtn = findViewById(R.id.singlePlayerBtn)
        multiPlayerBtn = findViewById(R.id.multiPlayerBtn)
        settingsBtn = findViewById(R.id.setting_btn)
        statisticsBtn = findViewById(R.id.statistics_btn)
        profileBtn = findViewById(R.id.profile_btn)
        availableCoins = findViewById(R.id.availableCoins)
        dialog = Dialog(this)

        // Load preferences
        loadPreferences()
    }

    private fun showBonusDialog() {
        val toShowDialog = sharedPreferences.getBoolean("showBonusDialog", false)
        if (toShowDialog) {
            val dialog = Dialog(this@PlayerChoiceActivity)
            dialog.setContentView(R.layout.welcome_bonus_layout)
            val closeBtn = dialog.findViewById<ImageView>(R.id.imgClose)
            closeBtn.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
            sharedPreferences.edit().putBoolean("showBonusDialog", false).commit()
        }
    }

    private fun loadPreferences() {
        sharedPreferences = getSharedPreferences("User Preferences", Context.MODE_PRIVATE)
        musicPreference = sharedPreferences.getBoolean("music", true)
        vibratePreferences = sharedPreferences.getBoolean("vibration", true)
        audioPreference = sharedPreferences.getBoolean("audio", true)
        notificationPreference = sharedPreferences.getBoolean("notification", true)
        availableCoins.text = sharedPreferences.getString("availableCoins", null)
    }

    private fun setClickListeners() {

        availableCoins.setOnClickListener {
            startActivity(Intent(this@PlayerChoiceActivity, ProfileActivity::class.java))
            finish()
        }

        statisticsBtn.setOnClickListener {
            startActivity(Intent(this@PlayerChoiceActivity, StatisticActivity::class.java))
            finish()
        }
        settingsBtn.setOnClickListener {
            openSettingsDialog()
        }

        profileBtn.setOnClickListener {
            clickSound()
            startActivity(Intent(this@PlayerChoiceActivity, ProfileActivity::class.java))
        }
        singlePlayerBtn.setOnClickListener {
            clickSound()
            vibrate(40)
            showDifficultySelectionDialog()
        }

        multiPlayerBtn.setOnClickListener {
            clickSound()
            vibrate(40)
            startActivity(Intent(this, MultiPlayerModeActivity::class.java))
        }
    }

    private fun openSettingsDialog() {
        val settingsDialog = Dialog(this)
        settingsDialog.setContentView(R.layout.settings_layout)
        val backBtn: ImageView = settingsDialog.findViewById(R.id.settingsBackBtn)
        val musicToggleBtn: SwitchCompat = settingsDialog.findViewById(R.id.musicToggleBtn)
        val audioToggleBtn: SwitchCompat = settingsDialog.findViewById(R.id.audioToggleBtn)
        val vibrationToggleBtn: SwitchCompat = settingsDialog.findViewById(R.id.vibrationToggleBtn)
        val notificationToggleBtn: SwitchCompat =
            settingsDialog.findViewById(R.id.notificationToggleBtn)
        val version: TextView = settingsDialog.findViewById(R.id.versionCode)

        // Initialize toggle buttons based on preferences
        musicToggleBtn.isChecked = musicPreference
        audioToggleBtn.isChecked = audioPreference
        vibrationToggleBtn.isChecked = vibratePreferences
        notificationToggleBtn.isChecked = notificationPreference

        // Set app version
        val versionCode = BuildConfig.VERSION_NAME
        version.text = "V:$versionCode"

        // Click listeners for toggle buttons
        setToggleListeners(musicToggleBtn, "music", ::playBackgroundMusic, ::stopBackgroundMusic)
        setToggleListeners(audioToggleBtn, "audio")
        setToggleListeners(vibrationToggleBtn, "vibration")
        setToggleListeners(notificationToggleBtn, "notification")

        // Troubleshoot button
        val troubleshootBtn: AppCompatButton = settingsDialog.findViewById(R.id.troubleshootBtn)
        troubleshootBtn.setOnClickListener {
            showTroubleshootDialog()
        }

        // Back button
        backBtn.setOnClickListener {
            showInterAd()
            settingsDialog.dismiss()
        }

        // Make the dialog not cancelable to prevent accidental dismissals
        settingsDialog.setCancelable(false)

        // Show the settings dialog
        settingsDialog.show()
    }

    private fun setToggleListeners(
        toggle: SwitchCompat,
        preferenceKey: String,
        onEnable: (() -> Unit)? = null,
        onDisable: (() -> Unit)? = null
    ) {
        toggle.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean(preferenceKey, isChecked).apply()
            if (isChecked && onEnable != null) {
                onEnable()
            } else if (!isChecked && onDisable != null) {
                onDisable()
            }
        }
    }

    private fun showTroubleshootDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Troubleshooting")
        builder.setIcon(R.drawable.ic_troubleshoot)
        builder.setMessage(
            "If you encounter any issues with the game, follow these steps:\n\n" +
                    "1. Clear Game Data:\n" +
                    "   If the game crashes or behaves unexpectedly, try clearing its data by going to Settings -> Apps -> Tic Tac Toe-Online -> Storage -> Clear Data.\n\n" +
                    "2. Update the Game:\n" +
                    "   Make sure you have the latest version of the game installed. Check for updates on the app store."
        )
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        builder.setCancelable(true)
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun showDifficultySelectionDialog() {

        val levelDialog = Dialog(this)
        levelDialog.setContentView(R.layout.difficulty_mode_dialog)
        val easyBtn: ImageButton = levelDialog.findViewById(R.id.btnEasy)
        val mediumBtn: ImageButton = levelDialog.findViewById(R.id.btnMedium)
        val hardBtn: ImageButton = levelDialog.findViewById(R.id.btnHard)
        val availableCoins = sharedPreferences.getString("availableCoins", "0")?.toInt()

        easyBtn.setOnClickListener {
            onDifficultySelected(availableCoins, COINS_REQUIRED_EASY, 1)
            levelDialog.dismiss()
        }

        mediumBtn.setOnClickListener {
            onDifficultySelected(availableCoins, COINS_REQUIRED_MEDIUM, 2)
            levelDialog.dismiss()
        }

        hardBtn.setOnClickListener {
            onDifficultySelected(availableCoins, COINS_REQUIRED_HARD, 3)
            levelDialog.dismiss()
        }

        levelDialog.create()
        levelDialog.show()
    }

    private fun onDifficultySelected(availableCoins: Int?, requiredCoins: Int, mode: Int) {
        if (availableCoins != null && availableCoins >= requiredCoins) {
            startActivity(
                Intent(this, SinglePlayingBoard::class.java)
                    .putExtra("singleUser", true)
                    .putExtra("mode", mode)
            )
        } else {
            val insufficientCoinsMessage = getString(R.string.insufficient_coins_message)
            Toast.makeText(this@PlayerChoiceActivity, insufficientCoinsMessage, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun loadInterAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            "ca-app-pub-5528158197168162/7817072111",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                }
            }
        )
    }

    private fun showInterAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    handleAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    handleAdFailedToShow()
                }
            }
            mInterstitialAd?.show(this)
        } else {
            handleAdFailedToShow()
        }
    }

    private fun handleAdDismissed() {
        loadInterAd()
        dialog.dismiss()
        restartActivity()
    }

    private fun handleAdFailedToShow() {
        dialog.dismiss()
        loadInterAd()
        restartActivity()
    }

    private fun restartActivity() {
        val intent = Intent(this@PlayerChoiceActivity, PlayerChoiceActivity::class.java)
        finish()
        startActivity(intent)
        overridePendingTransition(0, 0)
    }


    private fun loadBannerAd() {
        MobileAds.initialize(this)
        val adView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()

        adView.loadAd(adRequest)

        adView.adListener = object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                // Handle ad loading failure
                Log.e("AdLoadingError", "Code: ${adError.code}, Message: ${adError.message}")
            }
        }
    }

    // Function to play a click sound if audio is enabled
    private fun clickSound() {
        if (audioPreference) {
            val sound = MediaPlayer.create(this, R.raw.click_sound)
            sound.start()
        }
    }

    // Function to vibrate the device if vibration is enabled
    private fun vibrate(time: Long) {
        if (vibratePreferences) {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(time)
        }
    }

    // Function to stop the background music
    private fun stopBackgroundMusic() {
        music.release()
    }

    // Function to play the background music if enabled
    private fun playBackgroundMusic() {
        if (musicPreference) {
            music = MediaPlayer.create(this, R.raw.background_music)
            music.isLooping = true
            music.start()
        }
    }

    // Lifecycle callback when the activity is paused
    override fun onPause() {
        super.onPause()

        // Stop background music if enabled
        if (musicPreference)
            stopBackgroundMusic()

        // Update availableCoins and load banner ad
        availableCoins.text = sharedPreferences.getString("availableCoins", null)
        loadBannerAd()
    }

    // Lifecycle callback when the activity is destroyed
    override fun onDestroy() {
        super.onDestroy()

        // Stop background music if enabled
        if (musicPreference)
            stopBackgroundMusic()
    }

    // Lifecycle callback when the activity is stopped
    override fun onStop() {
        super.onStop()

        // Stop background music if enabled
        if (musicPreference)
            stopBackgroundMusic()
    }

    // Lifecycle callback when the activity is resumed
    override fun onResume() {
        super.onResume()

        // Play background music if enabled
        playBackgroundMusic()

        // Update availableCoins
        availableCoins.text = sharedPreferences.getString("availableCoins", null)
    }


    private var doubleBackToExitPressedOnce = false
    private val exitConfirmationDelayMillis = 2000 // Adjust the delay as needed

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
        } else {
            doubleBackToExitPressedOnce = true
            showExitConfirmation()
            Handler(Looper.getMainLooper()).postDelayed({
                doubleBackToExitPressedOnce = false
            }, exitConfirmationDelayMillis.toLong())
        }
    }

    private fun showExitConfirmation() {
        val snackbar = Snackbar.make(
            findViewById(android.R.id.content),
            "Tap again to exit",
            Snackbar.LENGTH_SHORT
        )
        snackbar.setAction("EXIT") { super.onBackPressed() }
        snackbar.show()
    }

}
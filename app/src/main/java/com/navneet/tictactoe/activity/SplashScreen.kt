package com.navneet.tictactoe.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.navneet.tictactoe.R
import com.navneet.tictactoe.activity.PlayerChoiceActivity


@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {

    private companion object {
        const val DELAY_DURATION = 1000L
        const val FIRST_TIME_KEY = "isFirstTimeOpen"
        const val PLAYER_NAME_KEY = "player_name"
        const val COINS_KEY = "availableCoins"
        const val BONUS_DIALOG_KEY = "showBonusDialog"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)
        // Configure fullscreen display and status bar color
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.WHITE
        }
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        // Initialize shared preferences
        val sharedPreferences = getSharedPreferences("User Preferences", Context.MODE_PRIVATE)

        // Check if it's the first time and set initial values
        if (sharedPreferences.getBoolean(FIRST_TIME_KEY, true)) {
            sharedPreferences.edit().run {
                putBoolean(FIRST_TIME_KEY, false)
                putString(COINS_KEY, "500")
                putBoolean(BONUS_DIALOG_KEY, true)
                apply() // Use apply() instead of commit() for background saving
            }
        }

        // Generate a random player name if not set
        if (sharedPreferences.getString(PLAYER_NAME_KEY, null) == null) {
            val playerName = "USER${(111..999).random()}"
            sharedPreferences.edit().putString(PLAYER_NAME_KEY, playerName).apply()
        }

        // Transition to PlayerChoiceActivity after a delay
        Handler().postDelayed({
            val intent = Intent(this, PlayerChoiceActivity::class.java)
            startActivity(intent)
            finish()
        }, DELAY_DURATION)
    }
}

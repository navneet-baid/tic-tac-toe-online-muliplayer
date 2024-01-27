package com.navneet.tictactoe.activity

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.media.MediaPlayer
import android.os.*
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.navneet.tictactoe.R


class SinglePlayingBoard : AppCompatActivity() {

    lateinit var button1: ImageButton
    lateinit var button2: ImageButton
    lateinit var button3: ImageButton
    lateinit var button4: ImageButton
    lateinit var button5: ImageButton
    lateinit var button6: ImageButton
    lateinit var button7: ImageButton
    lateinit var button8: ImageButton
    lateinit var button9: ImageButton
    lateinit var resetBtn: ImageButton
    lateinit var score1: TextView
    lateinit var score2: TextView
    lateinit var user1txt: TextView
    lateinit var user2txt: TextView
    lateinit var player2Icon: ImageView

    lateinit var music: MediaPlayer

    lateinit var mAdView: AdView
    private var mInterstitialAd: InterstitialAd? = null

    var singleUser: Boolean = false
    var playerTurn = true
    var player1Score = 0
    var player2Score = 0
    var namePlayer1 = "PLayer1"
    var namePlayer2 = "PLayer2"
    var check = 1
    var player1 = ArrayList<Int>()
    var player2 = ArrayList<Int>()
    var emptyCells = ArrayList<Int>()
    var activeUser = 1

    lateinit var sharedPreferences: SharedPreferences
    var vibratePreferences = true
    var musicPreference = true
    var audioPreference = true

    var crossImage=0
    var circleImage=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_playing_board)
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

        mAdView = findViewById(R.id.adView)
        button1 = findViewById(R.id.button)
        button2 = findViewById(R.id.button2)
        button3 = findViewById(R.id.button3)
        button4 = findViewById(R.id.button4)
        button5 = findViewById(R.id.button5)
        button6 = findViewById(R.id.button6)
        button7 = findViewById(R.id.button7)
        button8 = findViewById(R.id.button8)
        button9 = findViewById(R.id.button9)
        resetBtn = findViewById(R.id.resetBtn)
        score1 = findViewById(R.id.txtUserScore)
        score2 = findViewById(R.id.txtRobotScore)
        user1txt = findViewById(R.id.user1txt)
        user2txt = findViewById(R.id.user2txt)
        player2Icon = findViewById(R.id.player2Icon)

        sharedPreferences = this.getSharedPreferences("User Preferences", Context.MODE_PRIVATE)
        musicPreference = sharedPreferences.getBoolean("music", true)
        vibratePreferences = sharedPreferences.getBoolean("vibration", true)
        audioPreference = sharedPreferences.getBoolean("audio", true)

        crossImage=sharedPreferences.getInt("CrossPreference",R.drawable.cross)
        circleImage=sharedPreferences.getInt("NoughtPreference",R.drawable.circle)


        resetBtn.setOnClickListener {
            reset()
        }

        loadBannerAd()
        loadInterAd()

        singleUser = intent.getBooleanExtra("singleUser", false)
        if (!singleUser) {
            namePlayer1 = intent.getStringExtra("namePlayer1").toString()
            user1txt.text = namePlayer1
            namePlayer2 = intent.getStringExtra("namePlayer2").toString()
            user2txt.text = namePlayer2
            player2Icon.setImageResource(R.drawable.user_icon)
        }
    }

    fun clickfun(view: View) {
        val difficultyMode = intent.getIntExtra("mode", 0).toInt()

        if (playerTurn) {
            val button = view as ImageButton
            var cellID = 0
            when (button.id) {
                R.id.button -> cellID = 1
                R.id.button2 -> cellID = 2
                R.id.button3 -> cellID = 3
                R.id.button4 -> cellID = 4
                R.id.button5 -> cellID = 5
                R.id.button6 -> cellID = 6
                R.id.button7 -> cellID = 7
                R.id.button8 -> cellID = 8
                R.id.button9 -> cellID = 9
            }
            playerTurn = false
            Handler().postDelayed(Runnable { playerTurn = true }, 700)
            playnow(button, cellID, singleUser, difficultyMode)
        }
    }

    private fun playnow(buttonSelected: ImageButton, cellID: Int, singleUser: Boolean, mode: Int) {

        vibrate(50)
        if (activeUser == 1) {
            buttonSelected.setImageResource(crossImage)
            user1txt.setTextColor(resources.getColor(R.color.active_user))
            user2txt.setTextColor(resources.getColor(R.color.white))
            player1.add(cellID)
            emptyCells.add(cellID)
            tapSound()
            buttonSelected.isEnabled = false
            val checkWinner = checkWinner(mode)
            if (checkWinner == 1) {
                stopBackgroundMusic()
                Handler().postDelayed(Runnable { reset(mode) }, 500)
            } else if (singleUser) {
                user2txt.setTextColor(resources.getColor(R.color.active_user))
                user1txt.setTextColor(resources.getColor(R.color.white))
                Handler().postDelayed(Runnable {
                    robot(mode)
                    user1txt.setTextColor(resources.getColor(R.color.active_user))
                    user2txt.setTextColor(resources.getColor(R.color.white))
                }, 900)
            } else {
                activeUser = 2
                user2txt.setTextColor(resources.getColor(R.color.active_user))
                user1txt.setTextColor(resources.getColor(R.color.white))
            }
        } else {
            buttonSelected.setImageResource(circleImage)
            tapSound()
            user1txt.setTextColor(resources.getColor(R.color.active_user))
            user2txt.setTextColor(resources.getColor(R.color.white))
            activeUser = 1
            player2.add(cellID)
            emptyCells.add(cellID)
            buttonSelected.isEnabled = false
            val checkWinner = checkWinner(mode)
            if (checkWinner == 1)
                Handler().postDelayed(Runnable { reset(mode) }, 4000)

        }
    }


    private fun robot(mode: Int) {
        vibrate(45)
        if (mode == 1) {
            val rnd = (1..9).random()
            if (emptyCells.contains(rnd)) {
                robot(1)
            } else {
                val buttonSelected: ImageButton = when (rnd) {
                    1 -> button1
                    2 -> button2
                    3 -> button3
                    4 -> button4
                    5 -> button5
                    6 -> button6
                    7 -> button7
                    8 -> button8
                    9 -> button9
                    else -> {
                        button1
                    }
                }
                emptyCells.add(rnd)
                tapSound()
                buttonSelected.setImageResource(circleImage)
                player2.add(rnd)
                buttonSelected.isEnabled = false
                val checkWinner = checkWinner(mode)
                if (checkWinner == 1) {
                    Handler().postDelayed(Runnable { reset(mode) }, 2000)
                }
            }
        }
        if (mode == 2) {
            if (player1.contains(1) && player1.contains(2) && !player2.contains(3)) {
                robotButton(button3, 3, mode)
            } else if (player1.contains(1) && player1.contains(3) && !player2.contains(2)) {
                robotButton(button2, 2, mode)
            } else if (player1.contains(2) && player1.contains(3) && !player2.contains(1)) {
                robotButton(button1, 1, mode)
            } else if (player1.contains(4) && player1.contains(5) && !player2.contains(6)) {
                robotButton(button6, 6, mode)
            } else if (player1.contains(4) && player1.contains(6) && !player2.contains(5)) {
                robotButton(button5, 5, mode)
            } else if (player1.contains(5) && player1.contains(6) && !player2.contains(4)) {
                robotButton(button4, 4, mode)
            } else if (player1.contains(7) && player1.contains(8) && !player2.contains(9)) {
                robotButton(button9, 9, mode)
            } else if (player1.contains(7) && player1.contains(9) && !player2.contains(8)) {
                robotButton(button8, 8, mode)
            } else if (player1.contains(8) && player1.contains(9) && !player2.contains(7)) {
                robotButton(button7, 7, mode)
            } else if (player1.contains(1) && player1.contains(4) && !player2.contains(7)) {
                robotButton(button7, 7, mode)
            } else if (player1.contains(1) && player1.contains(7) && !player2.contains(4)) {
                robotButton(button4, 4, mode)
            } else if (player1.contains(4) && player1.contains(7) && !player2.contains(1)) {
                robotButton(button1, 1, mode)
            } else if (player1.contains(2) && player1.contains(5) && !player2.contains(8)) {
                robotButton(button8, 8, mode)
            } else if (player1.contains(2) && player1.contains(8) && !player2.contains(5)) {
                robotButton(button5, 5, mode)
            } else if (player1.contains(5) && player1.contains(8) && !player2.contains(2)) {
                robotButton(button2, 2, mode)
            } else if (player1.contains(3) && player1.contains(6) && !player2.contains(9)) {
                robotButton(button9, 9, mode)
            } else if (player1.contains(3) && player1.contains(9) && !player2.contains(6)) {
                robotButton(button6, 6, mode)
            } else if (player1.contains(6) && player1.contains(9) && !player2.contains(3)) {
                robotButton(button3, 3, mode)
            } else if (player1.contains(1) && player1.contains(5) && !player2.contains(9)) {
                robotButton(button9, 9, mode)
            } else if (player1.contains(1) && player1.contains(9) && !player2.contains(5)) {
                robotButton(button5, 5, mode)
            } else if (player1.contains(5) && player1.contains(9) && !player2.contains(1)) {
                robotButton(button1, 1, mode)
            } else if (player1.contains(3) && player1.contains(5) && !player2.contains(7)) {
                robotButton(button7, 7, mode)
            } else if (player1.contains(3) && player1.contains(7) && !player2.contains(5)) {
                robotButton(button5, 5, mode)
            } else if (player1.contains(5) && player1.contains(7) && !player2.contains(3)) {
                robotButton(button3, 3, mode)
            } else {
                robot(1)
            }
        }
        if (mode == 3) {
            if (player2.contains(1) && player2.contains(2) && !player1.contains(3)) {
                robotButton(button3, 3, mode)
            } else if (player2.contains(1) && player2.contains(3) && !player1.contains(2)) {
                robotButton(button2, 2, mode)
            } else if (player2.contains(2) && player2.contains(3) && !player1.contains(1)) {
                robotButton(button1, 1, mode)
            } else if (player2.contains(4) && player2.contains(5) && !player1.contains(6)) {
                robotButton(button6, 6, mode)
            } else if (player2.contains(4) && player2.contains(6) && !player1.contains(5)) {
                robotButton(button5, 5, mode)
            } else if (player2.contains(5) && player2.contains(6) && !player1.contains(4)) {
                robotButton(button4, 4, mode)
            } else if (player2.contains(7) && player2.contains(8) && !player1.contains(9)) {
                robotButton(button9, 9, mode)
            } else if (player2.contains(7) && player2.contains(9) && !player1.contains(8)) {
                robotButton(button8, 8, mode)
            } else if (player2.contains(8) && player2.contains(9) && !player1.contains(7)) {
                robotButton(button7, 7, mode)
            } else if (player2.contains(1) && player2.contains(4) && !player1.contains(7)) {
                robotButton(button7, 7, mode)
            } else if (player2.contains(1) && player2.contains(7) && !player1.contains(4)) {
                robotButton(button4, 4, mode)
            } else if (player2.contains(4) && player2.contains(7) && !player1.contains(1)) {
                robotButton(button1, 1, mode)
            } else if (player2.contains(2) && player2.contains(5) && !player1.contains(8)) {
                robotButton(button8, 8, mode)
            } else if (player2.contains(2) && player2.contains(8) && !player1.contains(5)) {
                robotButton(button5, 5, mode)
            } else if (player2.contains(5) && player2.contains(8) && !player1.contains(2)) {
                robotButton(button2, 2, mode)
            } else if (player2.contains(3) && player2.contains(6) && !player1.contains(9)) {
                robotButton(button9, 9, mode)
            } else if (player2.contains(3) && player2.contains(9) && !player1.contains(6)) {
                robotButton(button6, 6, mode)
            } else if (player2.contains(6) && player2.contains(9) && !player1.contains(3)) {
                robotButton(button3, 3, mode)
            } else if (player2.contains(1) && player2.contains(5) && !player1.contains(9)) {
                robotButton(button9, 9, mode)
            } else if (player2.contains(1) && player2.contains(9) && !player1.contains(5)) {
                robotButton(button5, 5, mode)
            } else if (player2.contains(5) && player2.contains(9) && !player1.contains(1)) {
                robotButton(button1, 1, mode)
            } else if (player2.contains(3) && player2.contains(5) && !player1.contains(7)) {
                robotButton(button7, 7, mode)
            } else if (player2.contains(3) && player2.contains(7) && !player1.contains(5)) {
                robotButton(button5, 5, mode)
            } else if (player2.contains(5) && player2.contains(7) && !player1.contains(3)) {
                robotButton(button3, 3, mode)
            } else if (player1.contains(1) && player1.contains(2) && !player2.contains(3)) {
                robotButton(button3, 3, mode)
            } else if (player1.contains(1) && player1.contains(3) && !player2.contains(2)) {
                robotButton(button2, 2, mode)
            } else if (player1.contains(2) && player1.contains(3) && !player2.contains(1)) {
                robotButton(button1, 1, mode)
            } else if (player1.contains(4) && player1.contains(5) && !player2.contains(6)) {
                robotButton(button6, 6, mode)
            } else if (player1.contains(4) && player1.contains(6) && !player2.contains(5)) {
                robotButton(button5, 5, mode)
            } else if (player1.contains(5) && player1.contains(6) && !player2.contains(4)) {
                robotButton(button4, 4, mode)
            } else if (player1.contains(7) && player1.contains(8) && !player2.contains(9)) {
                robotButton(button9, 9, mode)
            } else if (player1.contains(7) && player1.contains(9) && !player2.contains(8)) {
                robotButton(button8, 8, mode)
            } else if (player1.contains(8) && player1.contains(9) && !player2.contains(7)) {
                robotButton(button7, 7, mode)
            } else if (player1.contains(1) && player1.contains(4) && !player2.contains(7)) {
                robotButton(button7, 7, mode)
            } else if (player1.contains(1) && player1.contains(7) && !player2.contains(4)) {
                robotButton(button4, 4, mode)
            } else if (player1.contains(4) && player1.contains(7) && !player2.contains(1)) {
                robotButton(button1, 1, mode)
            } else if (player1.contains(2) && player1.contains(5) && !player2.contains(8)) {
                robotButton(button8, 8, mode)
            } else if (player1.contains(2) && player1.contains(8) && !player2.contains(5)) {
                robotButton(button5, 5, mode)
            } else if (player1.contains(5) && player1.contains(8) && !player2.contains(2)) {
                robotButton(button2, 2, mode)
            } else if (player1.contains(3) && player1.contains(6) && !player2.contains(9)) {
                robotButton(button9, 9, mode)
            } else if (player1.contains(3) && player1.contains(9) && !player2.contains(6)) {
                robotButton(button6, 6, mode)
            } else if (player1.contains(6) && player1.contains(9) && !player2.contains(3)) {
                robotButton(button3, 3, mode)
            } else if (player1.contains(1) && player1.contains(5) && !player2.contains(9)) {
                robotButton(button9, 9, mode)
            } else if (player1.contains(1) && player1.contains(9) && !player2.contains(5)) {
                robotButton(button5, 5, mode)
            } else if (player1.contains(5) && player1.contains(9) && !player2.contains(1)) {
                robotButton(button1, 1, mode)
            } else if (player1.contains(3) && player1.contains(5) && !player2.contains(7)) {
                robotButton(button7, 7, mode)
            } else if (player1.contains(3) && player1.contains(7) && !player2.contains(5)) {
                robotButton(button5, 5, mode)
            } else if (player1.contains(5) && player1.contains(7) && !player2.contains(3)) {
                robotButton(button3, 3, mode)
            } else {
                robot(1)
            }
        }
    }

    private fun checkWinner(mode: Int): Int {
        if (
            (player1.contains(1) && player1.contains(2) && player1.contains(3)) ||
            (player1.contains(4) && player1.contains(5) && player1.contains(6)) ||
            (player1.contains(7) && player1.contains(8) && player1.contains(9)) ||
            (player1.contains(1) && player1.contains(4) && player1.contains(7)) ||
            (player1.contains(2) && player1.contains(5) && player1.contains(8)) ||
            (player1.contains(3) && player1.contains(6) && player1.contains(9)) ||
            (player1.contains(1) && player1.contains(5) && player1.contains(9)) ||
            (player1.contains(3) && player1.contains(5) && player1.contains(7))
        ) {
            player1Score += 1
            buttonDisable()
            disableReset()
            if (singleUser)
                Handler().postDelayed(Runnable {
                    winDialog(null, mode)
                    loadBannerAd()
                    val totalWins = sharedPreferences.getInt("totalWins", 0)
                    val easyWin = sharedPreferences.getInt("easyWin", 0)
                    val mediumWin = sharedPreferences.getInt("mediumWin", 0)
                    val hardWin = sharedPreferences.getInt("hardWin", 0)
                    sharedPreferences.edit().putInt("totalWins", totalWins + 1).apply()
                    when (mode) {
                        1 -> sharedPreferences.edit().putInt("easyWin", easyWin + 1).apply()
                        2 -> sharedPreferences.edit().putInt("mediumWin", mediumWin + 1).apply()
                        3 -> sharedPreferences.edit().putInt("hardWin", hardWin + 1).apply()
                    }

                }, 400)
            if (!singleUser)
                Handler().postDelayed(Runnable { winDialog(namePlayer1, mode);loadBannerAd() }, 400)
            return 1
        } else if (
            (player2.contains(1) && player2.contains(2) && player2.contains(3)) ||
            (player2.contains(4) && player2.contains(5) && player2.contains(6)) ||
            (player2.contains(7) && player2.contains(8) && player2.contains(9)) ||
            (player2.contains(1) && player2.contains(4) && player2.contains(7)) ||
            (player2.contains(2) && player2.contains(5) && player2.contains(8)) ||
            (player2.contains(3) && player2.contains(6) && player2.contains(9)) ||
            (player2.contains(1) && player2.contains(5) && player2.contains(9)) ||
            (player2.contains(3) && player2.contains(5) && player2.contains(7))
        ) {
            player2Score += 1
            buttonDisable()
            disableReset()
            if (singleUser)
                Handler().postDelayed(Runnable {
                    loseDialog(mode)
                    loadBannerAd()
                    val totalLosses = sharedPreferences.getInt("totalLosses", 0)
                    val easyLoss = sharedPreferences.getInt("easyLoss", 0)
                    val mediumLoss = sharedPreferences.getInt("mediumLoss", 0)
                    val hardLoss = sharedPreferences.getInt("hardLoss", 0)
                    sharedPreferences.edit().putInt("totalLosses", totalLosses + 1).apply()
                    when (mode) {
                        1 -> sharedPreferences.edit().putInt("easyLoss", easyLoss + 1).apply()
                        2 -> sharedPreferences.edit().putInt("mediumLoss", mediumLoss + 1).apply()
                        3 -> sharedPreferences.edit().putInt("hardLoss", hardLoss + 1).apply()
                    }
                }, 400)
            if (!singleUser)
                Handler().postDelayed(Runnable { winDialog(namePlayer2);loadBannerAd() }, 400)
            return 1
        } else if (
            emptyCells.contains(1) && emptyCells.contains(2) && emptyCells.contains(3) &&
            emptyCells.contains(4) && emptyCells.contains(5) && emptyCells.contains(6) &&
            emptyCells.contains(7) && emptyCells.contains(8) && emptyCells.contains(9)
        ) {
            buttonDisable()
            disableReset()
            Handler().postDelayed(Runnable { drawDialog(mode);loadBannerAd() }, 400)
            return 1
        }
        return 0
    }

    private fun robotButton(button: ImageButton, cell: Int, mode: Int) {
        button.setImageResource(circleImage)
        emptyCells.add(cell)
        player2.add(cell)
        button.isEnabled = false
        val checkWinner = checkWinner(mode)
        if (checkWinner == 1) {
            Handler().postDelayed(Runnable { reset(mode) }, 300)
        }
    }

    private fun reset(mode: Int = 0) {
        vibrate(100)
        user1txt.setTextColor(resources.getColor(R.color.active_user))
        user2txt.setTextColor(resources.getColor(R.color.white))
        player1.clear()
        player2.clear()
        emptyCells.clear()
        activeUser = 1
        for (i in 1..9) {
            val buttonSelected: ImageButton = when (i) {
                1 -> button1
                2 -> button2
                3 -> button3
                4 -> button4
                5 -> button5
                6 -> button6
                7 -> button7
                8 -> button8
                9 -> button9
                else -> {
                    button1
                }
            }
            buttonSelected.isEnabled = true
            buttonSelected.setImageResource(R.color.white)
            score1.text = "$player1Score"
            score2.text = "$player2Score"
        }
    }

    private fun buttonDisable() {
        for (i in 1..9) {
            val buttonSelected = when (i) {
                1 -> button1
                2 -> button2
                3 -> button3
                4 -> button4
                5 -> button5
                6 -> button6
                7 -> button7
                8 -> button8
                9 -> button9
                else -> {
                    button1
                }
            }
            if (buttonSelected.isEnabled == true) {
                button1.isEnabled = false
            }
        }
    }

    private fun disableReset() {
        resetBtn.isEnabled = false
        Handler().postDelayed(Runnable { resetBtn.isEnabled = true }, 2200)
    }

    private fun winDialog(playerName: String?, mode: Int = 0) {
        if (mode != 0) {
            val availableCoins = sharedPreferences.getString("availableCoins", "0")?.toInt()
            val easyTotal = sharedPreferences.getInt("easyTotal", 0)
            val mediumTotal = sharedPreferences.getInt("mediumTotal", 0)
            val hardTotal = sharedPreferences.getInt("hardTotal", 0)
            when (mode) {
                1 -> {
                    sharedPreferences.edit().putInt("easyTotal", easyTotal + 1).apply()
                    sharedPreferences.edit()
                        .putString("availableCoins", (availableCoins!! + 20).toString()).apply()
                    val availableCoins = sharedPreferences.getString("availableCoins", "0")?.toInt()
                    if (availableCoins!! < 10) {
                        Toast.makeText(
                            this@SinglePlayingBoard,
                            "Insufficient coins",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                }
                2 -> {
                    sharedPreferences.edit().putInt("mediumTotal", mediumTotal + 1).apply()
                    sharedPreferences.edit()
                        .putString("availableCoins", (availableCoins!! + 60).toString()).apply()
                    val availableCoins = sharedPreferences.getString("availableCoins", "0")?.toInt()
                    if (availableCoins!! < 30) {
                        Toast.makeText(
                            this@SinglePlayingBoard,
                            "Insufficient coins",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                }
                3 -> {
                    sharedPreferences.edit().putInt("hardTotal", hardTotal + 1).apply()
                    sharedPreferences.edit()
                        .putString("availableCoins", (availableCoins!! + 160).toString()).apply()
                    val availableCoins = sharedPreferences.getString("availableCoins", "0")?.toInt()
                    if (availableCoins!! < 80) {
                        Toast.makeText(
                            this@SinglePlayingBoard,
                            "Insufficient coins",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                }
            }
        }
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.winner_dialog_layout)
        dialog.setCancelable(false)
        val txt1: TextView = dialog.findViewById(R.id.txtOther1)
        val btnExit: ImageButton = dialog.findViewById(R.id.btnExit)
        val btnPlay: ImageButton = dialog.findViewById(R.id.btnPlayAgain)
        btnExit.setOnClickListener {
            showInterAd("exit", dialog)
        }
        btnPlay.setOnClickListener {
            showInterAd("play", dialog)
        }
        if (playerName == null) {
            dialog.show()
        } else {
            txt1.text = "$playerName wins the Game !!"
            dialog.show()
        }
    }

    fun loseDialog(mode: Int = 0) {
        if (mode != 0) {
            val availableCoins = sharedPreferences.getString("availableCoins", "0")?.toInt()
            val easyTotal = sharedPreferences.getInt("easyTotal", 0)
            val mediumTotal = sharedPreferences.getInt("mediumTotal", 0)
            val hardTotal = sharedPreferences.getInt("hardTotal", 0)
            when (mode) {
                1 -> {
                    sharedPreferences.edit().putInt("easyTotal", easyTotal + 1).apply()
                    sharedPreferences.edit()
                        .putString("availableCoins", (availableCoins!! - 10).toString()).apply()
                    val availableCoins = sharedPreferences.getString("availableCoins", "0")?.toInt()
                    if (availableCoins!! < 10) {
                        Toast.makeText(
                            this@SinglePlayingBoard,
                            "Insufficient coins",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                }
                2 -> {
                    sharedPreferences.edit().putInt("mediumTotal", mediumTotal + 1).apply()
                    sharedPreferences.edit()
                        .putString("availableCoins", (availableCoins!! - 30).toString()).apply()
                    val availableCoins = sharedPreferences.getString("availableCoins", "0")?.toInt()
                    if (availableCoins!! < 30) {
                        Toast.makeText(
                            this@SinglePlayingBoard,
                            "Insufficient coins",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                }
                3 -> {
                    sharedPreferences.edit().putInt("hardTotal", hardTotal + 1).apply()
                    sharedPreferences.edit()
                        .putString("availableCoins", (availableCoins!! - 80).toString()).apply()
                    val availableCoins = sharedPreferences.getString("availableCoins", "0")?.toInt()
                    if (availableCoins!! < 80) {
                        Toast.makeText(
                            this@SinglePlayingBoard,
                            "Insufficient coins",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                }
            }
        }
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.lose_dialog_layout)
        dialog.setCancelable(false)
        val btnExit: ImageButton = dialog.findViewById(R.id.btnExit)
        val btnPlay: ImageButton = dialog.findViewById(R.id.btnPlayAgain)
        btnExit.setOnClickListener {
            showInterAd("exit", dialog)
        }
        btnPlay.setOnClickListener {
            showInterAd("play", dialog)
        }
        dialog.show()
    }

    fun drawDialog(mode: Int = 0) {
        if (mode != 0) {
            val easyTotal = sharedPreferences.getInt("easyTotal", 0)
            val mediumTotal = sharedPreferences.getInt("mediumTotal", 0)
            val hardTotal = sharedPreferences.getInt("hardTotal", 0)
            when (mode) {
                1 -> sharedPreferences.edit().putInt("easyTotal", easyTotal + 1).apply()
                2 -> sharedPreferences.edit().putInt("mediumTotal", mediumTotal + 1).apply()
                3 -> sharedPreferences.edit().putInt("hardTotal", hardTotal + 1).apply()
            }
        }
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.draw_layout)
        dialog.setCancelable(false)
        val btnExit: ImageButton = dialog.findViewById(R.id.btnExit)
        val btnPlay: ImageButton = dialog.findViewById(R.id.btnPlayAgain)
        btnExit.setOnClickListener {
            showInterAd("exit", dialog)
        }
        btnPlay.setOnClickListener {
            showInterAd("play", dialog)
        }
        dialog.show()
    }

    fun tapSound() {
        if (audioPreference) {
            val tapSound = MediaPlayer.create(this, R.raw.tap)
            tapSound.start()
            Handler().postDelayed(Runnable { tapSound.release() }, 500)
        }
    }

    private fun playBackgroundMusic() {
        music = MediaPlayer.create(this, R.raw.background_music)
        if (musicPreference) {
            music.isLooping = true
            music.start()
        }
    }

    fun stopBackgroundMusic() {
        music.release()
    }

    fun loadBannerAd() {
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

    private fun loadInterAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            "ca-app-pub-5528158197168162/7144146518",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                }
            })
    }

    private fun showInterAd(click: String, dialog: Dialog) {
        if (click == "exit") {
            if (mInterstitialAd != null) {
                mInterstitialAd?.show(this@SinglePlayingBoard)
            } else {
                dialog.dismiss()
                finish()
            }

            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

                override fun onAdDismissedFullScreenContent() {
                    dialog.dismiss()
                    finish()
                    mInterstitialAd = null

                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    dialog.dismiss()
                    finish()
                    mInterstitialAd = null
                }

            }
        }
        if (click == "play") {
            if (check % 3 == 0) {
                check++
                if (mInterstitialAd != null) {
                    mInterstitialAd?.show(this@SinglePlayingBoard)
                } else {
                    dialog.dismiss()
                }
                mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        loadInterAd()
                        dialog.dismiss()
                        playBackgroundMusic()
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        dialog.dismiss()
                        playBackgroundMusic()
                        mInterstitialAd = null
                    }

                }
            } else {
                dialog.dismiss()
                playBackgroundMusic()
                check++
            }
        }
    }

    private fun vibrate(time: Long) {
        if (vibratePreferences) {
            val vibrate = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrate.vibrate(time)
        }
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

    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            stopBackgroundMusic()
            super.onBackPressed()
        }
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Tap again to exit.", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            doubleBackToExitPressedOnce = false
        }, 2000)
    }
}
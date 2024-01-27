package com.navneet.tictactoe.activity

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
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
import com.google.firebase.database.*
import com.navneet.tictactoe.R
import com.navneet.tictactoe.util.ConnectionManager


class OnlinePlayingActivity : AppCompatActivity() {
    //Declaring layout variables
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
    lateinit var user1txt: TextView
    lateinit var user2txt: TextView
    lateinit var user1Img: ImageView
    lateinit var user2Img: ImageView
    lateinit var score1: TextView
    lateinit var score2: TextView
    lateinit var coinsValue: TextView

    //Declaring progressDialog
    lateinit var progressDialog: ProgressDialog

    //Declaring MediaPlayer
    lateinit var music: MediaPlayer

    //Declaring sharedPreference file and preferences variable
    lateinit var sharedPreferences: SharedPreferences
    var vibratePreferences = true
    var musicPreference = true
    var audioPreference = true

    //Declaring Ads variables
    lateinit var loseAdView: AdView
    lateinit var mAdView: AdView
    private var mInterstitialAd: InterstitialAd? = null

    //Declaring database reference and eventListeners
    lateinit var turnsEventListener: ValueEventListener
    lateinit var wonEventListener: ValueEventListener
    val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance()
            .getReferenceFromUrl("https://tic-tac-toe-afabf-default-rtdb.firebaseio.com/")

    //Declaring file variables
    var opponentUniqueId = "0"
    var playerUniqueId = "0"
    var player1Id = "0"
    var playerTurn = ""
    var player1Score = 0
    var player2Score = 0
    var matchCoins = 100
    private val doneBoxes = arrayListOf<String>()
    private val combinationList = arrayListOf<List<Int>>()
    private var boxesSelectedBy = arrayListOf("", "", "", "", "", "", "", "", "")
    var winOnceCount = 1
    var lossOnceCount = 1

    var crossImage=0
    var circleImage=0
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_playing)

        //Setting Status and Navigation bar's
        this.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        val window: Window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.WHITE

        //Initializing sharedPreference file
        sharedPreferences =
            this.getSharedPreferences("User Preferences", Context.MODE_PRIVATE)

        //Initializing layout variables
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
        user1Img = findViewById(R.id.user1Img)
        user2Img = findViewById(R.id.user2Img)
        coinsValue = findViewById(R.id.txtCoinsVal)

        resetBtn.visibility = View.GONE
        score1.text = player1Score.toString()
        score2.text = player2Score.toString()
        playerUniqueId = intent.getStringExtra("playerId").toString()

        //Initializing and showing progressDialog
        progressDialog = ProgressDialog(this@OnlinePlayingActivity)
        progressDialog.setMessage("wait for opponent...")
        progressDialog.setCancelable(false)
        progressDialog.setButton(
            DialogInterface.BUTTON_NEGATIVE, "Leave"
        ) { dialog, which ->
            finish()
        }
        progressDialog.show()

        //Generating winning possibilities
        combinationList.add(arrayListOf(1, 2, 3))
        combinationList.add(arrayListOf(4, 5, 6))
        combinationList.add(arrayListOf(7, 8, 9))
        combinationList.add(arrayListOf(1, 4, 7))
        combinationList.add(arrayListOf(2, 5, 8))
        combinationList.add(arrayListOf(3, 6, 9))
        combinationList.add(arrayListOf(1, 5, 9))
        combinationList.add(arrayListOf(3, 5, 7))

        //Fetching preferences
        musicPreference = sharedPreferences.getBoolean("music", true)
        vibratePreferences = sharedPreferences.getBoolean("vibration", true)
        audioPreference = sharedPreferences.getBoolean("audio", true)
        crossImage=sharedPreferences.getInt("CrossPreference",R.drawable.cross)
        circleImage=sharedPreferences.getInt("NoughtPreference",R.drawable.circle)

        //Loading Interstitial Ad
        loadInterAd()

        if (!checkNetwork()) {
            val inflater = layoutInflater
            val layout = inflater.inflate(R.layout.internet_toast, null)
            val toast = Toast(this@OnlinePlayingActivity)
            toast.duration = Toast.LENGTH_SHORT
            toast.view = layout
            toast.show()
        }

        FirebaseDatabase.getInstance(url).reference.child("codes").child(code)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val playerCount = snapshot.childrenCount.toInt()
                    if (playerCount == 2) {
                        progressDialog.dismiss()
                        FirebaseDatabase.getInstance(url).reference.child("coins").child(code)
                            .child("start")
                            .setValue(1)
                        for (players: DataSnapshot in snapshot.children) {
                            val getPlayerUniqueId = players.key!!
                            if (getPlayerUniqueId == playerUniqueId) {
                                playerTurn = playerUniqueId
                                player1Id = getPlayerUniqueId
                                applyPlayerTurn(playerTurn)
                                user1txt.text = players.child("player_name").value.toString()
                                val getPlayerImage: Int =
                                    players.child("playerImg").value.toString().toInt()
                                user1Img.setImageResource(getPlayerImage)
                                progressDialog.dismiss()
                                databaseReference.child("turns").child(code)
                                    .addValueEventListener(turnsEventListener)
                                databaseReference.child("won").child(code)
                                    .addValueEventListener(wonEventListener)
                            } else {
                                opponentUniqueId = getPlayerUniqueId
                                playerTurn = opponentUniqueId
                                applyPlayerTurn(playerTurn)
                                progressDialog.dismiss()
                                user2txt.text = players.child("player_name").value.toString()
                                val getOpponentImage: Int? =
                                    players.child("playerImg").getValue(Int::class.java)
                                if (getOpponentImage != null)
                                    user2Img.setImageResource(getOpponentImage)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        //Coins event listener


        FirebaseDatabase.getInstance(url).reference.child("coins").child(code)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChildren()) {
                        val getCoinsValue = snapshot.child("coinValue").value.toString()
                        if (getCoinsValue != null)
                            matchCoins = getCoinsValue.toInt()
                        coinsValue.text = matchCoins.toString()
                        val availableCoins: Int =
                            sharedPreferences.getString("availableCoins", "0").toString().toInt()
                        if (availableCoins < matchCoins) {
                            finish()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        //Score event listener
        FirebaseDatabase.getInstance(url).reference.child("scores").child(code)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (players: DataSnapshot in snapshot.children) {
                        val playerId = players.key
                        if (playerId == playerUniqueId) {
                            score1.text = players.value.toString()
                        } else {
                            score2.text = players.value.toString()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

        //User left game event listener
        FirebaseDatabase.getInstance(url).reference.child("user_leaves").child(code)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (players: DataSnapshot in snapshot.children) {
                        val playerId = players.key
                        if (playerId != player1Id) {
                            Toast.makeText(
                                this@OnlinePlayingActivity,
                                "Opponent left the game",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        //Turns event listener
        turnsEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot: DataSnapshot in snapshot.children) {
                    if (dataSnapshot.childrenCount.toInt() == 2) {
                        val getBoxPosition =
                            dataSnapshot.child("box_position").getValue(String::class.java)?.toInt()
                        val getPlayerId =
                            dataSnapshot.child("player_id").getValue(String::class.java)
                        if (!doneBoxes.contains(getBoxPosition.toString())) {
                            doneBoxes.add(getBoxPosition.toString())
                            when (getBoxPosition) {
                                1 -> selectBox(button1, getBoxPosition, getPlayerId!!)
                                2 -> selectBox(button2, getBoxPosition, getPlayerId!!)
                                3 -> selectBox(button3, getBoxPosition, getPlayerId!!)
                                4 -> selectBox(button4, getBoxPosition, getPlayerId!!)
                                5 -> selectBox(button5, getBoxPosition, getPlayerId!!)
                                6 -> selectBox(button6, getBoxPosition, getPlayerId!!)
                                7 -> selectBox(button7, getBoxPosition, getPlayerId!!)
                                8 -> selectBox(button8, getBoxPosition, getPlayerId!!)
                                9 -> selectBox(button9, getBoxPosition, getPlayerId!!)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }
        //Winning user event listener

        wonEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild("player_id")) {
                    val getWinPlayerId: String? =
                        snapshot.child("player_id").getValue(String::class.java)
                    if (getWinPlayerId == player1Id) {
                        if (winOnceCount == 1) {
                            winDialog()
                            val totalWins = sharedPreferences.getInt("totalWins", 0)
                            val friendsWin = sharedPreferences.getInt("friendsWin", 0)
                            sharedPreferences.edit().putInt("totalWins", totalWins + 1).apply()
                            sharedPreferences.edit().putInt("friendsWin", friendsWin + 1).apply()
                            val availableCoins: Int =
                                sharedPreferences.getString("availableCoins", "0").toString()
                                    .toInt()
                            sharedPreferences.edit()
                                .putString(
                                    "availableCoins",
                                    (availableCoins + matchCoins).toString()
                                )
                                .apply()
                            winOnceCount++
                        }
                    } else {
                        if (lossOnceCount == 1) {
                            loseDialog(user2txt.text.toString())
                            val totalLosses = sharedPreferences.getInt("totalLosses", 0)
                            val friendsLoss = sharedPreferences.getInt("friendsLoss", 0)
                            sharedPreferences.edit().putInt("totalLosses", totalLosses + 1).apply()
                            sharedPreferences.edit().putInt("friendsLoss", friendsLoss + 1).apply()
                            val availableCoins: Int =
                                sharedPreferences.getString("availableCoins", "0").toString()
                                    .toInt()
                            sharedPreferences.edit()
                                .putString(
                                    "availableCoins",
                                    (availableCoins - matchCoins).toString()
                                )
                                .apply()
                            lossOnceCount++
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }

        //implementing clickListener on buttons
        button1.setOnClickListener {
            if (!doneBoxes.contains("1") && playerTurn == playerUniqueId) {
                button1.setImageResource(crossImage)
                databaseReference.child("turns").child(code)
                    .child((doneBoxes.size + 1).toString()).child("box_position").setValue("1")
                databaseReference.child("turns").child(code)
                    .child((doneBoxes.size + 1).toString()).child("player_id")
                    .setValue(playerUniqueId)
                playerTurn = opponentUniqueId
            }
        }
        button2.setOnClickListener {
            if (!doneBoxes.contains("2") && playerTurn == playerUniqueId) {
                button2.setImageResource(crossImage)
                databaseReference.child("turns").child(code)
                    .child((doneBoxes.size + 1).toString()).child("box_position").setValue("2")
                databaseReference.child("turns").child(code)
                    .child((doneBoxes.size + 1).toString()).child("player_id")
                    .setValue(playerUniqueId)
                playerTurn = opponentUniqueId
            }
        }
        button3.setOnClickListener {
            if (!doneBoxes.contains("3") && playerTurn == playerUniqueId) {
                button3.setImageResource(crossImage)
                databaseReference.child("turns").child(code)
                    .child((doneBoxes.size + 1).toString()).child("box_position").setValue("3")
                databaseReference.child("turns").child(code)
                    .child((doneBoxes.size + 1).toString()).child("player_id")
                    .setValue(playerUniqueId)
                playerTurn = opponentUniqueId
            }
        }
        button4.setOnClickListener {
            if (!doneBoxes.contains("4") && playerTurn == playerUniqueId) {
                button4.setImageResource(crossImage)
                databaseReference.child("turns").child(code)
                    .child((doneBoxes.size + 1).toString()).child("box_position").setValue("4")
                databaseReference.child("turns").child(code)
                    .child((doneBoxes.size + 1).toString()).child("player_id")
                    .setValue(playerUniqueId)
                playerTurn = opponentUniqueId
            }
        }
        button5.setOnClickListener {
            if (!doneBoxes.contains("5") && playerTurn == playerUniqueId) {
                button5.setImageResource(crossImage)
                databaseReference.child("turns").child(code)
                    .child((doneBoxes.size + 1).toString()).child("box_position").setValue("5")
                databaseReference.child("turns").child(code)
                    .child((doneBoxes.size + 1).toString()).child("player_id")
                    .setValue(playerUniqueId)
                playerTurn = opponentUniqueId
            }
        }
        button6.setOnClickListener {
            if (!doneBoxes.contains("6") && playerTurn == playerUniqueId) {
                button6.setImageResource(crossImage)
                databaseReference.child("turns").child(code)
                    .child((doneBoxes.size + 1).toString()).child("box_position").setValue("6")
                databaseReference.child("turns").child(code)
                    .child((doneBoxes.size + 1).toString()).child("player_id")
                    .setValue(playerUniqueId)
                playerTurn = opponentUniqueId
            }
        }
        button7.setOnClickListener {
            if (!doneBoxes.contains("7") && playerTurn == playerUniqueId) {
                button7.setImageResource(crossImage)
                databaseReference.child("turns").child(code)
                    .child((doneBoxes.size + 1).toString()).child("box_position").setValue("7")
                databaseReference.child("turns").child(code)
                    .child((doneBoxes.size + 1).toString()).child("player_id")
                    .setValue(playerUniqueId)
                playerTurn = opponentUniqueId
            }
        }
        button8.setOnClickListener {
            if (!doneBoxes.contains("8") && playerTurn == playerUniqueId) {
                button8.setImageResource(crossImage)
                databaseReference.child("turns").child(code)
                    .child((doneBoxes.size + 1).toString()).child("box_position").setValue("8")
                databaseReference.child("turns").child(code)
                    .child((doneBoxes.size + 1).toString()).child("player_id")
                    .setValue(playerUniqueId)
                playerTurn = opponentUniqueId
            }
        }
        button9.setOnClickListener {
            if (!doneBoxes.contains("9") && playerTurn == playerUniqueId) {
                button9.setImageResource(crossImage)
                databaseReference.child("turns").child(code)
                    .child((doneBoxes.size + 1).toString()).child("box_position").setValue("9")
                databaseReference.child("turns").child(code)
                    .child((doneBoxes.size + 1).toString()).child("player_id")
                    .setValue(playerUniqueId)
                playerTurn = opponentUniqueId
            }
        }

    }

    //Function changing colour of text of username and displaying current user's turn
    private fun applyPlayerTurn(playerUniqueId2: String?) {
        if (playerUniqueId2 == playerUniqueId) {
            vibrate(40)
            tapSound()
            user1txt.setTextColor(resources.getColor(R.color.active_user))
            user2txt.setTextColor(resources.getColor(R.color.white))
        } else {
            vibrate(40)
            tapSound()
            user2txt.setTextColor(resources.getColor(R.color.active_user))
            user1txt.setTextColor(resources.getColor(R.color.white))
        }
    }

    //Function for displaying "X" || "O" according to user's turn
    private fun selectBox(
        button: ImageButton,
        selectedBoxPosition: Int,
        selectedByPlayer: String
    ) {
        boxesSelectedBy[selectedBoxPosition - 1] = selectedByPlayer
        playerTurn = if (selectedByPlayer == playerUniqueId) {
            button.setImageResource(crossImage)
            opponentUniqueId
        } else {
            button.setImageResource(circleImage)
            playerUniqueId
        }
        applyPlayerTurn(playerTurn)
        if (checkPlayerWin(selectedByPlayer)) {
            databaseReference.child("won").child(code).child("player_id")
                .setValue(selectedByPlayer)
            if (selectedByPlayer == player1Id) {
                player1Score++
            } else {
                player2Score++
            }
            updateScore(player1Score, player2Score)
        } else if (doneBoxes.size == 9) {
            drawDialog()
        }
    }

    //Function for checking if player is winning or not
    private fun checkPlayerWin(selectedByPlayer: String): Boolean {
        var isPlayerWon = false
        for (i in 0 until combinationList.size) {
            val combination: List<Int> = combinationList[i]
            if (boxesSelectedBy[combination[0] - 1] == selectedByPlayer && boxesSelectedBy[combination[1] - 1] == selectedByPlayer && boxesSelectedBy[combination[2] - 1] == selectedByPlayer
            ) {
                isPlayerWon = true
            }
        }
        return isPlayerWon
    }

    //Function removes all data of particular room code
    private fun removeCode() {
        FirebaseDatabase.getInstance(url).reference.child("codes").child(code).removeValue()
        FirebaseDatabase.getInstance(url).reference.child("turns").child(code).removeValue()
        FirebaseDatabase.getInstance(url).reference.child("scores").child(code).removeValue()
        FirebaseDatabase.getInstance(url).reference.child("won").child(code).removeValue()
        FirebaseDatabase.getInstance(url).reference.child("user_leaves").child(code).removeValue()
        FirebaseDatabase.getInstance(url).reference.child("coins").child(code).removeValue()
    }

    //Function updates current score to database
    private fun updateScore(score1: Int, score2: Int) {
        FirebaseDatabase.getInstance(url).reference.child("scores").child(code)
            .child(player1Id).setValue(score1)
        FirebaseDatabase.getInstance(url).reference.child("scores").child(code)
            .child(opponentUniqueId).setValue(score2)
    }

    //Function resets board and restart other round
    fun reset() {
        vibrate(80)
        doneBoxes.clear()
        boxesSelectedBy = arrayListOf("", "", "", "", "", "", "", "", "")
        applyPlayerTurn(playerTurn)
        winOnceCount = 1
        lossOnceCount = 1
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
        }
        FirebaseDatabase.getInstance(url).reference.child("turns").child(code)
            .removeValue()
        FirebaseDatabase.getInstance(url).reference.child("won").child(code).removeValue()
    }

    //Winner,Looser,Draw Dialog
    fun winDialog() {
        val friendsTotal = sharedPreferences.getInt("friendsTotal", 0)
        sharedPreferences.edit().putInt("friendsTotal", friendsTotal + 1).apply()
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.winner_dialog_layout)
        dialog.setCancelable(false)
        val txt1: TextView = dialog.findViewById(R.id.txtOther1)
        val btnExit: ImageButton = dialog.findViewById(R.id.btnExit)
        val btnPlay: ImageButton = dialog.findViewById(R.id.btnPlayAgain)
        btnExit.setOnClickListener {
            removeCode()
            showInterAd("exit", dialog)
        }
        btnPlay.setOnClickListener {
            showInterAd("play", dialog)
        }
        dialog.setCancelable(false)
        dialog.show()
    }

    fun loseDialog(opponentName: String) {
        val friendsTotal = sharedPreferences.getInt("friendsTotal", 0)
        sharedPreferences.edit().putInt("friendsTotal", friendsTotal + 1).apply()
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.lose_dialog_layout)
        dialog.setCancelable(false)
        val btnExit: ImageButton = dialog.findViewById(R.id.btnExit)
        val btnPlay: ImageButton = dialog.findViewById(R.id.btnPlayAgain)
        val text: TextView = dialog.findViewById(R.id.txtOther1)
        val imgTrophy: ImageView = dialog.findViewById<ImageView?>(R.id.imgTrophy)
        imgTrophy.visibility = View.GONE
        loseAdView = dialog.findViewById(R.id.adView)
        loseAdView.visibility = View.VISIBLE
        loadBannerAd()
        text.text = "$opponentName wins the game"
        btnExit.setOnClickListener {
            removeCode()
            showInterAd("exit", dialog)
        }
        btnPlay.setOnClickListener {
            showInterAd("play", dialog)
        }
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun drawDialog() {
        val friendsTotal = sharedPreferences.getInt("friendsTotal", 0)
        sharedPreferences.edit().putInt("friendsTotal", friendsTotal + 1).apply()
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.draw_layout)
        dialog.setCancelable(false)
        val btnExit: ImageButton = dialog.findViewById(R.id.btnExit)
        val btnPlay: ImageButton = dialog.findViewById(R.id.btnPlayAgain)
        btnExit.setOnClickListener {
            removeCode()
            showInterAd("exit", dialog)
        }
        btnPlay.setOnClickListener {
            showInterAd("play", dialog)
        }
        dialog.setCancelable(false)
        dialog.show()
    }

    //Function loads bannerAd
    private fun loadBannerAd() {
        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()

        mAdView.loadAd(adRequest)
        loseAdView.loadAd(adRequest)
        mAdView.adListener = object : AdListener() {
            override fun onAdClosed() {
                loadBannerAd()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                println(adError)
            }
        }
        loseAdView.adListener = object : AdListener() {
            override fun onAdClosed() {
                loadBannerAd()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                println(adError)
            }
        }
    }

    //Function loads Inter Ad
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

    //Function showing interstitial ad
    var count = 1
    fun showInterAd(click: String, dialog: Dialog) {
        if (click == "exit") {
            if (mInterstitialAd != null) {
                mInterstitialAd?.show(this@OnlinePlayingActivity)
            } else {
                dialog.dismiss()
                finish()
            }

            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

                override fun onAdDismissedFullScreenContent() {
                    dialog.dismiss()
                    mInterstitialAd = null
                    finish()
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    dialog.dismiss()
                    finish()
                    mInterstitialAd = null
                }

            }
        }
        if (click == "play") {
            count++
            val availableCoins: Int =
                sharedPreferences.getString("availableCoins", "0").toString()
                    .toInt()
            if (count % 4 == 0) {
                if (mInterstitialAd != null) {
                    mInterstitialAd?.show(this@OnlinePlayingActivity)
                } else {
                    dialog.dismiss()
                    reset()
                    if (availableCoins < matchCoins) {
                        finish()
                        Toast.makeText(
                            this@OnlinePlayingActivity,
                            "Insufficient coins, required $matchCoins coins",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
                mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        loadInterAd()
                        dialog.dismiss()
                        reset()
                        if (availableCoins < matchCoins) {
                            finish()
                            Toast.makeText(
                                this@OnlinePlayingActivity,
                                "Insufficient coins, required $matchCoins coins",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        dialog.dismiss()
                        mInterstitialAd = null
                        reset()
                        if (availableCoins < matchCoins) {
                            finish()
                            Toast.makeText(
                                this@OnlinePlayingActivity,
                                "Insufficient coins, required $matchCoins coins",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            } else {
                loadInterAd()
                reset()
                dialog.dismiss()
                if (availableCoins < matchCoins) {
                    finish()
                    Toast.makeText(
                        this@OnlinePlayingActivity,
                        "Insufficient coins, required $matchCoins coins",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    fun tapSound() {
        if (audioPreference) {
            val tapSound = MediaPlayer.create(this, R.raw.tap)
            tapSound.start()
            Handler().postDelayed(Runnable { tapSound.release() }, 500)
        }
    }

    fun playBackgroundMusic() {
        music = MediaPlayer.create(this, R.raw.background_music)
        if (musicPreference) {
            music.isLooping = true
            music.start()
        }
    }

    fun stopBackgroundMusic() {
        music.release()
    }

    fun vibrate(time: Long) {
        if (vibratePreferences) {
            val vibrate = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrate.vibrate(time)
        }
    }

    //activityLifecycle Function
    override fun onPause() {
        if (musicPreference)
            stopBackgroundMusic()
        super.onPause()
    }

    override fun onDestroy() {
        if (musicPreference)
            stopBackgroundMusic()
        FirebaseDatabase.getInstance(url).reference.child("user_leaves").child(code)
            .child(player1Id)
            .setValue(true)
        finish()
        removeCode()
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
            removeCode()
            finish()
        }
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Tap again to exit.", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            doubleBackToExitPressedOnce = false
        }, 2000)
    }

    fun checkNetwork(): Boolean {
        return ConnectionManager().checkConnectivity(this@OnlinePlayingActivity)
    }
}
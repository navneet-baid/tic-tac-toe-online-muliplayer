package com.navneet.tictactoe.activity

import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
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

class RandomPlayersActivity : AppCompatActivity() {

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

    lateinit var music: MediaPlayer

    private val combinationList = arrayListOf<List<Int>>()
    private val doneBoxes = arrayListOf<String>()
    private val boxesSelectedBy = arrayListOf<String>("", "", "", "", "", "", "", "", "")

    var opponentFound: Boolean = false
    var playerUniqueId: String = "0"
    var opponentUniqueId: String? = "0"
    var status = "matching"
    var playerTurn = ""
    var connectionId: String? = ""

    val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance()
            .getReferenceFromUrl("https://tic-tac-toe-afabf-default-rtdb.firebaseio.com/")
    lateinit var turnsEventListener: ValueEventListener
    lateinit var wonEventListener: ValueEventListener
    lateinit var userLeavesEventListener: ValueEventListener
    lateinit var sharedPreferences: SharedPreferences
    var vibratePreferences = true
    var musicPreference = true
    var audioPreference = true

    private var mInterstitialAd: InterstitialAd? = null
    lateinit var mAdView: AdView
    lateinit var loseAdView: AdView

    var crossImage=0
    var circleImage=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_random_players)
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
        user1txt = findViewById(R.id.user1txt)
        user2txt = findViewById(R.id.user2txt)
        user1Img = findViewById(R.id.user1Img)
        user2Img = findViewById(R.id.user2Img)

        sharedPreferences = this.getSharedPreferences("User Preferences", Context.MODE_PRIVATE)
        musicPreference = sharedPreferences.getBoolean("music", true)
        vibratePreferences = sharedPreferences.getBoolean("vibration", true)
        audioPreference = sharedPreferences.getBoolean("audio", true)
        crossImage=sharedPreferences.getInt("CrossPreference",R.drawable.cross)
        circleImage=sharedPreferences.getInt("NoughtPreference",R.drawable.circle)

        val getPlayerName = sharedPreferences.getString("player_name", "Player")
        val getPlayerImage = sharedPreferences.getInt("userProfilePicture", R.drawable.default_icon)



        loadInterAd()

        combinationList.add(arrayListOf(1, 2, 3))
        combinationList.add(arrayListOf(4, 5, 6))
        combinationList.add(arrayListOf(7, 8, 9))
        combinationList.add(arrayListOf(1, 4, 7))
        combinationList.add(arrayListOf(2, 5, 8))
        combinationList.add(arrayListOf(3, 6, 9))
        combinationList.add(arrayListOf(1, 5, 9))
        combinationList.add(arrayListOf(3, 5, 7))

        loadBannerAd()

        val progressDialog = ProgressDialog(this@RandomPlayersActivity)
        progressDialog.setMessage("Waiting for opponent...");
        progressDialog.setCancelable(false)
        progressDialog.setButton(
            DialogInterface.BUTTON_NEGATIVE, "Leave"
        ) { dialog, which ->
            finish()
        }
        progressDialog.show()

        playerUniqueId = (System.currentTimeMillis()).toString()
        user1txt.text = getPlayerName
        user1Img.setImageResource(getPlayerImage)

        if (!checkNetwork()) {
            val inflater = layoutInflater
            val layout = inflater.inflate(R.layout.internet_toast, null)
            val toast = Toast(this@RandomPlayersActivity)
            toast.duration = Toast.LENGTH_SHORT;
            toast.setView(layout)
            toast.show()
        }
        databaseReference.child("connections").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!opponentFound) {
                    if (snapshot.hasChildren()) {
                        for (connections: DataSnapshot in snapshot.children) {
                            val conId: String? = (connections.key)
                            val playersCount = connections.childrenCount.toInt()
                            if (status == "waiting") {
                                if (playersCount == 2) {
                                    playerTurn = playerUniqueId
                                    applyPlayerTurn(playerTurn)
                                    var playerFound = false
                                    for (players: DataSnapshot in connections.children) {
                                        val getPlayerUniqueId = players.key
                                        if (getPlayerUniqueId == playerUniqueId) {
                                            playerFound = true
                                            databaseReference.child("turns").child(connectionId!!)
                                                .addValueEventListener(turnsEventListener)
                                            databaseReference.child("won").child(connectionId!!)
                                                .addValueEventListener(wonEventListener)
                                            progressDialog.dismiss()
                                            databaseReference.child("connections")
                                                .removeEventListener(this)
                                        } else if (playerFound) {
                                            progressDialog.dismiss()
                                            val getOpponentPlayerName =
                                                players.child("player_name").getValue(
                                                    String::class.java
                                                )
                                            val getOpponentImg = players.child("player_img")
                                                .getValue(Int::class.java)
                                            opponentUniqueId = players.key
                                            user2txt.text = getOpponentPlayerName
                                            if (getOpponentImg != null)
                                                user2Img.setImageResource(getOpponentImg)
                                            connectionId = conId
                                            opponentFound = true
                                            databaseReference.child("turns").child(connectionId!!)
                                                .addValueEventListener(turnsEventListener)
                                            databaseReference.child("won").child(connectionId!!)
                                                .addValueEventListener(wonEventListener)
                                            databaseReference.child("user_leaves").child(connectionId!!)
                                                .addValueEventListener(userLeavesEventListener)
                                            progressDialog.dismiss()
                                            databaseReference.child("connections")
                                                .removeEventListener(this)
                                        }
                                    }
                                }
                            } else {
                                if (playersCount == 1) {
                                    connections.child(playerUniqueId)
                                        .child("player_name").ref.setValue(getPlayerName)
                                    for (players: DataSnapshot in connections.children) {
                                        val getOpponentName = players.child("player_name")
                                            .getValue(String::class.java)
                                        val getOpponentImg = players.child("player_img")
                                            .getValue(Int::class.java)
                                        opponentUniqueId = players.key
                                        playerTurn = opponentUniqueId!!
                                        applyPlayerTurn(playerTurn)
                                        user2txt.text = getOpponentName
                                        user2Img.setImageResource(getOpponentImg!!)
                                        connectionId = conId
                                        opponentFound = true
                                        databaseReference.child("turns").child(connectionId!!)
                                            .addValueEventListener(turnsEventListener)
                                        databaseReference.child("won").child(connectionId!!)
                                            .addValueEventListener(wonEventListener)
                                        databaseReference.child("user_leaves").child(connectionId!!)
                                            .addValueEventListener(userLeavesEventListener)
                                        progressDialog.dismiss()
                                        databaseReference.child("connections")
                                            .removeEventListener(this)
                                        break
                                    }
                                }
                            }
                        }
                        if (!opponentFound && (status != "waiting")) {
                            val connectionUniqueId = System.currentTimeMillis().toString()
                            snapshot.child(connectionUniqueId).child(playerUniqueId)
                                .child("player_name").ref.setValue(getPlayerName)
                            snapshot.child(connectionUniqueId).child(playerUniqueId)
                                .child("player_img").ref.setValue(getPlayerImage)
                            status = "waiting"
                        }
                    } else if (!opponentFound && (status != "waiting")) {
                        val connectionUniqueId = System.currentTimeMillis().toString()
                        snapshot.child(connectionUniqueId).child(playerUniqueId)
                            .child("player_name").ref.setValue(getPlayerName)
                        snapshot.child(connectionUniqueId).child(playerUniqueId)
                            .child("player_img").ref.setValue(getPlayerImage)
                        status = "waiting"
                    }
                } else {
                    val connectionUniqueId = System.currentTimeMillis().toString()
                    snapshot.child(connectionUniqueId).child(playerUniqueId)
                        .child("player_name").ref.setValue(getPlayerName)
                    snapshot.child(connectionUniqueId).child(playerUniqueId)
                        .child("player_img").ref.setValue(getPlayerImage)
                    status = "waiting"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        userLeavesEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (players: DataSnapshot in snapshot.children) {
                    val playerId = players.key
                    if (playerId != playerUniqueId) {
                        Toast.makeText(
                            this@RandomPlayersActivity,
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
        }

        turnsEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot: DataSnapshot in snapshot.children) {
                    if (dataSnapshot.childrenCount.toInt() == 2) {
                        val getBoxPosition =
                            dataSnapshot.child("box_position").getValue(String::class.java)?.toInt()
                        val getPlayerId =
                            dataSnapshot.child("player_id").getValue(String::class.java)
                        if (!doneBoxes.contains(getBoxPosition.toString()) && getBoxPosition != null) {
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

        wonEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild("player_id")) {
                    val getWinPlayerId: String? =
                        snapshot.child("player_id").getValue(String::class.java)
                    val dialog = AlertDialog.Builder(this@RandomPlayersActivity)
                    if (getWinPlayerId == playerUniqueId) {
                        winDialog()
                        val totalWins = sharedPreferences.getInt("totalWins", 0)
                        val randomWin = sharedPreferences.getInt("randomWin", 0)
                        sharedPreferences.edit().putInt("totalWins", totalWins + 1).apply()
                        sharedPreferences.edit().putInt("randomWin", randomWin + 1).apply()
                    } else {
                        loseDialog(user2txt.text.toString())
                        val randomLoss = sharedPreferences.getInt("randomLoss", 0)
                        val totalLosses = sharedPreferences.getInt("totalLosses", 0)
                        sharedPreferences.edit().putInt("totalLosses", totalLosses + 1).apply()
                        sharedPreferences.edit().putInt("randomLoss", randomLoss + 1).apply()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }

        button1.setOnClickListener {
            if (checkNetwork()) {
                if (!doneBoxes.contains("1") && playerTurn == playerUniqueId) {
                    button1.setImageResource(crossImage)
                    databaseReference.child("turns").child(connectionId!!)
                        .child((doneBoxes.size + 1).toString()).child("box_position").setValue("1")
                    databaseReference.child("turns").child(connectionId!!)
                        .child((doneBoxes.size + 1).toString()).child("player_id")
                        .setValue(playerUniqueId)
                    playerTurn = opponentUniqueId!!
                }
            } else
                internetToast()
        }
        button2.setOnClickListener {
            if (checkNetwork()) {
                if (!doneBoxes.contains("2") && playerTurn == playerUniqueId) {
                    button2.setImageResource(crossImage)
                    databaseReference.child("turns").child(connectionId!!)
                        .child((doneBoxes.size + 1).toString()).child("box_position").setValue("2")
                    databaseReference.child("turns").child(connectionId!!)
                        .child((doneBoxes.size + 1).toString()).child("player_id")
                        .setValue(playerUniqueId)
                    playerTurn = opponentUniqueId!!
                }
            } else
                internetToast()
        }
        button3.setOnClickListener {
            if (checkNetwork()) {
                if (!doneBoxes.contains("3") && playerTurn == playerUniqueId) {
                    button3.setImageResource(crossImage)
                    databaseReference.child("turns").child(connectionId!!)
                        .child((doneBoxes.size + 1).toString()).child("box_position").setValue("3")
                    databaseReference.child("turns").child(connectionId!!)
                        .child((doneBoxes.size + 1).toString()).child("player_id")
                        .setValue(playerUniqueId)
                    playerTurn = opponentUniqueId!!
                }
            } else
                internetToast()
        }
        button4.setOnClickListener {
            if (checkNetwork()) {
                if (!doneBoxes.contains("4") && playerTurn == playerUniqueId) {
                    button4.setImageResource(crossImage)
                    databaseReference.child("turns").child(connectionId!!)
                        .child((doneBoxes.size + 1).toString()).child("box_position").setValue("4")
                    databaseReference.child("turns").child(connectionId!!)
                        .child((doneBoxes.size + 1).toString()).child("player_id")
                        .setValue(playerUniqueId)
                    playerTurn = opponentUniqueId!!
                }
            } else
                internetToast()
        }
        button5.setOnClickListener {
            if (checkNetwork()) {
                if (!doneBoxes.contains("5") && playerTurn == playerUniqueId) {
                    button5.setImageResource(crossImage)
                    databaseReference.child("turns").child(connectionId!!)
                        .child((doneBoxes.size + 1).toString()).child("box_position").setValue("5")
                    databaseReference.child("turns").child(connectionId!!)
                        .child((doneBoxes.size + 1).toString()).child("player_id")
                        .setValue(playerUniqueId)
                    playerTurn = opponentUniqueId!!
                }
            } else
                internetToast()
        }
        button6.setOnClickListener {
            if (checkNetwork()) {
                if (!doneBoxes.contains("6") && playerTurn == playerUniqueId) {
                    button6.setImageResource(crossImage)
                    databaseReference.child("turns").child(connectionId!!)
                        .child((doneBoxes.size + 1).toString()).child("box_position").setValue("6")
                    databaseReference.child("turns").child(connectionId!!)
                        .child((doneBoxes.size + 1).toString()).child("player_id")
                        .setValue(playerUniqueId)
                    playerTurn = opponentUniqueId!!
                }
            } else
                internetToast()
        }
        button7.setOnClickListener {
            if (checkNetwork()) {
                if (!doneBoxes.contains("7") && playerTurn == playerUniqueId) {
                    button7.setImageResource(crossImage)
                    databaseReference.child("turns").child(connectionId!!)
                        .child((doneBoxes.size + 1).toString()).child("box_position").setValue("7")
                    databaseReference.child("turns").child(connectionId!!)
                        .child((doneBoxes.size + 1).toString()).child("player_id")
                        .setValue(playerUniqueId)
                    playerTurn = opponentUniqueId!!
                }
            } else
                internetToast()
        }
        button8.setOnClickListener {
            if (checkNetwork()) {
                if (!doneBoxes.contains("8") && playerTurn == playerUniqueId) {
                    button8.setImageResource(crossImage)
                    databaseReference.child("turns").child(connectionId!!)
                        .child((doneBoxes.size + 1).toString()).child("box_position").setValue("8")
                    databaseReference.child("turns").child(connectionId!!)
                        .child((doneBoxes.size + 1).toString()).child("player_id")
                        .setValue(playerUniqueId)
                    playerTurn = opponentUniqueId!!
                }
            } else
                internetToast()
        }
        button9.setOnClickListener {
            if (checkNetwork()) {
                if (!doneBoxes.contains("9") && playerTurn == playerUniqueId) {
                    button9.setImageResource(crossImage)
                    databaseReference.child("turns").child(connectionId!!)
                        .child((doneBoxes.size + 1).toString()).child("box_position").setValue("9")
                    databaseReference.child("turns").child(connectionId!!)
                        .child((doneBoxes.size + 1).toString()).child("player_id")
                        .setValue(playerUniqueId)
                    playerTurn = opponentUniqueId!!
                }
            } else
                internetToast()
        }

    }


    private fun applyPlayerTurn(playerUniqueId2: String) {
        if (playerUniqueId2 == playerUniqueId) {
            tapSound()
            vibrate(40)
            user1txt.setTextColor(resources.getColor(R.color.active_user))
            user2txt.setTextColor(resources.getColor(R.color.white))
        } else {
            tapSound()
            vibrate(40)
            user2txt.setTextColor(resources.getColor(R.color.active_user))
            user1txt.setTextColor(resources.getColor(R.color.white))
        }
    }

    private fun selectBox(
        button: ImageButton,
        selectedBoxPosition: Int,
        selectedByPlayer: String
    ) {
        boxesSelectedBy[selectedBoxPosition - 1] = selectedByPlayer
        playerTurn = if (selectedByPlayer == playerUniqueId) {
            button.setImageResource(crossImage)
            opponentUniqueId!!
        } else {
            button.setImageResource(circleImage)
            playerUniqueId
        }
        applyPlayerTurn(playerTurn)
        if (checkPlayerWin(selectedByPlayer)) {
            databaseReference.child("won").child(connectionId!!).child("player_id")
                .setValue(selectedByPlayer)
        } else if (doneBoxes.size == 9) {
            drawDialog()
        }
    }

    private fun checkPlayerWin(playerId: String): Boolean {
        var isPlayerWon = false
        for (i in 0 until combinationList.size) {
            val combination: List<Int> = combinationList[i]
            if (boxesSelectedBy[combination[0] - 1] == playerId && boxesSelectedBy[combination[1] - 1] == playerId && boxesSelectedBy[combination[2] - 1] == playerId
            ) {
                isPlayerWon = true
            }
        }
        return isPlayerWon
    }

    fun removeCode() {
        FirebaseDatabase.getInstance(url).reference.child("connections").child(connectionId!!)
            .removeValue()
        FirebaseDatabase.getInstance(url).reference.child("turns").child(connectionId!!)
            .removeValue()
        FirebaseDatabase.getInstance(url).reference.child("won").child(connectionId!!).removeValue()
        FirebaseDatabase.getInstance(url).reference.child("user_leaves").child(connectionId!!)
            .removeValue()
    }

    //Winner,Looser,Draw Dialog
    fun winDialog() {
        val randomTotal = sharedPreferences.getInt("randomTotal", 0)
        sharedPreferences.edit().putInt("randomTotal", randomTotal + 1).apply()
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.winner_dialog_layout)
        dialog.setCancelable(false)
        val btnExit: ImageButton = dialog.findViewById(R.id.btnExit)
        val btnPlay: ImageButton = dialog.findViewById(R.id.btnPlayAgain)
        val text2: TextView = dialog.findViewById(R.id.txtOther2)
        text2.text = "Do you want to play new match?"
        btnExit.setOnClickListener {
            removeCode()
            showInterAd("exit", dialog)
            finish()
        }
        btnPlay.setOnClickListener {
            showInterAd("play", dialog)
            finish()
        }
        dialog.setCancelable(false)
        dialog.show()
    }

    fun loseDialog(opponentName: String) {
        val randomTotal = sharedPreferences.getInt("randomTotal", 0)
        sharedPreferences.edit().putInt("randomTotal", randomTotal + 1).apply()
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.lose_dialog_layout)
        dialog.setCancelable(false)
        val btnExit: ImageButton = dialog.findViewById(R.id.btnExit)
        val btnPlay: ImageButton = dialog.findViewById(R.id.btnPlayAgain)
        val text: TextView = dialog.findViewById(R.id.txtOther1)
        val text2: TextView = dialog.findViewById(R.id.txtOther2)
        val imgTrophy: ImageView = dialog.findViewById<ImageView?>(R.id.imgTrophy)
        imgTrophy.visibility = View.GONE
        loseAdView = dialog.findViewById(R.id.adView)
        loseAdView.visibility = View.VISIBLE
        loadLoseAd()
        text.text = "$opponentName wins the game"
        text2.text = "Do you want to play new match?"
        btnExit.setOnClickListener {
            removeCode()
            showInterAd("exit", dialog)
            finish()
        }
        btnPlay.setOnClickListener {
            showInterAd("play", dialog)
            finish()
        }
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun drawDialog() {
        val randomTotal = sharedPreferences.getInt("randomTotal", 0)
        sharedPreferences.edit().putInt("randomTotal", randomTotal + 1).apply()
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.draw_layout)
        dialog.setCancelable(false)
        val btnExit: ImageButton = dialog.findViewById(R.id.btnExit)
        val btnPlay: ImageButton = dialog.findViewById(R.id.btnPlayAgain)
        val text2: TextView = dialog.findViewById(R.id.txtOther2)
        text2.text = "Do you want to play new match?"
        btnExit.setOnClickListener {
            removeCode()
            showInterAd("exit", dialog)
            finish()

        }
        btnPlay.setOnClickListener {
            showInterAd("play", dialog)
            finish()
        }
        dialog.setCancelable(false)
        dialog.show()
    }

    var count = 1
    fun showInterAd(click: String, dialog: Dialog) {
        if (click == "exit") {
            if (mInterstitialAd != null) {
                mInterstitialAd?.show(this@RandomPlayersActivity)
            } else {
                dialog.dismiss()
                finish()
            }

            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

                override fun onAdDismissedFullScreenContent() {
                    loadInterAd()
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
            if (count % 6 == 0) {
                if (mInterstitialAd != null) {
                    mInterstitialAd?.show(this@RandomPlayersActivity)
                } else {
                    databaseReference.child("turns").child(connectionId!!)
                        .removeEventListener(turnsEventListener)
                    databaseReference.child("won").child(connectionId!!)
                        .removeEventListener(wonEventListener)
                    databaseReference.child("user_leaves").child(connectionId!!)
                        .removeEventListener(turnsEventListener)
                    removeCode()
                    startActivity(
                        Intent(
                            this@RandomPlayersActivity,
                            MultiPlayerModeActivity::class.java
                        )
                    )
                    finish()
                    dialog.dismiss()
                }
                mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        dialog.dismiss()
                        databaseReference.child("turns").child(connectionId!!)
                            .removeEventListener(turnsEventListener)
                        databaseReference.child("won").child(connectionId!!)
                            .removeEventListener(wonEventListener)
                        databaseReference.child("user_leaves").child(connectionId!!)
                            .removeEventListener(turnsEventListener)
                        removeCode()
                        startActivity(
                            Intent(
                                this@RandomPlayersActivity,
                                MultiPlayerModeActivity::class.java
                            )
                        )
                        finish()
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        dialog.dismiss()
                        databaseReference.child("turns").child(connectionId!!)
                            .removeEventListener(turnsEventListener)
                        databaseReference.child("won").child(connectionId!!)
                            .removeEventListener(wonEventListener)
                        databaseReference.child("user_leaves").child(connectionId!!)
                            .removeEventListener(turnsEventListener)
                        removeCode()
                        startActivity(
                            Intent(
                                this@RandomPlayersActivity,
                                MultiPlayerModeActivity::class.java
                            )
                        )
                        finish()
                        mInterstitialAd = null
                    }
                }
            } else {
                dialog.dismiss()
                databaseReference.child("turns").child(connectionId!!)
                    .removeEventListener(turnsEventListener)
                databaseReference.child("won").child(connectionId!!)
                    .removeEventListener(wonEventListener)
                databaseReference.child("user_leaves").child(connectionId!!)
                    .removeEventListener(turnsEventListener)
                removeCode()
                startActivity(
                    Intent(
                        this@RandomPlayersActivity,
                        MultiPlayerModeActivity::class.java
                    )
                )
                finish()
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

    fun loadLoseAd() {
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        loseAdView.loadAd(adRequest)
        loseAdView.adListener = object : AdListener() {
            override fun onAdClosed() {
                loadBannerAd()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                println(adError)
            }
        }
    }

    private fun vibrate(time: Long) {
        if (vibratePreferences) {
            val vibrate = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrate.vibrate(time)
        }
    }

    private fun tapSound() {
        if (audioPreference) {
            val tapSound = MediaPlayer.create(this, R.raw.tap)
            tapSound.start()
            Handler().postDelayed(Runnable { tapSound.release() }, 500)
        }
    }

    private fun stopBackgroundMusic() {
        music.release()
    }

    private fun playBackgroundMusic() {
        music = MediaPlayer.create(this, R.raw.background_music)
        if (musicPreference) {
            music.isLooping = true
            music.start()
        }
    }

    override fun onPause() {
        if (musicPreference)
            stopBackgroundMusic()
        super.onPause()
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

    override fun onDestroy() {
        if (musicPreference)
            stopBackgroundMusic()
        FirebaseDatabase.getInstance(url).reference.child("user_leaves").child(connectionId!!)
            .child(playerUniqueId)
            .setValue(true)
        finish()

        removeCode()
        super.onDestroy()
    }

    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            removeCode()
            super.onBackPressed()
        }
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Tap again to exit.", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            doubleBackToExitPressedOnce = false
        }, 2000)
    }

    fun checkNetwork(): Boolean {
        return ConnectionManager().checkConnectivity(this@RandomPlayersActivity)
    }

    fun internetToast() {
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.internet_toast, null)
        val toast = Toast(this@RandomPlayersActivity)
        toast.duration = Toast.LENGTH_SHORT;
        toast.setView(layout)
        toast.show()
    }
}

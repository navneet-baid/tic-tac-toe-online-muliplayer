package com.navneet.tictactoe.activity


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Vibrator
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.navneet.tictactoe.BuildConfig
import com.navneet.tictactoe.R
import com.navneet.tictactoe.util.ConnectionManager


var isCodeMaker = false
var code = "null"
var codeFound = false
var checkTemp = true
var keyValue = "null"
const val url = "https://tic-tac-toe-afabf-default-rtdb.firebaseio.com/"

class OnlineCodeGeneratorActivity : AppCompatActivity() {

    lateinit var createCodeBtn: ImageButton
    lateinit var joinCodeBtn: ImageButton
    lateinit var btnStartJoin: ImageButton
    lateinit var btnStartCreate: ImageButton
    lateinit var createCodeView: View
    lateinit var joinCodeView: View
    lateinit var txtOnlineCode: TextView
    lateinit var etJoinCode: EditText
    lateinit var pbLoading: ProgressBar
    lateinit var mainCard: CardView
    lateinit var increaseCoins: ImageView
    lateinit var decreaseCoins: ImageView
    lateinit var txtCoins: TextView


    lateinit var music: MediaPlayer
    lateinit var playerUniqueId: String

    lateinit var sharedPreferences: SharedPreferences
    var vibratePreferences = true
    var musicPreference = true
    var audioPreference = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences =
            this.getSharedPreferences("User Preferences", Context.MODE_PRIVATE)
        setContentView(R.layout.activity_online_code_generator)
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

        createCodeBtn = findViewById(R.id.createGameRoom)
        joinCodeBtn = findViewById(R.id.btnJoinRoom)
        btnStartJoin = findViewById(R.id.btnStartJoin)
        btnStartCreate = findViewById(R.id.btnStartCreate)
        createCodeView = findViewById(R.id.createCodeView)
        joinCodeView = findViewById(R.id.joinCodeView)
        txtOnlineCode = findViewById(R.id.txtOnlineCode)
        etJoinCode = findViewById(R.id.etJoinCode)
        pbLoading = findViewById(R.id.pbLoading)
        mainCard = findViewById(R.id.mainCard)
        increaseCoins = findViewById(R.id.increaseCoins)
        decreaseCoins = findViewById(R.id.decreaseCoins)
        txtCoins = findViewById(R.id.coins)

        musicPreference = sharedPreferences.getBoolean("music", true)
        vibratePreferences = sharedPreferences.getBoolean("vibration", true)
        audioPreference = sharedPreferences.getBoolean("audio", true)

        displayCode()

        increaseCoins.setOnClickListener {
            val availableCoins = sharedPreferences.getString("availableCoins", "0")?.toInt()
            val currentCoins = txtCoins.text.toString().toInt()
            if (availableCoins!! >= currentCoins + 100)
                txtCoins.text = (currentCoins + 100).toString()
        }
        decreaseCoins.setOnClickListener {
            val currentCoins = txtCoins.text.toString().toInt()
            if (currentCoins != 100)
                txtCoins.text = (currentCoins - 100).toString()
        }

        createCodeBtn.setOnClickListener {
            createCodeBtn.setBackgroundColor(R.drawable.background_btn)
            joinCodeBtn.setBackgroundColor(resources.getColor(R.color.white))
            joinCodeView.visibility = View.GONE
            createCodeView.visibility = View.VISIBLE
        }
        joinCodeBtn.setOnClickListener {
            joinCodeBtn.setBackgroundColor(R.drawable.background_btn)
            createCodeBtn.setBackgroundColor(resources.getColor(R.color.white))
            createCodeView.visibility = View.GONE
            joinCodeView.visibility = View.VISIBLE
        }
        playerUniqueId = System.currentTimeMillis().toString()
        btnStartCreate.setOnClickListener {
            clickSound()
            vibrate(40)
            if (checkNetwork()) {
                code = "null"
                codeFound = false
                checkTemp = true
                keyValue = "null"
                isCodeMaker = true
                code = txtOnlineCode.text.toString()
                val getPlayerName = sharedPreferences.getString("player_name", "Player1")
                val getPlayerImage = sharedPreferences.getInt(
                    "userProfilePicture",
                    R.drawable.default_icon
                )
                FirebaseDatabase.getInstance(url).reference.child("codes")
                    .child(code).child(playerUniqueId).child("player_name")
                    .setValue(getPlayerName)
                FirebaseDatabase.getInstance(url).reference.child("codes")
                    .child(code).child(playerUniqueId).child("playerImg")
                    .setValue(getPlayerImage)
                FirebaseDatabase.getInstance(url).reference.child("coins").child(code)
                    .child("coinValue")
                    .setValue(txtCoins.text.toString())
                checkTemp = false
                Handler().postDelayed({
                    accepted()
                }, 200)
            } else {
                val inflater = layoutInflater
                val layout = inflater.inflate(R.layout.internet_toast, null)
                val toast = Toast(this@OnlineCodeGeneratorActivity)
                toast.duration = Toast.LENGTH_SHORT;
                toast.setView(layout)
                toast.show()
            }
        }
        btnStartJoin.setOnClickListener {
            clickSound()
            vibrate(40)
            if (checkNetwork()) {
                code = "null"
                codeFound = false
                checkTemp = true
                keyValue = "null"
                code = etJoinCode.text.toString()
                if (code != "null" && code != "") {
                    mainCard.visibility = View.GONE
                    pbLoading.visibility = View.VISIBLE
                        FirebaseDatabase.getInstance(url).reference.child("codes")
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (!codeFound) {
                                    val data = isValueAvailable(snapshot, code)

                                    Handler().postDelayed({
                                        if (data) {
                                            checkCoinsBal(code)
                                        } else {
                                            mainCard.visibility = View.VISIBLE
                                            pbLoading.visibility = View.GONE
                                            Toast.makeText(
                                                this@OnlineCodeGeneratorActivity,
                                                "Invalid Code",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            FirebaseDatabase.getInstance(url).reference.child("codes").removeEventListener(this)
                                        }
                                    }, 800)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        })
                } else {
                    Toast.makeText(
                        this@OnlineCodeGeneratorActivity,
                        "Please enter a valid code",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                val inflater = layoutInflater
                val layout = inflater.inflate(R.layout.internet_toast, null)
                val toast = Toast(this@OnlineCodeGeneratorActivity)
                toast.duration = Toast.LENGTH_SHORT;
                toast.setView(layout)
                toast.show()
            }
        }
        txtOnlineCode.setOnClickListener {
            vibrate(20)
            try {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My application name")
                var shareMessage =
                    "\nI want to play Tic Tac Toe with you ❌/⭕\nCode:'${txtOnlineCode.text}'\nTo join follow:\nStart Game > Multiplayer Mode >Play with friends > Join\nInstall from Playstore :"
                shareMessage =
                    """
                    ${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}
                    """.trimIndent()
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                startActivity(Intent.createChooser(shareIntent, "choose one"))
            } catch (e: Exception) {
                //e.toString();
            }
        }
    }

    var sufficientBal = true
    private fun checkCoinsBal(code: String) {
        FirebaseDatabase.getInstance(url).reference.child("coins").child(code).child("coinValue")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value != null) {
                        val availableCoins: Int =
                            sharedPreferences.getString("availableCoins", "0").toString().toInt()
                        sufficientBal = availableCoins >= snapshot.value.toString().toInt()
                        if (sufficientBal) {
                            val getPlayerName =
                                sharedPreferences.getString(
                                    "player_name",
                                    "Player2"
                                )
                            val getPlayerImage = sharedPreferences.getInt(
                                "userProfilePicture",
                                R.drawable.default_icon
                            )
                            FirebaseDatabase.getInstance(url).reference.child("codes")
                                .child(code).child(playerUniqueId)
                                .child("player_name")
                                .setValue(getPlayerName)
                            FirebaseDatabase.getInstance(url).reference.child("codes")
                                .child(code).child(playerUniqueId)
                                .child("playerImg")
                                .setValue(getPlayerImage)
                            codeFound = true
                            accepted()
                            mainCard.visibility = View.VISIBLE
                            pbLoading.visibility = View.GONE
                        } else {
                            mainCard.visibility = View.VISIBLE
                            pbLoading.visibility = View.GONE
                            Toast.makeText(
                                this@OnlineCodeGeneratorActivity,
                                "Insufficient coins, required ${snapshot.value} coins",
                                Toast.LENGTH_LONG
                            ).show()
                            startActivity(
                                Intent(
                                    this@OnlineCodeGeneratorActivity,
                                    OnlineCodeGeneratorActivity::class.java
                                )
                            )
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    fun accepted() {
        vibrate(30)
        pbLoading.visibility = View.GONE
        mainCard.visibility = View.VISIBLE
        Toast.makeText(this@OnlineCodeGeneratorActivity, "Starting game..", Toast.LENGTH_SHORT)
            .show()
        finish()
        startActivity(
            Intent(this, OnlinePlayingActivity::class.java).putExtra(
                "playerId",
                playerUniqueId
            )
        )

    }

    fun isValueAvailable(snapshot: DataSnapshot, code: String): Boolean {
        val data = snapshot.children
        data.forEach {
            if (it.key == code) {
                keyValue = it.key.toString()
                return true
            }
        }
        return false
    }

    private fun codeGenerator(): CharSequence {
        pbLoading.visibility = View.GONE
        pbLoading.visibility = View.VISIBLE
        val rndCode = (123456..9999999).random()
        return rndCode.toString()
    }

    fun displayCode() {
        val code = codeGenerator().toString()
        FirebaseDatabase.getInstance(url).reference.child("codes")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = isValueAvailable(snapshot, code)
                    if (!data) {
                        txtOnlineCode.text = code
                    } else {
                        displayCode()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }


    private fun vibrate(time: Long) {
        if (vibratePreferences) {
            val vibrate = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrate.vibrate(time)
        }
    }

    private fun clickSound() {
        if (audioPreference) {
            val sound = MediaPlayer.create(this, R.raw.click_sound).start()
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

    fun checkNetwork(): Boolean {
        return ConnectionManager().checkConnectivity(this@OnlineCodeGeneratorActivity)
    }
}
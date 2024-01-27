package com.navneet.tictactoe.activity


import android.content.Context
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
import androidx.appcompat.widget.TooltipCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.navneet.tictactoe.R
import com.navneet.tictactoe.adapter.CrossRecyclerView
import com.navneet.tictactoe.adapter.NoughtRecycerView
import com.navneet.tictactoe.adapter.ProfileImagesRecyclerAdapter
import com.navneet.tictactoe.util.ConnectionManager


class ProfileActivity : AppCompatActivity() {

    lateinit var profileImage: ImageView
    lateinit var txtPlayerName: TextView
    lateinit var txtAvailableCoins: TextView
    lateinit var winStats: TextView
    lateinit var loseStats: TextView
    lateinit var backBtn: ImageButton
    lateinit var editProfileBtn: ImageButton
    lateinit var freeCoins: ImageButton
    lateinit var statsInfo: ImageView
    lateinit var music: MediaPlayer

    private var mRewardedAd: RewardedAd? = null

    lateinit var sharedPreferences: SharedPreferences
    var vibratePreferences = true
    var musicPreference = true
    var audioPreference = true

    lateinit var crossRecycler:RecyclerView
    lateinit var noughtRecycler:RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var crossRecyclerView: CrossRecyclerView
    lateinit var noughtRecycerView: NoughtRecycerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        sharedPreferences =
            this.getSharedPreferences("User Preferences", Context.MODE_PRIVATE)
        musicPreference = sharedPreferences.getBoolean("music", true)
        vibratePreferences = sharedPreferences.getBoolean("vibration", true)
        audioPreference = sharedPreferences.getBoolean("audio", true)

        val playerName = sharedPreferences.getString("player_name", null)
        val availableCoins = sharedPreferences.getString("availableCoins", null)
        val totalWins = sharedPreferences.getInt("totalWins", 0)
        val totalLosses = sharedPreferences.getInt("totalLosses", 0)
        loadAd(this@ProfileActivity)
        profileImage = findViewById(R.id.imgUserProfilePhoto)
        txtPlayerName = findViewById(R.id.txtUserName)
        txtAvailableCoins = findViewById(R.id.availableCoins)
        winStats = findViewById(R.id.winningStats)
        loseStats = findViewById(R.id.loseStats)
        backBtn = findViewById(R.id.backBtn)
        editProfileBtn = findViewById(R.id.editProfileBtn)
        freeCoins = findViewById(R.id.freeCoinsVideo)
        statsInfo = findViewById(R.id.statsInfo)
        crossRecycler = findViewById(R.id.crossRecycler)
        noughtRecycler = findViewById(R.id.noughtRecycler)
        this.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        val window: Window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.WHITE

        val crossList: MutableList<Int> =
            mutableListOf(
                R.drawable.cross,
                R.drawable.x1,
                R.drawable.x2,
                R.drawable.x3,
                R.drawable.x4,
                R.drawable.x5,
                R.drawable.x6,
            )
        val noughtList: MutableList<Int> =
            mutableListOf(
                R.drawable.circle,
                R.drawable.c1,
                R.drawable.c2,
                R.drawable.c3,
                R.drawable.c4,
                R.drawable.c5,
                R.drawable.c6,
            )

        layoutManager = LinearLayoutManager(this@ProfileActivity,LinearLayoutManager.HORIZONTAL,false)
        crossRecyclerView = CrossRecyclerView(this@ProfileActivity,crossList)
        crossRecycler.adapter = crossRecyclerView
        crossRecycler.layoutManager = layoutManager

        layoutManager = LinearLayoutManager(this@ProfileActivity,LinearLayoutManager.HORIZONTAL,false)
        noughtRecycerView = NoughtRecycerView(this@ProfileActivity,noughtList)
        noughtRecycler.adapter = noughtRecycerView
        noughtRecycler.layoutManager = layoutManager

        val currentNought=sharedPreferences.getInt("NoughtPreference",R.drawable.circle)

        txtPlayerName.text = playerName
        txtAvailableCoins.text = availableCoins
        winStats.text = totalWins.toString()
        loseStats.text = totalLosses.toString()

        profileImage.setImageResource(
            sharedPreferences.getInt(
                "userProfilePicture",
                R.drawable.default_icon
            )
        )

        backBtn.setOnClickListener {
            clickSound()
            vibrate(40)
            startActivity(Intent(this@ProfileActivity, PlayerChoiceActivity::class.java))
            finish()
        }

        editProfileBtn.setOnClickListener {
            clickSound()
            vibrate(40)
            startActivity(Intent(this@ProfileActivity, ProfileEditActivity::class.java))
            finish()
        }
        var count = 1
        TooltipCompat.setTooltipText(
            freeCoins,
            "Please come after some time"
        )
        freeCoins.visibility = View.GONE
        freeCoins.setOnClickListener {
            clickSound()
            if (checkNetwork()) {
                if (count == 1) {
                    showAd()
                    count = 2
                } else {
                    freeCoins.performLongClick()
                }
            } else {
                val inflater = layoutInflater
                val layout = inflater.inflate(R.layout.internet_toast, null)
                val toast = Toast(this@ProfileActivity)
                toast.duration = Toast.LENGTH_SHORT;
                toast.view = layout
                toast.show()
            }
        }
        TooltipCompat.setTooltipText(
            statsInfo,
            "This data show's total number of winning and losses the matches in single player mode and online multiplayer mode."
        )
        statsInfo.setOnClickListener {
            statsInfo.performLongClick()
        }
    }

    fun loadAd(context: Context) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            "ca-app-pub-5528158197168162/6306998567",
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mRewardedAd = null
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    mRewardedAd = rewardedAd
                    freeCoins.visibility = View.VISIBLE
                }
            })
    }

    private fun showAd() {
        if (mRewardedAd != null) {
            mRewardedAd?.show(this@ProfileActivity, OnUserEarnedRewardListener() {
                fun onUserEarnedReward(rewardItem: RewardItem) {
                    val rewardAmount = rewardItem.amount
                    val rewardType = rewardItem.type
                    adReward(rewardAmount)
                }
                onUserEarnedReward(it)
                loadAd(this@ProfileActivity)
            })
        } else {
            loadAd(this@ProfileActivity)
            Toast.makeText(this, "!! Failed to load Ad", Toast.LENGTH_SHORT).show()
        }
    }

    private fun adReward(coins: Int) {
        val currentCoin = sharedPreferences.getString("availableCoins", "0")
        val updatedCoinBal = currentCoin?.toInt()!! + coins
        sharedPreferences.edit().putString("availableCoins", updatedCoinBal.toString()).apply()
        txtAvailableCoins.text = updatedCoinBal.toString()
        Toast.makeText(this@ProfileActivity, "$coins reward coins added.", Toast.LENGTH_SHORT)
            .show()

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
        startActivity(Intent(this@ProfileActivity, PlayerChoiceActivity::class.java))
        finish()
    }

    private fun checkNetwork(): Boolean {
        return ConnectionManager().checkConnectivity(this@ProfileActivity)
    }
}
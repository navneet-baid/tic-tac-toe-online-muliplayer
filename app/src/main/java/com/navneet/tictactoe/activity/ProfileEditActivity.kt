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
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.navneet.tictactoe.adapter.ProfileImagesRecyclerAdapter
import com.navneet.tictactoe.R


class ProfileEditActivity : AppCompatActivity() {
    private lateinit var continueBtn: ImageButton
    private lateinit var image: ImageView
    private lateinit var backBtn: ImageButton
    private lateinit var userName: EditText
    private lateinit var recyclerViewImages: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var recyclerAdapter: ProfileImagesRecyclerAdapter
    lateinit var music: MediaPlayer
    lateinit var sharedPreferences: SharedPreferences
    var vibratePreferences = true
    var musicPreference = true
    var audioPreference = true


    private val imagesArray: MutableList<Int> =
        mutableListOf(
            R.drawable.default_icon,
            R.drawable.p1,
            R.drawable.p2,
            R.drawable.p3,
            R.drawable.p4,
            R.drawable.p5,
            R.drawable.p6,
            R.drawable.p7,
            R.drawable.p8,
            R.drawable.p9,
            R.drawable.p10,
            R.drawable.p11,
            R.drawable.p12
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

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

        sharedPreferences = this.getSharedPreferences("User Preferences", Context.MODE_PRIVATE)
        musicPreference = sharedPreferences.getBoolean("music", true)
        vibratePreferences = sharedPreferences.getBoolean("vibration", true)
        audioPreference = sharedPreferences.getBoolean("audio", true)


        continueBtn = findViewById(R.id.continueBtn)
        image = findViewById(R.id.imgUserProfilePhoto)
        backBtn = findViewById(R.id.backBtn)
        userName = findViewById(R.id.etUserName)
        recyclerViewImages = findViewById(R.id.recyclerView)

        image.tag = sharedPreferences.getInt("profilePictureTag", R.drawable.default_icon)

        layoutManager = GridLayoutManager(this@ProfileEditActivity, 4)
        recyclerAdapter = ProfileImagesRecyclerAdapter(this@ProfileEditActivity, imagesArray, image)
        recyclerViewImages.adapter = recyclerAdapter
        recyclerViewImages.layoutManager = layoutManager
        userName.isFocusable = true

        userName.setText(sharedPreferences.getString("player_name", "---"))
        image.setImageResource(
            sharedPreferences.getInt(
                "userProfilePicture",
                R.drawable.default_icon
            )
        )

        continueBtn.setOnClickListener {
            clickSound()
            vibrate(40)
            sharedPreferences.edit().putInt("userProfilePicture", image.tag.toString().toInt())
                .apply()
            sharedPreferences.edit().putString("player_name", userName.text.toString()).apply()
            startActivity(Intent(this@ProfileEditActivity, ProfileActivity::class.java))
            finish()
        }

        backBtn.setOnClickListener {
            vibrate(40)
            clickSound()
            finish()
            startActivity(Intent(this@ProfileEditActivity, ProfileActivity::class.java))
        }
    }

    private fun vibrate(time: Long) {
        if (vibratePreferences) {
            val vibrate = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrate.vibrate(time)
        }
    }

    private fun clickSound() {
        if (audioPreference) {
            val sound = MediaPlayer.create(this, R.raw.click_sound)
            sound.start()
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
        super.onBackPressed()
        return
    }
}
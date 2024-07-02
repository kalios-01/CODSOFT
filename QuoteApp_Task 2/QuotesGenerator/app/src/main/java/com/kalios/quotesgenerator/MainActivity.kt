package com.kalios.quotesgenerator

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.kalios.quotesgenerator.databinding.ActivityMainBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.min

class MainActivity : AppCompatActivity() {

    private var mInterstitialAd: InterstitialAd? = null
    private val API_KEY = "Eu1cpDHCuHKgWzf00eHbxA==og6ZDGXuLmp97KZU"
    private val API_BASE_URL = "https://api.api-ninjas.com/v1/quotes"
    private var selectedCatagory: String? = null
    private lateinit var db: AppDatabase
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.getDatabase(this)

        val adapter = ArrayAdapter(this, android.R.layout.select_dialog_item, Catagory.WORDS)
        binding.edtCatagory.setAdapter(adapter)
        binding.edtCatagory.threshold = 1
        binding.edtCatagory.setOnItemClickListener { parent, view, position, id ->
            selectedCatagory = parent.getItemAtPosition(position) as String
        }

        if (isDarkModeActive()) {
            // Dark mode is active, set light image
            binding.themeSwitch.setImageResource(R.drawable.dark_mode)
        } else {
            // Light mode is active, set dark image
            binding.themeSwitch.setImageResource(R.drawable.light_mode)
        }
        // theme change
        binding.themeSwitch.setOnClickListener {
            // Toggle between light and dark images
            if (isDarkModeActive()) {
                // Dark mode is active, set light image
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                // Light mode is active, set dark image
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }

        binding.btnFavouriteList.setOnClickListener {
            favlist()
        }

        binding.copyButton.setOnClickListener {
            copyToClipboard()
        }
        binding.addToFav.setOnClickListener {
            addQuoteToFavorites()
        }
        loadInterstitialAd()
        binding.btnGenerate.setOnClickListener {
            selectedCatagory?.let { fetchRandomQuote(it) }
            showInterstitialAd()
        }
        binding.generateAnother.setOnClickListener {
            selectedCatagory?.let { fetchRandomQuote(it) }
            showInterstitialAd()
        }
        //
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
        showInterstitialAd()

    }// OnCreate Over Here

    private fun addQuoteToFavorites() {
        val quoteText = binding.quotes.text.toString()
        val authorText = binding.authorName.text.toString()

        if (quoteText.isNotEmpty() && authorText.isNotEmpty()) {
            val quote = Quote(quote = quoteText, author = authorText)
            GlobalScope.launch(Dispatchers.IO) {
                db.quoteDao().insert(quote)
            }
            Toast.makeText(this, "Quote added to favorites", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No quote to add", Toast.LENGTH_SHORT).show()
        }
    }

    //Function Start
    private fun fetchRandomQuote(category: String) {
        val quotableApiUrl = "$API_BASE_URL?category=$category"

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL(quotableApiUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.setRequestProperty("X-Api-Key", API_KEY)

                val apiResponse = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(apiResponse)
                // Choose a random quote from the array
                val randomIndex = (0 until jsonArray.length()).random()
                val quoteObject = jsonArray.getJSONObject(randomIndex)

                val quote = quoteObject.getString("quote")
                val author = quoteObject.getString("author")

                val formattedQuote = "\"$quote\""

                launch(Dispatchers.Main) {
                    binding.quotes.text = formattedQuote
                    binding.authorName.text=author
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    @SuppressLint("ServiceCast")
    private fun copyToClipboard() {
        val clipboardManager =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("quote", binding.quotes.text)
        clipboardManager.setPrimaryClip(clipData)

        Toast.makeText(this, "Quote copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            "ca-app-pub-7322204604787687/3188334684", // Replace with your Ad Unit ID
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError.toString())
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    mInterstitialAd = interstitialAd
                }
            })
    }

    // Call this method when you want to show the interstitial ad
    private fun showInterstitialAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this)
        } else {
            Log.d(TAG, "The interstitial ad wasn't loaded yet.")
        }
    }
    private fun isDarkModeActive(): Boolean {
        return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }
    }
    private fun favlist(){
        val intent = Intent(this, FavoritesActivity::class.java)
        startActivity(intent)
    }
}
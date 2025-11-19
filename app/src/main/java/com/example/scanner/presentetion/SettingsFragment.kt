package com.example.scanner.presentetion

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scanner.R
import com.example.scanner.databinding.BottomsheetRateUsBinding
import com.example.scanner.databinding.FragmentSettingsBinding
import com.example.scanner.util.Constants
import com.example.scanner.util.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.core.content.edit

class SettingsFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var pref: PreferenceManager
    private lateinit var prefs: SharedPreferences
    private var selectedLanguage = ""
    private var selectedSearchEngine = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(layoutInflater, container, false)

        prefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val isBeepEnabled = prefs.getBoolean("isBeep", false)
        val isCopyEnabled = prefs.getBoolean("isCopy",false)
        binding.switchBeep.isChecked = isBeepEnabled
        binding.switchClipboard.isChecked = isCopyEnabled

        binding.switchBeep.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit { putBoolean("isBeep", isChecked) }
        }
        binding.switchClipboard.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("isCopy", isChecked).apply()
        }


        pref = PreferenceManager(requireActivity())
        selectedLanguage = pref.getString(Constants.LANGUAGE)
        selectedSearchEngine = pref.getString(Constants.SELECTED_SEARCH_ENGINE)
        if (selectedSearchEngine.isEmpty()) {
            selectedSearchEngine = "Google"
            binding.tvSearchName.text = selectedSearchEngine
        } else {
            binding.tvSearchName.text = selectedSearchEngine
        }
        binding.llSearchEngine.setOnClickListener(this)
        binding.llLanguage.setOnClickListener(this)
        binding.tvFaq.setOnClickListener(this)
        binding.llFeedBack.setOnClickListener(this)
        binding.rlRateUs.setOnClickListener(this)
        binding.tvPrivacyPolicy.setOnClickListener(this)
        binding.tvTermsOfUse.setOnClickListener(this)

        binding.tvLanguageName.text = selectedLanguage

        val versionName = requireActivity().packageManager.getPackageInfo(
            requireActivity().packageName,
            0
        ).versionName
        binding.tvVersion.text = "Version $versionName"

        return binding.root
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.llSearchEngine -> {
                showSearchEngineDialog()
            }

            R.id.llLanguage -> {
                val intent = Intent(requireActivity(), SelectLanguageActivity::class.java)
                intent.putExtra(Constants.FROM, Constants.SETTINGS)
                startActivity(intent)
            }

            R.id.tvFaq -> {
                val intent = Intent(requireActivity(), FAQActivity::class.java)
                startActivity(intent)
            }

            R.id.llFeedBack -> {
                val intent = Intent(requireActivity(), FeedbackActivity::class.java)
                startActivity(intent)
            }

            R.id.rlRateUs -> {
                rateUs()
            }

            R.id.tvPrivacyPolicy -> {
                val url = "https://www.google.com/"
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = url.toUri()
                startActivity(intent)
            }

            R.id.tvTermsOfUse -> {
                val url = "https://www.google.com/"
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = url.toUri()
                startActivity(intent)
            }
        }
    }

    private fun rateUs() {
        val dialog = BottomSheetDialog(requireActivity())
        val binding = BottomsheetRateUsBinding.inflate(LayoutInflater.from(requireActivity()))
        dialog.setContentView(binding.root)
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as BottomSheetDialog)
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(android.R.color.transparent)
        }

        dialog.behavior.isDraggable = true

        val stars = listOf(
            binding.ivStar1,
            binding.ivStar2,
            binding.ivStar3,
            binding.ivStar4,
            binding.ivStar5
        )
        val emojiImages = listOf(
            R.drawable.emoji_zero,
            R.drawable.emoji_one,
            R.drawable.emoji_two,
            R.drawable.emoji_three,
            R.drawable.emoji_four,
            R.drawable.emoji_five
        )

        val ratingTexts = listOf(
            getString(R.string.thank_you_for_your_support),
            getString(R.string.oh_we_re_sorry),
            getString(R.string.oh_we_re_sorry),
            getString(R.string.oh_we_re_sorry),
            getString(R.string.much_appreciated),
            getString(R.string.much_appreciated)
        )

        val ratingSubtitles = listOf(
            getString(R.string.we_would_be_very_grateful_if_you_can_rate_us),
            getString(R.string.your_feedback_is_welcome),
            getString(R.string.your_feedback_is_welcome),
            getString(R.string.your_feedback_is_welcome),
            getString(R.string.your_support_is_our_biggest_motivation),
            getString(R.string.your_support_is_our_biggest_motivation)
        )
        var selectedRating = 0


        for (star in stars) {
            star.background = ContextCompat.getDrawable(requireContext(), R.drawable.star_blink_white)
            star.isClickable = true
            star.isFocusable = true
        }

        fun startStarAnimation() {
            for (i in 0 until 4) {
                stars[i].setImageResource(R.drawable.ic_icon_grey_star)
            }
            stars[4].setImageResource(R.drawable.ic_icon_star)

            fun animateStarsSequentially(index: Int) {
                if (index >= stars.size) {
                    for (i in 0 until 4) {
                        stars[i].setImageResource(R.drawable.ic_icon_grey_star)
                        stars[i].clearAnimation()
                    }
                    val continuousBlink = AlphaAnimation(0.3f, 1.0f)
                    continuousBlink.duration = 1000
                    continuousBlink.repeatCount = Animation.INFINITE
                    continuousBlink.repeatMode = Animation.REVERSE

                    stars[4].setImageResource(R.drawable.ic_icon_yellow_star)
                    stars[4].startAnimation(continuousBlink)

                    binding.imgEmoji.setImageResource(emojiImages[0])
                    binding.tvTitle.text = ratingTexts[0]
                    binding.tvSubtitle.text = ratingSubtitles[0]
                    return
                }
                val blinkAnimation = AlphaAnimation(0.3f, 1.0f)
                blinkAnimation.duration = 100
                blinkAnimation.repeatCount = 1
                blinkAnimation.repeatMode = Animation.REVERSE

                stars[index].setImageResource(R.drawable.ic_icon_yellow_star)
                stars[index].startAnimation(blinkAnimation)

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    val rippleDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ripple_yellow)
                    stars[index].background = rippleDrawable

                    stars[index].isPressed = true
                    stars[index].postDelayed({
                        stars[index].isPressed = false
                    }, 200)
                }

                blinkAnimation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation?) {
                        stars[index].postDelayed({
                            stars[index].background = null
                        }, 100)

                        if (index < stars.size - 1) {
                            stars[index].setImageResource(R.drawable.ic_icon_yellow_star)
                        }

                        stars[index].postDelayed({
                            animateStarsSequentially(index + 1)
                        }, 100)
                    }

                    override fun onAnimationRepeat(animation: Animation?) {}
                })
            }
            animateStarsSequentially(0)
        }

        fun updateStars(rating: Int) {

            if (rating > 0) {
                for (star in stars) {
                    star.clearAnimation()
                }
            }

            if (rating == 0) {
                for (i in 0 until 4) {
                    stars[i].setImageResource(R.drawable.ic_icon_grey_star)
                }
                stars[4].setImageResource(R.drawable.ic_icon_star)
            } else {
                for (i in 0 until 5) {
                    stars[i].setImageResource(
                        if (i < rating) R.drawable.ic_icon_yellow_star
                        else R.drawable.ic_icon_grey_star
                    )
                }
            }

            binding.imgEmoji.setImageResource(emojiImages[rating])
            binding.tvTitle.text = ratingTexts[rating]
            binding.tvSubtitle.text = ratingSubtitles[rating]

            binding.topBar.visibility = if (rating >= 1) View.GONE else View.VISIBLE

            binding.btnSubmit.isEnabled = rating >= 1
            binding.btnSubmit.backgroundTintList = ContextCompat.getColorStateList(
                requireContext(),
                if (rating >= 1) R.color.txt_color_blue else R.color.txt_color_grey
            )
            binding.btnSubmit.setTextColor( ContextCompat.getColorStateList(
                requireContext(),
                if (rating >= 1) R.color.white else R.color.off_white
            ))
        }

        for (i in stars.indices) {
            stars[i].setOnClickListener {
                stars[i].background = ContextCompat.getDrawable(requireContext(), R.drawable.star_blink_white)
                selectedRating = i + 1
                updateStars(selectedRating)
            }
        }

        binding.btnSubmit.setOnClickListener {
            if (selectedRating >= 3) {
                val appPackageName = requireContext().packageName
                try {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        "market://details?id=$appPackageName".toUri()
                    )
                    intent.setPackage("com.android.vending")
                    startActivity(intent)
                } catch (_: Exception) {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                    )
                    startActivity(intent)
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.thanks_for_your_feedback_we_ll_improve_your_experience),
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            }
        }
        dialog.setOnShowListener {
            val bottomSheet = dialog
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(android.R.color.transparent)
            stars[4].postDelayed({
                startStarAnimation()
            }, 300)
        }
        updateStars(0)

        dialog.show()
    }
    private fun showSearchEngineDialog() {
        val searchEngines = arrayOf(
            getString(R.string.google),
            getString(R.string.bing), getString(R.string.yahoo),
            getString(R.string.duckduckgo), getString(R.string.ecosia), getString(R.string.yandex)
        )

        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_search_engine)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.rvSearch)
        val btnClose = dialog.findViewById<ImageButton>(R.id.buttonClose)

        val adapter =
            SearchEngineAdapter(searchEngines.toList(), selectedSearchEngine) { selectedEngine ->
                selectedSearchEngine = selectedEngine
                binding.tvSearchName.text = selectedSearchEngine
                pref.setString(Constants.SELECTED_SEARCH_ENGINE, selectedSearchEngine)
                dialog.dismiss()
            }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
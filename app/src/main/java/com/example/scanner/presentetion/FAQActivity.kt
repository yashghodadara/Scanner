package com.example.scanner.presentetion

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.scanner.R
import com.example.scanner.databinding.ActivityFaqactivityBinding
import com.example.scanner.util.BaseActivity

class FAQActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivityFaqactivityBinding
    private var isCodeExpanded = false
    private var isWifiExpanded = false
    private var isProblemExpanded = false
    private var isInfoExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFaqactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        hideSystemUIAAndSetStatusBarColor()
        binding.txtCodeA.text = createStyledAnswer(
            binding.txtCodeA.context,
            getString(R.string.a_rich_contrast_and_not_blurry),
            listOf(getString(R.string.not_blurry))

        )

        binding.txtCodeB.text = createStyledAnswer(
            binding.txtCodeB.context,
            getString(R.string.b_no_light_reflections_or_shadows_on_the_code),
            listOf(getString(R.string.no_light_reflections_or_shadows))
        )

        binding.txtCodeC.text = createStyledAnswer(
            binding.txtCodeC.context,
            getString(R.string.c_complete_with_no_missing_parts),
            listOf(getString(R.string.complete))
        )

        binding.txtCodeThree.text = createStyledAnswer(
            binding.txtCodeThree.context,
            getString(R.string._3_if_you_still_can_t_get_the_result_when_scanning_the_barcode_or_qr_code_please_send_an_email_via_feedback_to_us_we_will_solve_the_problem_for_you_as_soon_as_possible),
            listOf(getString(R.string.feedback))
        )

        binding.txtWifiOne.text = createStyledAnswer(
            binding.txtWifiOne.context,
            getString(R.string._1_make_sure_you_are_within_the_wi_fi_coverage_area_and_the_wi_fi_username_and_password_are_correct),
            listOf(getString(R.string.within_the_wi_fi_coverage_area))
        )

        binding.txtWifiTwo.text = createStyledAnswer(
            binding.txtWifiTwo.context,
            getString(R.string._2_if_the_scan_result_is_displayed_as_text_you_need_to_manually_connect_to_wi_fi_in_your_phone_s_network_settings_based_on_the_wi_fi_name_and_password_provided_in_the_text),
            listOf(getString(R.string.manually_connect_to_wi_fi))
        )

        binding.tvProblemOne.text = createStyledAnswer(
            binding.tvProblemOne.context,
            getString(R.string._1_this_app_can_t_change_the_content_of_the_qr_code_so_if_you_can_t_open_this_website_it_is_more_likely_that_there_is_a_problem_with_the_website_itself_unfortunately_there_is_nothing_we_can_do_about_it),
            listOf(getString(R.string.website_itself))
        )

        binding.tvProblemTwo.text = createStyledAnswer(
            binding.tvProblemTwo.context,
            getString(R.string._2_it_is_also_possible_that_the_creator_of_the_qr_code_entered_the_wrong_link_or_the_qr_code_has_expired_causing_you_to_be_unable_to_open_the_website_if_possible_you_can_try_to_confirm_the_correctness_of_the_qr_code_with_the_creator),
            listOf(getString(R.string.wrong_link), getString(R.string.expired))
        )

        binding.tvProblemThree.text = createStyledAnswer(
            binding.tvProblemThree.context,
            getString(R.string._3_in_particular_some_web_links_need_to_be_opened_under_special_conditions_for_example_they_need_to_be_opened_using_a_certain_app_please_follow_the_instructions_of_the_qr_code_provider),
            listOf(getString(R.string.certain_app))
        )

        binding.txtInfoOne.text = createStyledAnswer(
            binding.txtInfoOne.context,
            getString(R.string.there_is_too_much_information_and_we_may_not_be_able_to_provide_the_information_you_want_every_time_you_can_check_for_more_information_through_web_search),
            listOf(getString(R.string.web_search_))
        )

        binding.llCodeHeader.setOnClickListener(this)
        binding.llWifiHeader.setOnClickListener(this)
        binding.llProblemHeader.setOnClickListener(this)
        binding.llInfoHeader.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
        binding.llSendFeedBack.setOnClickListener(this)

        binding.txtCodeThree.movementMethod = LinkMovementMethod.getInstance()
        binding.txtCodeThree.highlightColor = ContextCompat.getColor(this, R.color.txt_color_blue)
    }


    private fun createStyledAnswer(
        context: Context,
        fullText: String,
        highlights: List<String>
    ): SpannableStringBuilder {

        val spannable = SpannableStringBuilder(fullText)

        val gray = ContextCompat.getColor(context, R.color.txt_color_grey)
        val white = ContextCompat.getColor(context, R.color.white)
        val blue = ContextCompat.getColor(context, R.color.txt_color_blue)

        spannable.setSpan(
            ForegroundColorSpan(gray),
            0,
            fullText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        for (highlight in highlights) {
            var start = fullText.indexOf(highlight)
            while (start != -1) {
                val isFeedback  = highlight.equals("feedback", ignoreCase = true)
                val colorToUse = if (isFeedback) blue else white
                spannable.setSpan(
                    ForegroundColorSpan(colorToUse),
                    start,
                    start + highlight.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                if (isFeedback) {
                    val clickableSpan = object : ClickableSpan() {
                        override fun onClick(widget: View) {
                             val intent = Intent(context, FeedbackActivity::class.java)
                             startActivity(intent)
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            super.updateDrawState(ds)
                            ds.color = blue
                            ds.isUnderlineText = true
                        }
                    }
                    spannable.setSpan(
                        clickableSpan,
                        start,
                        start + highlight.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                start = fullText.indexOf(highlight, start + highlight.length)
            }
        }
        return spannable
    }

    private fun toggleCodeDescription() {
        if (isCodeExpanded) {
            binding.llCodeDescription.visibility = View.GONE
            binding.ivCodeArrow.animate().rotation(0f).setDuration(200).start()

        } else {
            binding.llCodeDescription.visibility = View.VISIBLE
            binding.ivCodeArrow.animate().rotation(180f).setDuration(200).start()
        }
        isCodeExpanded = !isCodeExpanded
    }

    private fun toggleWifiDescription() {
        if (isWifiExpanded) {
            binding.llWifiDescription.visibility = View.GONE
            binding.ivWifiArrow.animate().rotation(0f).setDuration(200).start()
        } else {
            binding.llWifiDescription.visibility = View.VISIBLE
            binding.ivWifiArrow.animate().rotation(180f).setDuration(200).start()
        }
        isWifiExpanded = !isWifiExpanded
    }

 private fun toggleInfoDescription() {
        if (isInfoExpanded) {
            binding.llInfoDescription.visibility = View.GONE
            binding.ivInfoArrow.animate().rotation(0f).setDuration(200).start()
        } else {
            binding.llInfoDescription.visibility = View.VISIBLE
            binding.ivInfoArrow.animate().rotation(180f).setDuration(200).start()
        }
     isInfoExpanded = !isInfoExpanded
    }

    private fun toggleProblemDescription() {
        if (isProblemExpanded) {
            binding.llProblemDescription.visibility = View.GONE
            binding.ivProblemArrow.animate().rotation(0f).setDuration(200).start()

        } else {
            binding.llProblemDescription.visibility = View.VISIBLE
            binding.ivProblemArrow.animate().rotation(180f).setDuration(200).start()
        }
        isProblemExpanded = !isProblemExpanded
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.llCodeHeader -> toggleCodeDescription()
            R.id.llWifiHeader -> toggleWifiDescription()
            R.id.llProblemHeader -> toggleProblemDescription()
            R.id.llInfoHeader -> toggleInfoDescription()
            R.id.ivBack -> finish()
            R.id.llSendFeedBack -> {
                val intent = Intent(this, FeedbackActivity::class.java)
                startActivity(intent)
            }
            }
        }

    private fun hideSystemUIAAndSetStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(
                    WindowInsets.Type.navigationBars()
                )
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    )
        }

        val bgColor = ContextCompat.getColor(this, R.color.bg_color)
        window.statusBarColor = bgColor

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility and
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
    }
}
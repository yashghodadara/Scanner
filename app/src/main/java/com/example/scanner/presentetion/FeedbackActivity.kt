package com.example.scanner.presentetion

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanner.R
import com.example.scanner.databinding.ActivityFeedbackBinding
import com.example.scanner.util.BaseActivity

class FeedbackActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding : ActivityFeedbackBinding
    private val selectedProblems = mutableListOf<String>()
    private val selectedImages = mutableListOf<Uri>()
    private lateinit var imageAdapter: FeedbackAdapter
    private var imageUri: Uri? = null
    companion object {
        private const val REQUEST_IMAGE_PICK = 1001
        private const val REQUEST_IMAGE_CAPTURE = 1002
        private const val MIN_CHARACTERS = 6
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        hideSystemUIAAndSetStatusBarColor()
        binding.ivBack.setOnClickListener(this)
        binding.btnScanning.setOnClickListener(this)
        binding.btnAds.setOnClickListener(this)
        binding.btnInfo.setOnClickListener(this)
        binding.btnOther.setOnClickListener(this)
        binding.btnCamera.setOnClickListener(this)
        binding.btnSubmit.setOnClickListener(this)
        setupRecyclerView()
        setupTextWatchers()
        updateSubmitButtonState()
    }

    private fun setupRecyclerView() {
        imageAdapter = FeedbackAdapter(selectedImages)
        binding.rvImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvImages.adapter = imageAdapter
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.ivBack -> finish()
            R.id.btnScanning -> selectProblem(binding.btnScanning, getString(R.string.scanning_not_working))
            R.id.btnAds -> selectProblem(binding.btnAds, getString(R.string.ads))
            R.id.btnInfo -> selectProblem(binding.btnInfo, getString(R.string.need_more_information_after_scanning))
            R.id.btnOther -> selectProblem(binding.btnOther, getString(R.string.others))
            R.id.btnCamera -> openImageChooser()
            R.id.btnSubmit -> if(isFormValid()) sendFeedbackEmail()
        }
    }

    private fun setupTextWatchers() {
        binding.etDetails.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateSubmitButtonState()
            }
        })
    }

    private fun selectProblem(selectedView: TextView, problem: String) {
        if (selectedProblems.contains(problem)) {
            selectedProblems.remove(problem)
            selectedView.setBackgroundResource(R.drawable.bg_problem_button)
            selectedView.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            selectedProblems.add(problem)
            selectedView.setBackgroundResource(R.drawable.bg_problem_selected)
            selectedView.setTextColor(ContextCompat.getColor(this, R.color.white))
        }
        updateSubmitButtonState()
    }

    private fun updateSubmitButtonState() {
        val isProblemSelected = selectedProblems.isNotEmpty()
        val hasMinCharacters = (binding.etDetails.text?.length ?: 0) >= MIN_CHARACTERS
        val isFormValid = isProblemSelected && hasMinCharacters

        binding.btnSubmit.isEnabled = isFormValid

        if (isFormValid) {
            binding.btnSubmit.setBackgroundResource(R.drawable.bg_submit_button)
            binding.btnSubmit.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.txt_color_blue))
            binding.btnSubmit.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            binding.btnSubmit.setBackgroundResource(R.drawable.bg_unsubmit_button)
            binding.btnSubmit.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.txt_color_grey))
            binding.btnSubmit.setTextColor(ContextCompat.getColor(this, R.color.off_white))
        }
    }

    private fun isFormValid(): Boolean {
        return selectedProblems.isNotEmpty() && (binding.etDetails.text?.length ?: 0) >= MIN_CHARACTERS
    }

    private fun openImageChooser() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        imageUri = createImageUri()
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.type = "image/*"

        val chooser = Intent.createChooser(galleryIntent, "Select Image")
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))

        startActivityForResult(chooser, REQUEST_IMAGE_PICK)
    }

    private fun createImageUri(): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "New Picture")
            put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    private fun takePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_IMAGE_CAPTURE)
            return
        }

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_PICK -> {
                    val selectedImageUri = data?.data ?: imageUri
                    selectedImageUri?.let {
                        selectedImages.add(it)
                        imageAdapter.notifyDataSetChanged()
                        updateSubmitButtonState()
                    }
                }
            }
        }
    }

    private fun sendFeedbackEmail() {
        if (selectedProblems.isEmpty()) {
            Toast.makeText(this, "Please select at least one problem", Toast.LENGTH_SHORT).show()
            return
        }

        val emailIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("vinodjavia1963@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Feedback - ${selectedProblems.joinToString(", ")}")
            putExtra(Intent.EXTRA_TEXT, buildEmailBody())

            if (selectedImages.isNotEmpty()) {
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(selectedImages))
            }

            setPackage("com.google.android.gm")
        }

        try {
            startActivity(emailIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "Gmail not installed", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }



    private fun buildEmailBody(): String {
        val problemsText = if (selectedProblems.isNotEmpty()) {
            selectedProblems.joinToString(", ")
        } else {
            getString(R.string.no_problems_selected)
        }

        val detailsText = binding.etDetails.text?.toString()?.trim().orEmpty()

        val imageInfo = if (selectedImages.isNotEmpty()) {
            getString(R.string.number_of_attached_images, selectedImages.size)
        } else {
            ""
        }

        return """
        Problems: $problemsText

        Details: $detailsText$imageInfo

        ---
        Sent from Scanner
    """.trimIndent()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto()
                } else {
                    Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
                }
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
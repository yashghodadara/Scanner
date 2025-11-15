package com.example.scanner.presentetion

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ScaleGestureDetector
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.scanner.R
import com.example.scanner.databinding.FragmentScanBinding
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.media.AudioManager
import android.media.ToneGenerator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import androidx.core.animation.doOnEnd
import androidx.core.graphics.toColorInt


class ScanFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentScanBinding
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var torchEnabled = false
    private var isScanning = false
    private lateinit var cameraExecutor: ExecutorService

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera()
        else Toast.makeText(requireActivity(), "Camera permission required", Toast.LENGTH_SHORT)
            .show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScanBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.flashlightButton.setOnClickListener(this)
        binding.btnZoomIn.setOnClickListener(this)
        binding.btnZoomOut.setOnClickListener(this)

        binding.seekZoom.max = 100
        binding.seekZoom.progress = 0
        binding.seekZoom.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                camera?.cameraControl?.setLinearZoom(progress / 100f)
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        val scaleGestureDetector = ScaleGestureDetector(
            requireActivity(),
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    camera?.let {
                        val currentZoom = it.cameraInfo.zoomState.value?.zoomRatio ?: 1f
                        val newZoom = currentZoom * detector.scaleFactor
                        it.cameraControl.setZoomRatio(newZoom)
                    }
                    return true
                }
            })
        binding.previewView.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            true
        }
        startScanAnimation()
        checkPermissionAndStart()
    }
    private fun startScanAnimation() {

        val scanLine = binding.scanLine

        val topColor = "#CC0188FE".toColorInt()
        val bottomColor = "#0188FECC".toColorInt()

        val gradientDrawable = GradientDrawable()
        scanLine.background = gradientDrawable


        val DURATION = 2000L

        // TOP → BOTTOM
        val topToBottom = TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, 0f,
            Animation.RELATIVE_TO_PARENT, 0f,
            Animation.RELATIVE_TO_PARENT, -1f,
            Animation.RELATIVE_TO_PARENT, 1f
        ).apply {
            duration = DURATION
            fillAfter = true
        }

        // BOTTOM → TOP
        val bottomToTop = TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, 0f,
            Animation.RELATIVE_TO_PARENT, 0f,
            Animation.RELATIVE_TO_PARENT, 1f,
            Animation.RELATIVE_TO_PARENT, -1f
        ).apply {
            duration = DURATION
            fillAfter = true
        }

        // LISTENERS
        topToBottom.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {

                gradientDrawable.colors = intArrayOf(bottomColor, topColor)
            }

            override fun onAnimationEnd(animation: Animation?) {
                scanLine.startAnimation(bottomToTop)
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        bottomToTop.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                gradientDrawable.colors =
                    intArrayOf(topColor, bottomColor)

            }

            override fun onAnimationEnd(animation: Animation?) {
                scanLine.startAnimation(topToBottom)
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        // START FIRST ANIMATION
        scanLine.startAnimation(topToBottom)
    }


    private fun blendColor(from: Int, to: Int, ratio: Float): Int {
        val inv = 1f - ratio
        val r = (Color.red(from) * inv + Color.red(to) * ratio).toInt()
        val g = (Color.green(from) * inv + Color.green(to) * ratio).toInt()
        val b = (Color.blue(from) * inv + Color.blue(to) * ratio).toInt()
        val a = (Color.alpha(from) * inv + Color.alpha(to) * ratio).toInt()
        return Color.argb(a, r, g, b)
    }


    private fun checkPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireActivity()))
    }

    private fun bindCameraUseCases() {
        val provider = cameraProvider ?: return

        val rotation = binding.previewView.display?.rotation ?: Surface.ROTATION_0

        val preview = Preview.Builder()
            .setTargetRotation(rotation)
            .build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }



        imageAnalyzer?.clearAnalyzer()
        imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalyzer?.setAnalyzer(cameraExecutor) { imageProxy ->
            if (isScanning) {
                imageProxy.close()
                return@setAnalyzer
            }
            processImageProxy(imageProxy)
        }

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        try {
            provider.unbindAll()
            camera = provider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            camera?.cameraControl?.enableTorch(torchEnabled)
        } catch (exc: Exception) {
            Log.e("QrScanner", "Use case binding failed", exc)
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return imageProxy.close()
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val scanner = BarcodeScanning.getClient()

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty() && !isScanning) {
                    val barcode = barcodes.first()
                    isScanning = true
                    playBeepIfEnabled()

                    val bitmap = imageProxyToBitmap(imageProxy)
                    val imageUri = saveBitmapToCache(bitmap)
                    requireActivity().runOnUiThread {
                        val details = extractQrDetails(barcode)
                        navigateToResult(details, imageUri.toString())
                    }
                }
            }
            .addOnFailureListener { it.printStackTrace() }
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun saveBitmapToCache(bitmap: Bitmap): Uri {
        val file = File(requireContext().cacheDir, "qr_preview_${System.currentTimeMillis()}.jpg")
        val out = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        out.flush()
        out.close()
        return FileProvider.getUriForFile(
            requireContext(),
            requireContext().packageName + ".provider",
            file
        )
    }

    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        val yBuffer = imageProxy.planes[0].buffer
        val uBuffer = imageProxy.planes[1].buffer
        val vBuffer = imageProxy.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(
            nv21,
            ImageFormat.NV21,
            imageProxy.width, imageProxy.height, null
        )

        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 90, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun navigateToResult(details: Map<String, String>, imageUri: String?) {
        when (details["type"]) {
            "URL" -> {
                val intent = Intent(requireActivity(), QRUrlDetailsActivity::class.java)
                intent.putExtra("qr_url", details["url"])
                intent.putExtra("qr_type", details["type"])
                intent.putExtra("qr_image", imageUri)
                startActivity(intent)
            }

            "Email" -> {
                val intent = Intent(requireActivity(), QRPhoneEmailDetailsActivity::class.java)
                intent.putExtra("qr_type", details["type"])
                intent.putExtra("qr_image", imageUri)
                intent.putExtra("qr_email_to", details["address"])
                intent.putExtra("qr_subject", details["subject"])
                intent.putExtra("qr_body", details["body"])
                startActivity(intent)
            }

            "Phone" -> {
                val intent = Intent(requireActivity(), QRPhoneEmailDetailsActivity::class.java)
                intent.putExtra("qr_type", details["type"])
                intent.putExtra("qr_image", imageUri)
                intent.putExtra("qr_number", details["number"])
                startActivity(intent)
            }

            "Contact" -> {
                val intent = Intent(requireActivity(), QRTextContactActivity::class.java)
                intent.putExtra("qr_type", details["type"])
                intent.putExtra("qr_image", imageUri)
                intent.putExtra("qr_name", details["name"])
                intent.putExtra("qr_organization", details["organization"])
                intent.putExtra("qr_title", details["title"])
                intent.putExtra("qr_address", details["address"])
                intent.putExtra("qr_phone", details["phone"])
                intent.putExtra("qr_email", details["email"])
                startActivity(intent)
            }

            "Text" -> {
                val intent = Intent(requireActivity(), QRTextContactActivity::class.java)
                intent.putExtra("qr_text", details["text"])
                intent.putExtra("qr_type", details["type"])
                intent.putExtra("qr_image", imageUri)
                startActivity(intent)
            }

            "WiFi" -> {
                val intent = Intent(requireActivity(), QRUrlDetailsActivity::class.java)
                intent.putExtra("qr_type", details["type"])
                intent.putExtra("qr_image", imageUri)
                intent.putExtra("qr_ssid", details["ssid"])
                intent.putExtra("qr_password", details["password"])
                intent.putExtra("qr_encryptionType", details["encryptionType"])
                startActivity(intent)
            }

            else -> {
                val intent = Intent(requireActivity(), QRUrlDetailsActivity::class.java)
                intent.putExtra("qr_raw", details["rawValue"])
                intent.putExtra("qr_type", details["type"])
                intent.putExtra("qr_image", imageUri)
                startActivity(intent)
            }
        }
    }

    private fun adjustZoom(delta: Float) {
        camera?.let { cam ->
            val zoomState = cam.cameraInfo.zoomState.value
            zoomState?.let {
                var newZoom = it.linearZoom + delta
                if (newZoom < 0f) newZoom = 0f
                if (newZoom > 1f) newZoom = 1f
                cam.cameraControl.setLinearZoom(newZoom)
                requireActivity().runOnUiThread {
                    binding.seekZoom.progress = (newZoom * 100).toInt()
                }
            }
        }
    }

    private fun toggleFlash() {
        camera?.let {
            torchEnabled = !torchEnabled
            it.cameraControl.enableTorch(torchEnabled)
            updateFlashUi(torchEnabled)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.flashlightButton -> {
                toggleFlash()
            }

            R.id.btnZoomIn -> {
                adjustZoom(+0.1f)
            }

            R.id.btnZoomOut -> {
                adjustZoom(-0.1f)
            }
        }
    }

    private fun extractQrDetails(barcode: Barcode): Map<String, String> {
        val details = mutableMapOf<String, String>()
        when (barcode.valueType) {
            Barcode.TYPE_URL -> {
                details["type"] = "URL"
                details["url"] = barcode.url?.url ?: ""
            }

            Barcode.TYPE_CONTACT_INFO -> {
                details["type"] = "Contact"
                barcode.contactInfo?.let { contact ->
                    details["name"] = contact.name?.formattedName ?: ""
                    details["organization"] = contact.organization ?: ""
                    details["title"] = contact.title ?: ""
                    details["address"] =
                        contact.addresses.firstOrNull()?.addressLines?.joinToString(", ") ?: ""
                    details["phone"] = contact.phones.firstOrNull()?.number ?: ""
                    details["email"] = contact.emails.firstOrNull()?.address ?: ""
                }
            }

            Barcode.TYPE_EMAIL -> {
                details["type"] = "Email"
                barcode.email?.let { email ->
                    details["address"] = email.address ?: ""
                    details["subject"] = email.subject ?: ""
                    details["body"] = email.body ?: ""
                }
            }

            Barcode.TYPE_PHONE -> {
                details["type"] = "Phone"
                details["number"] = barcode.phone?.number ?: ""
            }

            Barcode.TYPE_TEXT -> {
                details["type"] = "Text"
                details["text"] = barcode.displayValue ?: ""
            }

            Barcode.TYPE_WIFI -> {
                details["type"] = "WiFi"
                barcode.wifi?.let { wifi ->
                    details["ssid"] = wifi.ssid ?: ""
                    details["password"] = wifi.password ?: ""
                    details["encryptionType"] = when (wifi.encryptionType) {
                        Barcode.WiFi.TYPE_OPEN -> "Open"
                        Barcode.WiFi.TYPE_WEP -> "WEP"
                        Barcode.WiFi.TYPE_WPA -> "WPA/WPA2"
                        else -> "Unknown"
                    }
                }
            }

            else -> {
                details["type"] = "Unknown"
                details["rawValue"] = barcode.rawValue ?: ""
            }
        }
        return details
    }

    private fun updateFlashUi(isOn: Boolean) {
        val context = requireContext()
        val iconColor = if (isOn)
            ContextCompat.getColor(context, R.color.txt_color_blue)
        else
            ContextCompat.getColor(context, R.color.white)

        val textColor = if (isOn)
            ContextCompat.getColor(context, R.color.txt_color_blue)
        else
            ContextCompat.getColor(context, R.color.white)

        // Update icon tint
        binding.btnFlashlight.setColorFilter(iconColor)

        // Update text color
        binding.tvFlashlight.setTextColor(textColor)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun playBeepIfEnabled() {
        val prefs =
            requireContext().getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
        val isBeepEnabled = prefs.getBoolean("isBeep", false)

        if (isBeepEnabled) {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 200)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 250)
            } catch (_: Exception) {
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isScanning = false
        if (camera == null) startCamera()
    }

    fun pauseScanning() {
        cameraProvider?.unbindAll()
    }

    fun resumeScanning() {
        startCamera()
    }
}
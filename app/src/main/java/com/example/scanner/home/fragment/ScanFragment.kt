package com.example.scanner.home.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ScaleGestureDetector
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
import androidx.fragment.app.Fragment
import com.example.scanner.QRDetailsActivity
import com.example.scanner.R
import com.example.scanner.databinding.FragmentScanBinding
import com.example.scanner.sqlite.QrAllHistoryDatabase
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanFragment : Fragment(), View.OnClickListener {
    private lateinit var binding : FragmentScanBinding
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var torchEnabled = false
    private var isScanning = false
    private lateinit var cameraExecutor: ExecutorService
    private var isGalleryOpen = false

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        isGalleryOpen = false
        updateGalleryUi(false)
        uri?.let { scanImageFromGallery(it) }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera()
        else Toast.makeText(requireActivity(), "Camera permission required", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScanBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.flashlightButton.setOnClickListener(this)
        binding.llGallery.setOnClickListener(this)
        binding.llBatch.setOnClickListener(this)
        binding.btnZoomIn.setOnClickListener(this)
        binding.btnZoomOut.setOnClickListener(this)

        // Zoom Seekbar
        binding.seekZoom.max = 100
        binding.seekZoom.progress = 0
        binding.seekZoom.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                camera?.cameraControl?.setLinearZoom(progress / 100f)
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // Pinch to zoom (optional)
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

        checkPermissionAndStart()
    }

    private fun checkPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
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

        val preview = Preview.Builder()
            .setTargetRotation(binding.previewView.display.rotation)
            .build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

        imageAnalyzer?.clearAnalyzer()
        imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalyzer?.setAnalyzer(cameraExecutor, { imageProxy ->
            if (isScanning) {
                imageProxy.close()
                return@setAnalyzer
            }
            processImageProxy(imageProxy)
        })

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
                    requireActivity().runOnUiThread {
                        val details = extractQrDetails(barcode)
                        navigateToResult(details, null)
                    }
                }
            }
            .addOnFailureListener { it.printStackTrace() }
            .addOnCompleteListener { imageProxy.close() }
    }
    private fun imageProxyToBitmap(imageProxy: ImageProxy): android.graphics.Bitmap {
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

        val yuvImage = android.graphics.YuvImage(
            nv21,
            android.graphics.ImageFormat.NV21,
            imageProxy.width, imageProxy.height, null
        )

        val out = java.io.ByteArrayOutputStream()
        yuvImage.compressToJpeg(android.graphics.Rect(0, 0, imageProxy.width, imageProxy.height), 90, out)
        val imageBytes = out.toByteArray()
        return android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }


    private fun navigateToResult(details: Map<String, String>, imageUri: String?) {
        val intent = Intent(requireActivity(), QRDetailsActivity::class.java)
        intent.putExtra("qr_type", details["type"])
        intent.putExtra("qr_image", imageUri)

        when (details["type"]) {
            "URL" -> intent.putExtra("qr_url", details["url"])
            "Contact" -> {
                intent.putExtra("qr_name", details["name"])
                intent.putExtra("qr_organization", details["organization"])
                intent.putExtra("qr_title", details["title"])
                intent.putExtra("qr_address", details["address"])
                intent.putExtra("qr_phone", details["phone"])
                intent.putExtra("qr_email", details["email"])
            }
            "Email" -> {
                intent.putExtra("qr_email_to", details["address"])
                intent.putExtra("qr_subject", details["subject"])
                intent.putExtra("qr_body", details["body"])
            }
            "Phone" -> intent.putExtra("qr_number", details["number"])
            "Text" -> intent.putExtra("qr_text", details["text"])
            else -> intent.putExtra("qr_raw", details["rawValue"])
        }
        startActivity(intent)
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
        when(v?.id){
            R.id.flashlightButton -> {
                toggleFlash()
            }

            R.id.llGallery -> {
                pickFromGallery()
            }

            R.id.llBatch -> {

            }

            R.id.btnZoomIn -> {
                adjustZoom(+0.1f)
            }

            R.id.btnZoomOut -> {
                adjustZoom(-0.1f)
            }
        }
    }

    private fun pickFromGallery() {
        isGalleryOpen = true
        updateGalleryUi(true)
        pickImageLauncher.launch("image/*")
    }

    private fun scanImageFromGallery(uri: Uri) {
        val image = InputImage.fromFilePath(requireContext(), uri)
        val scanner = BarcodeScanning.getClient()
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    val barcode = barcodes.first()
                    val details = extractQrDetails(barcode)
                    navigateToResult(details, uri.toString())
                } else {
                    Toast.makeText(requireContext(), "No QR code found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to scan image", Toast.LENGTH_SHORT).show()
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
                    details["address"] = contact.addresses?.firstOrNull()?.addressLines?.joinToString(", ") ?: ""
                    details["phone"] = contact.phones?.firstOrNull()?.number ?: ""
                    details["email"] = contact.emails?.firstOrNull()?.address ?: ""
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
    private fun updateGalleryUi(isOpen: Boolean) {
        val context = requireContext()
        val iconColor = if (isOpen)
            ContextCompat.getColor(context, R.color.txt_color_blue)
        else
            ContextCompat.getColor(context, R.color.white)

        val textColor = if (isOpen)
            ContextCompat.getColor(context, R.color.txt_color_blue)
        else
            ContextCompat.getColor(context, R.color.white)

        binding.apply {
            ivGallery.setColorFilter(iconColor)
            tvGallery.setTextColor(textColor)
        }
    }

    override fun onResume() {
        super.onResume()
        isScanning = false
        if (camera == null) startCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

}
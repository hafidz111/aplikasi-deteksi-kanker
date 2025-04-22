package com.example.aplikasideteksikanker.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.aplikasideteksikanker.R
import com.example.aplikasideteksikanker.databinding.ActivityMainBinding
import com.example.aplikasideteksikanker.helper.ImageClassifierHelper
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.File
import com.yalantis.ucrop.UCrop

class MainActivity : AppCompatActivity(), ImageClassifierHelper.ClassifierListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var imageClassifierHelper: ImageClassifierHelper
    private var currentImageUri: Uri? = null

    @SuppressLint("UseKtx")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            view.setPadding(0, top, 0, 0)
            insets
        }

        imageClassifierHelper = ImageClassifierHelper(
            context = this,
            classifierListener = this
        )

        savedInstanceState?.getString("imageUri")?.let {
            currentImageUri = Uri.parse(it)
            showImage()
        }

        binding.galleryButton.setOnClickListener { startGallery() }
        binding.analyzeButton.setOnClickListener {
            currentImageUri?.let {
                analyzeImage()
            } ?: run {
                showToast(getString(R.string.empty_image_warning))
            }
        }
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri = result.data?.data
            selectedImageUri?.let { uri ->
                startCrop(uri)
            }
        }
    }

    private fun startCrop(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "cropped_${System.currentTimeMillis()}.jpg"))
        val options = UCrop.Options().apply {
            setFreeStyleCropEnabled(true)
            setToolbarTitle("Edit Image")
            setShowCropGrid(true)
            setShowCropFrame(true)
            setCircleDimmedLayer(false)
        }
        val uCrop = UCrop.of(sourceUri, destinationUri)
            .withOptions(options)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(224, 224)
        cropLauncher.launch(uCrop.getIntent(this))
    }

    private val cropLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val resultUri = UCrop.getOutput(result.data!!)
            resultUri?.let { uri ->
                currentImageUri = uri
                showImage()
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(result.data!!)
            cropError?.let {
                showToast(it.message ?: "Crop error")
            }
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = data?.let { UCrop.getOutput(it) }
            resultUri?.let { uri ->
                currentImageUri = uri
                showImage()
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = data?.let { UCrop.getError(it) }
            cropError?.let {
                showToast(it.message ?: "Crop error")
            }
        }
    }

    private fun startGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        launcherGallery.launch(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        currentImageUri?.let {
            outState.putString("imageUri", it.toString())
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun analyzeImage() {
        currentImageUri?.let { uri ->
            binding.progressIndicator.visibility = View.VISIBLE

            imageClassifierHelper.classifyStaticImage(uri)
        } ?: showToast(getString(R.string.empty_image_warning))

    }

    override fun onError(error: String) {
        binding.progressIndicator.visibility = View.GONE
        showToast(error)
    }

    override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
        binding.progressIndicator.visibility = View.GONE
        results?.firstOrNull()?.categories?.maxByOrNull { it.score }?.let { category ->
            val label = category.label
            val score = category.score
            moveToResult(label, score)
        } ?: showToast("No classification results.")
    }

    private fun moveToResult(result: String, score: Float) {
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra(ResultActivity.EXTRA_IMAGE_URI, currentImageUri.toString())
            putExtra(ResultActivity.EXTRA_RESULT, result)
            putExtra(ResultActivity.EXTRA_SCORE, score)
        }
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}